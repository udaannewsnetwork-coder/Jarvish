package com.example.ui.hud

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGreenNominal
import com.example.ui.theme.JarvisHudBackground
import com.example.ui.theme.JarvisHudBorder
import com.example.ui.theme.JarvisHudSurface
import com.example.ui.theme.JarvisHudSurfaceElevated
import com.example.ui.theme.JarvisTextDim
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun HudCodebaseTab(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFileIndex by remember { mutableStateOf(0) }

    val codeFiles = remember {
        listOf(
            CodeSnippet(
                filename = "jarvis_agent.py",
                language = "Python",
                description = "Core Agent Architecture & Gemini LLM Tool Router",
                content = """
                    import os, sys, json, requests
                    from voice_engine import VoiceEngine
                    from tool_executor import ToolExecutor

                    class JarvisAgent:
                        def __init__(self, api_key: str = None):
                            self.api_key = api_key or os.getenv("GEMINI_API_KEY", "")
                            self.voice = VoiceEngine()
                            self.executor = ToolExecutor()
                            self.persona = (
                                "You are J.A.R.V.I.S., Tony Stark's ultra-sophisticated AI companion. "
                                "Address the user as 'sir' with a witty, dry British cadence. "
                                "You have direct terminal, app, web, and file system capabilities."
                            )

                        def run_loop(self):
                            self.voice.speak("All J.A.R.V.I.S. neural systems online, sir.")
                            print("=== J.A.R.V.I.S. AGENT SYSTEM ACTIVE ===")
                            while True:
                                # 1. Wake-word & Speech-to-Text
                                user_prompt = self.voice.listen_for_wake_word("jarvis")
                                if not user_prompt:
                                    continue

                                print(f"[USER]: {user_prompt}")
                                # 2. Process with LLM & Tool Router
                                response, tool_call = self.process_llm(user_prompt)

                                # 3. Autonomous Tool Execution
                                if tool_call:
                                    result = self.executor.execute(tool_call['tool'], tool_call.get('args', {}))
                                    print(f"[TOOL EXECUTED]: {result}")

                                # 4. Low-Latency TTS Output
                                print(f"[JARVIS]: {response}")
                                self.voice.speak(response)

                        def process_llm(self, query: str):
                            # Connect to Gemini-3.5-Flash with function calling
                            url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key={self.api_key}"
                            payload = {
                                "contents": [{"role": "user", "parts": [{"text": query}]}],
                                "systemInstruction": {"parts": [{"text": self.persona}]}
                            }
                            resp = requests.post(url, json=payload, timeout=60)
                            data = resp.json()
                            text = data['candidates'][0]['content']['parts'][0]['text']
                            return text, None

                    if __name__ == '__main__':
                        agent = JarvisAgent()
                        agent.run_loop()
                """.trimIndent()
            ),
            CodeSnippet(
                filename = "voice_engine.py",
                language = "Python",
                description = "Voice Wake-word, STT & Low-Latency TTS Synthesis",
                content = """
                    import speech_recognition as sr
                    import pyttsx3
                    import time

                    class VoiceEngine:
                        def __init__(self):
                            self.recognizer = sr.Recognizer()
                            self.mic = sr.Microphone()
                            self.tts = pyttsx3.init()
                            self.setup_tts()

                        def setup_tts(self):
                            # Calibrate for British cadence & low latency
                            voices = self.tts.getProperty('voices')
                            for v in voices:
                                if "en_GB" in v.id or "uk" in v.id.lower() or "british" in v.name.lower():
                                    self.tts.setProperty('voice', v.id)
                                    break
                            self.tts.setProperty('rate', 175)
                            self.tts.setProperty('pitch', 95)

                        def listen_for_wake_word(self, wake_word: str = "jarvis") -> str:
                            with self.mic as source:
                                self.recognizer.adjust_for_ambient_noise(source, duration=0.5)
                                print("[VOICE] Listening for speech frequency...")
                                try:
                                    audio = self.recognizer.listen(source, timeout=5, phrase_time_limit=10)
                                    text = self.recognizer.recognize_google(audio).lower()
                                    if wake_word in text:
                                        command = text.replace(wake_word, "").strip()
                                        return command or "status"
                                    return text
                                except (sr.WaitTimeoutError, sr.UnknownValueError):
                                    return ""

                        def speak(self, phrase: str):
                            print(f"[TTS Audio Output]: {phrase}")
                            self.tts.say(phrase)
                            self.tts.runAndWait()
                """.trimIndent()
            ),
            CodeSnippet(
                filename = "tool_executor.py",
                language = "Python",
                description = "Subprocess Terminal, App Launcher, Web & File Tools",
                content = """
                    import subprocess, webbrowser, requests, os, json

                    class ToolExecutor:
                        def execute(self, tool_name: str, args: dict) -> dict:
                            tools = {
                                "execute_terminal": self.run_terminal,
                                "open_application": self.open_app,
                                "fetch_web_data": self.fetch_web,
                                "manage_file": self.manage_file
                            }
                            func = tools.get(tool_name)
                            if func:
                                return func(**args)
                            return {"error": f"Unknown tool: {tool_name}"}

                        def run_terminal(self, command: str) -> dict:
                            try:
                                res = subprocess.run(command, shell=True, capture_output=True, text=True, timeout=10)
                                return {"stdout": res.stdout, "stderr": res.stderr, "exit_code": res.returncode}
                            except Exception as e:
                                return {"error": str(e)}

                        def open_app(self, app_name: str) -> dict:
                            apps = {
                                "camera": "start microsoft.windows.camera:",
                                "browser": "start https://google.com",
                                "calculator": "calc",
                                "maps": "start https://maps.google.com"
                            }
                            cmd = apps.get(app_name.lower(), f"start {app_name}")
                            subprocess.Popen(cmd, shell=True)
                            return {"status": f"Application '{app_name}' launched."}

                        def fetch_web(self, query: str) -> dict:
                            url = f"https://api.duckduckgo.com/?q={requests.utils.quote(query)}&format=json"
                            resp = requests.get(url, timeout=5).json()
                            summary = resp.get("AbstractText") or "Intelligence gathered."
                            return {"query": query, "summary": summary}

                        def manage_file(self, action: str, path: str, content: str = "") -> dict:
                            if action == "write":
                                os.makedirs(os.path.dirname(path) or ".", exist_ok=True)
                                with open(path, "w") as f:
                                    f.write(content)
                                return {"status": f"Committed {len(content)} bytes to {path}"}
                            elif action == "read":
                                with open(path, "r") as f:
                                    return {"content": f.read()}
                            return {"error": "Invalid file action"}
                """.trimIndent()
            ),
            CodeSnippet(
                filename = "index.html",
                language = "HTML+CSS",
                description = "Holographic Arc Reactor Web HUD Interface",
                content = """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                      <meta charset="UTF-8">
                      <title>J.A.R.V.I.S. // Stark Industries HUD</title>
                      <link rel="stylesheet" href="style.css">
                    </head>
                    <body>
                      <div class="hud-container">
                        <header class="hud-header">
                          <span class="badge">STARK IND // MARK VII</span>
                          <span class="telemetry" id="telemetry">ARC CORE: 100% | DEFCON 5 NOMINAL</span>
                        </header>

                        <!-- Holographic Arc Reactor Core -->
                        <div class="arc-reactor-wrapper" id="arcReactor">
                          <div class="ring outer-ring"></div>
                          <div class="ring mid-ring"></div>
                          <div class="ring inner-ring"></div>
                          <div class="core-plasma"></div>
                          <canvas id="waveformCanvas" width="300" height="300"></canvas>
                        </div>

                        <div class="status-feed" id="statusFeed">J.A.R.V.I.S. STANDBY // SAY 'HEY JARVIS'</div>

                        <!-- Tactical Terminal Panel -->
                        <div class="terminal-panel">
                          <div class="terminal-logs" id="terminalLogs">
                            [08:00:00] Stark OS Initialized.<br>
                            [08:00:01] Speech Engine Online (en-GB).<br>
                            [08:00:02] Tool Router Standing By.
                          </div>
                          <div class="terminal-input-bar">
                            <span class="prompt">stark@jarvis:~$</span>
                            <input type="text" id="cmdInput" placeholder="Command J.A.R.V.I.S..." />
                            <button id="micBtn" class="hud-btn">🎤 MIC</button>
                          </div>
                        </div>
                      </div>
                      <script src="hud_app.js"></script>
                    </body>
                    </html>
                """.trimIndent()
            ),
            CodeSnippet(
                filename = "hud_app.js",
                language = "JavaScript",
                description = "Web Speech API, Audio Visualizer & Real-Time Tool Bridge",
                content = """
                    // J.A.R.V.I.S. Web Speech & Holographic Audio Reactive Engine
                    const recognition = new (window.SpeechRecognition || window.webkitSpeechRecognition)();
                    const synth = window.speechSynthesis;
                    recognition.continuous = true;
                    recognition.interimResults = true;

                    const statusFeed = document.getElementById('statusFeed');
                    const terminalLogs = document.getElementById('terminalLogs');

                    function jarvisSpeak(text) {
                      const utterance = new SpeechSynthesisUtterance(text);
                      // Calibrate British English
                      const voices = synth.getVoices();
                      const britishVoice = voices.find(v => v.lang.includes('GB') || v.name.includes('UK'));
                      if (britishVoice) utterance.voice = britishVoice;
                      utterance.pitch = 0.95;
                      utterance.rate = 1.05;
                      synth.speak(utterance);
                    }

                    recognition.onresult = (event) => {
                      const transcript = event.results[event.results.length - 1][0].transcript.trim();
                      statusFeed.innerText = '» "' + transcript + '"';
                      if (event.results[event.results.length - 1].isFinal) {
                        handleCommand(transcript);
                      }
                    };

                    async function handleCommand(cmd) {
                      logTerminal('[USER]: ' + cmd);
                      // Call J.A.R.V.I.S. backend tool router
                      const resp = await fetch('/api/jarvis', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify({query: cmd})
                      });
                      const data = await resp.json();
                      logTerminal('[JARVIS]: ' + data.response);
                      jarvisSpeak(data.response);
                    }

                    function logTerminal(msg) {
                      terminalLogs.innerHTML += '<div>' + msg + '</div>';
                      terminalLogs.scrollTop = terminalLogs.scrollHeight;
                    }

                    document.getElementById('micBtn').onclick = () => recognition.start();
                """.trimIndent()
            ),
            CodeSnippet(
                filename = "style.css",
                language = "CSS",
                description = "Iron Man Arc Reactor CSS Animation & Cyberpunk Styles",
                content = """
                    body {
                      margin: 0;
                      background: #020610;
                      color: #00E5FF;
                      font-family: 'Courier New', monospace;
                      overflow: hidden;
                    }
                    .hud-container {
                      display: flex;
                      flex-direction: column;
                      height: 100vh;
                      padding: 20px;
                      box-sizing: border-box;
                    }
                    .arc-reactor-wrapper {
                      position: relative;
                      width: 260px;
                      height: 260px;
                      margin: 20px auto;
                      border-radius: 50%;
                      box-shadow: 0 0 50px rgba(0, 229, 255, 0.4);
                    }
                    .ring {
                      position: absolute;
                      border-radius: 50%;
                      border: 2px solid #00E5FF;
                    }
                    .outer-ring {
                      inset: 0;
                      border-style: dashed;
                      animation: rotateCw 12s linear infinite;
                    }
                    .mid-ring {
                      inset: 25px;
                      border-width: 3px;
                      border-color: #0284C7;
                      animation: rotateCcw 8s linear infinite;
                    }
                    .inner-ring {
                      inset: 60px;
                      border-color: #FFB300;
                    }
                    .core-plasma {
                      position: absolute;
                      inset: 90px;
                      background: radial-gradient(circle, #FFF 0%, #00E5FF 70%, transparent 100%);
                      border-radius: 50%;
                      animation: pulse 1.8s ease-in-out infinite alternate;
                    }
                    @keyframes rotateCw { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
                    @keyframes rotateCcw { from { transform: rotate(360deg); } to { transform: rotate(0deg); } }
                    @keyframes pulse { from { transform: scale(0.9); } to { transform: scale(1.1); } }
                """.trimIndent()
            )
        )
    }

    val currentFile = codeFiles[selectedFileIndex]

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisHudBackground)
            .padding(12.dp)
    ) {
        // Architecture Overview Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(JarvisHudSurfaceElevated)
                .border(1.dp, JarvisHudBorder, RoundedCornerShape(8.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Codebase Architecture",
                        tint = JarvisCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "J.A.R.V.I.S. MODULAR ARCHITECTURE",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Complete Python + HTML+JS Agent codebase for STT, Gemini LLM router & Arc HUD.",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextSecondary
                )
            }

            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(currentFile.filename, currentFile.content)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied ${currentFile.filename} to clipboard, sir.", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black),
                modifier = Modifier.testTag("copy_code_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Code",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("COPY", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // File Selector Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            codeFiles.forEachIndexed { index, file ->
                val isSelected = index == selectedFileIndex
                Box(
                    modifier = Modifier
                        .testTag("code_tab_$index")
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisHudSurface)
                        .border(1.dp, if (isSelected) JarvisCyan else JarvisHudBorder, RoundedCornerShape(6.dp))
                        .clickable { selectedFileIndex = index }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = file.filename,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) JarvisCyan else JarvisTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Code Viewer Screen
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF030814))
                .border(1.dp, JarvisHudBorder, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${currentFile.filename} (${currentFile.language})",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = JarvisAmber
                )
                Text(
                    text = currentFile.description,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextDim
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = currentFile.content,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextPrimary
                )
            }
        }
    }
}

data class CodeSnippet(
    val filename: String,
    val language: String,
    val description: String,
    val content: String
)
