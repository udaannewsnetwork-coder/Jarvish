package com.example.ui.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AlertLevel
import com.example.model.HudTab
import com.example.model.JarvisTelemetry
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGreenNominal
import com.example.ui.theme.JarvisHudBorder
import com.example.ui.theme.JarvisHudSurface
import com.example.ui.theme.JarvisHudSurfaceElevated
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import java.util.Locale

@Composable
fun HudHeader(
    telemetry: JarvisTelemetry,
    currentTab: HudTab,
    onTabSelected: (HudTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(JarvisHudSurface)
            .border(width = 1.dp, color = JarvisHudBorder)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Top Stark Brand & Protocol Line
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            when (telemetry.alertLevel) {
                                AlertLevel.NORMAL -> JarvisCyan
                                AlertLevel.WARNING -> JarvisAmber
                                AlertLevel.CRITICAL_DEFENSE -> JarvisRedAlert
                            }
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "STARK IND // J.A.R.V.I.S.",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = JarvisCyan
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(JarvisCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "MARK VII",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Power & Telemetry pill
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (telemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                    contentDescription = "Battery Status",
                    tint = if (telemetry.batteryPercent < 20) JarvisRedAlert else JarvisGreenNominal,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${telemetry.batteryPercent}%",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextPrimary
                )

                Spacer(modifier = Modifier.width(10.dp))

                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = "RAM Usage",
                    tint = JarvisAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${telemetry.ramUsedMb}M",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TabButton(
                label = "CORE",
                icon = Icons.Default.Mic,
                isSelected = currentTab == HudTab.VOICE_CORE,
                tag = "tab_voice_core",
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(HudTab.VOICE_CORE) }
            )
            TabButton(
                label = "TERMINAL",
                icon = Icons.Default.Terminal,
                isSelected = currentTab == HudTab.TACTICAL_TERMINAL,
                tag = "tab_tactical_terminal",
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(HudTab.TACTICAL_TERMINAL) }
            )
            TabButton(
                label = "FILES",
                icon = Icons.Default.Folder,
                isSelected = currentTab == HudTab.FILE_SYSTEM,
                tag = "tab_file_system",
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(HudTab.FILE_SYSTEM) }
            )
            TabButton(
                label = "CODE",
                icon = Icons.Default.Code,
                isSelected = currentTab == HudTab.MODULAR_CODEBASE,
                tag = "tab_modular_codebase",
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(HudTab.MODULAR_CODEBASE) }
            )
            TabButton(
                label = "CONFIG",
                icon = Icons.Default.Settings,
                isSelected = currentTab == HudTab.SETTINGS,
                tag = "tab_settings",
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(HudTab.SETTINGS) }
            )
        }
    }
}

@Composable
private fun TabButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    tag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .testTag(tag)
            .height(38.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisHudSurfaceElevated)
            .border(
                width = 1.dp,
                color = if (isSelected) JarvisCyan else JarvisHudBorder,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) JarvisCyan else JarvisTextSecondary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) JarvisCyan else JarvisTextSecondary
            )
        }
    }
}
