package com.example.domain.models

import java.util.UUID

enum class ActionType(val category: String, val displayName: String, val defaultRisk: RiskLevel) {
    // Communication
    CALL_CONTACT("Communication", "Call Contact", RiskLevel.MEDIUM),
    SEND_SMS("Communication", "Send SMS", RiskLevel.MEDIUM),
    SEND_EMAIL("Communication", "Send Email", RiskLevel.MEDIUM),
    OPEN_CHAT("Communication", "Open Chat", RiskLevel.LOW),

    // Device
    ENABLE_WIFI("Device", "Enable Wi-Fi", RiskLevel.LOW),
    DISABLE_WIFI("Device", "Disable Wi-Fi", RiskLevel.LOW),
    ENABLE_BLUETOOTH("Device", "Enable Bluetooth", RiskLevel.LOW),
    DISABLE_BLUETOOTH("Device", "Disable Bluetooth", RiskLevel.LOW),
    ENABLE_DND("Device", "Enable Do Not Disturb", RiskLevel.LOW),
    DISABLE_DND("Device", "Disable Do Not Disturb", RiskLevel.LOW),
    SET_VOLUME("Device", "Set Volume", RiskLevel.LOW),
    SET_BRIGHTNESS("Device", "Set Brightness", RiskLevel.LOW),
    LOCK_DEVICE("Device", "Lock Device", RiskLevel.MEDIUM),
    TAKE_SCREENSHOT("Device", "Take Screenshot", RiskLevel.LOW),

    // Media
    PLAY_MUSIC("Media", "Play Music", RiskLevel.LOW),
    PAUSE_MUSIC("Media", "Pause Music", RiskLevel.LOW),
    NEXT_TRACK("Media", "Next Track", RiskLevel.LOW),
    PREVIOUS_TRACK("Media", "Previous Track", RiskLevel.LOW),

    // Applications
    OPEN_APP("Applications", "Open Application", RiskLevel.LOW),
    SEARCH_APP("Applications", "Search in App", RiskLevel.LOW),
    OPEN_SETTINGS("Applications", "Open System Settings", RiskLevel.LOW),

    // Productivity
    CREATE_NOTE("Productivity", "Create Note", RiskLevel.LOW),
    CREATE_REMINDER("Productivity", "Create Reminder", RiskLevel.LOW),
    CREATE_TASK("Productivity", "Create Task", RiskLevel.LOW),
    SET_ALARM("Productivity", "Set Alarm", RiskLevel.LOW),
    SET_TIMER("Productivity", "Set Timer", RiskLevel.LOW),
    CREATE_CALENDAR_EVENT("Productivity", "Calendar Event", RiskLevel.MEDIUM),

    // Information
    CHECK_BATTERY("Information", "Check Battery", RiskLevel.LOW),
    CHECK_STORAGE("Information", "Check Storage", RiskLevel.LOW),
    CHECK_TIME("Information", "Check Time", RiskLevel.LOW),
    CHECK_DATE("Information", "Check Date", RiskLevel.LOW),
    READ_NOTIFICATIONS("Information", "Read Notifications", RiskLevel.LOW),

    // Automation
    RUN_WORKFLOW("Automation", "Run Automation", RiskLevel.LOW),
    CREATE_WORKFLOW("Automation", "Create Automation", RiskLevel.MEDIUM),

    // Fallback
    CUSTOM_ACTION("Custom", "Custom Action", RiskLevel.LOW)
}

enum class RiskLevel {
    LOW,      // Executes immediately
    MEDIUM,   // Needs user confirmation (e.g. SMS, Call, Calendar)
    HIGH      // High risk (e.g. deleting data, critical device changes)
}

enum class StepStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class ActionStep(
    val id: String = UUID.randomUUID().toString(),
    val actionType: ActionType,
    val title: String,
    val description: String,
    val params: Map<String, String> = emptyMap(),
    val riskLevel: RiskLevel = actionType.defaultRisk,
    val requiredPermission: String? = null,
    var status: StepStatus = StepStatus.PENDING,
    var resultMessage: String? = null
)

data class TaskPlan(
    val id: String = UUID.randomUUID().toString(),
    val originalQuery: String,
    val steps: List<ActionStep>,
    val isCompound: Boolean = steps.size > 1,
    val naturalResponse: String,
    val riskLevel: RiskLevel = steps.maxOfOrNull { it.riskLevel } ?: RiskLevel.LOW,
    val requiresConfirmation: Boolean = steps.any { it.riskLevel != RiskLevel.LOW }
)

data class ExecutionResult(
    val isSuccess: Boolean,
    val message: String,
    val executedStepsCount: Int,
    val error: String? = null
)
