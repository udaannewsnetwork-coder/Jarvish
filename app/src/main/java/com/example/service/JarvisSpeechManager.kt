package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class JarvisSpeechManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _rmsAmplitude = MutableStateFlow(0f)
    val rmsAmplitude: StateFlow<Float> = _rmsAmplitude.asStateFlow()

    private val _currentTranscript = MutableStateFlow("")
    val currentTranscript: StateFlow<String> = _currentTranscript.asStateFlow()

    private var onSpeechResultListener: ((String) -> Unit)? = null
    private var isContinuousWakeWordActive: Boolean = false

    init {
        initRecognizer()
    }

    private fun initRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                }

                override fun onBeginningOfSpeech() {
                    _isListening.value = true
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Normalize RMS dB (typically -2 to 10 dB) to 0f .. 1f
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                    _rmsAmplitude.value = normalized
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                    _rmsAmplitude.value = 0f
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    _rmsAmplitude.value = 0f
                    // If continuous listening mode is on and error was no match / timeout, restart
                    if (isContinuousWakeWordActive && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                        startListeningInternal()
                    }
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    _rmsAmplitude.value = 0f
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.firstOrNull()?.trim() ?: ""

                    if (spokenText.isNotEmpty()) {
                        _currentTranscript.value = spokenText
                        onSpeechResultListener?.invoke(spokenText)
                    }

                    if (isContinuousWakeWordActive) {
                        startListeningInternal()
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partials = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    partials?.firstOrNull()?.let {
                        _currentTranscript.value = it
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    fun startListening(onResult: (String) -> Unit) {
        this.onSpeechResultListener = onResult
        _currentTranscript.value = ""
        startListeningInternal()
    }

    private fun startListeningInternal() {
        if (speechRecognizer == null) {
            initRecognizer()
        }

        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (_: Exception) {
            _isListening.value = false
        }
    }

    fun stopListening() {
        isContinuousWakeWordActive = false
        speechRecognizer?.stopListening()
        _isListening.value = false
        _rmsAmplitude.value = 0f
    }

    fun toggleContinuousWakeWord(enabled: Boolean, onTrigger: (String) -> Unit) {
        this.isContinuousWakeWordActive = enabled
        this.onSpeechResultListener = onTrigger
        if (enabled) {
            startListeningInternal()
        } else {
            stopListening()
        }
    }

    fun destroy() {
        isContinuousWakeWordActive = false
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
