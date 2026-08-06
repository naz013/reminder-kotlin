package com.github.naz013.repository.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
  tableName = "WorkflowRule",
  indices = [
    Index(value = ["scopeType", "scopeId"]),
    Index(value = ["triggerType"]),
    Index(value = ["isEnabled"])
  ]
)
@Keep
internal data class WorkflowRuleEntity(
  @PrimaryKey
  val uuId: String = UUID.randomUUID().toString(),
  val title: String = "",
  val templateId: String? = null,
  val scopeType: String,
  val scopeId: String? = null,

  val triggerType: String,
  val triggerPayload: String,
  val conditionsPayload: String = "[]",
  val actionType: String,
  val actionPayload: String,

  val isEnabled: Boolean = true,
  val createdAt: Long,
  val lastRunAt: Long? = null,

  val version: Long = 0L,
  val syncState: String
)
