package com.example.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.SmartSosApplication
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 10f,
    val isFallback: Boolean = false,
    val addressEstimate: String = ""
)

data class SmsDispatchResult(
    val contactName: String,
    val phone: String,
    val isSuccess: Boolean,
    val statusMessage: String,
    val isHandedOffToIntent: Boolean = false
)

class SosEmergencyManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult {
        val hasFine = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            // Permission denied fallback coordinates (San Francisco City Center / Demo Landmark)
            return LocationResult(
                latitude = 37.7749,
                longitude = -122.4194,
                accuracy = 100f,
                isFallback = true,
                addressEstimate = "Location Permission Denied (Default San Francisco Landmark)"
            )
        }

        return try {
            val cts = CancellationTokenSource()
            val location: Location? = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cts.token
            ).await()

            if (location != null) {
                LocationResult(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    isFallback = false,
                    addressEstimate = "GPS Location (Accuracy: ${location.accuracy.toInt()}m)"
                )
            } else {
                // Try last known location
                val lastLoc = fusedLocationClient.lastLocation.await()
                if (lastLoc != null) {
                    LocationResult(
                        latitude = lastLoc.latitude,
                        longitude = lastLoc.longitude,
                        accuracy = lastLoc.accuracy,
                        isFallback = false,
                        addressEstimate = "Last Known GPS Location"
                    )
                } else {
                    LocationResult(
                        latitude = 37.7749,
                        longitude = -122.4194,
                        accuracy = 50f,
                        isFallback = true,
                        addressEstimate = "GPS Signal Weak (Cached Landmark Location)"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("SosEmergencyManager", "Failed to acquire location", e)
            LocationResult(
                latitude = 37.7749,
                longitude = -122.4194,
                accuracy = 100f,
                isFallback = true,
                addressEstimate = "GPS Service Unavailable (Fallback Location)"
            )
        }
    }

    fun formatEmergencyMessage(
        userName: String,
        latitude: Double,
        longitude: Double,
        customNote: String = ""
    ): String {
        val timeString = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        val mapLink = "https://maps.google.com/?q=$latitude,$longitude"
        val extra = if (customNote.isNotBlank()) " Note: $customNote." else ""
        return "SOS ALERT! $userName needs urgent assistance.$extra Location: $mapLink. Time: $timeString."
    }

    fun dispatchSmsAlert(
        contactName: String,
        phoneNumber: String,
        messageText: String
    ): SmsDispatchResult {
        val hasSmsPermission = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (phoneNumber.isBlank()) {
            return SmsDispatchResult(
                contactName = contactName,
                phone = phoneNumber,
                isSuccess = false,
                statusMessage = "Invalid phone number"
            )
        }

        if (hasSmsPermission) {
            return try {
                val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

                val parts = smsManager.divideMessage(messageText)
                if (parts.size > 1) {
                    smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(phoneNumber, null, messageText, null, null)
                }

                SmsDispatchResult(
                    contactName = contactName,
                    phone = phoneNumber,
                    isSuccess = true,
                    statusMessage = "SMS alert handed off to cellular system"
                )
            } catch (e: Exception) {
                Log.e("SosEmergencyManager", "Direct SMS failed", e)
                SmsDispatchResult(
                    contactName = contactName,
                    phone = phoneNumber,
                    isSuccess = false,
                    statusMessage = "SMS system failure: ${e.localizedMessage}. Use manual SMS fallback."
                )
            }
        } else {
            return SmsDispatchResult(
                contactName = contactName,
                phone = phoneNumber,
                isSuccess = false,
                statusMessage = "SEND_SMS permission not granted. Handed off to manual messaging.",
                isHandedOffToIntent = true
            )
        }
    }

    fun openManualSmsIntent(phoneNumber: String, messageText: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${Uri.encode(phoneNumber)}")
                putExtra("sms_body", messageText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("SosEmergencyManager", "Manual SMS intent error", e)
        }
    }

    fun initiateEmergencyCall(phoneNumber: String): Boolean {
        if (phoneNumber.isBlank()) return false

        val hasCallPermission = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val intent = if (hasCallPermission) {
            Intent(Intent.ACTION_CALL, Uri.parse("tel:${Uri.encode(phoneNumber)}"))
        } else {
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phoneNumber)}"))
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("SosEmergencyManager", "Call intent failed", e)
            false
        }
    }

    fun showSystemNotification(
        notificationId: Int,
        title: String,
        body: String,
        isEmergency: Boolean = false
    ) {
        val channelId = if (isEmergency) SmartSosApplication.CHANNEL_EMERGENCY else SmartSosApplication.CHANNEL_STATUS

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(if (isEmergency) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}
