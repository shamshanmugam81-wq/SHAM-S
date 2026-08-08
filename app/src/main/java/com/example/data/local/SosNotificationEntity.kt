package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sos_notifications")
data class SosNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sosId: Long,
    val contactId: Long? = null,
    val contactName: String = "",
    val notificationType: String = "SMS", // SMS, CALL, APP_ALERT
    val status: String = "SENT", // PREPARED, HANDED_OFF, SENT, FAILED
    val timestamp: Long = System.currentTimeMillis()
)
