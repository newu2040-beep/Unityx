package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AssistantPreferenceDao
import com.example.data.dao.ContextMemoryDao
import com.example.data.dao.CustomCommandDao
import com.example.data.dao.ExecutionLogDao
import com.example.data.dao.WorkflowDao
import com.example.data.entity.AssistantPreferenceEntity
import com.example.data.entity.ContextMemoryEntity
import com.example.data.entity.CustomCommandEntity
import com.example.data.entity.ExecutionLogEntity
import com.example.data.entity.WorkflowEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        WorkflowEntity::class,
        CustomCommandEntity::class,
        ExecutionLogEntity::class,
        ContextMemoryEntity::class,
        AssistantPreferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class UnityXDatabase : RoomDatabase() {
    abstract fun workflowDao(): WorkflowDao
    abstract fun customCommandDao(): CustomCommandDao
    abstract fun executionLogDao(): ExecutionLogDao
    abstract fun contextMemoryDao(): ContextMemoryDao
    abstract fun assistantPreferenceDao(): AssistantPreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: UnityXDatabase? = null

        fun getInstance(context: Context): UnityXDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UnityXDatabase::class.java,
                    "unityx_database.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial data asynchronously
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            seedInitialData(database)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: UnityXDatabase) {
            // Seed Assistant Preferences
            db.assistantPreferenceDao().savePreferences(
                AssistantPreferenceEntity(
                    id = 1,
                    userName = "Rahul",
                    language = "en",
                    voiceResponseEnabled = true,
                    wakeWordEnabled = true,
                    confirmationMode = "SMART",
                    isOfflineModeOnly = true
                )
            )

            // Pre-populate Default Workflows as requested in PRD
            val morningActions = """[{"type":"ENABLE_WIFI","params":{}},{"type":"SET_VOLUME","params":{"level":"40"}},{"type":"OPEN_APP","params":{"app":"Spotify"}},{"type":"CREATE_REMINDER","params":{"note":"Leave for work","time":"08:30"}}]"""
            db.workflowDao().insertWorkflow(
                WorkflowEntity(
                    name = "Morning Mode",
                    description = "Turn on Wi-Fi, set volume to 40%, open Spotify, and schedule morning commute reminder.",
                    triggerType = "TIME",
                    triggerValue = "08:00 AM",
                    condition = "Every day at 8:00 AM",
                    actionsJson = morningActions,
                    isEnabled = true
                )
            )

            val workActions = """[{"type":"ENABLE_DND","params":{}},{"type":"SET_BRIGHTNESS","params":{"level":"35"}},{"type":"ENABLE_WIFI","params":{}},{"type":"OPEN_APP","params":{"app":"Gmail"}},{"type":"SET_TIMER","params":{"durationMinutes":"45","label":"Deep Work Sprint"}}]"""
            db.workflowDao().insertWorkflow(
                WorkflowEntity(
                    name = "Work Mode",
                    description = "Enable DND, dim screen to 35%, open Gmail, and start a 45-minute focus sprint timer.",
                    triggerType = "VOICE",
                    triggerValue = "Work Mode",
                    condition = "Voice Trigger or Quick Action",
                    actionsJson = workActions,
                    isEnabled = true
                )
            )

            val sleepActions = """[{"type":"ENABLE_DND","params":{}},{"type":"SET_BRIGHTNESS","params":{"level":"5"}},{"type":"DISABLE_BLUETOOTH","params":{}},{"type":"DISABLE_WIFI","params":{}},{"type":"SET_ALARM","params":{"time":"07:00","message":"Morning Wakeup"}}]"""
            db.workflowDao().insertWorkflow(
                WorkflowEntity(
                    name = "Sleep Mode",
                    description = "Silence phone, reduce brightness to 5%, turn off radios, and set 7:00 AM alarm.",
                    triggerType = "TIME",
                    triggerValue = "23:00",
                    condition = "IF time = 23:00 or 'Good Night'",
                    actionsJson = sleepActions,
                    isEnabled = true
                )
            )

            val batteryActions = """[{"type":"DISABLE_WIFI","params":{}},{"type":"DISABLE_BLUETOOTH","params":{}},{"type":"SET_BRIGHTNESS","params":{"level":"15"}},{"type":"CREATE_NOTE","params":{"note":"Battery low triggered at "}}]"""
            db.workflowDao().insertWorkflow(
                WorkflowEntity(
                    name = "Battery Saver",
                    description = "When battery drops below 20%, turn off radios and minimize brightness.",
                    triggerType = "BATTERY",
                    triggerValue = "Battery < 20%",
                    condition = "IF battery < 20%",
                    actionsJson = batteryActions,
                    isEnabled = true
                )
            )

            val travelActions = """[{"type":"ENABLE_WIFI","params":{}},{"type":"SET_VOLUME","params":{"level":"80"}},{"type":"OPEN_APP","params":{"app":"Maps"}}]"""
            db.workflowDao().insertWorkflow(
                WorkflowEntity(
                    name = "Travel Mode",
                    description = "Boost media volume and open navigation maps.",
                    triggerType = "VOICE",
                    triggerValue = "Travel Mode",
                    condition = "Manual or Voice trigger",
                    actionsJson = travelActions,
                    isEnabled = false
                )
            )

            // Pre-populate Custom Commands
            db.customCommandDao().insertCustomCommand(
                CustomCommandEntity(
                    name = "Good Morning",
                    triggerPhrases = "good morning, start my day, morning mode",
                    actionType = "RUN_WORKFLOW",
                    parametersJson = """{"workflowName":"Morning Mode"}""",
                    isEnabled = true
                )
            )

            db.customCommandDao().insertCustomCommand(
                CustomCommandEntity(
                    name = "Focus Sprint",
                    triggerPhrases = "focus sprint, pomodoro, study time",
                    actionType = "SET_TIMER",
                    parametersJson = """{"durationMinutes":"25","label":"Study Sprint"}""",
                    isEnabled = true
                )
            )

            db.customCommandDao().insertCustomCommand(
                CustomCommandEntity(
                    name = "Going to Sleep",
                    triggerPhrases = "going to sleep, good night, bedtime",
                    actionType = "RUN_WORKFLOW",
                    parametersJson = """{"workflowName":"Sleep Mode"}""",
                    isEnabled = true
                )
            )

            // Seed initial execution log
            db.executionLogDao().insertLog(
                ExecutionLogEntity(
                    title = "UNITYX Initialized",
                    triggerSource = "SYSTEM",
                    actionsCount = 1,
                    status = "SUCCESS",
                    details = "Offline Intelligence Engine ready. Speech recognizer and action registry active.",
                    durationMs = 45,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 15
                )
            )
        }
    }
}
