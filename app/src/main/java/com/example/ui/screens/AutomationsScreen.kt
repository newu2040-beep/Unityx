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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.WorkflowEntity
import com.example.ui.animation.StaggeredAnimatedItem
import com.example.ui.animation.expressiveAnimateContentSize
import com.example.ui.animation.expressiveBounceClick
import com.example.ui.animation.expressivePressScale
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishEmerald
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishSurfaceVariant
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.theme.PolishTextTertiary
import com.example.ui.theme.StatusGreen
import com.example.ui.viewmodel.UnityXViewModel

@Composable
fun AutomationsScreen(
    viewModel: UnityXViewModel,
    modifier: Modifier = Modifier
) {
    val workflows by viewModel.workflows.collectAsState()
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
                    text = "AUTOMATIONS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = PolishPrimary
                    )
                )
                Text(
                    text = "Local Workflows & Routines",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Multi-step actions triggered by voice, schedule, or device state.",
                    style = MaterialTheme.typography.bodySmall.copy(color = PolishTextSecondary)
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            itemsIndexed(workflows, key = { _, workflow -> workflow.id }) { index, workflow ->
                StaggeredAnimatedItem(index = index) {
                    WorkflowCard(
                        workflow = workflow,
                        onToggle = { viewModel.toggleWorkflow(workflow) },
                        onRun = { viewModel.runWorkflow(workflow) },
                        onDelete = { viewModel.deleteWorkflow(workflow.id) }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button to Create New Automation
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = PolishPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .expressivePressScale()
                .testTag("create_automation_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Create Automation")
        }

        // Create Workflow Dialog
        if (showCreateDialog) {
            CreateWorkflowDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name, desc, triggerType, triggerVal, condition, actionsJson ->
                    viewModel.createWorkflow(name, desc, triggerType, triggerVal, condition, actionsJson)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
fun WorkflowCard(
    workflow: WorkflowEntity,
    onToggle: () -> Unit,
    onRun: () -> Unit,
    onDelete: () -> Unit,
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
            .testTag("workflow_card_${workflow.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (workflow.isEnabled) PolishEmerald else PolishTextTertiary)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = workflow.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )
                    )
                }

                Switch(
                    checked = workflow.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PolishPrimary,
                        uncheckedThumbColor = PolishTextTertiary,
                        uncheckedTrackColor = PolishSurfaceVariant
                    ),
                    modifier = Modifier.testTag("workflow_switch_${workflow.id}")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = workflow.description,
                style = MaterialTheme.typography.bodySmall.copy(color = PolishTextSecondary)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Trigger and Condition badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PolishSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = PolishPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${workflow.triggerType}: ${workflow.triggerValue}",
                            style = MaterialTheme.typography.labelSmall.copy(color = PolishPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                workflow.condition?.let { cond ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PolishSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = PolishPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = cond,
                                style = MaterialTheme.typography.labelSmall.copy(color = PolishTextSecondary, fontSize = 11.sp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = PolishTextTertiary, modifier = Modifier.size(20.dp))
                }

                Button(
                    onClick = onRun,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PolishPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("run_workflow_button_${workflow.id}")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Run Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun CreateWorkflowDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, desc: String, triggerType: String, triggerVal: String, condition: String, actionsJson: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var triggerType by remember { mutableStateOf("VOICE") }
    var triggerValue by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PolishSurface,
        title = {
            Text("Create Automation", color = PolishTextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Workflow Name (e.g. Evening Chill)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PolishTextPrimary,
                        unfocusedTextColor = PolishTextPrimary,
                        focusedBorderColor = PolishPrimary,
                        unfocusedBorderColor = PolishBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PolishTextPrimary,
                        unfocusedTextColor = PolishTextPrimary,
                        focusedBorderColor = PolishPrimary,
                        unfocusedBorderColor = PolishBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = triggerValue,
                    onValueChange = { triggerValue = it },
                    label = { Text("Trigger (Phrase or Time: e.g. 21:00)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PolishTextPrimary,
                        unfocusedTextColor = PolishTextPrimary,
                        focusedBorderColor = PolishPrimary,
                        unfocusedBorderColor = PolishBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = condition,
                    onValueChange = { condition = it },
                    label = { Text("Condition (e.g. IF time > 20:00)") },
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
                    if (name.isNotBlank()) {
                        val actionsJson = """[{"type":"ENABLE_DND","params":{}},{"type":"SET_VOLUME","params":{"level":"30"}},{"type":"SET_BRIGHTNESS","params":{"level":"20"}}]"""
                        onCreate(name, description, triggerType, triggerValue, condition, actionsJson)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary, contentColor = Color.White)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = PolishTextSecondary)
            }
        }
    )
}

