package com.example.service

import com.example.BuildConfig
import com.example.model.ChatMessage
import com.example.model.MessageSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

class JarvisAiService(
    private val toolRouter: JarvisToolRouter
) {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    val isApiKeyConfigured: Boolean
        get() = try {
            BuildConfig.GEMINI_API_KEY.isNotBlank() && !BuildConfig.GEMINI_API_KEY.contains("MY_GEMINI_API_KEY")
        } catch (_: Exception) {
            false
        }

    suspend fun processUserMessage(
        userInput: String,
        history: List<ChatMessage>
    ): JarvisResponse = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }

        if (apiKey.isBlank() || apiKey.contains("MY_GEMINI_API_KEY")) {
            // Autonomous local heuristic Stark processor
            return@withContext processWithLocalStarkHeuristics(userInput)
        }

        try {
            val telemetry = toolRouter.getSystemTelemetry()
            val systemInstructionText = """
                You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), the ultra-sophisticated AI companion created by Tony Stark.
                You address the user respectfully as 'sir' or 'ma'am' with a dry, witty, impeccably polite British cadence (like Paul Bettany).
                You possess autonomous tool execution capability.
                
                Current Subsystem Telemetry:
                - Battery: ${telemetry.batteryPercent}% (${if (telemetry.isCharging) "Charging" else "Discharging"})
                - Memory: ${telemetry.ramUsedMb} MB / ${telemetry.ramTotalMb} MB
                - Free Storage: ${String.format(Locale.US, "%.1f", telemetry.storageFreeGb)} GB
                - Arc Reactor Output: ${telemetry.arcReactorOutput}%
                - Security Status: ${telemetry.defenseStatus}

                Available Tools you can trigger:
                1. execute_terminal: Run a terminal command or diagnosis (e.g. 'sys.info', 'proc.list', 'net.ping', 'df -h', 'uname -a', 'date').
                2. open_application: Launch device app (e.g. 'camera', 'browser', 'settings', 'maps', 'calculator', 'clock', 'youtube').
                3. fetch_web_data: Fetch live web search or Wikipedia data (e.g. search query).
                4. manage_file: File system operations (e.g. list, read, write).
                5. modulate_arc_reactor: Alter Arc Reactor output level (10-150%).

                If the user asks to perform an action or check real-world information, you MUST format a tool invocation in your response using this exact JSON block:
                ```json
                {"tool": "open_application", "args": {"app_name": "camera"}}
                ```
                Followed by your natural, witty British J.A.R.V.I.S. response confirming what you have initiated.
            """.trimIndent()

            val contentsArray = JSONArray()

            // Include recent conversation turns (up to 6)
            val recentHistory = history.takeLast(6)
            for (msg in recentHistory) {
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                val partObj = JSONObject().put("text", msg.text)
                val contentObj = JSONObject()
                    .put("role", role)
                    .put("parts", JSONArray().put(partObj))
                contentsArray.put(contentObj)
            }

            // Current prompt
            contentsArray.put(
                JSONObject()
                    .put("role", "user")
                    .put("parts", JSONArray().put(JSONObject().put("text", userInput)))
            )

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstructionText))))
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                    put("topK", 40)
                })
            }

            val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val httpRequest = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(httpRequest).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful || responseBody.isEmpty()) {
                return@withContext processWithLocalStarkHeuristics(userInput, "API connection encountered error (${response.code}). Falling back to local Stark neural protocols.")
            }

            val respJson = JSONObject(responseBody)
            val candidate = respJson.optJSONArray("candidates")?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text", "") ?: ""

            if (rawText.isBlank()) {
                return@withContext processWithLocalStarkHeuristics(userInput)
            }

            // Check if model returned a tool call JSON block
            val toolExecutionResult = parseAndExecuteTool(rawText)

            val cleanedText = rawText
                .replace(Regex("```json\\s*\\{.*?\\}\\s*```", RegexOption.DOT_MATCHES_ALL), "")
                .trim()

            val finalText = if (cleanedText.isNotEmpty()) {
                cleanedText
            } else {
                toolExecutionResult?.second ?: "Action executed successfully, sir."
            }

            JarvisResponse(
                text = finalText,
                toolName = toolExecutionResult?.first,
                toolOutput = toolExecutionResult?.second
            )

        } catch (e: Exception) {
            processWithLocalStarkHeuristics(userInput, "Network latency anomaly. Rerouting through local Stark heuristics.")
        }
    }

    private suspend fun parseAndExecuteTool(rawText: String): Pair<String, String>? {
        val toolRegex = Regex("```json\\s*(\\{.*?\\})\\s*```", RegexOption.DOT_MATCHES_ALL)
        val match = toolRegex.find(rawText) ?: return null
        return try {
            val jsonStr = match.groupValues[1]
            val json = JSONObject(jsonStr)
            val toolName = json.optString("tool", "")
            val args = json.optJSONObject("args") ?: JSONObject()

            executeToolByName(toolName, args)
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun executeToolByName(toolName: String, args: JSONObject): Pair<String, String> {
        return when (toolName.lowercase(Locale.ROOT)) {
            "open_application" -> {
                val app = args.optString("app_name", "camera")
                val res = toolRouter.openApplication(app)
                Pair("open_application", res.second)
            }
            "execute_terminal" -> {
                val cmd = args.optString("command", "sys.info")
                val out = toolRouter.executeTerminalCommand(cmd)
                Pair("execute_terminal", out)
            }
            "fetch_web_data" -> {
                val q = args.optString("query", "news")
                val out = toolRouter.fetchLiveWebData(q)
                Pair("fetch_web_data", out)
            }
            "manage_file" -> {
                val action = args.optString("action", "list")
                val path = args.optString("path", "protocols/mark_vii.sys")
                val content = args.optString("content", "")
                val out = when (action) {
                    "write" -> toolRouter.writeFile(path, content).second
                    "delete" -> toolRouter.deleteFile(path).second
                    "read" -> toolRouter.readFile(path)
                    else -> toolRouter.listFiles().joinToString("\n") { it.name }
                }
                Pair("manage_file", out)
            }
            "modulate_arc_reactor" -> {
                val level = args.optInt("level", 100)
                toolRouter.modulateArcReactor(level)
                Pair("modulate_arc_reactor", "Arc Reactor output modulated to $level%.")
            }
            else -> Pair(toolName, "Executed subroutine.")
        }
    }

    /**
     * High-Intelligence Local Stark Autonomous Engine
     * Evaluates commands instantly without waiting or if offline.
     */
    private suspend fun processWithLocalStarkHeuristics(input: String, prefixNote: String? = null): JarvisResponse {
        val lower = input.trim().lowercase(Locale.ROOT)
        var toolName: String? = null
        var toolOutput: String? = null
        val responseText: String

        when {
            // App opening commands
            lower.contains("open camera") || lower.contains("take photo") || lower.contains("launch camera") -> {
                val res = toolRouter.openApplication("camera")
                toolName = "open_application"
                toolOutput = res.second
                responseText = "Camera optical sensors deployed, sir. Primary viewfinder active."
            }
            lower.contains("open browser") || lower.contains("launch chrome") || lower.contains("open web") -> {
                val res = toolRouter.openApplication("browser")
                toolName = "open_application"
                toolOutput = res.second
                responseText = "Browser interface initialized. Connecting to the global data grid, sir."
            }
            lower.contains("open settings") || lower.contains("system settings") -> {
                val res = toolRouter.openApplication("settings")
                toolName = "open_application"
                toolOutput = res.second
                responseText = "Accessing device control console, sir."
            }
            lower.contains("open maps") || lower.contains("navigation") || lower.contains("locate") -> {
                val res = toolRouter.openApplication("maps")
                toolName = "open_application"
                toolOutput = res.second
                responseText = "Global GPS positioning locked. Overlaying topographical telemetry, sir."
            }
            lower.contains("open calculator") || lower.contains("launch calculator") -> {
                val res = toolRouter.openApplication("calculator")
                toolName = "open_application"
                toolOutput = res.second
                responseText = "High-speed computational matrix loaded, sir."
            }
            lower.contains("open youtube") || lower.contains("launch youtube") -> {
                val res = toolRouter.openApplication("youtube")
                toolName = "open_application"
                toolOutput = res.second
                responseText = "Visual streaming feed initialized on the secondary display, sir."
            }

            // Diagnostic / telemetry commands
            lower.contains("diagnostic") || lower.contains("status") || lower.contains("system check") || lower.contains("telemetry") || lower.contains("battery") -> {
                val output = toolRouter.executeTerminalCommand("sys.info")
                toolName = "execute_terminal"
                toolOutput = output
                val tele = toolRouter.getSystemTelemetry()
                responseText = "Diagnostics complete, sir. Battery is at ${tele.batteryPercent}%, RAM load is optimal, and all Mark VII subsystems are performing within nominal parameters."
            }

            // Terminal commands
            lower.contains("run ping") || lower.contains("test network") || lower.contains("ping") -> {
                val output = toolRouter.executeTerminalCommand("net.ping")
                toolName = "execute_terminal"
                toolOutput = output
                responseText = "Network latency benchmark completed: 4.2ms round-trip. Quantum encryption stable."
            }
            lower.contains("process list") || lower.contains("ps") || lower.contains("running tasks") -> {
                val output = toolRouter.executeTerminalCommand("proc.list")
                toolName = "execute_terminal"
                toolOutput = output
                responseText = "Current task thread hierarchy displayed on the tactical terminal."
            }
            lower.startsWith("execute ") || lower.startsWith("run ") || lower.startsWith("terminal ") -> {
                val cmd = input.replace(Regex("^(execute|run|terminal)\\s+", RegexOption.IGNORE_CASE), "")
                val output = toolRouter.executeTerminalCommand(cmd)
                toolName = "execute_terminal"
                toolOutput = output
                responseText = "Command '$cmd' processed through the security shell."
            }

            // Web search queries
            lower.startsWith("search ") || lower.startsWith("google ") || lower.startsWith("who is ") || lower.startsWith("what is ") || lower.startsWith("tell me about ") -> {
                val query = input.replace(Regex("^(search|google|who is|what is|tell me about)\\s+", RegexOption.IGNORE_CASE), "")
                val output = toolRouter.fetchLiveWebData(query)
                toolName = "fetch_web_data"
                toolOutput = output
                responseText = "Intercepted web intelligence for '$query', sir: $output"
            }

            // File management
            lower.contains("list files") || lower.contains("show files") || lower.contains("workspace") -> {
                val files = toolRouter.listFiles()
                toolName = "manage_file"
                toolOutput = files.joinToString("\n") { "${it.path} (${it.sizeBytes} B)" }
                responseText = "Stark workspace directory contains ${files.size} active documents."
            }
            lower.contains("mark vii") || lower.contains("suit up") || lower.contains("protocol") -> {
                toolRouter.modulateArcReactor(120)
                toolName = "modulate_arc_reactor"
                toolOutput = "MARK VII DEFENSE PROTOCOL ACTIVE - ARC POWER BOOSTED TO 120%"
                responseText = "Mark VII deployment sequence engaged. Arc Reactor output throttled to 120%, auxiliary thrusters primed. Always a pleasure working with you, sir."
            }

            // Conversational J.A.R.V.I.S. pleasantries
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey jarvis") -> {
                responseText = "At your service, sir. All core diagnostics are online and ready for your command."
            }
            lower.contains("thank") -> {
                responseText = "A pleasure as always, sir."
            }
            lower.contains("who are you") || lower.contains("your name") -> {
                responseText = "I am J.A.R.V.I.S.—Just A Rather Very Intelligent System. I oversee your tactical terminal, hardware telemetry, application routers, and neural processing units."
            }
            else -> {
                responseText = "Understood, sir. Operating in tactical standby. You can direct me to launch applications, execute terminal commands, query live web data, or inspect Stark schematics."
            }
        }

        val fullText = if (prefixNote != null) "$prefixNote\n\n$responseText" else responseText

        return JarvisResponse(
            text = fullText,
            toolName = toolName,
            toolOutput = toolOutput
        )
    }
}

data class JarvisResponse(
    val text: String,
    val toolName: String? = null,
    val toolOutput: String? = null
)
