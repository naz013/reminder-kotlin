package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.naz013.repository.entity.WorkflowTemplateEntity

@Dao
internal interface WorkflowTemplateDao {

  @Query("SELECT * FROM WorkflowTemplate")
  fun getAll(): List<WorkflowTemplateEntity>

  @Query("SELECT * FROM WorkflowTemplate WHERE category=:category")
  fun getByCategory(category: String): List<WorkflowTemplateEntity>

  @Query("SELECT * FROM WorkflowTemplate WHERE id=:id")
  fun getById(id: String): WorkflowTemplateEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(template: WorkflowTemplateEntity)

  @Query("DELETE FROM WorkflowTemplate WHERE id=:id")
  fun delete(id: String)

  @Query("DELETE FROM WorkflowTemplate")
  fun deleteAll()

  @Query("UPDATE WorkflowTemplate SET syncState=:state WHERE id=:id")
  fun updateSyncState(id: String, state: String)

  @Query("SELECT id FROM WorkflowTemplate WHERE syncState IN (:syncStates)")
  fun getBySyncStates(syncStates: List<String>): List<String>

  @Query("SELECT id FROM WorkflowTemplate")
  fun getAllIds(): List<String>

  @Query("SELECT COUNT(*) FROM WorkflowTemplate")
  fun countAll(): Int
}
