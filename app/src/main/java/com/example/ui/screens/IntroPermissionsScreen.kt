package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.design.adaptive.LocalAdaptiveDimensions
import com.example.core.design.surface.ElevatedExpressiveCard
import com.example.core.design.surface.ElevatedLevel
import com.example.integration.PermissionItem
import com.example.ui.animation.expressiveBounceClick
import com.example.ui.animation.expressivePressScale
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishEmerald
import com.example.ui.theme.PolishEmeraldBg
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishSurfaceVariant
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.theme.PolishTextTertiary
import com.example.ui.theme.PolishWarning
import com.example.ui.viewmodel.UnityXViewModel
import kotlinx.coroutines.delay

@Composable
fun IntroPermissionsScreen(
    viewModel: UnityXViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val permissions by viewModel.permissionsList.collectAsState()
    val adaptive = LocalAdaptiveDimensions.current
    var autoDismissTriggered by remember { mutableStateOf(false) }

    val batchPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshPermissions()
    }

    val singlePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshPermissions()
    }

    // Check if all core permissions are granted
    val corePermissions = permissions.filter { it.permissionManifest != null }
    val grantedCount = corePermissions.count { it.isGranted }
    val totalCount = corePermissions.size
    val allCoreGranted = totalCount > 0 && grantedCount == totalCount

    // Automatically hide intro once all permissions are granted
    LaunchedEffect(allCoreGranted) {
        if (allCoreGranted && !autoDismissTriggered) {
            autoDismissTriggered = true
            delay(750) // Brief feedback for user to see success state
            viewModel.completeIntro()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PolishPrimaryContainer.copy(alpha = 0.35f),
                        PolishBackground,
                        PolishBackground
                    )
                )
            )
            .padding(horizontal = adaptive.horizontalPadding)
            .testTag("intro_permissions_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(adaptive.itemSpacing)
        ) {
            item {
                Spacer(modifier = Modifier.height(if (adaptive.isCompact) 18.dp else 36.dp))

                // Hero App Badge
                Surface(
                    shape = CircleShape,
                    color = PolishPrimary,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .size(if (adaptive.isCompact) 64.dp else 80.dp)
                        .testTag("intro_hero_badge")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security & Full Access",
                            tint = Color.White,
                            modifier = Modifier.size(if (adaptive.isCompact) 32.dp else 42.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title & Subtitle using Material 3 Expressive Typography
                Text(
                    text = "Welcome to UNITYX",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PolishTextPrimary,
                        letterSpacing = (-0.5).sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Enable notification, gallery, and voice access for hands-free offline automations. All permissions operate on-device.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PolishTextSecondary,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Counter Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (allCoreGranted) PolishEmeraldBg else PolishSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (allCoreGranted) PolishEmerald else PolishBorder
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (allCoreGranted) Icons.Default.CheckCircle else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (allCoreGranted) PolishEmerald else PolishPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (allCoreGranted) "All Permissions Granted! Entering App..." else "$grantedCount of $totalCount permissions enabled",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (allCoreGranted) PolishEmerald else PolishTextPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // List of Permission Items with elevated cards
            items(corePermissions, key = { it.id }) { item ->
                IntroPermissionItemCard(
                    item = item,
                    onGrantClick = {
                        item.permissionManifest?.let { manifestPermission ->
                            singlePermissionLauncher.launch(manifestPermission)
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Fixed Elevated Bottom Action Bar
        Surface(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = PolishSurface,
            shadowElevation = 12.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .testTag("intro_bottom_action_bar")
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = adaptive.horizontalPadding, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Primary Action Button: "Grant All Permissions"
                Button(
                    onClick = {
                        if (allCoreGranted) {
                            viewModel.completeIntro()
                        } else {
                            val perms = viewModel.getCoreRuntimePermissions()
                            batchPermissionLauncher.launch(perms)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allCoreGranted) PolishEmerald else PolishPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (adaptive.isCompact) 48.dp else 52.dp)
                        .expressiveBounceClick {
                            if (allCoreGranted) {
                                viewModel.completeIntro()
                            } else {
                                val perms = viewModel.getCoreRuntimePermissions()
                                batchPermissionLauncher.launch(perms)
                            }
                        }
                        .testTag("grant_all_permissions_button")
                ) {
                    Icon(
                        imageVector = if (allCoreGranted) Icons.Default.Check else Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (allCoreGranted) "Get Started" else "Grant All Permissions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (adaptive.isCompact) 14.sp else 16.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Dismiss / Continue Option
                OutlinedButton(
                    onClick = { viewModel.completeIntro() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PolishTextTertiary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("intro_skip_button")
                ) {
                    Text(
                        text = if (allCoreGranted) "Continue to UNITYX" else "Continue with limited access",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = PolishTextSecondary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun IntroPermissionItemCard(
    item: PermissionItem,
    onGrantClick: () -> Unit
) {
    val adaptive = LocalAdaptiveDimensions.current
    val icon = when (item.id) {
        "notifications" -> Icons.Default.Notifications
        "gallery" -> Icons.Default.Image
        "mic" -> Icons.Default.Mic
        "phone" -> Icons.Default.Phone
        "contacts" -> Icons.Default.ContactPhone
        "sms" -> Icons.Default.Sms
        else -> Icons.Default.Security
    }

    ElevatedExpressiveCard(
        level = if (item.isGranted) ElevatedLevel.Level0 else ElevatedLevel.Level1,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.isGranted) PolishEmerald.copy(alpha = 0.35f) else PolishBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("intro_perm_card_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .padding(if (adaptive.isCompact) 12.dp else 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (item.isGranted) PolishEmeraldBg else PolishSurfaceVariant,
                    modifier = Modifier.size(if (adaptive.isCompact) 36.dp else 42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = item.title,
                            tint = if (item.isGranted) PolishEmerald else PolishPrimary,
                            modifier = Modifier.size(if (adaptive.isCompact) 20.dp else 22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PolishTextPrimary,
                                fontSize = if (adaptive.isCompact) 13.sp else 15.sp
                            )
                        )
                        if (item.isGranted) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Granted",
                                tint = PolishEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PolishTextSecondary,
                            fontSize = if (adaptive.isCompact) 11.sp else 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (item.isGranted) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PolishEmeraldBg
                ) {
                    Text(
                        text = "ACTIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PolishEmerald,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Button(
                    onClick = onGrantClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PolishPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .height(34.dp)
                        .expressivePressScale()
                        .testTag("grant_btn_${item.id}")
                ) {
                    Text(
                        text = "Allow",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}
