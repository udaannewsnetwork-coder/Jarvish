package com.example.ui.hud

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisRedAlert
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorView(
    modifier: Modifier = Modifier,
    isListening: Boolean = false,
    isSpeaking: Boolean = false,
    isProcessing: Boolean = false,
    audioAmplitude: Float = 0f,
    powerOutputPercent: Int = 100,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorTransitions")

    // Slow clockwise rotation
    val angleCw by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isProcessing) 2500 else 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AngleCW"
    )

    // Counter-clockwise rotation
    val angleCcw by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isProcessing) 3500 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AngleCCW"
    )

    // Core breathing / pulsing
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening || isSpeaking) 600 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CorePulse"
    )

    val coreColor = when {
        powerOutputPercent > 110 -> JarvisRedAlert
        isListening -> JarvisCyan
        isSpeaking -> JarvisGold
        else -> JarvisCyan
    }

    Box(
        modifier = modifier
            .size(260.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f * 0.92f

            // Dynamic amplitude expansion
            val activeAmplified = if (isListening) (audioAmplitude * 1.5f).coerceIn(0.1f, 1f) else if (isSpeaking) pulse * 0.4f else 0f

            // 1. Outer Glow Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreColor.copy(alpha = 0.25f + activeAmplified * 0.2f),
                        coreColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius * 1.15f
                ),
                radius = maxRadius * 1.15f,
                center = center
            )

            // 2. Outermost Segmented Dial Ring
            rotate(angleCw, pivot = center) {
                val segments = 36
                for (i in 0 until segments) {
                    val sweep = 6f
                    val startAngle = i * (360f / segments)
                    drawArc(
                        color = coreColor.copy(alpha = if (i % 3 == 0) 0.8f else 0.35f),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(center.x - maxRadius, center.y - maxRadius),
                        size = Size(maxRadius * 2, maxRadius * 2),
                        style = Stroke(width = if (i % 3 == 0) 3.5f else 1.8f, cap = StrokeCap.Round)
                    )
                }
            }

            // 3. Audio Frequency Waveform Spectrum (reacting to Speech)
            val freqRadius = maxRadius * 0.84f
            val barCount = 48
            for (i in 0 until barCount) {
                val theta = (i * 2 * PI / barCount).toFloat()
                val waveHeight = if (isListening || isSpeaking) {
                    val variation = sin(theta * 6 + angleCw * 0.05f).toFloat() * 12f
                    (activeAmplified * 24f + variation).coerceAtLeast(4f)
                } else {
                    4f + (sin(theta * 4).toFloat() * 2f)
                }

                val startX = center.x + (freqRadius - waveHeight / 2) * cos(theta)
                val startY = center.y + (freqRadius - waveHeight / 2) * sin(theta)
                val endX = center.x + (freqRadius + waveHeight / 2) * cos(theta)
                val endY = center.y + (freqRadius + waveHeight / 2) * sin(theta)

                drawLine(
                    color = coreColor.copy(alpha = 0.6f + activeAmplified * 0.3f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.2f,
                    cap = StrokeCap.Round
                )
            }

            // 4. Counter-rotating Intermediate Power Ring
            rotate(angleCcw, pivot = center) {
                val midRadius = maxRadius * 0.70f
                drawCircle(
                    color = coreColor.copy(alpha = 0.5f),
                    radius = midRadius,
                    center = center,
                    style = Stroke(width = 2.5f)
                )

                // 10 Stark Power Transformer Coils around circle
                val coilCount = 10
                for (i in 0 until coilCount) {
                    val theta = (i * 2 * PI / coilCount).toFloat()
                    val coilDist = midRadius
                    val coilCenter = Offset(
                        center.x + coilDist * cos(theta),
                        center.y + coilDist * sin(theta)
                    )

                    // Draw rectangular coil blocks
                    drawCircle(
                        color = if (i % 2 == 0) JarvisAmber else coreColor,
                        radius = 5.5f,
                        center = coilCenter
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.2f,
                        center = coilCenter
                    )
                }
            }

            // 5. Inner Core Ring
            val innerRingRadius = maxRadius * 0.50f
            drawCircle(
                color = JarvisCyanDark.copy(alpha = 0.8f),
                radius = innerRingRadius,
                center = center
            )
            drawCircle(
                color = coreColor,
                radius = innerRingRadius,
                center = center,
                style = Stroke(width = 3.5f)
            )

            // 6. Iconic Triangular Reactor Frame (Iron Man Mark VI / VII Arc Core)
            rotate(angleCw * 0.6f, pivot = center) {
                val triRadius = innerRingRadius * 0.82f
                val path = Path().apply {
                    for (i in 0 until 3) {
                        val angle = (i * 2 * PI / 3 - PI / 2).toFloat()
                        val x = center.x + triRadius * cos(angle)
                        val y = center.y + triRadius * sin(angle)
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }

                drawPath(
                    path = path,
                    color = JarvisGold.copy(alpha = 0.9f),
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )
            }

            // 7. Central Fusion Core (Pure white / cyan radiant energy)
            val centerCoreRadius = innerRingRadius * 0.40f * pulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        coreColor.copy(alpha = 0.9f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = centerCoreRadius * 1.5f
                ),
                radius = centerCoreRadius * 1.5f,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = centerCoreRadius * 0.7f,
                center = center
            )
        }
    }
}
