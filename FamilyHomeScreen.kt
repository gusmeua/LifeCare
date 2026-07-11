package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.ui.theme.EmergencyRed
import com.example.ui.viewmodel.HealthViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyHomeScreen(
    viewModel: HealthViewModel,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val patient by viewModel.selectedPatient.collectAsState()
    val measurements by viewModel.measurements.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val aiReport by viewModel.aiReport.collectAsState()
    val aiLoading by viewModel.aiLoading.collectAsState()

    var showSosAlert by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("بوابة المتابعة العائلية") },
                actions = {
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("family_logout_button")) {
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
            // Patient overview card
            if (patient == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لم يتم العثور على المريض المراد متابعته",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "تأكد من أن المريض قد سجل حسابه برقم الهاتف: ${currentUser?.linkedPatientPhone ?: ""} قبل متابعته.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                // Flashing Emergency Radar Monitor
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (showSosAlert) EmergencyRed.copy(alpha = 0.15f) 
                                         else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        color = if (showSosAlert) EmergencyRed else Color(0xFF4CAF50),
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "رادار الطوارئ SOS العائلي",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (showSosAlert) EmergencyRed else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (showSosAlert) "تحذير: المريض يطلب المساعدة العاجلة!" else "حالة مريضك مستقرة ولم يتم رصد طوارئ",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Button(
                            onClick = { showSosAlert = !showSosAlert },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showSosAlert) Color(0xFF4CAF50) else EmergencyRed
                            )
                        ) {
                            Text(if (showSosAlert) "محاكاة الاستقرار" else "محاكاة طوارئ مريضك")
                        }
                    }
                }

                // SOS Popup simulator
                if (showSosAlert) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmergencyRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.CrisisAlert, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⚠️ نداء استغاثة عاجل من مريضك!",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "مريضك ${patient?.name} ضغط على زر الاستغاثة SOS الآن.\nموقعه الجغرافي: الرياض (24.7136° N, 46.6753° E)",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Patient Info Summary
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AccountBox, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "المريض الخاضع للمتابعة: ${patient?.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(text = "العمر: ${patient?.age} سنة • الجنس: ${patient?.gender}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }

                // Recent measurements
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "آخر قياسات المريض الحيوية:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (measurements.isEmpty()) {
                            Text(
                                text = "لا توجد قياسات مسجلة للمريض حتى الآن.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        } else {
                            measurements.take(3).forEach { m ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = when (m.type) {
                                            "DIABETES" -> "سكر الدم"
                                            "HYPERTENSION" -> "ضغط الدم والنبض"
                                            "ASTHMA" -> "نوبات الربو"
                                            "HEART" -> "نبض القلب"
                                            else -> "قياس حيوي"
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = when (m.type) {
                                            "DIABETES" -> "${m.value1.toInt()} mg/dL"
                                            "HYPERTENSION" -> "${m.value1.toInt()}/${m.value2.toInt()} mmHg"
                                            "ASTHMA" -> "${m.value1.toInt()} نوبات"
                                            "HEART" -> "${m.value1.toInt()} bpm"
                                            else -> "${m.value1}"
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Patient Medication Status
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "التزام المريض بتناول الأدوية لليوم:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        val meds = reminders.filter { it.type == "MEDICATION" }
                        val completedMeds = meds.count { it.isCompletedToday }

                        if (meds.isEmpty()) {
                            Text(
                                text = "لم يتم ضبط مواعيد أدوية للمريض بعد.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        } else {
                            LinearProgressIndicator(
                                progress = { completedMeds.toFloat() / meds.size.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(Color.Transparent, RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "أخذ مريضك $completedMeds من أصل ${meds.size} جرعة دواء مقررة لليوم.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // AI Insight report request for patient
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "تقرير استشاري الذكاء الاصطناعي للمريض:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Button(
                            onClick = { viewModel.runAiAnalysis() },
                            enabled = !aiLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (aiLoading) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("إنشاء وتحليل التقرير الطبي لـ ${patient?.name}")
                            }
                        }

                        if (aiReport.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = aiReport,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
