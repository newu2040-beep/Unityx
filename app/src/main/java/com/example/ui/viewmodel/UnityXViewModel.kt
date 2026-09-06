package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.UnityXDatabase
import com.example.data.entity.AssistantPreferenceEntity
import com.example.data.entity.CustomCommandEntity
import com.example.data.entity.ExecutionLogEntity
import com.example.data.entity.WorkflowEntity
import com.example.data.repository.UnityXRepository
import com.example.domain.intelligence.ActionExecutor
import com.example.domain.intelligence.NluEngine
import com.example.domain.intelligence.TaskPlanner
import com.example.domain.models.ActionStep
import com.example.domain.models.ActionType
import com.example.domain.models.ExecutionResult
import com.example.domain.models.RiskLevel
import com.example.domain.models.StepStatus
import com.example.domain.models.TaskPlan
import com.example.integration.PermissionItem
import com.example.integration.PermissionManager
import com.example.voice.SpeechManager
import com.example.voice.TtsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class PrivacyMetrics(
    val micInteractionsToday: Int = 0,
    val contactsLookupsToday: Int = 0,
    val messagesSentToday: Int = 0,
    val locationRequestsToday: Int = 0,
    val cloudRequestsToday: Int = 0 // Always 0 for true offline architecture
)

class UnityXViewModel(application: Application) : AndroidViewModel(application) {

    private val db = UnityXDatabase.getInstance(application)
    private val repository = UnityXRepository(db)
    private val nluEngine = NluEngine()
    private val taskPlanner = TaskPlanner(nluEngine)
    private val actionExecutor = ActionExecutor(application, repository)
    private val speechManager = SpeechManager(application)
    private val ttsManager = TtsManager(application)
    private val permissionManager = PermissionManager(application)

    // Data streams from Room
    val workflows: StateFlow<List<WorkflowEntity>> = repository.workflows
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customCommands: StateFlow<List<CustomCommandEntity>> = repository.customCommands
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val executionLogs: StateFlow<List<ExecutionLogEntity>> = repository.executionLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val preferences: StateFlow<AssistantPreferenceEntity?> = repository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // UI State
    private val _queryInput = MutableStateFlow("")
    val queryInput: StateFlow<String> = _queryInput.asStateFlow()

    private val _activePlan = MutableStateFlow<TaskPlan?>(null)
    val activePlan: StateFlow<TaskPlan?> = _activePlan.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    private val _lastExecutionMessage = MutableStateFlow<String?>(null)
    val lastExecutionMessage: StateFlow<String?> = _lastExecutionMessage.asStateFlow()

    private val _isEmergencyStopped = MutableStateFlow(false)
    val isEmergencyStopped: StateFlow<Boolean> = _isEmergencyStopped.asStateFlow()

    // Floating Bubble Overlay Service State
    val isFloatingBubbleRunning: StateFlow<Boolean> = com.example.voice.FloatingBubbleService.isRunning
    val isFloatingBubbleHidden: StateFlow<Boolean> = com.example.voice.FloatingBubbleService.isHidden
    val isFloatingBubblePaused: StateFlow<Boolean> = com.example.voice.FloatingBubbleService.isPaused
    val isFloatingBubbleListening: StateFlow<Boolean> = com.example.voice.FloatingBubbleService.isListening

    fun canDrawOverlays(): Boolean = permissionManager.canDrawOverlays()

    fun toggleFloatingBubble() {
        val app = getApplication<Application>()
        if (com.example.voice.FloatingBubbleService.isRunning.value) {
            com.example.voice.FloatingBubbleService.stop(app)
            _lastExecutionMessage.value = "Floating circle assistant stopped."
        } else {
            if (!permissionManager.canDrawOverlays()) {
                _lastExecutionMessage.value = "Please grant 'Display over other apps' to enable floating circle."
                return
            }
            com.example.voice.FloatingBubbleService.start(app)
            _lastExecutionMessage.value = "Floating circle assistant started."
        }
    }

    fun startFloatingBubble() {
        val app = getApplication<Application>()
        if (!permissionManager.canDrawOverlays()) {
            _lastExecutionMessage.value = "Please grant 'Display over other apps' to enable floating circle."
            return
        }
        com.example.voice.FloatingBubbleService.start(app)
        _lastExecutionMessage.value = "Floating circle assistant activated."
    }

    fun stopFloatingBubble() {
        val app = getApplication<Application>()
        com.example.voice.FloatingBubbleService.stop(app)
        _lastExecutionMessage.value = "Floating circle assistant stopped."
    }

    fun toggleFloatingBubbleVisibility() {
        val app = getApplication<Application>()
        com.example.voice.FloatingBubbleService.toggleVisibility(app)
    }

    fun toggleFloatingBubblePause() {
        val app = getApplication<Application>()
        com.example.voice.FloatingBubbleService.togglePause(app)
    }

    private val _permissionsList = MutableStateFlow<List<PermissionItem>>(emptyList())
    val permissionsList: StateFlow<List<PermissionItem>> = _permissionsList.asStateFlow()

    private val prefs = application.getSharedPreferences("unityx_app_prefs", android.content.Context.MODE_PRIVATE)
    private val _isIntroCompleted = MutableStateFlow(
        prefs.getBoolean("intro_completed", false) || permissionManager.areAllCorePermissionsGranted()
    )
    val isIntroCompleted: StateFlow<Boolean> = _isIntroCompleted.asStateFlow()

    fun completeIntro() {
        prefs.edit().putBoolean("intro_completed", true).apply()
        _isIntroCompleted.value = true
    }

    fun resetIntro() {
        prefs.edit().putBoolean("intro_completed", false).apply()
        _isIntroCompleted.value = false
    }

    fun getCoreRuntimePermissions(): Array<String> {
        return permissionManager.getCoreRuntimePermissions()
    }

    fun areAllCorePermissionsGranted(): Boolean {
        return permissionManager.areAllCorePermissionsGranted()
    }

    private val _privacyMetrics = MutableStateFlow(PrivacyMetrics())
    val privacyMetrics: StateFlow<PrivacyMetrics> = _privacyMetrics.asStateFlow()

    val isListening: StateFlow<Boolean> = speechManager.isListening
    val rmsDb: StateFlow<Float> = speechManager.rmsDb
    val speechError: StateFlow<String?> = speechManager.error

    private var executionJob: Job? = null

    init {
        refreshPermissions()

        speechManager.setOnResultListener { transcript ->
            _queryInput.value = transcript
            _privacyMetrics.value = _privacyMetrics.value.copy(
                micInteractionsToday = _privacyMetrics.value.micInteractionsToday + 1
            )
            processQuery(transcript)
        }
    }

    fun onQueryChange(text: String) {
        _queryInput.value = text
    }

    fun refreshPermissions() {
        _permissionsList.value = permissionManager.getPermissionsList()
        if (permissionManager.areAllCorePermissionsGranted() && !_isIntroCompleted.value) {
            completeIntro()
        }
    }

    fun toggleListening() {
        if (speechManager.isListening.value) {
            speechManager.stopListening()
        } else {
            speechManager.startListening()
        }
    }

    fun submitManualQuery() {
        val query = _queryInput.value.trim()
        if (query.isNotEmpty()) {
            processQuery(query)
        }
    }

    private fun processQuery(rawQuery: String) {
        if (_isEmergencyStopped.value) return

        viewModelScope.launch {
            // Check custom commands first
            val commands = customCommands.value
            val matchedCmd = commands.firstOrNull { cmd ->
                cmd.isEnabled && cmd.triggerPhrases.split(",").any { phrase ->
                    rawQuery.trim().equals(phrase.trim(), ignoreCase = true)
                }
            }

            if (matchedCmd != null) {
                if (matchedCmd.actionType == "RUN_WORKFLOW") {
                    val wfName = try {
                        JSONObject(matchedCmd.parametersJson).optString("workflowName", "")
                    } catch (e: Exception) { "" }
                    val wf = workflows.value.firstOrNull { it.name.equals(wfName, ignoreCase = true) }
                    if (wf != null) {
                        runWorkflow(wf)
                        return@launch
                    }
                }
            }

            val lastContact = repository.getMemory("last_contact")
            val plan = taskPlanner.planTask(rawQuery, lastContact)
            _activePlan.value = plan

            // If low risk and no confirmation required, execute immediately
            if (!plan.requiresConfirmation) {
                executePlan(plan)
            } else {
                // Keep active plan visible for user confirmation
                speakResponse("I've prepared ${plan.steps.size} action${if (plan.steps.size > 1) "s" else ""}. Please confirm.")
            }
        }
    }

    fun confirmAndExecute() {
        val plan = _activePlan.value ?: return
        executePlan(plan)
    }

    fun cancelActivePlan() {
        _activePlan.value = null
        _lastExecutionMessage.value = "Action cancelled by user."
    }

    private fun executePlan(plan: TaskPlan) {
        executionJob?.cancel()
        executionJob = viewModelScope.launch {
            _isExecuting.value = true
            val startTime = System.currentTimeMillis()
            var allSuccess = true
            val executedResults = mutableListOf<String>()

            for (step in plan.steps) {
                if (_isEmergencyStopped.value) {
                    step.status = StepStatus.CANCELLED
                    allSuccess = false
                    break
                }

                val result = actionExecutor.executeStep(step)
                if (result.isSuccess) {
                    executedResults.add(result.message)
                    // Update context memory if communication
                    val recipient = step.params["recipient"]
                    if (!recipient.isNullOrBlank()) {
                        repository.setMemory("last_contact", recipient)
                        _privacyMetrics.value = _privacyMetrics.value.copy(
                            contactsLookupsToday = _privacyMetrics.value.contactsLookupsToday + 1,
                            messagesSentToday = if (step.actionType == ActionType.SEND_SMS) _privacyMetrics.value.messagesSentToday + 1 else _privacyMetrics.value.messagesSentToday
                        )
                    }
                } else {
                    allSuccess = false
                    executedResults.add(result.error ?: "Failed: ${step.title}")
                }
            }

            val duration = System.currentTimeMillis() - startTime
            val finalStatus = if (_isEmergencyStopped.value) "CANCELLED" else if (allSuccess) "SUCCESS" else "FAILED"
            val summaryDetails = executedResults.joinToString(" | ")

            // Log to execution history in Room
            repository.insertLog(
                ExecutionLogEntity(
                    title = plan.originalQuery.ifEmpty { plan.steps.firstOrNull()?.title ?: "Command" },
                    triggerSource = "VOICE_ACTION",
                    actionsCount = plan.steps.size,
                    status = finalStatus,
                    details = summaryDetails,
                    durationMs = duration
                )
            )

            _isExecuting.value = false
            val responseText = if (allSuccess) {
                if (plan.steps.size == 1) executedResults.firstOrNull() ?: "Done."
                else "Completed ${plan.steps.size} actions successfully."
            } else {
                "Some actions could not be finished."
            }

            _lastExecutionMessage.value = responseText
            speakResponse(responseText)
        }
    }

    fun runWorkflow(workflow: WorkflowEntity) {
        if (_isEmergencyStopped.value) return

        viewModelScope.launch {
            _isExecuting.value = true
            val startTime = System.currentTimeMillis()
            val steps = parseWorkflowActions(workflow.actionsJson)

            val plan = TaskPlan(
                originalQuery = "Run ${workflow.name}",
                steps = steps,
                naturalResponse = "Executing ${workflow.name}",
                riskLevel = RiskLevel.LOW,
                requiresConfirmation = false
            )
            _activePlan.value = plan

            executePlan(plan)
        }
    }

    private fun parseWorkflowActions(actionsJson: String): List<ActionStep> {
        val steps = mutableListOf<ActionStep>()
        try {
            val arr = JSONArray(actionsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val typeName = obj.optString("type", "CUSTOM_ACTION")
                val paramsObj = obj.optJSONObject("params")
                val params = mutableMapOf<String, String>()
                paramsObj?.keys()?.forEach { k ->
                    params[k] = paramsObj.getString(k)
                }

                val actionType = try {
                    ActionType.valueOf(typeName)
                } catch (e: Exception) {
                    ActionType.CUSTOM_ACTION
                }

                steps.add(
                    ActionStep(
                        actionType = actionType,
                        title = actionType.displayName,
                        description = "Workflow Step ${i + 1}: ${actionType.displayName}",
                        params = params,
                        riskLevel = actionType.defaultRisk
                    )
                )
            }
        } catch (e: Exception) {
            steps.add(
                ActionStep(
                    actionType = ActionType.CUSTOM_ACTION,
                    title = "Custom Routine",
                    description = "Execute routine sequence",
                    riskLevel = RiskLevel.LOW
                )
            )
        }
        return steps
    }

    fun toggleWorkflow(workflow: WorkflowEntity) {
        viewModelScope.launch {
            repository.updateWorkflow(workflow.copy(isEnabled = !workflow.isEnabled))
        }
    }

    fun deleteWorkflow(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkflowById(id)
        }
    }

    fun createWorkflow(
        name: String,
        description: String,
        triggerType: String,
        triggerValue: String,
        condition: String,
        actionsJson: String
    ) {
        viewModelScope.launch {
            repository.insertWorkflow(
                WorkflowEntity(
                    name = name,
                    description = description,
                    triggerType = triggerType,
                    triggerValue = triggerValue,
                    condition = condition,
                    actionsJson = actionsJson,
                    isEnabled = true
                )
            )
            _lastExecutionMessage.value = "Workflow \"$name\" created."
            speakResponse("Workflow created successfully.")
        }
    }

    fun createCustomCommand(name: String, phrases: String, actionType: String, paramsJson: String) {
        viewModelScope.launch {
            repository.insertCustomCommand(
                CustomCommandEntity(
                    name = name,
                    triggerPhrases = phrases,
                    actionType = actionType,
                    parametersJson = paramsJson,
                    isEnabled = true
                )
            )
            _lastExecutionMessage.value = "Custom command \"$name\" registered."
        }
    }

    fun deleteCustomCommand(command: CustomCommandEntity) {
        viewModelScope.launch {
            repository.deleteCustomCommand(command)
        }
    }

    fun triggerEmergencyStop() {
        _isEmergencyStopped.value = true
        executionJob?.cancel()
        _isExecuting.value = false
        speechManager.stopListening()
        ttsManager.stop()
        com.example.voice.FloatingBubbleService.stop(getApplication())

        viewModelScope.launch {
            repository.insertLog(
                ExecutionLogEntity(
                    title = "EMERGENCY STOP TRIGGERED",
                    triggerSource = "USER_EMERGENCY_BUTTON",
                    actionsCount = 0,
                    status = "CANCELLED",
                    details = "All active workflows and commands immediately halted by user override.",
                    durationMs = 0
                )
            )
            _lastExecutionMessage.value = "EMERGENCY STOP: All tasks and automations cancelled."
        }
    }

    fun resetEmergencyStop() {
        _isEmergencyStopped.value = false
        _lastExecutionMessage.value = "Emergency stop cleared. UNITYX is ready."
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun updatePreferences(prefs: AssistantPreferenceEntity) {
        viewModelScope.launch {
            repository.savePreferences(prefs)
        }
    }

    private fun speakResponse(text: String) {
        val pref = preferences.value
        if (pref?.voiceResponseEnabled != false && !_isEmergencyStopped.value) {
            ttsManager.speak(text)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.stopListening()
        ttsManager.release()
    }
}
