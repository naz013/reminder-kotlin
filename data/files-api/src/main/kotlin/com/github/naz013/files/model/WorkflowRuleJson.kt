package com.github.naz013.files.model

import com.google.gson.annotations.SerializedName

data class WorkflowRuleJson(
  @SerializedName("schemaVersion")
  val schemaVersion: String = "v1.0",
  @SerializedName("uuId")
  val uuId: String,
  @SerializedName("title")
  val title: String = "",
  @SerializedName("templateId")
  val templateId: String? = null,
  @SerializedName("scopeType")
  val scopeType: String,
  @SerializedName("scopeId")
  val scopeId: String? = null,
  @SerializedName("triggerType")
  val triggerType: String,
  @SerializedName("triggerPayload")
  val triggerPayload: String,
  @SerializedName("conditionsPayload")
  val conditionsPayload: String = "[]",
  @SerializedName("actionType")
  val actionType: String,
  @SerializedName("actionPayload")
  val actionPayload: String,
  @SerializedName("isEnabled")
  val isEnabled: Boolean = true,
  @SerializedName("createdAt")
  val createdAt: String,
  @SerializedName("lastRunAt")
  val lastRunAt: String? = null,
  @SerializedName("versionId")
  val version: Long = 0L
)
