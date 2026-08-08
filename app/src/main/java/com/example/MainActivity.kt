package com.example

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.SmartSosViewModel
import com.example.ui.navigation.Screen
import com.example.ui.screens.AboutHelpScreen
import com.example.ui.screens.ActiveSosScreen
import com.example.ui.screens.AddEditContactScreen
import com.example.ui.screens.ContactsListScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmergencyHistoryScreen
import com.example.ui.screens.LiveLocationMapScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PermissionsSafetyScreen
import com.example.ui.screens.RegistrationScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SosConfirmationScreen
import com.example.ui.screens.SosDetailScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.theme.SmartSosTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false
        if (fineGranted) {
            Toast.makeText(this, "GPS Location Permission Granted", Toast.LENGTH_SHORT).show()
        }
        if (smsGranted) {
            Toast.makeText(this, "SMS Dispatch Permission Granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request core emergency permissions on launch
        requestEmergencyPermissions()

        setContent {
            SmartSosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmartSosApp(
                        onRequestPermissions = { requestEmergencyPermissions() },
                        onShareText = { shareText(it) },
                        onCopyText = { copyToClipboard(it) }
                    )
                }
            }
        }
    }

    private fun requestEmergencyPermissions() {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionLauncher.launch(perms.toTypedArray())
    }

    private fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Share Emergency SOS Link"))
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Coordinates", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun SmartSosApp(
    viewModel: SmartSosViewModel = viewModel(),
    onRequestPermissions: () -> Unit,
    onShareText: (String) -> Unit,
    onCopyText: (String) -> Unit
) {
    val navController = rememberNavController()

    val user by viewModel.user.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val sosEvents by viewModel.sosEvents.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val countdownRemaining by viewModel.countdownRemaining.collectAsStateWithLifecycle()
    val activeSosEvent by viewModel.activeSosEvent.collectAsStateWithLifecycle()
    val isSosActive by viewModel.isSosActive.collectAsStateWithLifecycle()
    val dispatchResults by viewModel.dispatchResults.collectAsStateWithLifecycle()
    val isPanicAlarmRunning by viewModel.isPanicAlarmRunning.collectAsStateWithLifecycle()
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // 1. Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateNext = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Registration.route)
                },
                onPerformLogin = { email, pass, callback ->
                    viewModel.loginUser(email, pass, callback)
                }
            )
        }

        // 3. Registration Screen
        composable(Screen.Registration.route) {
            RegistrationScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Registration.route) { inclusive = true }
                    }
                },
                onPerformRegister = { name, email, phone, blood, medical, callback ->
                    viewModel.registerUser(name, email, phone, blood, medical, callback)
                }
            )
        }

        // 4. Main Dashboard
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                user = user,
                contacts = contacts,
                location = currentLocation,
                isSosActive = isSosActive,
                isPanicAlarmRunning = isPanicAlarmRunning,
                isFlashlightOn = isFlashlightOn,
                onTriggerSos = {
                    viewModel.startSosCountdown {
                        navController.navigate(Screen.ActiveSos.route)
                    }
                    navController.navigate(Screen.SosConfirmation.route)
                },
                onNavigateToActiveSos = {
                    navController.navigate(Screen.ActiveSos.route)
                },
                onTogglePanicAlarm = { viewModel.togglePanicAlarm() },
                onToggleFlashlight = { viewModel.toggleFlashlight() },
                onInitiateCall = { phone -> viewModel.callContactOrService(phone) },
                onNavigateContacts = { navController.navigate(Screen.ContactsList.route) },
                onNavigateMap = { navController.navigate(Screen.LiveLocationMap.route) },
                onNavigateHistory = { navController.navigate(Screen.EmergencyHistory.route) },
                onNavigateProfile = { navController.navigate(Screen.UserProfile.route) },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) },
                onNavigatePermissions = { navController.navigate(Screen.PermissionsSafety.route) },
                onNavigateHelp = { navController.navigate(Screen.AboutHelp.route) }
            )
        }

        // 5. SOS Countdown Confirmation Screen
        composable(Screen.SosConfirmation.route) {
            SosConfirmationScreen(
                countdownRemaining = countdownRemaining,
                onCancelSos = {
                    viewModel.cancelSosCountdown()
                    navController.popBackStack()
                }
            )
        }

        // 6. Active SOS Screen
        composable(Screen.ActiveSos.route) {
            ActiveSosScreen(
                sosEvent = activeSosEvent,
                location = currentLocation,
                dispatchResults = dispatchResults,
                onEndSos = {
                    viewModel.endSosEmergency()
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onCallNumber = { phone -> viewModel.callContactOrService(phone) },
                onManualSmsFallback = { phone, msg ->
                    viewModel.triggerManualSmsFallback(phone, msg)
                },
                onShareLocationLink = onShareText,
                onCopyCoordinates = onCopyText
            )
        }

        // 7. Emergency Contacts List
        composable(Screen.ContactsList.route) {
            ContactsListScreen(
                contacts = contacts,
                onBack = { navController.popBackStack() },
                onAddContact = { navController.navigate(Screen.AddEditContact.createRoute(null)) },
                onEditContact = { id -> navController.navigate(Screen.AddEditContact.createRoute(id)) },
                onDeleteContact = { id -> viewModel.deleteEmergencyContact(id) },
                onCallContact = { phone -> viewModel.callContactOrService(phone) },
                onSmsContact = { phone ->
                    val lat = currentLocation?.latitude ?: 37.7749
                    val lng = currentLocation?.longitude ?: -122.4194
                    val text = "Emergency Alert check: Current location https://maps.google.com/?q=$lat,$lng"
                    viewModel.triggerManualSmsFallback(phone, text)
                }
            )
        }

        // 8. Add / Edit Contact Screen
        composable(
            route = Screen.AddEditContact.route,
            arguments = listOf(navArgument("contactId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getLong("contactId") ?: -1L
            val existing = contacts.firstOrNull { it.id == contactId }

            AddEditContactScreen(
                contactToEdit = existing,
                onBack = { navController.popBackStack() },
                onSaveContact = { name, phone, rel, isImp ->
                    viewModel.addEmergencyContact(name, phone, rel, isImp) {
                        navController.popBackStack()
                    }
                },
                onUpdateExistingContact = { updated ->
                    viewModel.updateEmergencyContact(updated) {
                        navController.popBackStack()
                    }
                }
            )
        }

        // 9. User Profile Screen
        composable(Screen.UserProfile.route) {
            UserProfileScreen(
                user = user,
                onBack = { navController.popBackStack() },
                onSaveProfile = { name, email, phone, blood, medical, info ->
                    viewModel.updateProfile(name, email, phone, blood, medical, info)
                },
                onLogout = {
                    viewModel.logoutUser()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // 10. Live Location Map Screen
        composable(Screen.LiveLocationMap.route) {
            LiveLocationMapScreen(
                location = currentLocation,
                onBack = { navController.popBackStack() },
                onRefreshLocation = { viewModel.refreshLocation() },
                onShareLocationLink = onShareText,
                onCopyCoordinates = onCopyText
            )
        }

        // 11. Emergency History Screen
        composable(Screen.EmergencyHistory.route) {
            EmergencyHistoryScreen(
                sosEvents = sosEvents,
                onBack = { navController.popBackStack() },
                onSelectEvent = { id -> navController.navigate(Screen.SosDetail.createRoute(id)) },
                onDeleteEvent = { id -> viewModel.deleteSosEvent(id) },
                onClearAllHistory = { viewModel.clearAllSosHistory() }
            )
        }

        // 12. SOS Detail Screen
        composable(
            route = Screen.SosDetail.route,
            arguments = listOf(navArgument("sosId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sosId = backStackEntry.arguments?.getLong("sosId") ?: -1L
            val event = sosEvents.firstOrNull { it.id == sosId }

            SosDetailScreen(
                sosEvent = event,
                onBack = { navController.popBackStack() },
                onDeleteEvent = { id -> viewModel.deleteSosEvent(id) },
                onShareLink = onShareText
            )
        }

        // 13. Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                appSettings = appSettings,
                onBack = { navController.popBackStack() },
                onSaveSettings = { newSettings -> viewModel.updateSettings(newSettings) }
            )
        }

        // 14. Permissions & Safety Screen
        composable(Screen.PermissionsSafety.route) {
            PermissionsSafetyScreen(
                onBack = { navController.popBackStack() },
                onRequestPermissions = onRequestPermissions
            )
        }

        // 15. About & Help Screen
        composable(Screen.AboutHelp.route) {
            AboutHelpScreen(
                onBack = { navController.popBackStack() },
                onCallNumber = { phone -> viewModel.callContactOrService(phone) }
            )
        }
    }
}
