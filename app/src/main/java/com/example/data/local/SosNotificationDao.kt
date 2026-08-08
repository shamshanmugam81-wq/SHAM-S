package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SosNotificationDao {
    @Query("SELECT * FROM sos_notifications WHERE sosId = :sosId ORDER BY timestamp DESC")
    fun getNotificationsForSos(sosId: Long): Flow<List<SosNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: SosNotificationEntity): Long

    @Query("DELETE FROM sos_notifications WHERE sosId = :sosId")
    suspend fun deleteForSos(sosId: Long)
}
