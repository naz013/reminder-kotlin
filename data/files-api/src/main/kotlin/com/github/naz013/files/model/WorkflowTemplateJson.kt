package com.github.naz013.files.model

import com.google.gson.annotations.SerializedName

data class WorkflowTemplateJson(
  @SerializedName("schemaVersion")
  val schemaVersion: String = "v1.0",
  @SerializedName("id")
  val id: String,
  @SerializedName("title")
  val title: String = "",
  @SerializedName("description")
  val description: String? = null,
  @SerializedName("category")
  val category: String,
  @SerializedName("supportedScopeTypes")
  val supportedScopeTypes: List<String> = emptyList(),
  @SerializedName("triggerType")
  val triggerType: String,
  @SerializedName("triggerPayload")
  val triggerPayload: String,
  @SerializedName("actionType")
  val actionType: String,
  @SerializedName("actionPayload")
  val actionPayload: String,
  @SerializedName("isBuiltIn")
  val isBuiltIn: Boolean = true,
  @SerializedName("useCount")
  val useCount: Int = 0,
  @SerializedName("createdAt")
  val createdAt: String,
  @SerializedName("versionId")
  val version: Long = 0L
)
