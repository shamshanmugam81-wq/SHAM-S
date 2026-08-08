package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val phone: String,
    val passwordHash: String = "secured_auth_hash",
    val bloodGroup: String = "O+",
    val medicalNotes: String = "No known severe allergies",
    val emergencyInfo: String = "Notify family immediately",
    val isLoggedIn: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
