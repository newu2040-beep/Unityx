package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.AssistantPreferenceEntity
import com.example.data.entity.ContextMemoryEntity
import com.example.data.entity.CustomCommandEntity
import com.example.data.entity.ExecutionLogEntity
import com.example.data.entity.WorkflowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkflowDao {
    @Query("SELECT * FROM workflows ORDER BY id ASC")
    fun getAllWorkflows(): Flow<List<WorkflowEntity>>

    @Query("SELECT * FROM workflows WHERE isEnabled = 1")
    fun getActiveWorkflows(): Flow<List<WorkflowEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkflow(workflow: WorkflowEntity): Long

    @Update
    suspend fun updateWorkflow(workflow: WorkflowEntity)

    @Delete
    suspend fun deleteWorkflow(workflow: WorkflowEntity)

    @Query("DELETE FROM workflows WHERE id = :id")
    suspend fun deleteWorkflowById(id: Long)
}

@Dao
interface CustomCommandDao {
    @Query("SELECT * FROM custom_commands ORDER BY id ASC")
    fun getAllCustomCommands(): Flow<List<CustomCommandEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomCommand(command: CustomCommandEntity): Long

    @Update
    suspend fun updateCustomCommand(command: CustomCommandEntity)

    @Delete
    suspend fun deleteCustomCommand(command: CustomCommandEntity)
}

@Dao
interface ExecutionLogDao {
    @Query("SELECT * FROM execution_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllLogs(): Flow<List<ExecutionLogEntity>>

    @Query("SELECT COUNT(*) FROM execution_logs WHERE timestamp >= :sinceTimestamp")
    fun getLogsCountSince(sinceTimestamp: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ExecutionLogEntity): Long

    @Query("DELETE FROM execution_logs")
    suspend fun clearAllLogs()
}

@Dao
interface ContextMemoryDao {
    @Query("SELECT * FROM context_memory WHERE `key` = :key LIMIT 1")
    suspend fun getMemory(key: String): ContextMemoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setMemory(memory: ContextMemoryEntity)

    @Query("DELETE FROM context_memory WHERE `key` = :key")
    suspend fun deleteMemory(key: String)

    @Query("DELETE FROM context_memory")
    suspend fun clearMemory()
}

@Dao
interface AssistantPreferenceDao {
    @Query("SELECT * FROM assistant_preferences WHERE id = 1 LIMIT 1")
    fun getPreferences(): Flow<AssistantPreferenceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreferences(preferences: AssistantPreferenceEntity)
}
