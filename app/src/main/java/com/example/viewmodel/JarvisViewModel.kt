package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ChatMessage
import com.example.model.HudTab
import com.example.model.JarvisTelemetry
import com.example.model.MessageSender
import com.example.model.ToolExecutionLog
import com.example.model.VirtualFile
import com.example.service.JarvisAiService
import com.example.service.JarvisSpeechManager
import com.example.service.JarvisToolRouter
import com.example.service.JarvisVoiceSynthesizer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    val toolRouter = JarvisToolRouter(application)
    val voiceSynthesizer = JarvisVoiceSynthesizer(application)
    val speechManager = JarvisSpeechManager(application)
    val aiService = JarvisAiService(toolRouter)

    // Current Active Tab
    private val _currentTab = MutableStateFlow(HudTab.VOICE_CORE)
    val currentTab: StateFlow<HudTab> = _currentTab.asStateFlow()

    // Chat History
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // System Telemetry
    private val _telemetry = MutableStateFlow(toolRouter.getSystemTelemetry())
    val telemetry: StateFlow<JarvisTelemetry> = _telemetry.asStateFlow()

    // Processing State
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Continuous Wake Word Mode
    private val _wakeWordActive = MutableStateFlow(false)
    val wakeWordActive: StateFlow<Boolean> = _wakeWordActive.asStateFlow()

    // Text Input in HUD
    private val _textInput = MutableStateFlow("")
    val textInput: StateFlow<String> = _textInput.asStateFlow()

    // Terminal Input
    private val _terminalInput = MutableStateFlow("")
    val terminalInput: StateFlow<String> = _terminalInput.asStateFlow()

    // Selected Virtual File for Editor
    private val _selectedFile = MutableStateFlow<VirtualFile?>(null)
    val selectedFile: StateFlow<VirtualFile?> = _selectedFile.asStateFlow()

    private val _fileEditorContent = MutableStateFlow("")
    val fileEditorContent: StateFlow<String> = _fileEditorContent.asStateFlow()

    // Virtual Files List
    private val _workspaceFiles = MutableStateFlow<List<VirtualFile>>(emptyList())
    val workspaceFiles: StateFlow<List<VirtualFile>> = _workspaceFiles.asStateFlow()

    // TTS & Modulation settings
    private val _speechPitch = MutableStateFlow(0.95f)
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    private val _speechRate = MutableStateFlow(1.02f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _ttsEnabled = MutableStateFlow(true)
    val ttsEnabled: StateFlow<Boolean> = _ttsEnabled.asStateFlow()

    private val _soundFxEnabled = MutableStateFlow(true)
    val soundFxEnabled: StateFlow<Boolean> = _soundFxEnabled.asStateFlow()

    private var telemetryJob: Job? = null

    init {
        // Initial welcome message from J.A.R.V.I.S.
        val welcome = ChatMessage(
            sender = MessageSender.JARVIS,
            text = "Good day, sir. All J.A.R.V.I.S. Mark VII protocols are initialized and operating within nominal parameters. Speech recognition, tactical terminal, application launcher, and neural routers are standing by."
        )
        _chatMessages.value = listOf(welcome)
        refreshFiles()
        startTelemetryLoop()

        // Welcome voice line after slight delay
        viewModelScope.launch {
            delay(1200)
            if (_ttsEnabled.value) {
                voiceSynthesizer.speak("Good day, sir. J.A.R.V.I.S. systems online and ready for your command.", playChime = true)
            }
        }
    }

    private fun startTelemetryLoop() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            while (isActive) {
                _telemetry.value = toolRouter.getSystemTelemetry()
                delay(3000)
            }
        }
    }

    fun setTab(tab: HudTab) {
        _currentTab.value = tab
    }

    fun onTextInputChange(text: String) {
        _textInput.value = text
    }

    fun onTerminalInputChange(text: String) {
        _terminalInput.value = text
    }

    fun submitUserMessage(userText: String? = null) {
        val query = userText ?: _textInput.value.trim()
        if (query.isEmpty() || _isProcessing.value) return

        _textInput.value = ""

        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = query
        )
        _chatMessages.value = _chatMessages.value + userMsg

        _isProcessing.value = true
        voiceSynthesizer.playTechChirp()

        viewModelScope.launch {
            val response = aiService.processUserMessage(query, _chatMessages.value)
            _isProcessing.value = false

            val jarvisMsg = ChatMessage(
                sender = MessageSender.JARVIS,
                text = response.text,
                toolName = response.toolName,
                toolDetails = response.toolOutput
            )
            _chatMessages.value = _chatMessages.value + jarvisMsg
            _telemetry.value = toolRouter.getSystemTelemetry()

            if (_ttsEnabled.value) {
                voiceSynthesizer.speak(response.text)
            }
        }
    }

    fun toggleVoiceListening() {
        if (speechManager.isListening.value) {
            speechManager.stopListening()
        } else {
            speechManager.startListening { spoken ->
                submitUserMessage(spoken)
            }
        }
    }

    fun toggleContinuousWakeWord() {
        val newState = !_wakeWordActive.value
        _wakeWordActive.value = newState
        speechManager.toggleContinuousWakeWord(newState) { spoken ->
            submitUserMessage(spoken)
        }
        if (newState) {
            voiceSynthesizer.playTechChirp()
        }
    }

    fun executeTerminalCommand(cmd: String? = null) {
        val command = cmd ?: _terminalInput.value.trim()
        if (command.isEmpty()) return

        _terminalInput.value = ""
        viewModelScope.launch {
            val result = toolRouter.executeTerminalCommand(command)
            _telemetry.value = toolRouter.getSystemTelemetry()
            val logMessage = ChatMessage(
                sender = MessageSender.TOOL,
                text = "Terminal Execution: '$command'",
                toolName = "execute_terminal",
                toolDetails = result
            )
            _chatMessages.value = _chatMessages.value + logMessage
            refreshFiles()
        }
    }

    fun refreshFiles() {
        _workspaceFiles.value = toolRouter.listFiles()
    }

    fun selectFile(file: VirtualFile) {
        _selectedFile.value = file
        _fileEditorContent.value = file.content
    }

    fun closeFileEditor() {
        _selectedFile.value = null
    }

    fun onFileEditorContentChange(newContent: String) {
        _fileEditorContent.value = newContent
    }

    fun saveCurrentFile() {
        val file = _selectedFile.value ?: return
        viewModelScope.launch {
            toolRouter.writeFile(file.path, _fileEditorContent.value)
            refreshFiles()
            _selectedFile.value = _selectedFile.value?.copy(content = _fileEditorContent.value)
            voiceSynthesizer.playTechChirp()
        }
    }

    fun createNewFile(name: String, content: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            toolRouter.writeFile(name, content)
            refreshFiles()
        }
    }

    fun deleteSelectedFile() {
        val file = _selectedFile.value ?: return
        viewModelScope.launch {
            toolRouter.deleteFile(file.path)
            _selectedFile.value = null
            refreshFiles()
        }
    }

    fun updateVoiceSettings(pitch: Float, rate: Float, enabled: Boolean, fx: Boolean) {
        _speechPitch.value = pitch
        _speechRate.value = rate
        _ttsEnabled.value = enabled
        _soundFxEnabled.value = fx
        voiceSynthesizer.updateVoiceModulation(pitch, rate, fx)
    }

    fun testVoicePhrase(sample: String = "Diagnostics indicate nominal subsystem operation, sir.") {
        voiceSynthesizer.speak(sample)
    }

    override fun onCleared() {
        super.onCleared()
        telemetryJob?.cancel()
        speechManager.destroy()
        voiceSynthesizer.shutdown()
    }
}
