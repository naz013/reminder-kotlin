package com.elementary.tasks.core.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity
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
  @PrimaryKey
  var uuId: String = UUID.randomUUID().toString()
)
