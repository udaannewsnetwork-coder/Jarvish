package com.example.ui.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AlertLevel
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGreenNominal
import com.example.ui.theme.JarvisHudBackground
import com.example.ui.theme.JarvisHudBorder
import com.example.ui.theme.JarvisHudSurface
import com.example.ui.theme.JarvisHudSurfaceElevated
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisTextDim
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.viewmodel.JarvisViewModel
import java.util.Locale

@Composable
fun HudSettingsTab(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val speechPitch by viewModel.speechPitch.collectAsState()
    val speechRate by viewModel.speechRate.collectAsState()
    val ttsEnabled by viewModel.ttsEnabled.collectAsState()
    val soundFxEnabled by viewModel.soundFxEnabled.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()

    val isApiConfigured = viewModel.aiService.isApiKeyConfigured

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisHudBackground)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section 1: Voice Calibration (British Accent Cadence)
        SettingsCard(
            title = "VOCAL SYNTHESIZER & CADENCE CALIBRATION",
            icon = Icons.Default.RecordVoiceOver,
            iconTint = JarvisCyan
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Text-to-Speech Output",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextPrimary
                )
                Switch(
                    checked = ttsEnabled,
                    onCheckedChange = {
                        viewModel.updateVoiceSettings(speechPitch, speechRate, it, soundFxEnabled)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = JarvisCyan,
                        checkedTrackColor = JarvisCyan.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("tts_toggle_switch")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Holographic Acoustic Chimes",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextPrimary
                )
                Switch(
                    checked = soundFxEnabled,
                    onCheckedChange = {
                        viewModel.updateVoiceSettings(speechPitch, speechRate, ttsEnabled, it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = JarvisCyan,
                        checkedTrackColor = JarvisCyan.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("soundfx_toggle_switch")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pitch slider
            Text(
                text = "Vocal Pitch Modulation: ${String.format(Locale.US, "%.2f", speechPitch)}x",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = JarvisTextSecondary
            )
            Slider(
                value = speechPitch,
                onValueChange = {
                    viewModel.updateVoiceSettings(it, speechRate, ttsEnabled, soundFxEnabled)
                },
                valueRange = 0.7f..1.3f,
                colors = SliderDefaults.colors(
                    thumbColor = JarvisCyan,
                    activeTrackColor = JarvisCyan,
                    inactiveTrackColor = JarvisHudBorder
                ),
                modifier = Modifier.testTag("pitch_slider")
            )

            // Speech Rate slider
            Text(
                text = "Cadence Delivery Rate: ${String.format(Locale.US, "%.2f", speechRate)}x",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = JarvisTextSecondary
            )
            Slider(
                value = speechRate,
                onValueChange = {
                    viewModel.updateVoiceSettings(speechPitch, it, ttsEnabled, soundFxEnabled)
                },
                valueRange = 0.8f..1.4f,
                colors = SliderDefaults.colors(
                    thumbColor = JarvisCyan,
                    activeTrackColor = JarvisCyan,
                    inactiveTrackColor = JarvisHudBorder
                ),
                modifier = Modifier.testTag("speech_rate_slider")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = { viewModel.testVoicePhrase() },
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan.copy(alpha = 0.2f), contentColor = JarvisCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, JarvisCyan, RoundedCornerShape(6.dp))
                    .testTag("test_voice_btn")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("TEST J.A.R.V.I.S. VOCAL PHRASE", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Section 2: LLM Model & API Key
        SettingsCard(
            title = "GEMINI LLM ENGINE STATUS",
            icon = Icons.Default.Key,
            iconTint = JarvisAmber
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Active Model:", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = JarvisTextSecondary)
                Text("gemini-3.5-flash", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = JarvisAmber)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("API Secret Status:", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = JarvisTextSecondary)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isApiConfigured) JarvisGreenNominal.copy(alpha = 0.2f) else JarvisAmber.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isApiConfigured) "ONLINE (AUTHENTICATED)" else "LOCAL STARK HEURISTICS",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isApiConfigured) JarvisGreenNominal else JarvisAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isApiConfigured) {
                    "Neural link established. Full generative reasoning and autonomous tool-calling active."
                } else {
                    "Operating under Stark autonomous tactical heuristics. To connect to Gemini cloud models, set 'GEMINI_API_KEY' in the AI Studio Secrets panel."
                },
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = JarvisTextDim
            )
        }

        // Section 3: Arc Reactor & Tactical Defense Controls
        SettingsCard(
            title = "ARC REACTOR POWER THROTTLE",
            icon = Icons.Default.Bolt,
            iconTint = if (telemetry.arcReactorOutput > 100) JarvisRedAlert else JarvisCyan
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Core Output: ${telemetry.arcReactorOutput}%",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (telemetry.arcReactorOutput > 100) JarvisRedAlert else JarvisCyan
                )
                Text(
                    text = telemetry.defenseStatus,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextDim
                )
            }

            Slider(
                value = telemetry.arcReactorOutput.toFloat(),
                onValueChange = { viewModel.toolRouter.modulateArcReactor(it.toInt()) },
                valueRange = 20f..150f,
                colors = SliderDefaults.colors(
                    thumbColor = if (telemetry.arcReactorOutput > 100) JarvisRedAlert else JarvisCyan,
                    activeTrackColor = if (telemetry.arcReactorOutput > 100) JarvisRedAlert else JarvisCyan,
                    inactiveTrackColor = JarvisHudBorder
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.toolRouter.setAlertLevel(AlertLevel.NORMAL)
                        viewModel.toolRouter.modulateArcReactor(100)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisHudSurfaceElevated, contentColor = JarvisCyan),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("NOMINAL", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
                Button(
                    onClick = {
                        viewModel.toolRouter.setAlertLevel(AlertLevel.CRITICAL_DEFENSE)
                        viewModel.toolRouter.modulateArcReactor(135)
                        viewModel.voiceSynthesizer.playAlertTone()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisRedAlert.copy(alpha = 0.2f), contentColor = JarvisRedAlert),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("RED ALERT", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(JarvisHudSurface)
            .border(1.dp, JarvisHudBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = iconTint
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}
