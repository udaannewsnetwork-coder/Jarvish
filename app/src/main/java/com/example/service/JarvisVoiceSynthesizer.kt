package com.example.service

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class JarvisVoiceSynthesizer(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var pitch: Float = 0.95f
    private var speechRate: Float = 1.02f
    private var soundEffectsEnabled: Boolean = true

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        } catch (_: Exception) {
            // Audio service fallback
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.let { engine ->
                // Prefer British English for authentic J.A.R.V.I.S. cadence
                val result = engine.setLanguage(Locale.UK)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.setLanguage(Locale.US)
                }
                engine.setPitch(pitch)
                engine.setSpeechRate(speechRate)

                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    fun speak(text: String, playChime: Boolean = true) {
        if (!isInitialized || tts == null) return

        if (playChime && soundEffectsEnabled) {
            playTechChirp()
        }

        // Clean any markdown formatting from speech
        val cleanText = text
            .replace(Regex("[*#`_~>]"), "")
            .replace(Regex("\\{.*?\\}"), "")
            .trim()

        if (cleanText.isEmpty()) return

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "JARVIS_MSG_${System.currentTimeMillis()}")
        }

        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, "JARVIS_MSG")
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun playTechChirp() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
        } catch (_: Exception) {}
    }

    fun playAlertTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
        } catch (_: Exception) {}
    }

    fun updateVoiceModulation(newPitch: Float, newRate: Float, soundEffects: Boolean) {
        this.pitch = newPitch
        this.speechRate = newRate
        this.soundEffectsEnabled = soundEffects
        tts?.setPitch(pitch)
        tts?.setSpeechRate(speechRate)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        try {
            toneGenerator?.release()
        } catch (_: Exception) {}
    }
}
