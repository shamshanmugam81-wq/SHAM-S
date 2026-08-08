package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        EmergencyContactEntity::class,
        SosEventEntity::class,
        SosNotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun sosEventDao(): SosEventDao
    abstract fun sosNotificationDao(): SosNotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_sos_db"
                )
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabase(database)
                    }
                }
            }

            suspend fun seedDatabase(db: AppDatabase) {
                // Seed default user
                val defaultUser = UserEntity(
                    id = 1,
                    name = "Alex Morgan",
                    email = "alex.morgan@example.com",
                    phone = "+1 555-0199",
                    bloodGroup = "O+",
                    medicalNotes = "Asthma (Inhaler in pocket), Penicillin Allergy",
                    emergencyInfo = "Contact spouse or parent first",
                    isLoggedIn = true
                )
                db.userDao().insertUser(defaultUser)

                // Seed initial emergency contacts
                val c1 = EmergencyContactEntity(
                    userId = 1,
                    name = "Sarah Morgan",
                    phone = "+1 555-0123",
                    relationship = "Spouse / Partner",
                    priority = 1,
                    isImportant = true
                )
                val c2 = EmergencyContactEntity(
                    userId = 1,
                    name = "Dr. Robert Vance",
                    phone = "+1 555-0144",
                    relationship = "Family Doctor",
                    priority = 2,
                    isImportant = true
                )
                val c3 = EmergencyContactEntity(
                    userId = 1,
                    name = "David Morgan",
                    phone = "+1 555-0188",
                    relationship = "Brother",
                    priority = 3,
                    isImportant = false
                )
                db.emergencyContactDao().insertContact(c1)
                db.emergencyContactDao().insertContact(c2)
                db.emergencyContactDao().insertContact(c3)

                // Seed one sample historic SOS event
                val sampleEvent = SosEventEntity(
                    id = 1,
                    userId = 1,
                    timestamp = System.currentTimeMillis() - 86400000L * 2, // 2 days ago
                    endedAt = System.currentTimeMillis() - (86400000L * 2) + 180000L,
                    latitude = 37.7749,
                    longitude = -122.4194,
                    addressLocation = "Market St & 4th St, San Francisco, CA",
                    status = "RESOLVED",
                    contactsNotifiedCount = 3,
                    alertSummary = "SOS Triggered. 3 Emergency contacts notified via SMS. Resolved safely.",
                    durationSeconds = 180,
                    mapLink = "https://maps.google.com/?q=37.7749,-122.4194"
                )
                db.sosEventDao().insertSosEvent(sampleEvent)
            }
        }
    }
}
