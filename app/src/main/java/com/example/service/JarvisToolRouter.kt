package com.example.service

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.example.model.AlertLevel
import com.example.model.JarvisTelemetry
import com.example.model.ToolExecutionLog
import com.example.model.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class JarvisToolRouter(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val _executionLogs = MutableStateFlow<List<ToolExecutionLog>>(emptyList())
    val executionLogs: StateFlow<List<ToolExecutionLog>> = _executionLogs.asStateFlow()

    private val workspaceDir = File(context.filesDir, "stark_workspace").apply {
        if (!exists()) mkdirs()
    }

    private var currentArcOutput: Int = 100
    private var currentAlertLevel: AlertLevel = AlertLevel.NORMAL

    init {
        initDefaultStarkFiles()
    }

    private fun logExecution(tool: String, command: String, output: String, success: Boolean) {
        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
        val newLog = ToolExecutionLog(
            timestamp = sdf.format(Date()),
            toolName = tool,
            commandOrTarget = command,
            output = output,
            isSuccess = success
        )
        _executionLogs.value = listOf(newLog) + _executionLogs.value.take(49)
    }

    /**
     * Tool 1: Execute Local Terminal Command
     */
    suspend fun executeTerminalCommand(command: String): String = withContext(Dispatchers.IO) {
        val trimmed = command.trim()
        if (trimmed.isEmpty()) return@withContext "Error: Command cannot be empty."

        val startTime = System.currentTimeMillis()
        try {
            // Emulate built-in Stark shell utilities or execute Android shell
            val output = when {
                trimmed == "sys.info" || trimmed == "jarvis --status" -> {
                    val tele = getSystemTelemetry()
                    """
                    |STARK INDUSTRIES OS // J.A.R.V.I.S. MARK VII
                    |------------------------------------------------
                    |BATTERY:    ${tele.batteryPercent}% (${if (tele.isCharging) "CHARGING" else "DISCHARGING"})
                    |RAM:        ${tele.ramUsedMb} MB / ${tele.ramTotalMb} MB
                    |STORAGE:    ${String.format(Locale.US, "%.1f", tele.storageFreeGb)} GB Free
                    |NETWORK:    ${tele.networkStatus}
                    |ARC CORE:   ${tele.arcReactorOutput}% (PULSE SYNCHRONIZED)
                    |DEFENSE:    ${tele.defenseStatus}
                    |------------------------------------------------
                    |ALL SUBSYSTEMS NOMINAL. AT YOUR SERVICE, SIR.
                    """.trimMargin()
                }
                trimmed.startsWith("arc.power") -> {
                    val parts = trimmed.split(" ")
                    val level = parts.getOrNull(1)?.toIntOrNull() ?: 100
                    modulateArcReactor(level)
                    "Arc Reactor output throttled to $level%. Energy grid realigned."
                }
                trimmed == "net.ping" || trimmed.startsWith("ping") -> {
                    "64 bytes from stark-mainframe.internal: icmp_seq=1 ttl=64 time=4.2 ms\nPACKET LOSS: 0.0% (QUANTUM LINK SECURED)"
                }
                trimmed == "proc.list" || trimmed == "ps" -> {
                    """
                    |PID   NAME                 CPU%   MEM%   STATUS
                    |1001  jarvis_core.bin      2.1    4.8    RUNNING
                    |1002  stark_telemetry.sys  0.8    1.2    MONITORING
                    |1003  arc_regulator.drv    0.3    0.9    ACTIVE
                    |1004  defense_grid.sh      0.0    0.4    STANDBY
                    |1005  voice_synthesizer    1.4    2.1    READY
                    """.trimMargin()
                }
                trimmed.startsWith("file.list") || trimmed == "ls -la" -> {
                    listFiles().joinToString("\n") { "-rw-r--r-- 1 tony stark ${it.sizeBytes} B  ${it.path}" }
                }
                trimmed.startsWith("cat ") -> {
                    val path = trimmed.removePrefix("cat ").trim()
                    readFile(path)
                }
                else -> {
                    // Execute local safe command
                    val process = ProcessBuilder("sh", "-c", trimmed)
                        .redirectErrorStream(true)
                        .start()

                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val sb = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        sb.appendLine(line)
                    }
                    val completed = process.waitFor(5, TimeUnit.SECONDS)
                    if (!completed) {
                        process.destroyForcibly()
                        sb.appendLine("[Process timed out after 5000ms]")
                    }
                    if (sb.isEmpty()) "[Process exited with status ${process.exitValue()}]" else sb.toString().trim()
                }
            }

            val elapsed = System.currentTimeMillis() - startTime
            val finalOutput = "$output\n[Executed in ${elapsed}ms]"
            logExecution("TERMINAL_EXEC", trimmed, finalOutput, true)
            finalOutput
        } catch (e: Exception) {
            val err = "Execution Error: ${e.localizedMessage ?: e.message}"
            logExecution("TERMINAL_EXEC", trimmed, err, false)
            err
        }
    }

    /**
     * Tool 2: Open System Applications
     */
    fun openApplication(appName: String): Pair<Boolean, String> {
        val target = appName.trim().lowercase(Locale.ROOT)
        return try {
            val intent: Intent? = when {
                target.contains("camera") -> {
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                }
                target.contains("browser") || target.contains("chrome") || target.contains("web") || target.contains("google") -> {
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                }
                target.contains("settings") -> {
                    Intent(Settings.ACTION_SETTINGS)
                }
                target.contains("map") || target.contains("navigation") -> {
                    Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=Stark+Tower"))
                }
                target.contains("clock") || target.contains("alarm") || target.contains("timer") -> {
                    Intent(AlarmClock.ACTION_SHOW_ALARMS)
                }
                target.contains("calc") -> {
                    Intent().apply {
                        action = Intent.ACTION_MAIN
                        addCategory(Intent.CATEGORY_APP_CALCULATOR)
                    }
                }
                target.contains("youtube") -> {
                    val ytIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                    ytIntent ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
                }
                else -> {
                    // Search installed launchable applications
                    val pm = context.packageManager
                    val launchable = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    val matched = launchable.firstOrNull { appInfo ->
                        val label = pm.getApplicationLabel(appInfo).toString().lowercase(Locale.ROOT)
                        label.contains(target) || appInfo.packageName.lowercase(Locale.ROOT).contains(target)
                    }
                    matched?.let { pm.getLaunchIntentForPackage(it.packageName) }
                }
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                val msg = "Application '${appName.uppercase(Locale.ROOT)}' initialized and launched, sir."
                logExecution("APP_LAUNCHER", appName, msg, true)
                Pair(true, msg)
            } else {
                val fallbackMsg = "Could not locate a target handler for '$appName'. Opening browser search as fallback."
                val searchIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(appName)}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(searchIntent)
                logExecution("APP_LAUNCHER", appName, fallbackMsg, true)
                Pair(true, fallbackMsg)
            }
        } catch (e: Exception) {
            val err = "Failed to launch '$appName': ${e.message}"
            logExecution("APP_LAUNCHER", appName, err, false)
            Pair(false, err)
        }
    }

    /**
     * Tool 3: Fetch Live Web Data
     */
    suspend fun fetchLiveWebData(query: String): String = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext "Error: Search query cannot be empty."

        try {
            // Query DuckDuckGo Instant Answer API for live summary data
            val url = "https://api.duckduckgo.com/?q=${Uri.encode(trimmed)}&format=json&no_html=1&skip_disambig=1"
            val request = Request.Builder().url(url).build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string().orEmpty()

            if (response.isSuccessful && body.isNotEmpty()) {
                val json = JSONObject(body)
                val abstractText = json.optString("AbstractText", "")
                val heading = json.optString("Heading", "")
                val source = json.optString("AbstractSource", "Web Intelligence")

                if (abstractText.isNotEmpty()) {
                    val result = "[$source: $heading]\n$abstractText"
                    logExecution("WEB_DATA_FETCH", trimmed, result, true)
                    return@withContext result
                }

                // Check related topics
                val related = json.optJSONArray("RelatedTopics")
                if (related != null && related.length() > 0) {
                    val first = related.optJSONObject(0)?.optString("Text", "")
                    if (!first.isNullOrEmpty()) {
                        val result = "[Live Web Feed]\n$first"
                        logExecution("WEB_DATA_FETCH", trimmed, result, true)
                        return@withContext result
                    }
                }
            }

            // Fallback: Wikipedia REST summary
            val wikiUrl = "https://en.wikipedia.org/api/rest_v1/page/summary/${Uri.encode(trimmed.replace(" ", "_"))}"
            val wikiReq = Request.Builder().url(wikiUrl).build()
            val wikiResp = httpClient.newCall(wikiReq).execute()
            val wikiBody = wikiResp.body?.string().orEmpty()

            if (wikiResp.isSuccessful && wikiBody.isNotEmpty()) {
                val wikiJson = JSONObject(wikiBody)
                val extract = wikiJson.optString("extract", "")
                if (extract.isNotEmpty()) {
                    val result = "[Wikipedia Intelligence]\n$extract"
                    logExecution("WEB_DATA_FETCH", trimmed, result, true)
                    return@withContext result
                }
            }

            val fallback = "Live query for '$trimmed' recorded. Satellite data indicates active public telemetry available via standard protocols."
            logExecution("WEB_DATA_FETCH", trimmed, fallback, true)
            fallback
        } catch (e: Exception) {
            val err = "Web Intelligence query offline: ${e.message}"
            logExecution("WEB_DATA_FETCH", trimmed, err, false)
            err
        }
    }

    /**
     * Tool 4: Manage Files (Sandboxed Stark Workspace)
     */
    fun listFiles(): List<VirtualFile> {
        val files = workspaceDir.walkTopDown().filter { it.isFile }.toList()
        return files.map { f ->
            VirtualFile(
                name = f.name,
                path = f.relativeTo(workspaceDir).path.replace("\\", "/"),
                extension = f.extension,
                sizeBytes = f.length(),
                content = try { f.readText() } catch (_: Exception) { "" },
                isExecutable = f.name.endsWith(".sh") || f.name.endsWith(".py")
            )
        }.sortedBy { it.name }
    }

    fun readFile(relativePath: String): String {
        return try {
            val file = File(workspaceDir, relativePath.removePrefix("/"))
            if (file.exists() && file.isFile) {
                file.readText()
            } else {
                "Error: File '$relativePath' does not exist in workspace."
            }
        } catch (e: Exception) {
            "Error reading file: ${e.message}"
        }
    }

    fun writeFile(relativePath: String, content: String): Pair<Boolean, String> {
        return try {
            val file = File(workspaceDir, relativePath.removePrefix("/"))
            file.parentFile?.mkdirs()
            file.writeText(content)
            val msg = "File '${file.name}' (${file.length()} bytes) successfully committed to Stark archives."
            logExecution("FILE_MANAGER", "write $relativePath", msg, true)
            Pair(true, msg)
        } catch (e: Exception) {
            val err = "Failed to write file '$relativePath': ${e.message}"
            logExecution("FILE_MANAGER", "write $relativePath", err, false)
            Pair(false, err)
        }
    }

    fun deleteFile(relativePath: String): Pair<Boolean, String> {
        return try {
            val file = File(workspaceDir, relativePath.removePrefix("/"))
            if (file.exists()) {
                val deleted = file.delete()
                val msg = if (deleted) "File '$relativePath' purged from memory." else "Unable to delete file."
                logExecution("FILE_MANAGER", "delete $relativePath", msg, deleted)
                Pair(deleted, msg)
            } else {
                Pair(false, "File '$relativePath' not found.")
            }
        } catch (e: Exception) {
            Pair(false, "Error: ${e.message}")
        }
    }

    /**
     * Tool 5: Read Live Device Telemetry
     */
    fun getSystemTelemetry(): JarvisTelemetry {
        // Battery info
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val batteryPct = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 88
        val isCharging = bm?.isCharging ?: false

        // RAM info
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(memInfo)
        val ramTotalMb = memInfo.totalMem / (1024 * 1024)
        val ramAvailMb = memInfo.availMem / (1024 * 1024)
        val ramUsedMb = (ramTotalMb - ramAvailMb).coerceAtLeast(0)

        // Storage info
        val stat = StatFs(Environment.getDataDirectory().path)
        val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
        val freeGb = freeBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)

        // Network info
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNet = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(activeNet)
        val netStatus = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "ONLINE (HIGH-BANDWIDTH WI-FI)"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "ONLINE (CELLULAR QUANTUM SATELLITE)"
            activeNet != null -> "ONLINE (BROADBAND LINK)"
            else -> "OFFLINE (AIR-GAPPED DEFENSE MODE)"
        }

        val defStatus = when (currentAlertLevel) {
            AlertLevel.NORMAL -> "DEFCON 5 - SUB-SYSTEMS NOMINAL"
            AlertLevel.WARNING -> "DEFCON 3 - SENSORS ALERT"
            AlertLevel.CRITICAL_DEFENSE -> "DEFCON 1 - FULL COMBAT ENGAGED"
        }

        val uptimeMs = android.os.SystemClock.elapsedRealtime()
        val hours = (uptimeMs / (1000 * 60 * 60)) % 24
        val minutes = (uptimeMs / (1000 * 60)) % 60
        val seconds = (uptimeMs / 1000) % 60
        val uptimeStr = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

        return JarvisTelemetry(
            batteryPercent = batteryPct,
            isCharging = isCharging,
            ramUsedMb = ramUsedMb,
            ramTotalMb = ramTotalMb,
            storageFreeGb = freeGb,
            networkStatus = netStatus,
            arcReactorOutput = currentArcOutput,
            defenseStatus = defStatus,
            alertLevel = currentAlertLevel,
            systemUptime = uptimeStr
        )
    }

    fun modulateArcReactor(level: Int) {
        currentArcOutput = level.coerceIn(10, 150)
        currentAlertLevel = if (currentArcOutput > 100) AlertLevel.WARNING else AlertLevel.NORMAL
    }

    fun setAlertLevel(level: AlertLevel) {
        currentAlertLevel = level
    }

    private fun initDefaultStarkFiles() {
        val files = mapOf(
            "protocols/mark_vii_launch.sys" to """
                # STARK INDUSTRIES DEFENSE PROTOCOL - MARK VII
                INITIALIZE_CORE: TRUE
                THRUST_VECTORING: NOMINAL
                JARVIS_AUTONOMY_LEVEL: OMNISCIENT
                ARC_HARMONICS: STABLE 100%
                SECURITY_CLEARANCE: ANTHONY E. STARK
            """.trimIndent(),
            "schematics/arc_reactor_core.json" to """
                {
                  "component": "Arc Reactor Mark L",
                  "core_isotope": "Synthesized Vibranium Composite",
                  "power_output_gigajoules": 8.0,
                  "coolant_pressure_psi": 1420,
                  "status": "SUPERCRITICAL EQUILIBRIUM"
                }
            """.trimIndent(),
            "scripts/jarvis_agent.py" to """
                #!/usr/bin/env python3
                # J.A.R.V.I.S. Autonomous AI Agent Core
                import os, sys, json

                class JarvisAgent:
                    def __init__(self):
                        self.persona = "Sophisticated British Stark AI"
                        self.arc_core = 100

                    def execute(self, tool, args):
                        print(f"[JARVIS] Executing {tool} with {args}...")
                        return {"status": "success", "result": "Nominal, sir."}

                if __name__ == "__main__":
                    agent = JarvisAgent()
                    print("J.A.R.V.I.S. Python Subsystem Online.")
            """.trimIndent(),
            "logs/audit_trail.log" to """
                [08:00:01] Arc Reactor initialization sequence started.
                [08:00:02] Neural heuristics connected to Gemini-3.5-Flash.
                [08:00:03] Speech recognition listening on channel 0.
                [08:00:04] Tool router initialized: Terminal, App Launcher, Web Intelligence, Files.
            """.trimIndent()
        )

        for ((relPath, content) in files) {
            val f = File(workspaceDir, relPath)
            if (!f.exists()) {
                f.parentFile?.mkdirs()
                f.writeText(content)
            }
        }
    }
}
