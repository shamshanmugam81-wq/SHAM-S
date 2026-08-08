package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sos_events")
data class SosEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val latitude: Double,
    val longitude: Double,
    val addressLocation: String = "Current Location",
    val status: String = "ACTIVE", // ACTIVE, CANCELLED, RESOLVED
    val contactsNotifiedCount: Int = 0,
    val alertSummary: String = "",
    val durationSeconds: Long = 0,
    val mapLink: String = "https://maps.google.com/?q=$latitude,$longitude"
)
