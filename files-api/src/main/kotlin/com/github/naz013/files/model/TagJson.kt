package com.github.naz013.files.model

import com.google.gson.annotations.SerializedName

data class TagJson(
  @SerializedName("schemaVersion")
  val schemaVersion: String = "v1.0",
  @SerializedName("id")
  val id: String,
  @SerializedName("name")
  val name: String = "",
  @SerializedName("color")
  val color: Int = 0,
  @SerializedName("versionId")
  val version: Long = 0L
)
