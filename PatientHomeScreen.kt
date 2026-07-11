package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Measurement
import com.example.data.model.Reminder
import com.example.ui.theme.EmergencyRed
import com.example.ui.viewmodel.HealthViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHomeScreen(
    viewModel: HealthViewModel,
    onNavigateToMeasurements: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToFiles: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToPricing: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val measurements by viewModel.measurements.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val streak by viewModel.commitmentStreak.collectAsState()

    var showEmergencyDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "مرحباً، ${currentUser?.name ?: ""}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تطبيق رعاية الحياة اليومية",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("patient_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Streak Flare Card ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = "Commitment Streak",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "التزامك المتواصل",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "لقد قمت بإدخال قياساتك والالتزام بأدويتك بانتظام!",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Text(
                        text = "$streak يوم",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // --- RED EMERGENCY BUTTON ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEmergencyDialog = true }
                    .testTag("emergency_button"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EmergencyRed),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContactPhone,
                            contentDescription = "SOS",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "أحتاج مساعدة عاجلة (SOS)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "اضغط هنا لإرسال موقعك وإشعار طوارئ فوراً لعائلتك وأرقام الإسعاف",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // --- Today's Status & Latest Measurement ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Today's general status card
                Card(
                    modifier = Modifier.weight(1.0f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "حالة اليوم",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val completedCount = reminders.count { it.isCompletedToday }
                        val totalCount = reminders.size
                        Text(
                            text = if (totalCount == 0) "لا توجد مهام اليوم" 
                                   else if (completedCount == totalCount) "مكتملة بالكامل! 🎉" 
                                   else "أتممت $completedCount من $totalCount",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Latest measurement card
                Card(
                    modifier = Modifier.weight(1.0f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "آخر قياس",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val lastMeas = measurements.firstOrNull()
                        Text(
                            text = if (lastMeas == null) "لا توجد قياسات"
                                   else when (lastMeas.type) {
                                       "DIABETES" -> "${lastMeas.value1.toInt()} mg/dL"
                                       "HYPERTENSION" -> "${lastMeas.value1.toInt()}/${lastMeas.value2.toInt()}"
                                       "ASTHMA" -> "${lastMeas.value1.toInt()} نوبات"
                                       "HEART" -> "${lastMeas.value1.toInt()} نبضة/د"
                                       else -> "${lastMeas.value1}"
                                   },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // --- Upcoming Reminders (الأدوبة القادمة) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "التذكيرات القادمة",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = onNavigateToReminders) {
                            Text("عرض الكل")
                        }
                    }

                    val pendingReminders = reminders.filter { !it.isCompletedToday }.take(2)
                    if (pendingReminders.isEmpty()) {
                        Text(
                            text = "رائع! لقد أتممت جميع التذكيرات لليوم.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        pendingReminders.forEach { r ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { viewModel.toggleReminder(r) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (r.type == "MEDICATION") Icons.Default.MedicalInformation else Icons.Default.Bloodtype,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1.0f)) {
                                    Text(
                                        text = r.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "الجرعة/القياس في: ${r.timeStr}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Checkbox(
                                    checked = r.isCompletedToday,
                                    onCheckedChange = { viewModel.toggleReminder(r) }
                                )
                            }
                        }
                    }
                }
            }

            // --- Grid Navigation Buttons ---
            Text(
                text = "الخدمات الطبية والملفات",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HomeShortcutButton(
                    title = "سجل القياسات",
                    subtitle = "إدخال واستعراض السكر والضغط",
                    icon = Icons.Default.MonitorHeart,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1.0f),
                    onClick = onNavigateToMeasurements
                )
                HomeShortcutButton(
                    title = "ملفاتي الطبية",
                    subtitle = "التحاليل والأشعة والتقارير",
                    icon = Icons.Default.FolderOpen,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1.0f),
                    onClick = onNavigateToFiles
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HomeShortcutButton(
                    title = "تحليل الذكاء الاصطناعي",
                    subtitle = "تحليل قياساتك ورصد التدهور",
                    icon = Icons.Default.AutoAwesome,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1.0f),
                    onClick = onNavigateToReport
                )
                HomeShortcutButton(
                    title = "العضوية المميزة Premium",
                    subtitle = "باقات الاشتراك والأسعار",
                    icon = Icons.Default.Star,
                    color = Color(0xFFFFB300),
                    modifier = Modifier.weight(1.0f),
                    onClick = onNavigateToPricing
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // SOS Emergency Alert Overlay Dialog
    if (showEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            confirmButton = {
                Button(
                    onClick = { showEmergencyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text("تم الفهم والمتابعة")
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = EmergencyRed,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "🚨 حالة طوارئ - تم التنبيه!",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = EmergencyRed
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "تم إرسال نداء استغاثة SOS فوري لجميع أفراد عائلتك المسجلين، بالإضافة لموقعك الجغرافي التقريبي:",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "موقعك الحالي: 24.7136° N, 46.6753° E (الرياض)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "أرقام الطوارئ المحلية للاتصال السريع:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    EmergencyPhoneRow(label = "الإسعاف السعودي", number = "997")
                    EmergencyPhoneRow(label = "الدفاع المدني", number = "998")
                    EmergencyPhoneRow(label = "الشرطة", number = "999")
                    EmergencyPhoneRow(label = "طوارئ الصحة", number = "937")
                }
            }
        )
    }
}

@Composable
fun HomeShortcutButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), lineHeight = 12.sp)
            }
        }
    }
}

@Composable
fun EmergencyPhoneRow(label: String, number: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Button(
            onClick = { /* Simulated Call */ },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = number, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}
