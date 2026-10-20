package com.example.model

enum class MessageSender {
    USER,
    JARVIS,
    SYSTEM,
    TOOL
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolName: String? = null,
    val toolDetails: String? = null,
    val isStreaming: Boolean = false
)

data class JarvisTelemetry(
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val ramUsedMb: Long = 0,
    val ramTotalMb: Long = 0,
    val storageFreeGb: Double = 0.0,
    val networkStatus: String = "ONLINE (QUANTUM ENCRYPTED)",
    val arcReactorOutput: Int = 100, // 0 - 100%
    val defenseStatus: String = "DEFCON 5 - NOMINAL",
    val alertLevel: AlertLevel = AlertLevel.NORMAL,
    val systemUptime: String = "00:00:00"
)

enum class AlertLevel {
    NORMAL,
    WARNING,
    CRITICAL_DEFENSE
}

data class ToolExecutionLog(
    val timestamp: String,
    val toolName: String,
    val commandOrTarget: String,
    val output: String,
    val isSuccess: Boolean
)

data class VirtualFile(
    val name: String,
    val path: String,
    val extension: String,
    val sizeBytes: Long,
    val content: String,
    val isExecutable: Boolean = false
)

enum class HudTab {
    VOICE_CORE,
    TACTICAL_TERMINAL,
    FILE_SYSTEM,
    MODULAR_CODEBASE,
    SETTINGS
}
