package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class HealthRepository(private val database: AppDatabase) {

    private val userDao = database.userDao()
    private val measurementDao = database.measurementDao()
    private val reminderDao = database.reminderDao()
    private val medicalFileDao = database.medicalFileDao()
    private val doctorFeedbackDao = database.doctorFeedbackDao()

    // --- User Operations ---
    suspend fun getUserByPhone(phone: String): User? {
        return userDao.getUserByPhone(phone)
    }

    suspend fun registerUser(user: User): Long {
        return userDao.insertUser(user)
    }

    fun getAllPatients(): Flow<List<User>> {
        return userDao.getAllPatients()
    }

    // --- Measurement Operations ---
    fun getMeasurements(phone: String): Flow<List<Measurement>> {
        return measurementDao.getMeasurementsForPatient(phone)
    }

    suspend fun addMeasurement(measurement: Measurement) {
        measurementDao.insertMeasurement(measurement)
    }

    suspend fun deleteMeasurement(id: Long) {
        measurementDao.deleteMeasurement(id)
    }

    // --- Reminder Operations ---
    fun getReminders(phone: String): Flow<List<Reminder>> {
        return reminderDao.getRemindersForPatient(phone)
    }

    suspend fun addReminder(reminder: Reminder) {
        reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(id: Long) {
        reminderDao.deleteReminder(id)
    }

    // --- Medical File Operations ---
    fun getFiles(phone: String): Flow<List<MedicalFile>> {
        return medicalFileDao.getFilesForPatient(phone)
    }

    suspend fun addFile(file: MedicalFile) {
        medicalFileDao.insertFile(file)
    }

    suspend fun deleteFile(id: Long) {
        medicalFileDao.deleteFile(id)
    }

    // --- Doctor Feedback Operations ---
    fun getFeedback(phone: String): Flow<List<DoctorFeedback>> {
        return doctorFeedbackDao.getFeedbackForPatient(phone)
    }

    suspend fun addFeedback(feedback: DoctorFeedback) {
        doctorFeedbackDao.insertFeedback(feedback)
    }
}
