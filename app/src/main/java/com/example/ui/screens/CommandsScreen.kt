package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CustomCommandEntity
import com.example.ui.animation.StaggeredAnimatedItem
import com.example.ui.animation.expressiveAnimateContentSize
import com.example.ui.animation.expressivePressScale
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishSurfaceVariant
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.theme.PolishTextTertiary
import com.example.ui.viewmodel.UnityXViewModel

@Composable
fun CommandsScreen(
    viewModel: UnityXViewModel,
    modifier: Modifier = Modifier
) {
    val commands by viewModel.customCommands.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PolishBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "COMMANDS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = PolishPrimary
                    )
                )
                Text(
                    text = "Custom Task Shortcuts",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Teach UNITYX what your personal phrases and shortcuts mean.",
                    style = MaterialTheme.typography.bodySmall.copy(color = PolishTextSecondary)
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            itemsIndexed(commands, key = { _, cmd -> cmd.id }) { index, cmd ->
                StaggeredAnimatedItem(index = index) {
                    CustomCommandCard(
                        command = cmd,
                        onDelete = { viewModel.deleteCustomCommand(cmd) },
                        onTest = {
                            val firstPhrase = cmd.triggerPhrases.split(",").firstOrNull()?.trim() ?: cmd.name
                            viewModel.onQueryChange(firstPhrase)
                            viewModel.submitManualQuery()
                        }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // FAB to add new command
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = PolishPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .expressivePressScale()
                .testTag("create_command_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Command")
        }

        if (showCreateDialog) {
            CreateCommandDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name, phrases, actionType, params ->
                    viewModel.createCustomCommand(name, phrases, actionType, params)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
fun CustomCommandCard(
    command: CustomCommandEntity,
    onDelete: () -> Unit,
    onTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PolishSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .expressiveAnimateContentSize()
            .testTag("command_card_${command.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = command.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PolishSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
                ) {
                    Text(
                        text = command.actionType,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PolishPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Trigger phrases
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = PolishPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Phrases: \"${command.triggerPhrases}\"",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PolishTextSecondary,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = PolishTextTertiary, modifier = Modifier.size(20.dp))
                }

                Button(
                    onClick = onTest,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PolishSurfaceVariant,
                        contentColor = PolishPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Command", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun CreateCommandDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, phrases: String, actionType: String, params: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phrases by remember { mutableStateOf("") }
    var actionType by remember { mutableStateOf("SET_TIMER") }
    var params by remember { mutableStateOf("""{"durationMinutes":"15"}""") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PolishSurface,
        title = { Text("New Custom Command", color = PolishTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Command Name (e.g. Quick Nap)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PolishTextPrimary,
                        unfocusedTextColor = PolishTextPrimary,
                        focusedBorderColor = PolishPrimary,
                        unfocusedBorderColor = PolishBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phrases,
                    onValueChange = { phrases = it },
                    label = { Text("Trigger Phrases (comma-separated)") },
                    placeholder = { Text("nap time, take a nap, quick rest") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PolishTextPrimary,
                        unfocusedTextColor = PolishTextPrimary,
                        focusedBorderColor = PolishPrimary,
                        unfocusedBorderColor = PolishBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = actionType,
                    onValueChange = { actionType = it },
                    label = { Text("Action Type (e.g. SET_TIMER, RUN_WORKFLOW)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PolishTextPrimary,
                        unfocusedTextColor = PolishTextPrimary,
                        focusedBorderColor = PolishPrimary,
                        unfocusedBorderColor = PolishBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phrases.isNotBlank()) {
                        onCreate(name, phrases, actionType, params)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary, contentColor = Color.White)
            ) {
                Text("Save Command")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel", color = PolishTextSecondary) }
        }
    )
}

