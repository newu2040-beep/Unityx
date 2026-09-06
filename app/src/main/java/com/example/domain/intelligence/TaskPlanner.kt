package com.example.domain.intelligence

import com.example.domain.models.ActionStep
import com.example.domain.models.ActionType
import com.example.domain.models.RiskLevel
import com.example.domain.models.TaskPlan

class TaskPlanner(private val nluEngine: NluEngine = NluEngine()) {

    fun planTask(rawQuery: String, lastContact: String? = null): TaskPlan {
        val atomicQueries = nluEngine.splitCompoundQuery(rawQuery)
        val steps = mutableListOf<ActionStep>()
        var currentContact = lastContact

        for (query in atomicQueries) {
            val step = nluEngine.parseAtomicCommand(query, currentContact)
            if (step != null) {
                steps.add(step)
                if (step.actionType == ActionType.CALL_CONTACT || step.actionType == ActionType.SEND_SMS) {
                    val recipient = step.params["recipient"]
                    if (!recipient.isNullOrBlank()) {
                        currentContact = recipient
                    }
                }
            }
        }

        val naturalResponse = generatePlanSummary(steps)
        val highestRisk = steps.maxOfOrNull { it.riskLevel } ?: RiskLevel.LOW
        val requiresConfirmation = steps.any { it.riskLevel != RiskLevel.LOW }

        return TaskPlan(
            originalQuery = rawQuery,
            steps = steps,
            isCompound = steps.size > 1,
            naturalResponse = naturalResponse,
            riskLevel = highestRisk,
            requiresConfirmation = requiresConfirmation
        )
    }

    private fun generatePlanSummary(steps: List<ActionStep>): String {
        if (steps.isEmpty()) return "I'm not sure how to handle that request yet."
        if (steps.size == 1) {
            val step = steps.first()
            return when (step.actionType) {
                ActionType.CALL_CONTACT -> "Calling ${step.params["recipient"] ?: "contact"}."
                ActionType.SEND_SMS -> "Sending SMS to ${step.params["recipient"] ?: "contact"}."
                ActionType.SET_TIMER -> "Starting a ${step.params["durationMinutes"] ?: "10"} minute timer."
                ActionType.SET_ALARM -> "Setting alarm for ${step.params["time"] ?: "7:00 AM"}."
                ActionType.CREATE_REMINDER -> "Creating reminder for: ${step.params["note"] ?: "task"}."
                ActionType.ENABLE_WIFI -> "Enabling Wi-Fi."
                ActionType.DISABLE_WIFI -> "Disabling Wi-Fi."
                ActionType.ENABLE_BLUETOOTH -> "Enabling Bluetooth."
                ActionType.DISABLE_BLUETOOTH -> "Disabling Bluetooth."
                ActionType.ENABLE_DND -> "Enabling Do Not Disturb."
                ActionType.DISABLE_DND -> "Disabling Do Not Disturb."
                ActionType.SET_VOLUME -> "Setting volume to ${step.params["level"] ?: "50"}%."
                ActionType.SET_BRIGHTNESS -> "Setting brightness to ${step.params["level"] ?: "40"}%."
                ActionType.PLAY_MUSIC -> "Playing music."
                ActionType.PAUSE_MUSIC -> "Pausing music playback."
                ActionType.OPEN_APP -> "Opening ${step.params["app"] ?: "app"}."
                ActionType.CHECK_BATTERY -> "Checking battery status."
                ActionType.CHECK_STORAGE -> "Checking available device storage."
                ActionType.CHECK_TIME -> "Checking current system time."
                ActionType.CHECK_DATE -> "Checking today's date."
                ActionType.RUN_WORKFLOW -> "Executing automation: ${step.params["workflowName"] ?: "routine"}."
                else -> "Executing ${step.title}."
            }
        }

        // Multi-step compound summary
        val stepTitles = steps.joinToString(", ") { it.title }
        return "${steps.size} actions queued: $stepTitles."
    }
}
