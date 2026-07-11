package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Reminder
import com.example.ui.viewmodel.HealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRemindersScreen(
    viewModel: HealthViewModel,
    onNavigateBack: () -> Unit
) {
    val reminders by viewModel.reminders.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("منظم المواعيد والأدوية") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_reminder_fab")
            ) {
                Icon(imageVector = Icons.Default.AddAlarm, contentDescription = "Add Alarm")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "جدول التذكيرات لليوم. النقر على الصندوق يثبت التزامك بالجرعة أو القياس لتحديث معدل نشاطك ومثابرتك اليومية.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            Text(
                text = "قائمة التذكيرات والجرعات",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NotificationAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد تذكيرات مسجلة. اضغط + لإضافة دواء أو موعد قياس.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1.0f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reminders) { item ->
                        ReminderRow(
                            item = item,
                            onToggle = { viewModel.toggleReminder(item) },
                            onDelete = { viewModel.deleteReminder(item.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, type, timeStr ->
                viewModel.addReminder(title, type, timeStr)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ReminderRow(
    item: Reminder,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompletedToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) 
                             else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.0f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = when (item.type) {
                                "MEDICATION" -> Color(0xFF00796B).copy(alpha = 0.1f)
                                "MEASUREMENT" -> Color(0xFFFFA000).copy(alpha = 0.1f)
                                "DOCTOR" -> Color(0xFF1976D2).copy(alpha = 0.1f)
                                "LAB" -> Color(0xFF7B1FA2).copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (item.type) {
                            "MEDICATION" -> Icons.Default.MedicalServices
                            "MEASUREMENT" -> Icons.Default.MonitorHeart
                            "DOCTOR" -> Icons.Default.AssignmentInd
                            "LAB" -> Icons.Default.Science
                            else -> Icons.Default.Alarm
                        },
                        contentDescription = null,
                        tint = when (item.type) {
                            "MEDICATION" -> Color(0xFF00796B)
                            "MEASUREMENT" -> Color(0xFFFFA000)
                            "DOCTOR" -> Color(0xFF1976D2)
                            "LAB" -> Color(0xFF7B1FA2)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${getArabicTypeLabel(item.type)} • في الوقت: ${item.timeStr}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = item.isCompletedToday,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.testTag("toggle_reminder_${item.id}")
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

fun getArabicTypeLabel(type: String): String {
    return when (type) {
        "MEDICATION" -> "جرعة دواء"
        "MEASUREMENT" -> "قياس حيوي"
        "DOCTOR" -> "زيارة طبيب"
        "LAB" -> "تحاليل دورية"
        else -> "تذكير"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, type: String, timeStr: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("MEDICATION") }
    var timeStr by remember { mutableStateOf("") }

    var errorText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || timeStr.isBlank()) {
                        errorText = "يرجى ملء جميع الحقول"
                        return@Button
                    }
                    onSave(title, type, timeStr)
                }
            ) {
                Text("إضافة التذكير")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        title = {
            Text(
                text = "إضافة موعد أو تذكير جديد",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorText.isNotBlank()) {
                    Text(text = errorText, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان التذكير (مثال: دواء الضغط، فحص السكر الصباحي)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = timeStr,
                    onValueChange = { timeStr = it },
                    label = { Text("الوقت (مثال: 08:30 AM أو 22:00)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("نوع التذكير:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val types = listOf("MEDICATION", "MEASUREMENT", "DOCTOR", "LAB")
                    types.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(getArabicTypeLabel(t)) }
                        )
                    }
                }
            }
        }
    )
}
