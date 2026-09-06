package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workflows")
data class WorkflowEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val triggerType: String, // VOICE, TIME, BATTERY, HEADPHONES, MANUAL
    val triggerValue: String, // e.g. "08:00" or "battery < 20" or "Good morning"
    val condition: String? = null,
    val actionsJson: String, // JSON array of action definitions
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_commands")
data class CustomCommandEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val triggerPhrases: String, // Comma separated or newline separated phrases
    val actionType: String,
    val parametersJson: String, // e.g. {"app":"Spotify","volume":"50"}
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "execution_logs")
data class ExecutionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val triggerSource: String, // VOICE, AUTOMATION, QUICK_ACTION, MANUAL
    val actionsCount: Int,
    val status: String, // SUCCESS, CANCELLED, FAILED, CONFIRMATION_REJECTED
    val details: String,
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "context_memory")
data class ContextMemoryEntity(
    @PrimaryKey val key: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "assistant_preferences")
data class AssistantPreferenceEntity(
    @PrimaryKey val id: Int = 1,
    val userName: String = "User",
    val language: String = "en", // "en", "hi", "ne"
    val voiceResponseEnabled: Boolean = true,
    val wakeWordEnabled: Boolean = true,
    val confirmationMode: String = "SMART", // ALWAYS, SMART, MINIMAL
    val isOfflineModeOnly: Boolean = true
)
