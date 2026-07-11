package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.HealthViewModel

@Composable
fun RegisterScreen(
    role: String,
    viewModel: HealthViewModel,
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val authError by viewModel.authError.collectAsState()

    var name by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("ذكر") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Patient chronic diseases selection states
    var diabetesSelected by remember { mutableStateOf(false) }
    var pressureSelected by remember { mutableStateOf(false) }
    var asthmaSelected by remember { mutableStateOf(false) }
    var heartSelected by remember { mutableStateOf(false) }

    // Linked Patient Phone (For Family & Doctor)
    var linkedPhone by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("register_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "إنشاء حساب جديد",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val roleArabic = when (role) {
                "PATIENT" -> "مريض"
                "FAMILY" -> "متابع عائلي"
                "DOCTOR" -> "طبيب مختص"
                else -> role
            }

            Text(
                text = "الحساب كـ: $roleArabic",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error Display
            authError?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // --- Form Fields ---

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("الاسم الكامل") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_name_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("رقم الهاتف") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_phone_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Age and Gender Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Age
                OutlinedTextField(
                    value = ageStr,
                    onValueChange = { ageStr = it },
                    label = { Text("العمر") },
                    leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                    modifier = Modifier
                        .weight(1.0f)
                        .testTag("register_age_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Gender Choice Box
                Column(
                    modifier = Modifier
                        .weight(1.0f)
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = "الجنس:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = gender == "ذكر",
                            onClick = { gender = "ذكر" },
                            label = { Text("ذكر") },
                            leadingIcon = { if (gender == "ذكر") Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        )
                        FilterChip(
                            selected = gender == "أنثى",
                            onClick = { gender = "أنثى" },
                            label = { Text("أنثى") },
                            leadingIcon = { if (gender == "أنثى") Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("كلمة المرور") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_password_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Dynamic Inputs based on Roles
            if (role == "PATIENT") {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "اختر الأمراض المزمنة التي ترغب بمتابعتها:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        DiseaseCheckbox(
                            label = "السكري",
                            checked = diabetesSelected,
                            onCheckedChange = { diabetesSelected = it }
                        )
                        DiseaseCheckbox(
                            label = "الضغط",
                            checked = pressureSelected,
                            onCheckedChange = { pressureSelected = it }
                        )
                        DiseaseCheckbox(
                            label = "الربو",
                            checked = asthmaSelected,
                            onCheckedChange = { asthmaSelected = it }
                        )
                        DiseaseCheckbox(
                            label = "القلب",
                            checked = heartSelected,
                            onCheckedChange = { heartSelected = it }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = linkedPhone,
                    onValueChange = { linkedPhone = it },
                    label = { Text("رقم هاتف المريض المراد متابعته") },
                    leadingIcon = { Icon(Icons.Default.PersonSearch, contentDescription = null) },
                    placeholder = { Text("أدخل رقم هاتف مريض مسجل") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_linked_phone_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Text(
                    text = "ملاحظة: تأكد أن المريض قد أنشأ حسابه أولاً باستخدام هذا الرقم لتتمكن من مراقبة بياناته وتنبيهاته.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp),
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Button
            Button(
                onClick = {
                    val diseasesList = mutableListOf<String>()
                    if (diabetesSelected) diseasesList.add("DIABETES")
                    if (pressureSelected) diseasesList.add("HYPERTENSION")
                    if (asthmaSelected) diseasesList.add("ASTHMA")
                    if (heartSelected) diseasesList.add("HEART")
                    val diseasesStr = diseasesList.joinToString(",")

                    viewModel.register(
                        name = name,
                        ageStr = ageStr,
                        gender = gender,
                        phone = phone,
                        role = role,
                        diseases = diseasesStr,
                        linkedPhone = linkedPhone,
                        onSuccess = onRegisterSuccess
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("register_submit_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "تأكيد وإنشاء الحساب",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DiseaseCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
