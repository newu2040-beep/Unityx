package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AssistantPreferenceEntity
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertCoral
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishEmerald
import com.example.ui.theme.PolishError
import com.example.ui.theme.PolishIndigo
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishSurfaceVariant
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.theme.PolishTextTertiary
import com.example.ui.viewmodel.UnityXViewModel

@Composable
fun SettingsScreen(
    viewModel: UnityXViewModel,
    modifier: Modifier = Modifier
) {
    val preferences by viewModel.preferences.collectAsState()
    val privacyMetrics by viewModel.privacyMetrics.collectAsState()
    val isEmergencyStopped by viewModel.isEmergencyStopped.collectAsState()

    val currentPrefs = preferences ?: AssistantPreferenceEntity()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PolishBackground)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SYSTEM & PRIVACY",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = PolishPrimary
            )
        )
        Text(
            text = "Settings & Metrics",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = PolishTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Privacy Metrics Card
        Text(
            text = "PRIVACY DASHBOARD (LOCAL ONLY)",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = PolishTextSecondary,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PolishSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricItem(
                        icon = Icons.Default.CloudOff,
                        label = "Cloud Calls",
                        value = "${privacyMetrics.cloudRequestsToday}",
                        color = PolishEmerald
                    )
                    MetricItem(
                        icon = Icons.Default.Mic,
                        label = "Voice Captures",
                        value = "${privacyMetrics.micInteractionsToday}",
                        color = PolishPrimary
                    )
                    MetricItem(
                        icon = Icons.Default.ContactPhone,
                        label = "Contact Lookups",
                        value = "${privacyMetrics.contactsLookupsToday}",
                        color = PolishIndigo
                    )
                    MetricItem(
                        icon = Icons.Default.Sms,
                        label = "SMS Prepared",
                        value = "${privacyMetrics.messagesSentToday}",
                        color = PolishTextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PolishEmerald.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✓ Zero audio or personal data is uploaded to remote servers. All NLU models execute strictly on-device.",
                        style = MaterialTheme.typography.labelSmall.copy(color = PolishEmerald, fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Preferences Group
        Text(
            text = "ASSISTANT PREFERENCES",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = PolishTextSecondary,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PolishSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Voice Response (TTS)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Spoken Voice Responses", style = MaterialTheme.typography.bodyMedium.copy(color = PolishTextPrimary, fontWeight = FontWeight.SemiBold))
                        Text(text = "Use Android TextToSpeech to speak actions aloud", style = MaterialTheme.typography.bodySmall.copy(color = PolishTextSecondary))
                    }
                    Switch(
                        checked = currentPrefs.voiceResponseEnabled,
                        onCheckedChange = { viewModel.updatePreferences(currentPrefs.copy(voiceResponseEnabled = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PolishPrimary,
                            uncheckedThumbColor = PolishTextTertiary,
                            uncheckedTrackColor = PolishSurfaceVariant
                        )
                    )
                }

                // Wake Word ("Hey UNITYX")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Wake Word (\"Hey UNITYX\")", style = MaterialTheme.typography.bodyMedium.copy(color = PolishTextPrimary, fontWeight = FontWeight.SemiBold))
                        Text(text = "Listen for offline wake trigger when screen is active", style = MaterialTheme.typography.bodySmall.copy(color = PolishTextSecondary))
                    }
                    Switch(
                        checked = currentPrefs.wakeWordEnabled,
                        onCheckedChange = { viewModel.updatePreferences(currentPrefs.copy(wakeWordEnabled = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PolishPrimary,
                            uncheckedThumbColor = PolishTextTertiary,
                            uncheckedTrackColor = PolishSurfaceVariant
                        )
                    )
                }

                // Offline-Only Enforcement
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Strict Offline-Only Mode", style = MaterialTheme.typography.bodyMedium.copy(color = PolishTextPrimary, fontWeight = FontWeight.SemiBold))
                        Text(text = "Disable all external network calls permanently", style = MaterialTheme.typography.bodySmall.copy(color = PolishTextSecondary))
                    }
                    Switch(
                        checked = currentPrefs.isOfflineModeOnly,
                        onCheckedChange = { viewModel.updatePreferences(currentPrefs.copy(isOfflineModeOnly = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PolishEmerald,
                            uncheckedThumbColor = PolishTextTertiary,
                            uncheckedTrackColor = PolishSurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Floating Background Circle Settings
        Text(
            text = "FLOATING OVERLAY & NOTIFICATION",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = PolishTextSecondary,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        val isFloatingRunning by viewModel.isFloatingBubbleRunning.collectAsState()
        val isFloatingHidden by viewModel.isFloatingBubbleHidden.collectAsState()
        val isFloatingPaused by viewModel.isFloatingBubblePaused.collectAsState()
        val context = LocalContext.current

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PolishSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Floating Assistant Circle",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = PolishTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = if (isFloatingRunning) "Circle runs smoothly over other apps" else "Enable floating circle overlay on home screen",
                            style = MaterialTheme.typography.bodySmall.copy(color = PolishTextSecondary)
                        )
                    }

                    Switch(
                        checked = isFloatingRunning,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                if (!viewModel.canDrawOverlays()) {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                } else {
                                    viewModel.startFloatingBubble()
                                }
                            } else {
                                viewModel.stopFloatingBubble()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PolishPrimary,
                            uncheckedThumbColor = PolishTextTertiary,
                            uncheckedTrackColor = PolishSurfaceVariant
                        )
                    )
                }

                if (isFloatingRunning) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.toggleFloatingBubbleVisibility() },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PolishTextPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isFloatingHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isFloatingHidden) "Show" else "Hide", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.toggleFloatingBubblePause() },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isFloatingPaused) AlertAmber else PolishBorder
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isFloatingPaused) AlertAmber else PolishTextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isFloatingPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isFloatingPaused) "Resume" else "Pause", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Safety Override Card
        Text(
            text = "SAFETY CONTROLS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = PolishTextSecondary,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PolishSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Emergency Stop Switch",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isEmergencyStopped) PolishError else PolishTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (isEmergencyStopped) "All automated routines and speech are halted." else "Tap to immediately halt all active automations.",
                            style = MaterialTheme.typography.bodySmall.copy(color = PolishTextSecondary)
                        )
                    }

                    Switch(
                        checked = isEmergencyStopped,
                        onCheckedChange = {
                            if (it) viewModel.triggerEmergencyStop()
                            else viewModel.resetEmergencyStop()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PolishError,
                            uncheckedThumbColor = PolishTextTertiary,
                            uncheckedTrackColor = PolishSurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun MetricItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PolishTextPrimary))
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = PolishTextTertiary, fontSize = 10.sp))
    }
}

