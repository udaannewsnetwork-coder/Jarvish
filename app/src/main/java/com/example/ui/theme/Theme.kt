package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = Color(0xFF00363F),
    primaryContainer = JarvisCyanDark,
    onPrimaryContainer = JarvisCyan,
    secondary = JarvisGold,
    onSecondary = Color(0xFF452B00),
    secondaryContainer = Color(0xFF332000),
    onSecondaryContainer = JarvisAmber,
    tertiary = JarvisGreenNominal,
    background = JarvisHudBackground,
    onBackground = JarvisTextPrimary,
    surface = JarvisHudSurface,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisHudSurfaceElevated,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisHudBorder,
    error = JarvisRedAlert,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // J.A.R.V.I.S. is fundamentally an authentic holographic HUD
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
