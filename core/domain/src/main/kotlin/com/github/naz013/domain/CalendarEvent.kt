package com.github.naz013.domain

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class CalendarEvent(
  @SerializedName("reminderId")
  val reminderId: String,
  @SerializedName("event")
  val event: String,
  @SerializedName("eventId")
  val eventId: Long,
  @SerializedName("allDay")
  val allDay: Boolean,
  @SerializedName("uuId")
  var uuId: String = UUID.randomUUID().toString()
)
