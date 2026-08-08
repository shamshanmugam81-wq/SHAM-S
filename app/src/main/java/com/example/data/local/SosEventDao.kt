package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SosEventDao {
    @Query("SELECT * FROM sos_events ORDER BY timestamp DESC")
    fun getAllSosEvents(): Flow<List<SosEventEntity>>

    @Query("SELECT * FROM sos_events WHERE id = :id LIMIT 1")
    suspend fun getSosEventById(id: Long): SosEventEntity?

    @Query("SELECT * FROM sos_events WHERE status = 'ACTIVE' ORDER BY timestamp DESC LIMIT 1")
    suspend fun getActiveSosEvent(): SosEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSosEvent(event: SosEventEntity): Long

    @Update
    suspend fun updateSosEvent(event: SosEventEntity)

    @Query("DELETE FROM sos_events WHERE id = :id")
    suspend fun deleteSosEventById(id: Long)

    @Query("DELETE FROM sos_events")
    suspend fun clearAllSosEvents()
}
