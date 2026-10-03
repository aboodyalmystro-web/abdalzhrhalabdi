package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class AudioService(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingRequest: Triple<String, String?, Float?>? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentUtterance = MutableStateFlow<String?>(null)
    val currentUtterance: StateFlow<String?> = _currentUtterance

    var currentAccent: String = "US" // "US" or "UK"
    var speechRate: Float = 0.95f // slightly slowed for optimal ESL learner comprehension

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                applySettings()
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isPlaying.value = true
                    }
                    override fun onDone(utteranceId: String?) {
                        _isPlaying.value = false
                        _currentUtterance.value = null
                    }
                    override fun onError(utteranceId: String?) {
                        _isPlaying.value = false
                        _currentUtterance.value = null
                    }
                })

                // Execute any queued request that was called prior to initialization completing
                pendingRequest?.let { (text, accent, rate) ->
                    pendingRequest = null
                    speak(text, accent, rate)
                }
            } else {
                Log.e("AudioService", "TTS Initialization failed: $status")
            }
        }
    }

    private fun applySettings() {
        if (!isInitialized) return
        val locale = if (currentAccent == "UK") Locale.UK else Locale.US
        val res = tts?.setLanguage(locale)
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.setLanguage(Locale.ENGLISH)
        }
        tts?.setSpeechRate(speechRate)
    }

    fun speak(text: String, accent: String? = null, rate: Float? = null) {
        if (!isInitialized) {
            pendingRequest = Triple(text, accent, rate)
            return
        }
        accent?.let { currentAccent = it }
        rate?.let { speechRate = it }
        applySettings()

        _currentUtterance.value = text
        val utteranceId = "tts_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isPlaying.value = false
        _currentUtterance.value = null
        pendingRequest = null
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        pendingRequest = null
    }
}
