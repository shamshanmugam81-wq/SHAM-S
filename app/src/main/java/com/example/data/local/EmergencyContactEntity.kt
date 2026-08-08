package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val name: String,
    val phone: String,
    val relationship: String = "Family",
    val priority: Int = 1,
    val isImportant: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
