package com.github.naz013.domain

import com.github.naz013.domain.sync.SyncState
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReminderGroup(
  @SerializedName("title")
  val groupTitle: String,
  @SerializedName("uuId")
  val groupUuId: String,
  @SerializedName("color")
  val groupColor: Int,
  @SerializedName("dateTime")
  val groupDateTime: String,
  @SerializedName("isDefaultGroup")
  val isDefaultGroup: Boolean,
  @SerializedName("versionId")
  var version: Long = 0L,
  @Transient
  val syncState: SyncState,
) : Serializable
