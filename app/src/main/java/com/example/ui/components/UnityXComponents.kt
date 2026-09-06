package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.ActionStep
import com.example.domain.models.RiskLevel
import com.example.domain.models.StepStatus
import com.example.ui.animation.expressiveAnimateContentSize
import com.example.ui.animation.expressiveBounceClick
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertCoral
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishEmerald
import com.example.ui.theme.PolishEmeraldBg
import com.example.ui.theme.PolishEmeraldDark
import com.example.ui.theme.PolishOnPrimaryContainer
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishSurfaceHover
import com.example.ui.theme.PolishSurfaceVariant
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.theme.PolishTextTertiary
import com.example.ui.theme.StatusGreen

@Composable
fun StatusPill(
    isReady: Boolean = true,
    isOffline: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = PolishEmeraldBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishEmerald.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isReady) PolishEmerald else AlertAmber)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isOffline) "OFFLINE NATIVE MODE" else "ONLINE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    fontSize = 10.sp,
                    color = if (isReady) PolishEmeraldDark else AlertAmber
                )
            )
        }
    }
}

@Composable
fun RiskBadge(riskLevel: RiskLevel, modifier: Modifier = Modifier) {
    val (color, text) = when (riskLevel) {
        RiskLevel.LOW -> Pair(StatusGreen, "LOW RISK")
        RiskLevel.MEDIUM -> Pair(AlertAmber, "CONFIRMATION NEEDED")
        RiskLevel.HIGH -> Pair(AlertCoral, "HIGH RISK")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun ProfessionalWaveformVisualization(
    isListening: Boolean,
    rmsDb: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_height"
    )

    val multiplier = if (isListening) (1f + (rmsDb / 20f).coerceIn(0f, 1f)) * pulse else 1f

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        val baseHeights = listOf(14.dp, 28.dp, 44.dp, 36.dp, 20.dp)
        val opacities = listOf(0.4f, 0.6f, 1.0f, 0.8f, 0.5f)

        baseHeights.forEachIndexed { index, baseHeight ->
            val animatedHeight = (baseHeight * (if (isListening) multiplier else 1f)).coerceIn(8.dp, 52.dp)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(animatedHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PolishPrimary.copy(alpha = opacities[index]))
            )
        }
    }
}

@Composable
fun ExpressiveMicrophoneButton(
    isListening: Boolean,
    rmsDb: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.15f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val adaptive = com.example.core.design.adaptive.LocalAdaptiveDimensions.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(adaptive.micGlowSize + 6.dp)
    ) {
        // Subtle outer pulse glow (Pure native circular geometry)
        Box(
            modifier = Modifier
                .size(adaptive.micGlowSize)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(PolishPrimaryContainer.copy(alpha = if (isListening) 0.6f else 0.30f))
        )

        // Main Circular Button (Pure native Android Circle, zero rectangle corners, expressive spring bounce)
        Surface(
            shape = CircleShape,
            color = if (isListening) Color(0xFFC0C8F8) else PolishPrimaryContainer,
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.9f)),
            shadowElevation = 6.dp,
            modifier = Modifier
                .size(adaptive.micButtonSize)
                .expressiveBounceClick(onClick = onClick)
                .testTag("mic_floating_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Assistant Microphone",
                    tint = PolishOnPrimaryContainer,
                    modifier = Modifier.size(adaptive.micIconSize)
                )
            }
        }
    }
}

@Composable
fun ActionStepItemView(step: ActionStep, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PolishSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
        modifier = modifier
            .fillMaxWidth()
            .expressiveAnimateContentSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            // Status Icon
            when (step.status) {
                StepStatus.PENDING -> Icon(
                    imageVector = Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Pending",
                    tint = PolishTextTertiary,
                    modifier = Modifier.size(20.dp)
                )
                StepStatus.RUNNING -> CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = PolishPrimary,
                    modifier = Modifier.size(20.dp)
                )
                StepStatus.COMPLETED -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = StatusGreen,
                    modifier = Modifier.size(20.dp)
                )
                StepStatus.FAILED -> Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Failed",
                    tint = AlertCoral,
                    modifier = Modifier.size(20.dp)
                )
                StepStatus.CANCELLED -> Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "Cancelled",
                    tint = AlertAmber,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = PolishTextPrimary
                    )
                )
                if (step.description.isNotEmpty()) {
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PolishTextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
                step.resultMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (step.status == StepStatus.COMPLETED) StatusGreen else AlertCoral,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            RiskBadge(riskLevel = step.riskLevel)
        }
    }
}
