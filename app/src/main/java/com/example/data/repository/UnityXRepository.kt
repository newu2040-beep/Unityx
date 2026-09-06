package com.example.data.repository

import com.example.data.UnityXDatabase
import com.example.data.entity.AssistantPreferenceEntity
import com.example.data.entity.ContextMemoryEntity
import com.example.data.entity.CustomCommandEntity
import com.example.data.entity.ExecutionLogEntity
import com.example.data.entity.WorkflowEntity
import kotlinx.coroutines.flow.Flow

class UnityXRepository(private val db: UnityXDatabase) {
    val workflows: Flow<List<WorkflowEntity>> = db.workflowDao().getAllWorkflows()
    val activeWorkflows: Flow<List<WorkflowEntity>> = db.workflowDao().getActiveWorkflows()
    val customCommands: Flow<List<CustomCommandEntity>> = db.customCommandDao().getAllCustomCommands()
    val executionLogs: Flow<List<ExecutionLogEntity>> = db.executionLogDao().getAllLogs()
    val preferences: Flow<AssistantPreferenceEntity?> = db.assistantPreferenceDao().getPreferences()

    suspend fun insertWorkflow(workflow: WorkflowEntity): Long = db.workflowDao().insertWorkflow(workflow)
    suspend fun updateWorkflow(workflow: WorkflowEntity) = db.workflowDao().updateWorkflow(workflow)
    suspend fun deleteWorkflow(workflow: WorkflowEntity) = db.workflowDao().deleteWorkflow(workflow)
    suspend fun deleteWorkflowById(id: Long) = db.workflowDao().deleteWorkflowById(id)

    suspend fun insertCustomCommand(command: CustomCommandEntity): Long = db.customCommandDao().insertCustomCommand(command)
    suspend fun updateCustomCommand(command: CustomCommandEntity) = db.customCommandDao().updateCustomCommand(command)
    suspend fun deleteCustomCommand(command: CustomCommandEntity) = db.customCommandDao().deleteCustomCommand(command)

    suspend fun insertLog(log: ExecutionLogEntity): Long = db.executionLogDao().insertLog(log)
    suspend fun clearLogs() = db.executionLogDao().clearAllLogs()

    suspend fun getMemory(key: String): String? = db.contextMemoryDao().getMemory(key)?.value
    suspend fun setMemory(key: String, value: String) = db.contextMemoryDao().setMemory(ContextMemoryEntity(key, value))

    suspend fun savePreferences(prefs: AssistantPreferenceEntity) = db.assistantPreferenceDao().savePreferences(prefs)
}
