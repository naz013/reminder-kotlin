package com.elementary.tasks.reminder.build.preset.data

import com.google.gson.annotations.SerializedName

data class CalendarItemJson(
  @SerializedName("name")
  val name: String,
  @SerializedName("id")
  val id: Long,
)
