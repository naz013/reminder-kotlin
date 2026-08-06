package com.github.naz013.repository.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.naz013.repository.converters.ListStringTypeConverter
import java.util.UUID

@Entity(
  tableName = "WorkflowTemplate",
  indices = [
    Index(value = ["category"]),
    Index(value = ["isBuiltIn"])
  ]
)
@TypeConverters(ListStringTypeConverter::class)
@Keep
internal data class WorkflowTemplateEntity(
  @PrimaryKey
  val id: String = UUID.randomUUID().toString(),
  val title: String = "",
  val description: String? = null,
  val category: String,
  val supportedScopeTypes: List<String> = emptyList(),

  val triggerType: String,
  val triggerPayload: String,
  val actionType: String,
  val actionPayload: String,

  val isBuiltIn: Boolean = true,
  val useCount: Int = 0,
  val createdAt: Long,

  val version: Long = 0L,
  val syncState: String
)
