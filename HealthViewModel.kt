package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.HealthAiAnalyzer
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.HealthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HealthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HealthRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HealthRepository(database)
    }

    // --- State Observables ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _selectedPatient = MutableStateFlow<User?>(null)
    val selectedPatient: StateFlow<User?> = _selectedPatient.asStateFlow()

    // Login & Register errors
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Reactive lists
    val measurements: StateFlow<List<Measurement>> = _currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else {
                // If user is Patient, load their measurements. 
                // If Doctor or Family, load the selected patient's measurements.
                val targetPhone = if (user.role == "PATIENT") user.phone else _selectedPatient.value?.phone ?: ""
                if (targetPhone.isNotEmpty()) {
                    repository.getMeasurements(targetPhone)
                } else {
                    flowOf(emptyList())
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<Reminder>> = _currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else {
                val targetPhone = if (user.role == "PATIENT") user.phone else _selectedPatient.value?.phone ?: ""
                if (targetPhone.isNotEmpty()) {
                    repository.getReminders(targetPhone)
                } else {
                    flowOf(emptyList())
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medicalFiles: StateFlow<List<MedicalFile>> = _currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else {
                val targetPhone = if (user.role == "PATIENT") user.phone else _selectedPatient.value?.phone ?: ""
                if (targetPhone.isNotEmpty()) {
                    repository.getFiles(targetPhone)
                } else {
                    flowOf(emptyList())
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val doctorFeedbacks: StateFlow<List<DoctorFeedback>> = _currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else {
                val targetPhone = if (user.role == "PATIENT") user.phone else _selectedPatient.value?.phone ?: ""
                if (targetPhone.isNotEmpty()) {
                    repository.getFeedback(targetPhone)
                } else {
                    flowOf(emptyList())
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Patients (for DOCTOR)
    val allPatients: StateFlow<List<User>> = repository.getAllPatients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI States
    private val _aiReport = MutableStateFlow<String>("")
    val aiReport: StateFlow<String> = _aiReport.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    // Streak calculation based on measurements and completed reminders
    val commitmentStreak: StateFlow<Int> = measurements.combine(reminders) { measList, remList ->
        // Dynamically compute the consecutive active days in the current month
        val dates = mutableSetOf<String>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        measList.forEach {
            dates.add(sdf.format(Date(it.timestamp)))
        }
        remList.forEach {
            if (it.isCompletedToday && it.lastCompletedDate.isNotEmpty()) {
                dates.add(it.lastCompletedDate)
            }
        }

        // Compute contiguous active days starting from today going backwards
        var streak = 0
        val cal = Calendar.getInstance()
        while (true) {
            val dateStr = sdf.format(cal.time)
            if (dates.contains(dateStr)) {
                streak++
                cal.add(Calendar.DATE, -1)
            } else {
                break
            }
        }
        
        // Return computed streak or a baseline of 1 if there's any action today
        if (streak == 0 && dates.isNotEmpty()) 1 else streak
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Authentication ---
    fun register(
        name: String,
        ageStr: String,
        gender: String,
        phone: String,
        role: String,
        diseases: String,
        linkedPhone: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _authError.value = null
            if (name.isBlank() || ageStr.isBlank() || phone.isBlank()) {
                _authError.value = "يرجى ملء جميع الحقول المطلوبة."
                return@launch
            }

            val age = ageStr.toIntOrNull()
            if (age == null || age <= 0) {
                _authError.value = "يرجى إدخال عمر صحيح."
                return@launch
            }

            val existing = repository.getUserByPhone(phone)
            if (existing != null) {
                _authError.value = "رقم الهاتف هذا مسجل بالفعل."
                return@launch
            }

            val newUser = User(
                name = name,
                age = age,
                gender = gender,
                phone = phone,
                role = role,
                diseases = diseases,
                linkedPatientPhone = linkedPhone
            )

            repository.registerUser(newUser)
            _currentUser.value = newUser
            
            // Auto initialize default reminders for Patients to guide them
            if (role == "PATIENT") {
                createDefaultRemindersForPatient(phone, diseases)
            } else if (linkedPhone.isNotEmpty()) {
                val patient = repository.getUserByPhone(linkedPhone)
                if (patient != null) {
                    _selectedPatient.value = patient
                }
            }

            onSuccess()
        }
    }

    fun login(phone: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            if (phone.isBlank()) {
                _authError.value = "يرجى إدخال رقم الهاتف."
                return@launch
            }

            val user = repository.getUserByPhone(phone)
            if (user != null) {
                _currentUser.value = user
                // If Family or Doctor, pre-select the patient they are linked to
                if ((user.role == "FAMILY" || user.role == "DOCTOR") && user.linkedPatientPhone.isNotEmpty()) {
                    val patient = repository.getUserByPhone(user.linkedPatientPhone)
                    if (patient != null) {
                        _selectedPatient.value = patient
                    }
                }
                onSuccess()
            } else {
                _authError.value = "رقم الهاتف غير مسجل. يرجى إنشاء حساب جديد."
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _selectedPatient.value = null
        _aiReport.value = ""
        _authError.value = null
    }

    fun selectPatient(patient: User) {
        _selectedPatient.value = patient
    }

    // --- Measurement Actions ---
    fun addMeasurement(type: String, val1: Double, val2: Double, note: String) {
        viewModelScope.launch {
            val phone = _currentUser.value?.phone ?: return@launch
            val newMeas = Measurement(
                patientPhone = phone,
                type = type,
                value1 = val1,
                value2 = val2,
                note = note
            )
            repository.addMeasurement(newMeas)
        }
    }

    fun deleteMeasurement(id: Long) {
        viewModelScope.launch {
            repository.deleteMeasurement(id)
        }
    }

    // --- Reminder Actions ---
    fun addReminder(title: String, type: String, timeStr: String) {
        viewModelScope.launch {
            val phone = _currentUser.value?.phone ?: return@launch
            val reminder = Reminder(
                patientPhone = phone,
                title = title,
                type = type,
                timeStr = timeStr
            )
            repository.addReminder(reminder)
        }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())
            val updated = reminder.copy(
                isCompletedToday = !reminder.isCompletedToday,
                lastCompletedDate = if (!reminder.isCompletedToday) todayStr else ""
            )
            repository.updateReminder(updated)
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            repository.deleteReminder(id)
        }
    }

    // --- Medical File Actions ---
    fun addFile(name: String, type: String, summary: String) {
        viewModelScope.launch {
            val phone = _currentUser.value?.phone ?: return@launch
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = sdf.format(Date())
            val newFile = MedicalFile(
                patientPhone = phone,
                fileName = name,
                fileType = type,
                dateStr = dateStr,
                summary = summary
            )
            repository.addFile(newFile)
        }
    }

    fun deleteFile(id: Long) {
        viewModelScope.launch {
            repository.deleteFile(id)
        }
    }

    // --- Doctor Actions ---
    fun addFeedback(patientPhone: String, text: String) {
        viewModelScope.launch {
            val doctorName = _currentUser.value?.name ?: "طبيب مختص"
            val newFeedback = DoctorFeedback(
                patientPhone = patientPhone,
                doctorName = doctorName,
                feedbackText = text
            )
            repository.addFeedback(newFeedback)
        }
    }

    // --- AI Analyzer Trigger ---
    fun runAiAnalysis() {
        val patient = if (_currentUser.value?.role == "PATIENT") {
            _currentUser.value
        } else {
            _selectedPatient.value
        } ?: return

        viewModelScope.launch {
            _aiLoading.value = true
            _aiReport.value = "جاري الاتصال بالذكاء الاصطناعي لتحليل قياساتك..."

            val activeMeas = measurements.value.take(10) // analyze last 10 entries
            val diseasesStr = patient.diseases

            val measurementsText = if (activeMeas.isEmpty()) {
                "لا توجد قياسات مسجلة بعد."
            } else {
                activeMeas.joinToString("\n") { m ->
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val date = sdf.format(Date(m.timestamp))
                    when (m.type) {
                        "DIABETES" -> "- [$date] سكر الدم: ${m.value1} mg/dL (${m.note})"
                        "HYPERTENSION" -> "- [$date] ضغط الدم: ${m.value1.toInt()}/${m.value2.toInt()} mmHg, النبض: ${m.note}"
                        "ASTHMA" -> "- [$date] نوبات الربو: ${m.value1.toInt()} نوبات (${m.note})"
                        "HEART" -> "- [$date] نبض القلب: ${m.value1.toInt()} bpm, الضغط: ${m.value2.toInt()} mmHg (${m.note})"
                        else -> "- [$date] قياس: ${m.value1} (${m.note})"
                    }
                }
            }

            val analysis = HealthAiAnalyzer.analyzeMeasurements(
                patientName = patient.name,
                diseases = diseasesStr,
                measurementsText = measurementsText
            )
            _aiReport.value = analysis
            _aiLoading.value = false
        }
    }

    // Helper to generate template/default reminders based on chosen diseases
    private suspend fun createDefaultRemindersForPatient(phone: String, diseases: String) {
        val diseasesList = diseases.split(",")
        // Generic reminders
        repository.addReminder(Reminder(patientPhone = phone, title = "شرب الماء بكثرة والمشي اليومي", type = "MEDICATION", timeStr = "08:00 AM"))
        
        if (diseasesList.contains("DIABETES")) {
            repository.addReminder(Reminder(patientPhone = phone, title = "قياس سكر الدم قبل الإفطار", type = "MEASUREMENT", timeStr = "07:30 AM"))
            repository.addReminder(Reminder(patientPhone = phone, title = "جرعة دواء السكري / الإنسولين", type = "MEDICATION", timeStr = "08:00 AM"))
            repository.addReminder(Reminder(patientPhone = phone, title = "قياس السكر بعد الوجبات", type = "MEASUREMENT", timeStr = "02:00 PM"))
        }
        if (diseasesList.contains("HYPERTENSION")) {
            repository.addReminder(Reminder(patientPhone = phone, title = "قياس ضغط الدم الصباحي", type = "MEASUREMENT", timeStr = "08:30 AM"))
            repository.addReminder(Reminder(patientPhone = phone, title = "جرعة دواء ضغط الدم والنبض", type = "MEDICATION", timeStr = "09:00 AM"))
        }
        if (diseasesList.contains("ASTHMA")) {
            repository.addReminder(Reminder(patientPhone = phone, title = "جرعة بخاخ الربو الوقائي", type = "MEDICATION", timeStr = "08:00 AM"))
            repository.addReminder(Reminder(patientPhone = phone, title = "جرعة بخاخ الربو المسائي", type = "MEDICATION", timeStr = "08:00 PM"))
        }
        if (diseasesList.contains("HEART")) {
            repository.addReminder(Reminder(patientPhone = phone, title = "قياس نبض القلب وضغط الدم", type = "MEASUREMENT", timeStr = "10:00 AM"))
            repository.addReminder(Reminder(patientPhone = phone, title = "جرعة منظم نبض القلب والأسبرين", type = "MEDICATION", timeStr = "09:00 AM"))
        }
        
        // Appointment reminder example
        repository.addReminder(Reminder(patientPhone = phone, title = "مراجعة الطبيب الدورية", type = "DOCTOR", timeStr = "04:00 PM"))
    }
}
