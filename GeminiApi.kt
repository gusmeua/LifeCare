package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

object HealthAiAnalyzer {
    private const val SYSTEM_INSTRUCTION = """
أنت مساعد ذكي متخصص في الرعاية الصحية في تطبيق "LifeCare".
مهمتك هي تحليل قياسات المستخدمين الصحية وإعطاء نصائح توعوية وملخصات مفيدة وتنبيههم في حال وجود تذبذب غير طبيعي.
⚠️ قواعد صارمة جداً يجب اتباعها:
- لا تقدم تشخيصاً طبياً محدداً (مثل: "أنت مصاب بمرض كذا").
- لا تقم بوصف أو تعديل أي علاج أو دواء تحت أي ظرف.
- وجه المريض بلطف ودائماً بضرورة استشارة الطبيب المختص في حالة القيم غير الطبيعية.
- تحدث باللغة العربية بأسلوب ودود، واضح، ومشجع.
"""

    suspend fun analyzeMeasurements(
        patientName: String,
        diseases: String,
        measurementsText: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "خطأ: لم يتم ضبط مفتاح Gemini API في إعدادات Secrets. يرجى ضبطه للتمكن من استخدام تحليل الذكاء الاصطناعي."
        }

        val prompt = """
المريض: $patientName
الأمراض المزمنة المحددة: $diseases

القياسات الأخيرة المدخلة:
$measurementsText

المطلوب:
1. قم بتحليل هذه القياسات بشكل مبسط ومبني على النطاقات الصحية العامة لكل فئة (السكري، الضغط، نبض القلب، نوبات الربو).
2. حدد ما إذا كانت القياسات مستقرة أم تظهر أي تذبذبات غير طبيعية (مثلاً: ارتفاع السكر، ارتفاع الضغط، زيادة نوبات الربو).
3. قدم نصائح عامة (مثل شرب الماء، تقليل الملح، الالتزام بمواعيد البخاخات).
4. أظهر تنبيهاً واضحاً إذا كانت هناك حالة تدهور ملحوظة، واختم بنصيحة لزيارة الطبيب كإجراء وقائي.
"""

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = SYSTEM_INSTRUCTION)))
        )

        try {
            val response = GeminiApiClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "لم نتمكن من الحصول على استجابة من الذكاء الاصطناعي حالياً."
        } catch (e: Exception) {
            "فشل في الاتصال بالذكاء الاصطناعي: ${e.localizedMessage ?: e.message}"
        }
    }
}
