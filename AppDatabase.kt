package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): User?

    @Query("SELECT * FROM users WHERE role = 'PATIENT'")
    fun getAllPatients(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long
}

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements WHERE patientPhone = :phone ORDER BY timestamp DESC")
    fun getMeasurementsForPatient(phone: String): Flow<List<Measurement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: Measurement)

    @Query("DELETE FROM measurements WHERE id = :id")
    suspend fun deleteMeasurement(id: Long)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE patientPhone = :phone")
    fun getRemindersForPatient(phone: String): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: Long)
}

@Dao
interface MedicalFileDao {
    @Query("SELECT * FROM medical_files WHERE patientPhone = :phone ORDER BY id DESC")
    fun getFilesForPatient(phone: String): Flow<List<MedicalFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: MedicalFile)

    @Query("DELETE FROM medical_files WHERE id = :id")
    suspend fun deleteFile(id: Long)
}

@Dao
interface DoctorFeedbackDao {
    @Query("SELECT * FROM doctor_feedback WHERE patientPhone = :phone ORDER BY timestamp DESC")
    fun getFeedbackForPatient(phone: String): Flow<List<DoctorFeedback>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: DoctorFeedback)
}

@Database(
    entities = [
        User::class,
        Measurement::class,
        Reminder::class,
        MedicalFile::class,
        DoctorFeedback::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun reminderDao(): ReminderDao
    abstract fun medicalFileDao(): MedicalFileDao
    abstract fun doctorFeedbackDao(): DoctorFeedbackDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lifecare_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
