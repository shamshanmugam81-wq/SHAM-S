package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.EmergencyContactEntity
import com.example.data.local.SosEventEntity
import com.example.data.local.SosNotificationEntity
import com.example.data.local.UserEntity
import com.example.data.repository.SosRepository
import com.example.services.LocationResult
import com.example.services.PanicAlarmManager
import com.example.services.SmsDispatchResult
import com.example.services.SosEmergencyManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppSettings(
    val countdownDurationSeconds: Int = 5,
    val autoCallEmergencyService: Boolean = false,
    val emergencyNumberToCall: String = "112",
    val customMessageTemplate: String = "",
    val isDarkMode: Boolean = false
)

class SmartSosViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application
    private val repository = SosRepository(com.example.SmartSosApplication().database.let {
        (app as? com.example.SmartSosApplication)?.database ?: com.example.data.local.AppDatabase.getDatabase(app)
    })

    val panicAlarmManager = PanicAlarmManager(app)
    val sosEmergencyManager = SosEmergencyManager(app)

    // Flow states
    val user: StateFlow<UserEntity?> = repository.activeUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val contacts: StateFlow<List<EmergencyContactEntity>> = repository.allContacts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val sosEvents: StateFlow<List<SosEventEntity>> = repository.allSosEvents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Internal UI States
    private val _currentLocation = MutableStateFlow<LocationResult?>(null)
    val currentLocation: StateFlow<LocationResult?> = _currentLocation.asStateFlow()

    private val _countdownRemaining = MutableStateFlow<Int?>(null)
    val countdownRemaining: StateFlow<Int?> = _countdownRemaining.asStateFlow()

    private val _activeSosEvent = MutableStateFlow<SosEventEntity?>(null)
    val activeSosEvent: StateFlow<SosEventEntity?> = _activeSosEvent.asStateFlow()

    private val _isSosActive = MutableStateFlow(false)
    val isSosActive: StateFlow<Boolean> = _isSosActive.asStateFlow()

    private val _dispatchResults = MutableStateFlow<List<SmsDispatchResult>>(emptyList())
    val dispatchResults: StateFlow<List<SmsDispatchResult>> = _dispatchResults.asStateFlow()

    private val _isPanicAlarmRunning = MutableStateFlow(false)
    val isPanicAlarmRunning: StateFlow<Boolean> = _isPanicAlarmRunning.asStateFlow()

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    private var countdownJob: Job? = null
    private var sosTrackingJob: Job? = null

    init {
        refreshLocation()
    }

    fun refreshLocation() {
        viewModelScope.launch {
            val loc = sosEmergencyManager.getCurrentLocation()
            _currentLocation.value = loc
        }
    }

    // --- SOS WORKFLOW ---
    fun startSosCountdown(onCountdownFinished: () -> Unit) {
        if (_isSosActive.value || _countdownRemaining.value != null) return

        val duration = _appSettings.value.countdownDurationSeconds
        _countdownRemaining.value = duration

        countdownJob = viewModelScope.launch {
            for (i in duration downTo 1) {
                _countdownRemaining.value = i
                panicAlarmManager.startVibration(viewModelScope)
                delay(1000)
                panicAlarmManager.stopVibration()
            }
            _countdownRemaining.value = null
            activateSosEmergency()
            onCountdownFinished()
        }
    }

    fun cancelSosCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        _countdownRemaining.value = null
        panicAlarmManager.stopVibration()
        sosEmergencyManager.showSystemNotification(
            notificationId = 101,
            title = "SOS Countdown Cancelled",
            body = "Emergency SOS countdown was safely cancelled by user.",
            isEmergency = false
        )
    }

    fun activateSosEmergency() {
        viewModelScope.launch {
            _isSosActive.value = true
            refreshLocation()

            val loc = _currentLocation.value ?: sosEmergencyManager.getCurrentLocation()
            val currUser = user.value
            val userName = currUser?.name ?: "User"
            val contactList = contacts.value

            val messageText = if (_appSettings.value.customMessageTemplate.isNotBlank()) {
                "${_appSettings.value.customMessageTemplate} Location: https://maps.google.com/?q=${loc.latitude},${loc.longitude}"
            } else {
                sosEmergencyManager.formatEmergencyMessage(
                    userName = userName,
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    customNote = currUser?.medicalNotes ?: ""
                )
            }

            // Insert active SOS event to Room
            val newSosEvent = SosEventEntity(
                userId = currUser?.id ?: 1,
                timestamp = System.currentTimeMillis(),
                latitude = loc.latitude,
                longitude = loc.longitude,
                addressLocation = loc.addressEstimate,
                status = "ACTIVE",
                contactsNotifiedCount = contactList.size,
                alertSummary = "ACTIVE EMERGENCY: Alert generated for ${contactList.size} emergency contacts.",
                durationSeconds = 0,
                mapLink = "https://maps.google.com/?q=${loc.latitude},${loc.longitude}"
            )

            val insertedId = repository.insertSosEvent(newSosEvent)
            val updatedEvent = newSosEvent.copy(id = insertedId)
            _activeSosEvent.value = updatedEvent

            // Dispatch SMS alerts to emergency contacts
            val results = mutableListOf<SmsDispatchResult>()
            for (contact in contactList) {
                val dispatch = sosEmergencyManager.dispatchSmsAlert(
                    contactName = contact.name,
                    phoneNumber = contact.phone,
                    messageText = messageText
                )
                results.add(dispatch)

                // Log notification event in DB
                repository.insertNotification(
                    SosNotificationEntity(
                        sosId = insertedId,
                        contactId = contact.id,
                        contactName = contact.name,
                        notificationType = "SMS",
                        status = if (dispatch.isSuccess) "HANDED_OFF" else if (dispatch.isHandedOffToIntent) "INTENT_FALLBACK" else "FAILED"
                    )
                )
            }
            _dispatchResults.value = results

            // Show System Notification
            sosEmergencyManager.showSystemNotification(
                notificationId = 999,
                title = "🚨 EMERGENCY SOS ACTIVE",
                body = "Location shared with ${contactList.size} contacts. Tap to view live tracking.",
                isEmergency = true
            )

            // Auto call emergency number if configured
            if (_appSettings.value.autoCallEmergencyService) {
                sosEmergencyManager.initiateEmergencyCall(_appSettings.value.emergencyNumberToCall)
            }

            // Start live tracking interval
            startLiveLocationTracking(insertedId)
        }
    }

    private fun startLiveLocationTracking(sosId: Long) {
        sosTrackingJob?.cancel()
        sosTrackingJob = viewModelScope.launch {
            var duration = 0L
            while (_isSosActive.value) {
                delay(5000) // Update location every 5s during active SOS
                duration += 5
                val newLoc = sosEmergencyManager.getCurrentLocation()
                _currentLocation.value = newLoc

                _activeSosEvent.value?.let { currentEvent ->
                    val updated = currentEvent.copy(
                        latitude = newLoc.latitude,
                        longitude = newLoc.longitude,
                        durationSeconds = duration,
                        mapLink = "https://maps.google.com/?q=${newLoc.latitude},${newLoc.longitude}"
                    )
                    _activeSosEvent.value = updated
                    repository.updateSosEvent(updated)
                }
            }
        }
    }

    fun endSosEmergency() {
        viewModelScope.launch {
            sosTrackingJob?.cancel()
            sosTrackingJob = null
            _isSosActive.value = false

            _activeSosEvent.value?.let { event ->
                val ended = event.copy(
                    status = "RESOLVED",
                    endedAt = System.currentTimeMillis(),
                    alertSummary = "Emergency resolved safely by user. Total duration: ${event.durationSeconds}s."
                )
                repository.updateSosEvent(ended)
                _activeSosEvent.value = null
            }

            sosEmergencyManager.showSystemNotification(
                notificationId = 1000,
                title = "SOS Emergency Resolved",
                body = "Emergency session ended and saved to history.",
                isEmergency = false
            )
        }
    }

    // --- PANIC ALARM ---
    fun togglePanicAlarm() {
        if (_isPanicAlarmRunning.value) {
            panicAlarmManager.stopAll()
            _isPanicAlarmRunning.value = false
            _isFlashlightOn.value = false
        } else {
            panicAlarmManager.startSiren(viewModelScope)
            panicAlarmManager.startVibration(viewModelScope)
            panicAlarmManager.startStrobeFlashlight(viewModelScope)
            _isPanicAlarmRunning.value = true
            _isFlashlightOn.value = true
        }
    }

    fun toggleFlashlight() {
        val newState = panicAlarmManager.toggleFlashlight()
        _isFlashlightOn.value = newState
    }

    // --- USER PROFILE & AUTH ---
    fun updateProfile(name: String, email: String, phone: String, bloodGroup: String, medicalNotes: String, emergencyInfo: String) {
        viewModelScope.launch {
            val curr = user.value ?: UserEntity(name = name, email = email, phone = phone)
            val updated = curr.copy(
                name = name,
                email = email,
                phone = phone,
                bloodGroup = bloodGroup,
                medicalNotes = medicalNotes,
                emergencyInfo = emergencyInfo
            )
            repository.insertUser(updated)
        }
    }

    fun loginUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (email.isBlank()) {
                onResult(false, "Please enter email address")
                return@launch
            }
            var existing = repository.getUserByEmail(email)
            if (existing == null) {
                // Register new user on demand for clean demo UX
                existing = UserEntity(
                    name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    email = email,
                    phone = "+1 555-0100",
                    isLoggedIn = true
                )
                repository.insertUser(existing)
            } else {
                repository.updateUser(existing.copy(isLoggedIn = true))
            }
            onResult(true, "Login Successful")
        }
    }

    fun registerUser(name: String, email: String, phone: String, bloodGroup: String, medicalNotes: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (name.isBlank() || phone.isBlank() || email.isBlank()) {
                onResult(false, "Please fill in all required fields")
                return@launch
            }
            repository.logoutAllUsers()
            val newUser = UserEntity(
                name = name,
                email = email,
                phone = phone,
                bloodGroup = bloodGroup,
                medicalNotes = medicalNotes,
                isLoggedIn = true
            )
            repository.insertUser(newUser)
            onResult(true, "Registration Successful")
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            repository.logoutAllUsers()
        }
    }

    // --- CONTACT MANAGEMENT ---
    fun addEmergencyContact(name: String, phone: String, relationship: String, isImportant: Boolean, onDone: () -> Unit) {
        viewModelScope.launch {
            val count = contacts.value.size
            val contact = EmergencyContactEntity(
                userId = user.value?.id ?: 1,
                name = name,
                phone = phone,
                relationship = relationship,
                priority = count + 1,
                isImportant = isImportant
            )
            repository.insertContact(contact)
            onDone()
        }
    }

    fun updateEmergencyContact(contact: EmergencyContactEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.updateContact(contact)
            onDone()
        }
    }

    fun deleteEmergencyContact(contactId: Long) {
        viewModelScope.launch {
            repository.deleteContactById(contactId)
        }
    }

    // --- HISTORY MANAGEMENT ---
    fun deleteSosEvent(sosId: Long) {
        viewModelScope.launch {
            repository.deleteSosEventById(sosId)
        }
    }

    fun clearAllSosHistory() {
        viewModelScope.launch {
            repository.clearAllSosEvents()
        }
    }

    // --- SETTINGS ---
    fun updateSettings(newSettings: AppSettings) {
        _appSettings.value = newSettings
    }

    fun triggerManualSmsFallback(contactPhone: String, text: String) {
        sosEmergencyManager.openManualSmsIntent(contactPhone, text)
    }

    fun callContactOrService(phoneNumber: String) {
        sosEmergencyManager.initiateEmergencyCall(phoneNumber)
    }

    override fun onCleared() {
        super.onCleared()
        panicAlarmManager.stopAll()
    }
}
