package com.example.ui.hud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.MessageSender
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreenNominal
import com.example.ui.theme.JarvisHudBackground
import com.example.ui.theme.JarvisHudBorder
import com.example.ui.theme.JarvisHudSurface
import com.example.ui.theme.JarvisHudSurfaceElevated
import com.example.ui.theme.JarvisTextDim
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.viewmodel.JarvisViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HudVoiceTab(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val isListening by viewModel.speechManager.isListening.collectAsState()
    val isSpeaking by viewModel.voiceSynthesizer.isSpeaking.collectAsState()
    val rmsAmplitude by viewModel.speechManager.rmsAmplitude.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val wakeWordActive by viewModel.wakeWordActive.collectAsState()
    val textInput by viewModel.textInput.collectAsState()
    val currentTranscript by viewModel.speechManager.currentTranscript.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()

    val listState = rememberLazyListState()

    // Auto-scroll when new messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisHudBackground)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Holographic Arc Reactor Core Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            ArcReactorView(
                isListening = isListening,
                isSpeaking = isSpeaking,
                isProcessing = isProcessing,
                audioAmplitude = rmsAmplitude,
                powerOutputPercent = telemetry.arcReactorOutput,
                onClick = { viewModel.toggleVoiceListening() }
            )
        }

        // Status Line & Ambient Listening Indicator
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(JarvisHudSurfaceElevated)
                .border(1.dp, JarvisHudBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isListening -> JarvisCyan
                            isSpeaking -> JarvisGold
                            isProcessing -> JarvisAmber
                            wakeWordActive -> JarvisGreenNominal
                            else -> JarvisTextDim
                        }
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when {
                    isListening -> "LISTENING TO VOCAL FREQUENCY..."
                    isSpeaking -> "TRANSMITTING VOCAL SYNTHESIS..."
                    isProcessing -> "NEURAL CORE ROUTING TOOLS..."
                    wakeWordActive -> "STANDBY: 'HEY JARVIS' WAKE WORD ACTIVE"
                    else -> "TAP MIC OR CORE TO SPEAK TO J.A.R.V.I.S."
                },
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    isListening -> JarvisCyan
                    isSpeaking -> JarvisGold
                    isProcessing -> JarvisAmber
                    else -> JarvisTextSecondary
                }
            )
        }

        // Live Partial Transcript Overlay
        AnimatedVisibility(
            visible = isListening && currentTranscript.isNotBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(JarvisCyan.copy(alpha = 0.1f))
                    .border(1.dp, JarvisCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "» \"$currentTranscript\"",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Action Chips Row
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            QuickChip(label = "Run Diagnostics", tag = "chip_diagnostics") {
                viewModel.submitUserMessage("Run system diagnostics and verify all sub-units")
            }
            QuickChip(label = "Launch Camera", tag = "chip_camera") {
                viewModel.submitUserMessage("Jarvis, open camera")
            }
            QuickChip(label = "Check Battery & RAM", tag = "chip_battery") {
                viewModel.submitUserMessage("Check battery level and RAM consumption")
            }
            QuickChip(label = "Mark VII Protocol", tag = "chip_mark_vii") {
                viewModel.submitUserMessage("Engage Mark VII armor protocol")
            }
            QuickChip(label = "Live Tech News", tag = "chip_news") {
                viewModel.submitUserMessage("Fetch live web news on artificial intelligence")
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Conversation Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(JarvisHudSurface.copy(alpha = 0.6f))
                .border(1.dp, JarvisHudBorder, RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatMessages, key = { it.id }) { msg ->
                ChatMessageItem(msg = msg, onSpeak = { viewModel.testVoicePhrase(msg.text) })
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Wake Word Continuous Toggle Button
            Box(
                modifier = Modifier
                    .testTag("wake_word_toggle_btn")
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (wakeWordActive) JarvisCyan.copy(alpha = 0.2f) else JarvisHudSurfaceElevated)
                    .border(1.dp, if (wakeWordActive) JarvisCyan else JarvisHudBorder, RoundedCornerShape(8.dp))
                    .clickable { viewModel.toggleContinuousWakeWord() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (wakeWordActive) "WAKE\nON" else "WAKE\nOFF",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (wakeWordActive) JarvisCyan else JarvisTextDim,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Text Input Box
            OutlinedTextField(
                value = textInput,
                onValueChange = { viewModel.onTextInputChange(it) },
                placeholder = {
                    Text(
                        "Command J.A.R.V.I.S...",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisTextDim
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_input_field"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { viewModel.submitUserMessage() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisHudBorder,
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary,
                    cursorColor = JarvisCyan,
                    focusedContainerColor = JarvisHudSurfaceElevated,
                    unfocusedContainerColor = JarvisHudSurfaceElevated
                ),
                shape = RoundedCornerShape(8.dp),
                trailingIcon = {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = JarvisCyan,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (textInput.isNotBlank()) {
                        IconButton(
                            onClick = { viewModel.submitUserMessage() },
                            modifier = Modifier.testTag("send_command_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Command",
                                tint = JarvisCyan
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Primary Mic Action Button
            Box(
                modifier = Modifier
                    .testTag("mic_action_btn")
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isListening) JarvisCyan else JarvisCyan.copy(alpha = 0.15f))
                    .border(1.5.dp, JarvisCyan, CircleShape)
                    .clickable { viewModel.toggleVoiceListening() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = if (isListening) Color.Black else JarvisCyan,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickChip(
    label: String,
    tag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .testTag(tag)
            .clip(RoundedCornerShape(6.dp))
            .background(JarvisHudSurfaceElevated)
            .border(1.dp, JarvisHudBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "› $label",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = JarvisCyan
        )
    }
}

@Composable
fun ChatMessageItem(
    msg: ChatMessage,
    onSpeak: () -> Unit
) {
    val isUser = msg.sender == MessageSender.USER
    val isTool = msg.sender == MessageSender.TOOL

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Sender Badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = when (msg.sender) {
                    MessageSender.USER -> "USER // AUTH TONY STARK"
                    MessageSender.JARVIS -> "J.A.R.V.I.S. // STARK AI"
                    MessageSender.TOOL -> "ROUTER // SUB-ROUTINE"
                    MessageSender.SYSTEM -> "SYSTEM // KERNEL"
                },
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = when (msg.sender) {
                    MessageSender.USER -> JarvisAmber
                    MessageSender.JARVIS -> JarvisCyan
                    MessageSender.TOOL -> JarvisGreenNominal
                    MessageSender.SYSTEM -> JarvisTextDim
                }
            )
            if (!isUser && msg.sender == MessageSender.JARVIS) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Replay Speech",
                    tint = JarvisCyan.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(14.dp)
                        .clickable(onClick = onSpeak)
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Message Content Box
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.85f else 1f)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    when {
                        isUser -> JarvisHudSurfaceElevated
                        isTool -> Color(0xFF031B15)
                        else -> JarvisHudSurface
                    }
                )
                .border(
                    width = 1.dp,
                    color = when {
                        isUser -> JarvisAmber.copy(alpha = 0.5f)
                        isTool -> JarvisGreenNominal.copy(alpha = 0.6f)
                        else -> JarvisCyan.copy(alpha = 0.4f)
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = msg.text,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontFamily = if (isTool) FontFamily.Monospace else FontFamily.Default,
                    color = JarvisTextPrimary
                )

                // Tool Execution Output Card if attached
                if (msg.toolName != null && msg.toolDetails != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF020914))
                            .border(1.dp, JarvisHudBorder, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = "Tool Executed",
                                    tint = JarvisGreenNominal,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "TOOL EXECUTED: ${msg.toolName.uppercase()}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisGreenNominal
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.toolDetails,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
