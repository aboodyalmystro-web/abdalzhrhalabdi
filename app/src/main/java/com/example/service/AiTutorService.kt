package com.example.service

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val sender: String, // "USER" or "AI"
    val text: String,
    val arabicTranslation: String = ""
)

data class WritingEvaluation(
    val correctedText: String,
    val score: Int, // 0 - 100
    val grammaticalErrors: List<String>,
    val enhancementTips: List<String>,
    val arabicSummary: String
)

class AiTutorService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun sendRoleplayMessage(
        scenario: String,
        userMessage: String,
        chatHistory: List<ChatMessage>
    ): ChatMessage = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = buildString {
                    append("You are an expert English language tutor conducting a conversational roleplay in the scenario: '$scenario'. ")
                    append("Your goal is to help ESL students improve fluency. Respond naturally as the roleplay character (e.g. barista, airport officer, interviewer). ")
                    append("Keep your answer conversational (2-3 sentences), encouraging, and clear. ")
                    append("Also provide a brief Arabic translation in this exact JSON format: {\"replyEn\": \"...\", \"replyAr\": \"...\"}\n")
                    append("Conversation history:\n")
                    chatHistory.takeLast(6).forEach {
                        append("${it.sender}: ${it.text}\n")
                    }
                    append("USER: $userMessage\n")
                }

                val jsonBody = JSONObject().apply {
                    val contentsArr = JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", prompt) })
                            })
                        })
                    }
                    put("contents", contentsArr)
                }

                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                    val jsonObj = JSONObject(responseBody)
                    val rawText = jsonObj.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")

                    // Clean markdown code blocks if present
                    val cleanJson = rawText.replace("```json", "").replace("```", "").trim()
                    try {
                        val parsed = JSONObject(cleanJson)
                        return@withContext ChatMessage(
                            sender = "AI",
                            text = parsed.optString("replyEn", rawText),
                            arabicTranslation = parsed.optString("replyAr", "")
                        )
                    } catch (e: Exception) {
                        return@withContext ChatMessage(
                            sender = "AI",
                            text = rawText,
                            arabicTranslation = "استجابة تم توليدها بالذكاء الاصطناعي لممارسة المحادثة."
                        )
                    }
                }
            } catch (e: Exception) {
                // Fall back to offline scripted tutor
            }
        }

        // Offline intelligent scenario responder
        val fallback = getOfflineScenarioResponse(scenario, userMessage)
        ChatMessage(sender = "AI", text = fallback.first, arabicTranslation = fallback.second)
    }

    suspend fun evaluateWriting(
        prompt: String,
        userSubmission: String
    ): WritingEvaluation = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val systemPrompt = """
                    You are a senior ESL Cambridge/IELTS writing examiner.
                    Analyze this student submission for prompt: '$prompt'.
                    Student text: '$userSubmission'.
                    Return ONLY a valid JSON object matching this schema:
                    {
                      "correctedText": "The fully corrected text",
                      "score": 85,
                      "errors": ["Error 1 description", "Error 2 description"],
                      "tips": ["Tip 1 to improve vocabulary", "Tip 2 to improve sentence structure"],
                      "arabicSummary": "ملخص الملاحظات النحوية بالعربية"
                    }
                """.trimIndent()

                val jsonBody = JSONObject().apply {
                    val contentsArr = JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", systemPrompt) })
                            })
                        })
                    }
                    put("contents", contentsArr)
                }

                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                    val jsonObj = JSONObject(responseBody)
                    val rawText = jsonObj.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")

                    val cleanJson = rawText.replace("```json", "").replace("```", "").trim()
                    val parsed = JSONObject(cleanJson)
                    val errorsArr = parsed.optJSONArray("errors") ?: JSONArray()
                    val tipsArr = parsed.optJSONArray("tips") ?: JSONArray()
                    val errors = (0 until errorsArr.length()).map { errorsArr.getString(it) }
                    val tips = (0 until tipsArr.length()).map { tipsArr.getString(it) }

                    return@withContext WritingEvaluation(
                        correctedText = parsed.optString("correctedText", userSubmission),
                        score = parsed.optInt("score", 80),
                        grammaticalErrors = errors,
                        enhancementTips = tips,
                        arabicSummary = parsed.optString("arabicSummary", "تم التدقيق بنجاح.")
                    )
                }
            } catch (e: Exception) {
                // fall back to offline grammar heuristics
            }
        }

        // Offline rule-based ESL evaluator
        evaluateOfflineWriting(userSubmission)
    }

    private fun getOfflineScenarioResponse(scenario: String, message: String): Pair<String, String> {
        val lower = message.lowercase()
        return when {
            scenario.contains("Job", ignoreCase = true) || scenario.contains("Interview", ignoreCase = true) -> {
                when {
                    lower.contains("hello") || lower.contains("hi") ->
                        Pair(
                            "Good morning! It's a pleasure to meet you. To start off, could you briefly introduce yourself and your background?",
                            "صباح الخير! يسعدني مقابلتك. لبدء المقابلة، هل يمكنك تقديم نفسك وخلفيتك المهنية باختصار؟"
                        )
                    lower.contains("experience") || lower.contains("work") || lower.contains("developer") ->
                        Pair(
                            "That is very relevant experience. Can you tell me about a time you encountered a conflict or setback on a project and how you resolved it?",
                            "هذه خبرة وثيقة الصلة. هل يمكنك إخباري عن موقف واجهت فيه عقبة بمشروع وكيف قمت بحلها؟"
                        )
                    else ->
                        Pair(
                            "Thank you for sharing that insight. How do you see yourself contributing to our company's future growth?",
                            "شكراً لمشاركتك هذه الرؤية. كيف ترى مساهمتك في نمو شركتنا المستقبلي؟"
                        )
                }
            }
            scenario.contains("Cafe", ignoreCase = true) || scenario.contains("Coffee", ignoreCase = true) -> {
                when {
                    lower.contains("latte") || lower.contains("cappuccino") || lower.contains("coffee") ->
                        Pair(
                            "Great choice! Would you like that hot or iced, and what size can I get for you?",
                            "اختيار رائع! هل ترغب به ساخناً أم مثلجاً، وما الحجم الذي تفضله؟"
                        )
                    lower.contains("oat") || lower.contains("milk") || lower.contains("sugar") ->
                        Pair(
                            "Sure thing! Anything to eat with that, like a warm blueberry muffin or croissant?",
                            "بكل تأكيد! هل ترغب في معجنات مع طلبك، مثل مافن التوت أو الكرواسون؟"
                        )
                    else ->
                        Pair(
                            "Hi there! Welcome to Central Brew. What can I craft for you today?",
                            "مرحباً بك في سنترال برو! ماذا يمكنني أن أصنع لك اليوم؟"
                        )
                }
            }
            scenario.contains("Airport", ignoreCase = true) -> {
                when {
                    lower.contains("passport") || lower.contains("ticket") || lower.contains("here") ->
                        Pair(
                            "Thank you. Are you checking any bags today or just traveling with carry-on luggage?",
                            "شكراً لك. هل ستقوم بشحن أي حقائب اليوم أم تسافر بأمتعة يد فقط؟"
                        )
                    lower.contains("bag") || lower.contains("suitcase") || lower.contains("carry") ->
                        Pair(
                            "Understood. Place your bags on the scale, please. Here is your boarding pass for Gate 14B.",
                            "مفهوم. تفضل بوضع الحقائب على الميزان. تفضل بطاقة الصعود لبوابة 14B."
                        )
                    else ->
                        Pair(
                            "Good afternoon, welcome to flight check-in. May I see your passport and booking reference?",
                            "مساء الخير، مرحباً بك في تسجيل الرحلة. هل تسمح برؤية جواز السفر ورقم الحجز؟"
                        )
                }
            }
            else -> {
                Pair(
                    "That sounds very interesting! Can you elaborate on what you mean and give an example?",
                    "هذا يبدو مثيراً للاهتمام! هل يمكنك التوضيح أكثر وتقديم مثال؟"
                )
            }
        }
    }

    private fun evaluateOfflineWriting(text: String): WritingEvaluation {
        val errors = mutableListOf<String>()
        val tips = mutableListOf<String>()
        var corrected = text

        // Check 1: Missing "is/are" in nominal sentences (typical Arabic learner error)
        if (Regex("\\b(he|she|it)\\s+(a|an|very|good|happy|tired)\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)) {
            errors.add("Missing auxiliary 'is' after singular pronoun (e.g., 'he is a' instead of 'he a').")
            corrected = corrected.replace(Regex("\\b(he|she|it)\\s+(a|an|very)\\b", RegexOption.IGNORE_CASE)) {
                "${it.groupValues[1]} is ${it.groupValues[2]}"
            }
        }

        // Check 2: "agree with" vs "am agree"
        if (text.contains("am agree", ignoreCase = true) || text.contains("is agree", ignoreCase = true)) {
            errors.add("In English, 'agree' is a verb on its own. Say 'I agree', not 'I am agree'.")
            corrected = corrected.replace("am agree", "agree", ignoreCase = true)
                .replace("is agree", "agrees", ignoreCase = true)
        }

        // Check 3: "since" with durations vs "for"
        if (Regex("since\\s+\\d+\\s+(years?|months?|days?|hours?)", RegexOption.IGNORE_CASE).containsMatchIn(text)) {
            errors.add("Use 'for' to describe a period of duration, and 'since' for a starting point (e.g., 'for 3 years' not 'since 3 years').")
            corrected = corrected.replace(Regex("since(\\s+\\d+\\s+(?:years?|months?|days?|hours?))", RegexOption.IGNORE_CASE)) {
                "for${it.groupValues[1]}"
            }
        }

        // Check 4: Capitalization of first letter
        if (text.isNotEmpty() && text.first().isLowerCase()) {
            errors.add("Sentences must begin with a capital letter.")
            corrected = corrected.replaceFirstChar { it.uppercase() }
        }

        if (errors.isEmpty()) {
            tips.add("Impressive sentence construction! Consider utilizing richer transitional linkers (e.g., Furthermore, Consequently).")
            tips.add("Try varying sentence lengths between compound and complex structures.")
        } else {
            tips.add("Review verb agreement and preposition pairs regularly using the SRS flashcard deck.")
        }

        val score = if (errors.isEmpty()) 95 else (85 - errors.size * 12).coerceAtLeast(40)

        return WritingEvaluation(
            correctedText = corrected,
            score = score,
            grammaticalErrors = if (errors.isEmpty()) listOf("No major grammatical errors detected!") else errors,
            enhancementTips = tips,
            arabicSummary = if (errors.isEmpty()) "صياغة ممتازة وسليمة نحوياً!" else "تم رصد بعض الملاحظات النحوية الدقيقة وتم توضيحها وتصحيحها."
        )
    }
}
