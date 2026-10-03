package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import kotlin.math.max

data class PronunciationResult(
    val spokenText: String,
    val targetText: String,
    val scorePercentage: Int,
    val feedbackEn: String,
    val feedbackAr: String
)

class SpeechRecognitionHelper(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _result = MutableStateFlow<PronunciationResult?>(null)
    val result: StateFlow<PronunciationResult?> = _result

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var currentTargetText: String = ""

    fun startListening(targetText: String, accent: String = "US") {
        mainHandler.post {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                _errorMessage.value = "التعرف على الصوت غير متاح على هذا الجهاز أو المحاكي"
                return@post
            }

            internalStopListening()
            currentTargetText = targetText
            _result.value = null
            _errorMessage.value = null

            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListening.value = true
                        }
                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {
                            _isListening.value = false
                        }
                        override fun onError(error: Int) {
                            _isListening.value = false
                            val errorDesc = when (error) {
                                SpeechRecognizer.ERROR_NO_MATCH -> "لم يتم التقاط صوت واضح، حاول مرة أخرى بالقرب من الميكروفون"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "انتهت مهلة الصوت دون استماع"
                                SpeechRecognizer.ERROR_AUDIO -> "خطأ في تسجيل الصوت"
                                else -> "خطأ في معالجة النطق ($error)"
                            }
                            _errorMessage.value = errorDesc
                        }

                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val spoken = matches?.firstOrNull() ?: ""
                            evaluatePronunciation(spoken, currentTargetText)
                        }

                        override fun onPartialResults(partialResults: Bundle?) {}
                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    val langTag = if (accent == "UK") "en-GB" else "en-US"
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, langTag)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                _isListening.value = false
                _errorMessage.value = "تعذر بدء التسجيل: ${e.message}"
            }
        }
    }

    private fun internalStopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // ignore
        }
        speechRecognizer = null
        _isListening.value = false
    }

    fun stopListening() {
        mainHandler.post {
            internalStopListening()
        }
    }

    private fun evaluatePronunciation(spoken: String, target: String) {
        val cleanSpoken = spoken.trim().lowercase(Locale.ENGLISH).replace(Regex("[^a-z0-9 ]"), "")
        val cleanTarget = target.trim().lowercase(Locale.ENGLISH).replace(Regex("[^a-z0-9 ]"), "")

        val score = calculateSimilarity(cleanSpoken, cleanTarget)

        val (feedbackEn, feedbackAr) = when {
            score >= 90 -> Pair(
                "Outstanding pronunciation! Native-like clarity.",
                "نطق مذهل ومطابق للكنة المطلوبة بدقة عالية!"
            )
            score >= 75 -> Pair(
                "Very good! Clear and understandable with minor accent variations.",
                "ممتاز جداً! مخارج الحروف واضحة ومفهومة بطلاقة."
            )
            score >= 55 -> Pair(
                "Good attempt! Focus on articulation and vowel length.",
                "محاولة جيدة! ركز على مخارج أصوات العلة وامتداد الصوت."
            )
            else -> Pair(
                "Keep practicing! Listen to the audio model again and repeat slowly.",
                "تحتاج لمزيد من المران؛ استمع للنموذج الصوتي وكرر ببطء."
            )
        }

        _result.value = PronunciationResult(
            spokenText = spoken,
            targetText = target,
            scorePercentage = score,
            feedbackEn = feedbackEn,
            feedbackAr = feedbackAr
        )
    }

    private fun calculateSimilarity(s1: String, s2: String): Int {
        if (s1 == s2) return 100
        if (s1.isEmpty() || s2.isEmpty()) return 0

        val distance = levenshteinDistance(s1, s2)
        val maxLen = max(s1.length, s2.length)
        val similarity = ((1.0 - distance.toDouble() / maxLen.toDouble()) * 100).toInt()
        return max(0, similarity)
    }

    private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        var cost = IntArray(lhs.length + 1) { it }
        var newCost = IntArray(lhs.length + 1) { 0 }

        for (i in 1..rhs.length) {
            newCost[0] = i
            for (j in 1..lhs.length) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1
                newCost[j] = minOf(costInsert, costDelete, costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[lhs.length]
    }
}
