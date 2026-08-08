package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.EmergencyContactEntity
import com.example.data.local.SosEventEntity
import com.example.data.local.SosNotificationEntity
import com.example.data.local.UserEntity
import kotlinx.coroutines.flow.Flow

class SosRepository(private val db: AppDatabase) {

    val activeUser: Flow<UserEntity?> = db.userDao().getActiveUser()
    val allContacts: Flow<List<EmergencyContactEntity>> = db.emergencyContactDao().getAllContacts()
    val contactCount: Flow<Int> = db.emergencyContactDao().getContactCount()
    val allSosEvents: Flow<List<SosEventEntity>> = db.sosEventDao().getAllSosEvents()

    suspend fun getUserById(id: Long): UserEntity? = db.userDao().getUserById(id)
    suspend fun getUserByEmail(email: String): UserEntity? = db.userDao().getUserByEmail(email)
    suspend fun insertUser(user: UserEntity): Long = db.userDao().insertUser(user)
    suspend fun updateUser(user: UserEntity) = db.userDao().updateUser(user)
    suspend fun logoutAllUsers() = db.userDao().logoutAllUsers()

    suspend fun getContactById(id: Long): EmergencyContactEntity? = db.emergencyContactDao().getContactById(id)
    suspend fun insertContact(contact: EmergencyContactEntity): Long = db.emergencyContactDao().insertContact(contact)
    suspend fun updateContact(contact: EmergencyContactEntity) = db.emergencyContactDao().updateContact(contact)
    suspend fun deleteContact(contact: EmergencyContactEntity) = db.emergencyContactDao().deleteContact(contact)
    suspend fun deleteContactById(id: Long) = db.emergencyContactDao().deleteContactById(id)

    suspend fun getSosEventById(id: Long): SosEventEntity? = db.sosEventDao().getSosEventById(id)
    suspend fun getActiveSosEvent(): SosEventEntity? = db.sosEventDao().getActiveSosEvent()
    suspend fun insertSosEvent(event: SosEventEntity): Long = db.sosEventDao().insertSosEvent(event)
    suspend fun updateSosEvent(event: SosEventEntity) = db.sosEventDao().updateSosEvent(event)
    suspend fun deleteSosEventById(id: Long) = db.sosEventDao().deleteSosEventById(id)
    suspend fun clearAllSosEvents() = db.sosEventDao().clearAllSosEvents()

    fun getNotificationsForSos(sosId: Long): Flow<List<SosNotificationEntity>> = db.sosNotificationDao().getNotificationsForSos(sosId)
    suspend fun insertNotification(notification: SosNotificationEntity): Long = db.sosNotificationDao().insertNotification(notification)
}
