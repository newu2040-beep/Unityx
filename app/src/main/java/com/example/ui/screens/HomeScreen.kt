package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.animation.AnimationManager
import com.example.ui.animation.ExpressiveAnimatedVisibility
import com.example.ui.animation.StaggeredAnimatedItem
import com.example.ui.animation.expressiveAnimateContentSize
import com.example.ui.animation.expressiveBounceClick
import com.example.ui.animation.expressivePressScale
import com.example.ui.components.ActionStepItemView
import com.example.ui.components.ExpressiveMicrophoneButton
import com.example.ui.components.ProfessionalWaveformVisualization
import com.example.ui.components.RiskBadge
import com.example.ui.components.StatusPill
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertCoral
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishEmerald
import com.example.ui.theme.PolishEmeraldBg
import com.example.ui.theme.PolishEmeraldDark
import com.example.ui.theme.PolishIndigo
import com.example.ui.theme.PolishOnPrimaryContainer
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishSurfaceHover
import com.example.ui.theme.PolishSurfaceVariant
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.theme.PolishTextTertiary
import com.example.ui.theme.PolishToastDark
import com.example.ui.theme.StatusGreen
import com.example.ui.viewmodel.UnityXViewModel

@Composable
fun HomeScreen(
    viewModel: UnityXViewModel,
    modifier: Modifier = Modifier
) {
    val queryInput by viewModel.queryInput.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val rmsDb by viewModel.rmsDb.collectAsState()
    val activePlan by viewModel.activePlan.collectAsState()
    val isExecuting by viewModel.isExecuting.collectAsState()
    val lastMessage by viewModel.lastExecutionMessage.collectAsState()
    val isEmergencyStopped by viewModel.isEmergencyStopped.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val isFloatingBubbleRunning by viewModel.isFloatingBubbleRunning.collectAsState()
    val isFloatingBubbleHidden by viewModel.isFloatingBubbleHidden.collectAsState()
    val isFloatingBubblePaused by viewModel.isFloatingBubblePaused.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val adaptive = com.example.core.design.adaptive.LocalAdaptiveDimensions.current

    val userName = preferences?.userName ?: "Rahul"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PolishBackground)
            .padding(horizontal = adaptive.horizontalPadding)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(if (adaptive.isCompact) 10.dp else 16.dp))

        // Professional Polish App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "UNITYX",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = PolishTextPrimary,
                        fontSize = 24.sp
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PolishEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OFFLINE NATIVE MODE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            fontSize = 10.sp,
                            color = PolishEmeraldDark
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Emergency Stop Button
                IconButton(
                    onClick = {
                        if (isEmergencyStopped) viewModel.resetEmergencyStop()
                        else viewModel.triggerEmergencyStop()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isEmergencyStopped) AlertAmber else PolishBorder)
                        .testTag("emergency_stop_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.StopCircle,
                        contentDescription = "Emergency Stop",
                        tint = if (isEmergencyStopped) Color.Black else (if (isEmergencyStopped) AlertCoral else PolishTextSecondary)
                    )
                }
            }
        }

        // Emergency Banner if active
        AnimatedVisibility(visible = isEmergencyStopped) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AlertCoral.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AlertCoral.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.StopCircle, contentDescription = null, tint = AlertCoral)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "EMERGENCY STOP ACTIVE. Tap button to resume.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AlertCoral,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Greeting & Waveform Visualization
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (queryInput.isNotEmpty()) "\"$queryInput\"" else "\"Call Rahul and tell him I'll be late.\"",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    color = PolishTextSecondary,
                    lineHeight = 26.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Centered 5-bar Professional Indigo Waveform
            ProfessionalWaveformVisualization(
                isListening = isListening,
                rmsDb = rmsDb,
                modifier = Modifier.height(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Central Squircle Mic Button (112dp squircle in #DBE1FF with #00174B icon)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            ExpressiveMicrophoneButton(
                isListening = isListening,
                rmsDb = rmsDb,
                onClick = { viewModel.toggleListening() }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Voice state subtitle
        Text(
            text = when {
                isListening -> "◉ Listening... speak your command"
                isExecuting -> "⚡ Executing action plan..."
                else -> "\"What can I do for you?\""
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isListening) PolishPrimary else PolishTextSecondary,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Natural Language Text Input Field
        OutlinedTextField(
            value = queryInput,
            onValueChange = { viewModel.onQueryChange(it) },
            placeholder = { Text("Ask anything or type command...", color = PolishTextTertiary, fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("assistant_input_field"),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PolishPrimary,
                unfocusedBorderColor = PolishBorder,
                focusedContainerColor = PolishSurfaceVariant,
                unfocusedContainerColor = PolishSurfaceVariant,
                focusedTextColor = PolishTextPrimary,
                unfocusedTextColor = PolishTextPrimary
            ),
            trailingIcon = {
                if (queryInput.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.submitManualQuery() },
                        modifier = Modifier.testTag("submit_query_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Submit", tint = PolishPrimary)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { viewModel.submitManualQuery() }),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Suggestions
        Text(
            text = "TRY ASKING",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = PolishTextTertiary,
                letterSpacing = 1.2.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf(
                "Call Rahul and tell him I'll reach in 20 minutes",
                "Work Mode",
                "Turn off Wi-Fi & Bluetooth, set alarm for 7 AM",
                "Set 15 minute timer",
                "Check battery",
                "Text Mom saying I'm on my way"
            )

            suggestions.forEachIndexed { index, prompt ->
                StaggeredAnimatedItem(index = index) {
                    AssistChip(
                        onClick = {
                            viewModel.onQueryChange(prompt)
                            viewModel.submitManualQuery()
                        },
                        label = { Text(prompt, fontSize = 12.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = PolishSurfaceVariant,
                            labelColor = PolishTextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                        modifier = Modifier.expressivePressScale()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Floating Circle Assistant Control Card
        FloatingAssistantCard(
            isRunning = isFloatingBubbleRunning,
            isHidden = isFloatingBubbleHidden,
            isPaused = isFloatingBubblePaused,
            canDrawOverlays = viewModel.canDrawOverlays(),
            onToggleRunning = {
                if (!viewModel.canDrawOverlays()) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                } else {
                    viewModel.toggleFloatingBubble()
                }
            },
            onToggleVisibility = { viewModel.toggleFloatingBubbleVisibility() },
            onTogglePause = { viewModel.toggleFloatingBubblePause() },
            onStop = { viewModel.stopFloatingBubble() }
        )

        // Active Action Plan Preview Card (Expressive Transition & Dynamic Content Resize)
        ExpressiveAnimatedVisibility(
            visible = activePlan != null
        ) {
            activePlan?.let { plan ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PolishSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .expressiveAnimateContentSize()
                        .testTag("action_plan_preview_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (plan.isCompound) "${plan.steps.size} ACTIONS PLANNED" else "ACTION READY",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PolishPrimary,
                                    letterSpacing = 1.sp
                                )
                            )
                            RiskBadge(riskLevel = plan.riskLevel)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = plan.naturalResponse,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = PolishTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Steps breakdown
                        plan.steps.forEach { step ->
                            ActionStepItemView(step = step, modifier = Modifier.padding(vertical = 4.dp))
                        }

                        // Confirmation Buttons if confirmation required and not yet executed
                        if (plan.requiresConfirmation && !isExecuting && plan.steps.any { it.status == com.example.domain.models.StepStatus.PENDING }) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.cancelActivePlan() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PolishTextSecondary),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                                    modifier = Modifier
                                        .expressivePressScale()
                                        .testTag("cancel_plan_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cancel")
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Button(
                                    onClick = { viewModel.confirmAndExecute() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PolishPrimary,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .expressivePressScale()
                                        .testTag("confirm_plan_button")
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Confirm & Run", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Task Feedback Toast Card (Professional Polish Dark Toast)
        lastMessage?.let { msg ->
            if (activePlan == null) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = PolishToastDark,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TASK COMPLETED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Quick Action Grid (2x2 Professional Cards as in design HTML)
        Text(
            text = "ACTIONS & SHORTCUTS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = PolishTextTertiary,
                letterSpacing = 1.2.sp
            )
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.Call,
                title = "Calls",
                subtitle = "Call contacts directly",
                modifier = Modifier.weight(1f)
            ) {
                viewModel.onQueryChange("Call Mom")
                viewModel.submitManualQuery()
            }

            QuickActionCard(
                icon = Icons.Default.DirectionsRun,
                title = "Automation",
                subtitle = "Work & Home routines",
                modifier = Modifier.weight(1f)
            ) {
                viewModel.onQueryChange("Work Mode")
                viewModel.submitManualQuery()
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.Message,
                title = "Messaging",
                subtitle = "Draft & send SMS",
                modifier = Modifier.weight(1f)
            ) {
                viewModel.onQueryChange("Send message to John: I'm outside")
                viewModel.submitManualQuery()
            }

            QuickActionCard(
                icon = Icons.Default.BatteryChargingFull,
                title = "System State",
                subtitle = "Battery & storage stats",
                modifier = Modifier.weight(1f)
            ) {
                viewModel.onQueryChange("Check battery")
                viewModel.submitManualQuery()
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = PolishSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = PolishPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = PolishTextPrimary,
                    fontSize = 14.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = PolishTextTertiary,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun FloatingAssistantCard(
    isRunning: Boolean,
    isHidden: Boolean,
    isPaused: Boolean,
    canDrawOverlays: Boolean,
    onToggleRunning: () -> Unit,
    onToggleVisibility: () -> Unit,
    onTogglePause: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PolishSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("floating_circle_assistant_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    !isRunning -> PolishSurfaceVariant
                                    isPaused -> AlertAmber.copy(alpha = 0.2f)
                                    isHidden -> PolishIndigo.copy(alpha = 0.2f)
                                    else -> PolishPrimary.copy(alpha = 0.15f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Floating Circle",
                            tint = when {
                                !isRunning -> PolishTextTertiary
                                isPaused -> AlertAmber
                                isHidden -> PolishIndigo
                                else -> PolishPrimary
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Floating Circle Assistant",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PolishTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when {
                                !isRunning -> "Background assistant overlay is off"
                                isHidden -> "Circle hidden • Tap Show to reveal"
                                isPaused -> "Assistant paused • Triggers suspended"
                                else -> "Active on screen • Running smoothly"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = when {
                                    !isRunning -> PolishTextTertiary
                                    isPaused -> AlertAmber
                                    isHidden -> PolishIndigo
                                    else -> PolishEmerald
                                },
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        !isRunning -> PolishBorder.copy(alpha = 0.5f)
                        isPaused -> AlertAmber.copy(alpha = 0.15f)
                        isHidden -> PolishIndigo.copy(alpha = 0.15f)
                        else -> PolishEmerald.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = when {
                            !isRunning -> "OFF"
                            isPaused -> "PAUSED"
                            isHidden -> "HIDDEN"
                            else -> "RUNNING"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = when {
                                !isRunning -> PolishTextSecondary
                                isPaused -> AlertAmber
                                isHidden -> PolishIndigo
                                else -> PolishEmerald
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!isRunning) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Runs smoothly outside the app over any screen.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PolishTextSecondary,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onToggleRunning,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PolishPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("start_floating_circle_button")
                    ) {
                        Text(
                            text = if (!canDrawOverlays) "Grant Permission" else "Enable Circle",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                // Interactive controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Hide / Show
                    OutlinedButton(
                        onClick = onToggleVisibility,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PolishTextPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("floating_hide_show_button")
                    ) {
                        Icon(
                            imageVector = if (isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isHidden) "Show" else "Hide", fontSize = 12.sp)
                    }

                    // Pause / Resume
                    OutlinedButton(
                        onClick = onTogglePause,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isPaused) AlertAmber else PolishBorder
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isPaused) AlertAmber else PolishTextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("floating_pause_resume_button")
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isPaused) "Resume" else "Pause", fontSize = 12.sp)
                    }

                    // Stop
                    OutlinedButton(
                        onClick = onStop,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AlertCoral.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCoral),
                        modifier = Modifier
                            .weight(0.9f)
                            .testTag("floating_stop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stop", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PolishSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 Control anytime from notification shade: Hide, Pause, Resume, or Stop without opening UNITYX.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PolishTextTertiary,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
