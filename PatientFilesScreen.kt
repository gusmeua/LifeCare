package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.MedicalFile
import com.example.ui.viewmodel.HealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientFilesScreen(
    viewModel: HealthViewModel,
    onNavigateBack: () -> Unit
) {
    val files by viewModel.medicalFiles.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الملفات الطبية المحفوظة") },
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
                modifier = Modifier.testTag("add_file_fab")
            ) {
                Icon(imageVector = Icons.Default.FileUpload, contentDescription = "Upload File")
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
            // Header Info Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "احفظ وتصفح جميع تحاليلك، صور الأشعة، الوصفات، والتقارير الطبية الهامة للرجوع إليها ومشاركتها مع طبيبك بسهولة.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            Text(
                text = "مستنداتك الطبية",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (files.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderSpecial,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد مستندات طبية مسجلة بعد. اضغط + لحفظ ملف جديد.",
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
                    items(files) { item ->
                        MedicalFileRow(
                            file = item,
                            onDelete = { viewModel.deleteFile(item.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFileDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, type, summary ->
                viewModel.addFile(name, type, summary)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MedicalFileRow(
    file: MedicalFile,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.0f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = when (file.fileType) {
                                    "SCAN" -> Color(0xFF1565C0).copy(alpha = 0.1f)
                                    "LAB" -> Color(0xFF7B1FA2).copy(alpha = 0.1f)
                                    "PRESCRIPTION" -> Color(0xFF2E7D32).copy(alpha = 0.1f)
                                    "REPORT" -> Color(0xFF37474F).copy(alpha = 0.1f)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (file.fileType) {
                                "SCAN" -> Icons.Default.BrokenImage // Scans / Rays
                                "LAB" -> Icons.Default.Science       // Lab Tests
                                "PRESCRIPTION" -> Icons.Default.Description // Prescription
                                "REPORT" -> Icons.Default.LibraryBooks     // Report
                                else -> Icons.Default.AttachFile
                            },
                            contentDescription = null,
                            tint = when (file.fileType) {
                                "SCAN" -> Color(0xFF1565C0)
                                "LAB" -> Color(0xFF7B1FA2)
                                "PRESCRIPTION" -> Color(0xFF2E7D32)
                                "REPORT" -> Color(0xFF37474F)
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = file.fileName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${getArabicFileLabel(file.fileType)} • تم الرفع في: ${file.dateStr}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "ملخص محتويات الملف / التقرير الطبي:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = if (file.summary.isNotBlank()) file.summary else "لا يوجد ملخص مضاف لهذا المستند.",
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

fun getArabicFileLabel(type: String): String {
    return when (type) {
        "SCAN" -> "صورة أشعة (Scan)"
        "LAB" -> "تحليل مخبري (Lab)"
        "PRESCRIPTION" -> "وصفة طبية (Prescription)"
        "REPORT" -> "تقرير طبي (Report)"
        else -> "مستند"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFileDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, summary: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("LAB") }
    var summary by remember { mutableStateOf("") }

    var errorText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || summary.isBlank()) {
                        errorText = "يرجى ملء جميع الحقول المطلوبة"
                        return@Button
                    }
                    onSave(name, type, summary)
                }
            ) {
                Text("حفظ المستند")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        title = {
            Text(
                text = "حفظ مستند أو ملف طبي جديد",
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
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المستند (مثال: أشعة الصدر، تحليل الحديد)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("نوع المستند:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val types = listOf("LAB", "SCAN", "PRESCRIPTION", "REPORT")
                    types.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(getArabicFileLabel(t).split(" ")[0]) }
                        )
                    }
                }

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("الملخص أو القراءة الطبية بالتفصيل") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        }
    )
}
