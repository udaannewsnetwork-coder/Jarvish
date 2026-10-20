package com.example.ui.hud

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Terminal
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
import com.example.model.ToolExecutionLog
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HudTerminalTab(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val executionLogs by viewModel.toolRouter.executionLogs.collectAsState()
    val terminalInput by viewModel.terminalInput.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(executionLogs.size) {
        if (executionLogs.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisHudBackground)
            .padding(12.dp)
    ) {
        // Terminal Title Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(JarvisHudSurfaceElevated)
                .border(1.dp, JarvisHudBorder, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Tactical Terminal",
                    tint = JarvisGreenNominal,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "STARK TACTICAL SHELL // DEV_TTY0",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = JarvisGreenNominal
                )
            }
            Text(
                text = "${executionLogs.size} LOGS CAPTURED",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = JarvisTextDim
            )
        }

        // Quick Shell Command Suggestions
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(JarvisHudSurface)
                .border(1.dp, JarvisHudBorder)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CommandChip("sys.info") { viewModel.executeTerminalCommand("sys.info") }
            CommandChip("proc.list") { viewModel.executeTerminalCommand("proc.list") }
            CommandChip("net.ping") { viewModel.executeTerminalCommand("net.ping") }
            CommandChip("df -h") { viewModel.executeTerminalCommand("df -h") }
            CommandChip("uname -a") { viewModel.executeTerminalCommand("uname -a") }
            CommandChip("file.list") { viewModel.executeTerminalCommand("file.list") }
            CommandChip("arc.power 120") { viewModel.executeTerminalCommand("arc.power 120") }
        }

        // Console Output View
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF020710))
                .border(1.dp, JarvisHudBorder)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (executionLogs.isEmpty()) {
                item {
                    Text(
                        text = "READY FOR COMMAND INPUT.\nSTARK KERNEL v8.4.2 [ONLINE]\nType a shell command or tap a quick routine above.",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisGreenNominal.copy(alpha = 0.8f)
                    )
                }
            } else {
                items(executionLogs) { log ->
                    TerminalLogItem(log)
                }
            }
        }

        // Shell Prompt Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                .background(JarvisHudSurfaceElevated)
                .border(1.dp, JarvisHudBorder, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "stark@jarvis:~$ ",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = JarvisGreenNominal
            )

            OutlinedTextField(
                value = terminalInput,
                onValueChange = { viewModel.onTerminalInputChange(it) },
                placeholder = {
                    Text(
                        "sys.info, df -h, ping...",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisTextDim
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("terminal_input_field"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { viewModel.executeTerminalCommand() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = JarvisCyan,
                    unfocusedTextColor = JarvisCyan,
                    cursorColor = JarvisGreenNominal,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            IconButton(
                onClick = { viewModel.executeTerminalCommand() },
                modifier = Modifier.testTag("execute_terminal_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Execute Command",
                    tint = JarvisGreenNominal
                )
            }
        }
    }
}

@Composable
private fun CommandChip(command: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(JarvisHudSurfaceElevated)
            .border(1.dp, JarvisGreenNominal.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = "$ $command",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = JarvisGreenNominal
        )
    }
}

@Composable
private fun TerminalLogItem(log: ToolExecutionLog) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF040D1A))
            .border(1.dp, if (log.isSuccess) JarvisHudBorder else JarvisRedAlert.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "[${log.timestamp}]",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextDim
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = log.toolName,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (log.isSuccess) JarvisCyan else JarvisRedAlert
                )
            }
            Text(
                text = if (log.isSuccess) "EXIT 0" else "EXIT 1",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = if (log.isSuccess) JarvisGreenNominal else JarvisRedAlert
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$ ${log.commandOrTarget}",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = JarvisAmber
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = log.output,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontFamily = FontFamily.Monospace,
            color = JarvisTextPrimary
        )
    }
}
