package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.naz013.repository.entity.WorkflowRuleEntity

@Dao
internal interface WorkflowRuleDao {

  @Query("SELECT * FROM WorkflowRule")
  fun getAll(): List<WorkflowRuleEntity>

  @Query("SELECT * FROM WorkflowRule WHERE isEnabled=1")
  fun getEnabled(): List<WorkflowRuleEntity>

  @Query("SELECT * FROM WorkflowRule WHERE uuId=:id")
  fun getById(id: String): WorkflowRuleEntity?

  @Query("SELECT * FROM WorkflowRule WHERE scopeType=:scopeType AND (scopeId=:scopeId OR (:scopeId IS NULL AND scopeId IS NULL))")
  fun getByScope(scopeType: String, scopeId: String?): List<WorkflowRuleEntity>

  @Query("SELECT * FROM WorkflowRule WHERE triggerType=:triggerType")
  fun getByTriggerType(triggerType: String): List<WorkflowRuleEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(rule: WorkflowRuleEntity)

  @Query("DELETE FROM WorkflowRule WHERE uuId=:id")
  fun delete(id: String)

  @Query("DELETE FROM WorkflowRule")
  fun deleteAll()

  @Query("UPDATE WorkflowRule SET syncState=:state WHERE uuId=:id")
  fun updateSyncState(id: String, state: String)

  @Query("SELECT uuId FROM WorkflowRule WHERE syncState IN (:syncStates)")
  fun getBySyncStates(syncStates: List<String>): List<String>

  @Query("SELECT uuId FROM WorkflowRule")
  fun getAllIds(): List<String>

  @Query("SELECT COUNT(*) FROM WorkflowRule")
  fun countAll(): Int
}
