package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.local.AppDatabase

class SmartSosApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val emergencyChannel = NotificationChannel(
                CHANNEL_EMERGENCY,
                "Emergency SOS Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority notifications for active SOS emergency alerts"
                enableVibration(true)
            }

            val statusChannel = NotificationChannel(
                CHANNEL_STATUS,
                "SOS Status Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General notifications for SOS status changes and contacts"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(emergencyChannel)
            notificationManager.createNotificationChannel(statusChannel)
        }
    }

    companion object {
        const val CHANNEL_EMERGENCY = "smart_sos_emergency_channel"
        const val CHANNEL_STATUS = "smart_sos_status_channel"
    }
}
