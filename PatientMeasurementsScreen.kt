package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import com.example.data.model.Measurement
import com.example.ui.viewmodel.HealthViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientMeasurementsScreen(
    viewModel: HealthViewModel,
    onNavigateBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val measurements by viewModel.measurements.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    // User's allowed types based on profile diseases
    val allowedDiseases = currentUser?.diseases?.split(",") ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سجل القياسات الحيوية") },
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
                modifier = Modifier.testTag("add_measurement_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
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
            // Trend Summary Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "التزامك بإدخال القياسات يومياً يساعد طبيبك على فهم تطور حالتك بدقة، ويساعد الذكاء الاصطناعي في تحليل تذبذباتها.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            Text(
                text = "القياسات الأخيرة",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (measurements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MonitorHeart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد أي قياسات مسجلة بعد. اضغط + لإضافة قياس جديد.",
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
                    items(measurements) { item ->
                        MeasurementRow(item = item, onDelete = { viewModel.deleteMeasurement(item.id) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMeasurementDialog(
            allowedTypes = allowedDiseases,
            onDismiss = { showAddDialog = false },
            onSave = { type, v1, v2, note ->
                viewModel.addMeasurement(type, v1, v2, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MeasurementRow(
    item: Measurement,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(item.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
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
                                "DIABETES" -> Color(0xFFFFEB3B).copy(alpha = 0.15f)
                                "HYPERTENSION" -> Color(0xFFE53935).copy(alpha = 0.15f)
                                "ASTHMA" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                "HEART" -> Color(0xFFE91E63).copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (item.type) {
                            "DIABETES" -> Icons.Default.Bloodtype
                            "HYPERTENSION" -> Icons.Default.Timeline
                            "ASTHMA" -> Icons.Default.Air
                            "HEART" -> Icons.Default.Favorite
                            else -> Icons.Default.MonitorHeart
                        },
                        contentDescription = null,
                        tint = when (item.type) {
                            "DIABETES" -> Color(0xFFF57F17)
                            "HYPERTENSION" -> Color(0xFFC62828)
                            "ASTHMA" -> Color(0xFF2E7D32)
                            "HEART" -> Color(0xFFAD1457)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = when (item.type) {
                            "DIABETES" -> "قياس سكر الدم"
                            "HYPERTENSION" -> "قياس ضغط الدم والنبض"
                            "ASTHMA" -> "نوبات الربو"
                            "HEART" -> "نبض القلب"
                            else -> "قياس حيوي"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    if (item.note.isNotBlank()) {
                        Text(
                            text = "ملاحظة: ${item.note}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 8.dp)) {
                    Text(
                        text = when (item.type) {
                            "DIABETES" -> "${item.value1.toInt()} mg/dL"
                            "HYPERTENSION" -> "${item.value1.toInt()}/${item.value2.toInt()} mmHg"
                            "ASTHMA" -> "${item.value1.toInt()} نوبات"
                            "HEART" -> "${item.value1.toInt()} bpm"
                            else -> "${item.value1}"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Color coded stability indicators
                    val (statusLabel, statusColor) = when (item.type) {
                        "DIABETES" -> {
                            val v = item.value1
                            if (v < 70) "منخفض" to Color(0xFFC62828)
                            else if (v > 180) "مرتفع" to Color(0xFFF57F17)
                            else "طبيعي" to Color(0xFF2E7D32)
                        }
                        "HYPERTENSION" -> {
                            val sys = item.value1
                            if (sys > 140) "مرتفع" to Color(0xFFC62828)
                            else if (sys < 90) "منخفض" to Color(0xFF1565C0)
                            else "طبيعي" to Color(0xFF2E7D32)
                        }
                        "ASTHMA" -> {
                            val count = item.value1
                            if (count >= 3) "نشط" to Color(0xFFC62828)
                            else if (count >= 1) "خفيف" to Color(0xFFF57F17)
                            else "مستقر" to Color(0xFF2E7D32)
                        }
                        "HEART" -> {
                            val rate = item.value1
                            if (rate < 60 || rate > 100) "تنبيه" to Color(0xFFC62828)
                            else "طبيعي" to Color(0xFF2E7D32)
                        }
                        else -> "مستقر" to Color(0xFF2E7D32)
                    }

                    Text(
                        text = statusLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeasurementDialog(
    allowedTypes: List<String>,
    onDismiss: () -> Unit,
    onSave: (type: String, v1: Double, v2: Double, note: String) -> Unit
) {
    // Default fallback list if user registration had no specific disease
    val types = if (allowedTypes.isEmpty() || allowedTypes.all { it.isBlank() }) {
        listOf("DIABETES", "HYPERTENSION", "ASTHMA", "HEART")
    } else {
        allowedTypes
    }

    var selectedType by remember { mutableStateOf(types.first()) }
    var val1 by remember { mutableStateOf("") }
    var val2 by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var errorText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val v1Double = val1.toDoubleOrNull()
                    if (v1Double == null || v1Double <= 0) {
                        errorText = "يرجى إدخال قيمة صحيحة وموجبة"
                        return@Button
                    }
                    val v2Double = val2.toDoubleOrNull() ?: 0.0
                    onSave(selectedType, v1Double, v2Double, note)
                }
            ) {
                Text("حفظ القياس")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        title = {
            Text(
                text = "إضافة قياس حيوي جديد",
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
                // Dropdown or list of available types
                Text("نوع القياس الصادر:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    types.forEach { t ->
                        val arabicName = when (t) {
                            "DIABETES" -> "سكري"
                            "HYPERTENSION" -> "ضغط"
                            "ASTHMA" -> "ربو"
                            "HEART" -> "قلب"
                            else -> t
                        }
                        FilterChip(
                            selected = selectedType == t,
                            onClick = { 
                                selectedType = t
                                val1 = ""
                                val2 = ""
                                errorText = ""
                            },
                            label = { Text(arabicName) }
                        )
                    }
                }

                if (errorText.isNotBlank()) {
                    Text(text = errorText, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                // Dynamic inputs
                when (selectedType) {
                    "DIABETES" -> {
                        OutlinedTextField(
                            value = val1,
                            onValueChange = { val1 = it },
                            label = { Text("مستوى السكر في الدم (mg/dL)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("الملاحظات (مثال: صائم، بعد الأكل بساعتين)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    "HYPERTENSION" -> {
                        OutlinedTextField(
                            value = val1,
                            onValueChange = { val1 = it },
                            label = { Text("الضغط الانقباضي (Systolic - العالي)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = val2,
                            onValueChange = { val2 = it },
                            label = { Text("الضغط الانبساطي (Diastolic - المنخفض)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("معدل النبض (مثال: 72)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    "ASTHMA" -> {
                        OutlinedTextField(
                            value = val1,
                            onValueChange = { val1 = it },
                            label = { Text("عدد نوبات الربو الملاحظة") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("المحفز أو التفاصيل (بخاخ، حبوب لقاح)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    "HEART" -> {
                        OutlinedTextField(
                            value = val1,
                            onValueChange = { val1 = it },
                            label = { Text("نبض القلب (bpm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = val2,
                            onValueChange = { val2 = it },
                            label = { Text("ضغط الدم المقترن (إن وجد)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("الحالة وقت القياس (مثال: مستلقٍ، بعد رياضة)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }
    )
}
