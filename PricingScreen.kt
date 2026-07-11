package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingScreen(
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("باقات الاشتراك والميزات") },
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
            Text(
                text = "اختر الباقة الصحية الأنسب لاحتياجاتك",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "استمتع بمتابعة حالتك ومزامنتها مباشرة مع عائلتك وطبيبك المختص",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 1. FREE BASIC
            PricingPlanCard(
                title = "الباقة الأساسية المجانية",
                price = "0 ريال / شهرياً",
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                features = listOf(
                    "متابعة قياس مرض واحد فقط",
                    "تنبيهات طوارئ SOS أساسية",
                    "حفظ حتى 5 ملفات طبية فقط",
                    "ملخصات قراءة القياسات المحدودة"
                )
            )

            // 2. PREMIUM
            PricingPlanCard(
                title = "العضوية المميزة (Premium) ⭐",
                price = "29 ريال / شهرياً",
                color = MaterialTheme.colorScheme.primary,
                titleColor = MaterialTheme.colorScheme.onPrimary,
                features = listOf(
                    "متابعة غير محدودة لجميع الأمراض الأربعة",
                    "مستشار الذكاء الاصطناعي (تحليل Gemini) المتقدم",
                    "مزامنة فورية ودعم رادار العائلة الفوري",
                    "حفظ غير محدود للتحاليل والأشعة والتقارير",
                    "مشاركة مباشرة لملفك الصحي مع الطبيب"
                ),
                isPopular = true
            )

            // 3. DOCTOR SUBSCRIPTION
            PricingPlanCard(
                title = "باقة الطبيب والعيادات المستقلة",
                price = "99 ريال / شهرياً",
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                features = listOf(
                    "لوحة تحكم طبي كاملة لمتابعة المرضى",
                    "استقبال تنبيهات وتذبذبات قياسات المرضى فوراً",
                    "إصدار توجيهات وملاحظات طبية مدمجة",
                    "تصدير تقارير تطور الحالة كملفات PDF"
                )
            )

            // 4. CLINIC / HOSPITAL BUNDLE
            PricingPlanCard(
                title = "باقة المستشفيات والعيادات الكبرى 🏥",
                price = "سعر مخصص للجهات",
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                features = listOf(
                    "ربط أنظمة المستشفى الإلكترونية بملفات المرضى",
                    "تفعيل لوحات تحكم لأكثر من 50 طبيباً",
                    "دعم فني وأمن معلوماتي متكامل على مدار الساعة",
                    "تخصيص كامل لواجهة التطبيق بشعار جهتك"
                )
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PricingPlanCard(
    title: String,
    price: String,
    color: Color,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    features: List<String>,
    isPopular: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPopular) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isPopular) {
                Text(
                    text = "🔥 الخيار الأفضل والموصى به",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isPopular) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = price,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPopular) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = if (isPopular) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            features.forEach { f ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isPopular) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = f,
                        fontSize = 13.sp,
                        color = if (isPopular) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { /* Simulated purchase */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPopular) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    contentColor = if (isPopular) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isPopular) "اشترك الآن واستفد من الذكاء الاصطناعي" else "اختيار الباقة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
