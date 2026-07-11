package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.viewmodel.HealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorHomeScreen(
    viewModel: HealthViewModel,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allPatients by viewModel.allPatients.collectAsState()
    val selectedPatient by viewModel.selectedPatient.collectAsState()
    val measurements by viewModel.measurements.collectAsState()
    val feedbacks by viewModel.doctorFeedbacks.collectAsState()

    var feedbackText by remember { mutableStateOf("") }
    var selectedPatientDropdownExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("لوحة تحكم الطبيب المختص") },
                actions = {
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("doctor_logout_button")) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مرحباً، د. ${currentUser?.name ?: ""}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "بوابة إدارة ومراقبة الحالة الصحية لمرضاك. يمكنك اختيار أي مريض مسجل لاستعراض تاريخ قياساته وإصدار مراجعات وتوجيهات طبية فورية.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Patient Selector
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "اختر المريض المراد فحصه ومتابعته:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { selectedPatientDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedPatient?.let { "${it.name} (${it.phone})" } ?: "انقر لاختيار مريض مسجل...",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    DropdownMenu(
                        expanded = selectedPatientDropdownExpanded,
                        onDismissRequest = { selectedPatientDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        if (allPatients.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("لا يوجد مرضى مسجلين بالنظام حالياً") },
                                onClick = { selectedPatientDropdownExpanded = false }
                            )
                        } else {
                            allPatients.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text("${p.name} • ${p.phone} (عمر: ${p.age})") },
                                    onClick = {
                                        viewModel.selectPatient(p)
                                        selectedPatientDropdownExpanded = false
                                        feedbackText = ""
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Selected Patient Detail Card
            if (selectedPatient != null) {
                val p = selectedPatient!!
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "اسم المريض: ${p.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(text = "العمر: ${p.age} • الجنس: ${p.gender} • الهاتف: ${p.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                        
                        val diseasesList = p.diseases.split(",").filter { it.isNotBlank() }
                        if (diseasesList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                diseasesList.forEach { d ->
                                    val arabicDiseaseName = when (d) {
                                        "DIABETES" -> "السكري"
                                        "HYPERTENSION" -> "ضغط الدم"
                                        "ASTHMA" -> "الربو"
                                        "HEART" -> "القلب"
                                        else -> d
                                    }
                                    SuggestionChip(
                                        onClick = { },
                                        label = { Text(arabicDiseaseName, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Medical Feedback Form
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "إصدار ملاحظة طبية وتوجيهات للمريض:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = feedbackText,
                            onValueChange = { feedbackText = it },
                            label = { Text("اكتب ملاحظاتك، إرشاداتك، أو توجيهات جرعات الدواء هنا...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (feedbackText.isNotBlank()) {
                                    viewModel.addFeedback(p.phone, feedbackText)
                                    feedbackText = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إرسال الملاحظة للمريض")
                        }
                    }
                }

                // Recent Feedbacks log
                if (feedbacks.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "تاريخ توجيهاتك الطبية السابقة:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            feedbacks.forEach { f ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(text = f.feedbackText, fontSize = 13.sp)
                                    Text(
                                        text = "د. ${f.doctorName}", 
                                        fontSize = 11.sp, 
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Divider()
                                }
                            }
                        }
                    }
                }

                // Patient measurements trend
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "سجل قياسات المريض الحيوية كاملة:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (measurements.isEmpty()) {
                            Text(
                                text = "لم يقم هذا المريض بإدخال أي قياسات بعد في ملفه الشخصي.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        } else {
                            measurements.forEach { m ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = when (m.type) {
                                                "DIABETES" -> "سكر الدم"
                                                "HYPERTENSION" -> "ضغط الدم والنبض"
                                                "ASTHMA" -> "نوبات الربو"
                                                "HEART" -> "نبض القلب"
                                                else -> "قياس حيوي"
                                            },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (m.note.isNotBlank()) {
                                            Text(text = "ملاحظة: ${m.note}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        }
                                    }
                                    Text(
                                        text = when (m.type) {
                                            "DIABETES" -> "${m.value1.toInt()} mg/dL"
                                            "HYPERTENSION" -> "${m.value1.toInt()}/${m.value2.toInt()} mmHg"
                                            "ASTHMA" -> "${m.value1.toInt()} نوبات"
                                            "HEART" -> "${m.value1.toInt()} bpm"
                                            else -> "${m.value1}"
                                        },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Divider()
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.SupervisedUserCircle, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "يرجى اختيار مريض مسجل من القائمة بالأعلى للبدء في تشخيص ومعاينة تاريخ حالته الصحية.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
