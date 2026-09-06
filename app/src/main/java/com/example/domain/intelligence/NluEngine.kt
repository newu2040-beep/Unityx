package com.example.domain.intelligence

import com.example.domain.models.ActionStep
import com.example.domain.models.ActionType
import com.example.domain.models.RiskLevel
import com.example.domain.models.TaskPlan
import java.util.Locale
import java.util.regex.Pattern

class NluEngine {

    /**
     * Splits compound queries containing multiple instructions, such as:
     * "Turn off wifi, turn off bluetooth and set an alarm for 6:30"
     * "Call Rahul and tell him I'll arrive in 20 minutes"
     */
    fun splitCompoundQuery(rawText: String): List<String> {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) return emptyList()

        // Check special compound pattern: "Call X and tell/text him/her Y"
        val callAndTextPattern = Pattern.compile(
            "^(?:call|phone|ring)\\s+([a-zA-Z\\s]+?)\\s+and\\s+(?:tell|text|message)\\s+(?:him|her|them)\\s+(.+)$",
            Pattern.CASE_INSENSITIVE
        )
        val match = callAndTextPattern.matcher(trimmed)
        if (match.find()) {
            val contact = match.group(1)?.trim() ?: ""
            val message = match.group(2)?.trim() ?: ""
            return listOf("Text $contact $message", "Call $contact")
        }

        // General compound splitting on conjunctions and punctuation
        val splitRegex = Regex("""(?i)\s*(?:,\s*and\s+|\s+and\s+also\s+|\s+and\s+then\s+|\s*,\s*then\s+|\s+and\s+|\s*;\s*|\s*,\s*)(?=[a-zA-Z\u0900-\u097F])""")
        val parts = trimmed.split(splitRegex)
            .map { it.trim().trim(',', '.', ';', '!') }
            .filter { it.isNotEmpty() }

        return if (parts.isEmpty()) listOf(trimmed) else parts
    }

    /**
     * Parse a single atomic command query into an ActionStep
     */
    fun parseAtomicCommand(query: String, lastContact: String? = null): ActionStep? {
        val normalized = normalizeText(query)

        // 1. CALL CONTACT
        if (matchesCall(normalized)) {
            val recipient = extractContactName(normalized, "call") ?: lastContact ?: "Contact"
            return ActionStep(
                actionType = ActionType.CALL_CONTACT,
                title = "Call $recipient",
                description = "Initiating phone call to $recipient",
                params = mapOf("recipient" to recipient),
                riskLevel = RiskLevel.MEDIUM,
                requiredPermission = "android.permission.CALL_PHONE"
            )
        }

        // 2. SEND SMS / TEXT
        if (matchesSms(normalized)) {
            val (recipient, message) = extractSmsDetails(normalized, lastContact)
            return ActionStep(
                actionType = ActionType.SEND_SMS,
                title = "Send SMS to $recipient",
                description = "Message: \"$message\"",
                params = mapOf("recipient" to recipient, "message" to message),
                riskLevel = RiskLevel.MEDIUM,
                requiredPermission = "android.permission.SEND_SMS"
            )
        }

        // 3. SET TIMER
        if (matchesTimer(normalized)) {
            val minutes = extractTimerMinutes(normalized)
            return ActionStep(
                actionType = ActionType.SET_TIMER,
                title = "Set $minutes-Minute Timer",
                description = "Countdown timer for $minutes minutes",
                params = mapOf("durationMinutes" to minutes.toString()),
                riskLevel = RiskLevel.LOW
            )
        }

        // 4. SET ALARM
        if (matchesAlarm(normalized)) {
            val time = extractAlarmTime(normalized)
            return ActionStep(
                actionType = ActionType.SET_ALARM,
                title = "Set Alarm for $time",
                description = "Alarm set for $time",
                params = mapOf("time" to time),
                riskLevel = RiskLevel.LOW
            )
        }

        // 5. CREATE REMINDER / NOTE / TASK
        if (matchesReminder(normalized)) {
            val note = extractReminderContent(normalized)
            return ActionStep(
                actionType = ActionType.CREATE_REMINDER,
                title = "Create Reminder",
                description = "\"$note\"",
                params = mapOf("note" to note),
                riskLevel = RiskLevel.LOW
            )
        }

        // 6. WI-FI CONTROL
        if (normalized.contains("wifi") || normalized.contains("wi-fi") || normalized.contains("वाइफाइ")) {
            val enable = !normalized.contains("off") && !normalized.contains("disable") && !normalized.contains("band") && !normalized.contains("बन्द")
            return ActionStep(
                actionType = if (enable) ActionType.ENABLE_WIFI else ActionType.DISABLE_WIFI,
                title = if (enable) "Enable Wi-Fi" else "Disable Wi-Fi",
                description = "Toggle device Wi-Fi connectivity",
                params = mapOf("state" to if (enable) "on" else "off"),
                riskLevel = RiskLevel.LOW
            )
        }

        // 7. BLUETOOTH CONTROL
        if (normalized.contains("bluetooth") || normalized.contains("ब्लुटुथ")) {
            val enable = !normalized.contains("off") && !normalized.contains("disable") && !normalized.contains("band") && !normalized.contains("बन्द")
            return ActionStep(
                actionType = if (enable) ActionType.ENABLE_BLUETOOTH else ActionType.DISABLE_BLUETOOTH,
                title = if (enable) "Enable Bluetooth" else "Disable Bluetooth",
                description = "Toggle device Bluetooth radio",
                params = mapOf("state" to if (enable) "on" else "off"),
                riskLevel = RiskLevel.LOW
            )
        }

        // 8. DO NOT DISTURB (DND)
        if (normalized.contains("dnd") || normalized.contains("do not disturb") || normalized.contains("silent") || normalized.contains("मौन")) {
            val enable = !normalized.contains("off") && !normalized.contains("disable")
            return ActionStep(
                actionType = if (enable) ActionType.ENABLE_DND else ActionType.DISABLE_DND,
                title = if (enable) "Enable DND" else "Disable DND",
                description = if (enable) "Silence notifications and calls" else "Turn off Do Not Disturb",
                params = mapOf("state" to if (enable) "on" else "off"),
                riskLevel = RiskLevel.LOW
            )
        }

        // 9. VOLUME
        if (normalized.contains("volume") || normalized.contains("sound") || normalized.contains("आवाज")) {
            val level = extractNumber(normalized) ?: 50
            return ActionStep(
                actionType = ActionType.SET_VOLUME,
                title = "Set Volume to $level%",
                description = "Adjust media audio output level",
                params = mapOf("level" to level.toString()),
                riskLevel = RiskLevel.LOW
            )
        }

        // 10. BRIGHTNESS
        if (normalized.contains("brightness") || normalized.contains("उज्यालो") || normalized.contains("रोशनी")) {
            val level = extractNumber(normalized) ?: 40
            return ActionStep(
                actionType = ActionType.SET_BRIGHTNESS,
                title = "Set Brightness to $level%",
                description = "Adjust screen backlight intensity",
                params = mapOf("level" to level.toString()),
                riskLevel = RiskLevel.LOW
            )
        }

        // 11. MEDIA / MUSIC
        if (normalized.contains("play") || normalized.contains("music") || normalized.contains("song") || normalized.contains("गीत")) {
            if (normalized.contains("pause") || normalized.contains("stop")) {
                return ActionStep(
                    actionType = ActionType.PAUSE_MUSIC,
                    title = "Pause Music",
                    description = "Halt media playback",
                    riskLevel = RiskLevel.LOW
                )
            }
            if (normalized.contains("next") || normalized.contains("skip")) {
                return ActionStep(
                    actionType = ActionType.NEXT_TRACK,
                    title = "Next Track",
                    description = "Skip to next music track",
                    riskLevel = RiskLevel.LOW
                )
            }
            val querySong = normalized.replace(Regex("(?i)^(?:play|open|start)\\s+(?:music|song)?"), "").trim()
            return ActionStep(
                actionType = ActionType.PLAY_MUSIC,
                title = if (querySong.isNotEmpty()) "Play $querySong" else "Play Music",
                description = "Resume or start music playback",
                params = mapOf("query" to querySong),
                riskLevel = RiskLevel.LOW
            )
        }

        // 12. APPS (Open / Launch)
        if (normalized.startsWith("open ") || normalized.startsWith("launch ") || normalized.startsWith("start ") || normalized.contains("खोल्नुहोस्") || normalized.contains("खोलो")) {
            val appName = extractAppName(normalized)
            return ActionStep(
                actionType = ActionType.OPEN_APP,
                title = "Open $appName",
                description = "Launch $appName on device",
                params = mapOf("app" to appName),
                riskLevel = RiskLevel.LOW
            )
        }

        // 13. INFORMATION / BATTERY
        if (normalized.contains("battery") || normalized.contains("चार्ज") || normalized.contains("ब्याट्री")) {
            return ActionStep(
                actionType = ActionType.CHECK_BATTERY,
                title = "Check Battery Level",
                description = "Query device battery status and health",
                riskLevel = RiskLevel.LOW
            )
        }

        // 14. INFORMATION / STORAGE
        if (normalized.contains("storage") || normalized.contains("space") || normalized.contains("मेमोरी")) {
            return ActionStep(
                actionType = ActionType.CHECK_STORAGE,
                title = "Check Storage",
                description = "Query available internal device storage",
                riskLevel = RiskLevel.LOW
            )
        }

        // 15. TIME / DATE
        if (normalized.contains("what time") || normalized.contains("current time") || normalized.contains("समय")) {
            return ActionStep(
                actionType = ActionType.CHECK_TIME,
                title = "Check Current Time",
                description = "Query local system clock",
                riskLevel = RiskLevel.LOW
            )
        }
        if (normalized.contains("what date") || normalized.contains("today's date") || normalized.contains("मिति")) {
            return ActionStep(
                actionType = ActionType.CHECK_DATE,
                title = "Check Date",
                description = "Query calendar date",
                riskLevel = RiskLevel.LOW
            )
        }

        // 16. WORKFLOW TRIGGERS BY NAME
        if (normalized.contains("work mode") || normalized.contains("morning mode") || normalized.contains("sleep mode") || normalized.contains("battery saver")) {
            val wfName = when {
                normalized.contains("work") -> "Work Mode"
                normalized.contains("morning") -> "Morning Mode"
                normalized.contains("sleep") || normalized.contains("night") -> "Sleep Mode"
                else -> "Battery Saver"
            }
            return ActionStep(
                actionType = ActionType.RUN_WORKFLOW,
                title = "Trigger $wfName",
                description = "Execute multi-action routine: $wfName",
                params = mapOf("workflowName" to wfName),
                riskLevel = RiskLevel.LOW
            )
        }

        // Fallback: General task / search
        return ActionStep(
            actionType = ActionType.CUSTOM_ACTION,
            title = "Execute Command",
            description = "\"$query\"",
            params = mapOf("query" to query),
            riskLevel = RiskLevel.LOW
        )
    }

    private fun normalizeText(text: String): String {
        return text.trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("""[?,!.'"]"""), "")
            .replace(Regex("""\s+"""), " ")
    }

    private fun matchesCall(text: String): Boolean {
        return text.startsWith("call ") || text.startsWith("phone ") ||
                text.startsWith("ring ") || text.startsWith("give a call to ") ||
                text.contains("dial ") || text.contains("को फोन गर") ||
                text.contains("फोन गरिदेऊ") || text.contains("कॉल करो") || text.contains("कॉल लगाओ")
    }

    private fun extractContactName(text: String, actionPrefix: String): String? {
        val cleaned = text
            .replace(Regex("^(?:can you|please|could you|hey unityx)?\\s*(?:call|phone|ring|dial|give a call to)\\s+"), "")
            .replace(Regex("^(?:my|the)\\s+"), "")
            .replace(Regex("\\s+(?:on phone|now|immediately|please)$"), "")
            .trim()
        return if (cleaned.isNotEmpty()) cleaned.replaceFirstChar { it.uppercase() } else null
    }

    private fun matchesSms(text: String): Boolean {
        return text.startsWith("text ") || text.startsWith("send sms ") ||
                text.startsWith("send message ") || text.startsWith("message ") ||
                text.contains("मेसेज पठाऊ") || text.contains("सन्देश पठाऊ") || text.contains("मैसेज भेजो")
    }

    private fun extractSmsDetails(text: String, lastContact: String?): Pair<String, String> {
        // e.g. "text mom that i'll be late"
        // e.g. "send message to rahul saying I'll reach in 20 minutes"
        // e.g. "tell him I'll be late"
        var clean = text
            .replace(Regex("^(?:can you|please|could you)?\\s*(?:send\\s+)?(?:text|sms|message)\\s*(?:to)?\\s+"), "")
            .replace(Regex("^(?:tell|notify)\\s+"), "")

        // Resolve context pronoun: "him", "her", "them"
        if ((clean.startsWith("him ") || clean.startsWith("her ") || clean.startsWith("them ")) && lastContact != null) {
            val msg = clean.substringAfter(" ").replace(Regex("^(?:that|saying)?\\s*"), "").trim()
            return Pair(lastContact, msg.ifEmpty { "I'll be there soon" })
        }

        val delimiters = listOf(" saying ", " that ", ": ", " say ", " - ")
        for (d in delimiters) {
            if (clean.contains(d)) {
                val recipient = clean.substringBefore(d).trim().replaceFirstChar { it.uppercase() }
                val msg = clean.substringAfter(d).trim()
                return Pair(recipient, msg)
            }
        }

        val words = clean.split(" ")
        return if (words.size > 1) {
            val recipient = words[0].replaceFirstChar { it.uppercase() }
            val msg = words.drop(1).joinToString(" ")
            Pair(recipient, msg)
        } else {
            Pair(clean.replaceFirstChar { it.uppercase() }, "Hello")
        }
    }

    private fun matchesTimer(text: String): Boolean {
        return text.contains("timer") || text.contains("काउन्टडाउन") || text.contains("टाइमर")
    }

    private fun extractTimerMinutes(text: String): Int {
        val num = extractNumber(text) ?: 10
        if (text.contains("hour") || text.contains("घन्टा")) return num * 60
        return num
    }

    private fun matchesAlarm(text: String): Boolean {
        return text.contains("alarm") || text.contains("अलार्म")
    }

    private fun extractAlarmTime(text: String): String {
        // Match e.g. "6:30", "7 am", "8:15 pm", "8"
        val timeRegex = Regex("""(\d{1,2}(?::\d{2})?\s*(?:am|pm)?)""", RegexOption.IGNORE_CASE)
        val match = timeRegex.find(text)
        return match?.value?.uppercase(Locale.ROOT) ?: "07:00 AM"
    }

    private fun matchesReminder(text: String): Boolean {
        return text.startsWith("remind me") || text.startsWith("create reminder") ||
                text.startsWith("note ") || text.startsWith("create note") ||
                text.contains("सम्झना") || text.contains("याद दिलाओ")
    }

    private fun extractReminderContent(text: String): String {
        return text
            .replace(Regex("^(?:remind me to|remind me|create reminder to|create reminder for|note that|take a note)\\s+"), "")
            .trim()
            .ifEmpty { "Important task" }
    }

    private fun extractNumber(text: String): Int? {
        val match = Regex("""\b(\d+)\b""").find(text)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun extractAppName(text: String): String {
        val raw = text.replace(Regex("^(?:open|launch|start|खोल्नुहोस्|खोलो)\\s+"), "").trim()
        val name = raw.replace(Regex("\\s+app$"), "").trim()
        return name.replaceFirstChar { it.uppercase() }
    }
}
