package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val age: Int,
    val gender: String,
    val phone: String, // unique phone number used as identifier for authentication
    val role: String, // "PATIENT", "FAMILY", "DOCTOR"
    val diseases: String = "", // comma-separated e.g. "DIABETES,HYPERTENSION,ASTHMA,HEART"
    val linkedPatientPhone: String = "" // For FAMILY or DOCTOR, monitoring this patient's phone
)

@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientPhone: String,
    val type: String, // "DIABETES" (Sugar), "HYPERTENSION" (Pressure), "ASTHMA" (Attacks), "HEART" (Rate/Pulse)
    val value1: Double, // Sugar level (mg/dL), Systolic blood pressure (mmHg), Attack count, Pulse / Heart rate (bpm)
    val value2: Double = 0.0, // Diastolic blood pressure (for pressure, if applicable), or 0
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientPhone: String,
    val title: String,
    val type: String, // "MEDICATION", "DOCTOR", "LAB", "MEASUREMENT"
    val timeStr: String, // e.g. "08:00 AM", "02:30 PM", "21:00"
    val isCompletedToday: Boolean = false,
    val lastCompletedDate: String = "" // Track completion day, e.g., "2026-07-10"
)

@Entity(tableName = "medical_files")
data class MedicalFile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientPhone: String,
    val fileName: String,
    val fileType: String, // "SCAN" (الأشعة), "LAB" (التحاليل), "PRESCRIPTION" (الوصفة), "REPORT" (التقرير)
    val dateStr: String,
    val summary: String = ""
)

@Entity(tableName = "doctor_feedback")
data class DoctorFeedback(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientPhone: String,
    val doctorName: String,
    val feedbackText: String,
    val timestamp: Long = System.currentTimeMillis()
)
