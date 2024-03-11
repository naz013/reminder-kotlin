package com.elementary.tasks.reminder.build.bi

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class TimerExclusion(
  @SerializedName("hours")
  var hours: List<Int>,
  @SerializedName("from")
  var from: String,
  @SerializedName("to")
  var to: String
)

@Keep
data class CalendarDuration(
  @SerializedName("allDay")
  var allDay: Boolean,
  @SerializedName("millis")
  var millis: Long
)

@Keep
data class OtherParams(
  @SerializedName("useGlobal")
  var useGlobal: Boolean = true,
  @SerializedName("vibrate")
  var vibrate: Boolean = false,
  @SerializedName("notifyByVoice")
  var notifyByVoice: Boolean = false,
  @SerializedName("unlockScreen")
  var unlockScreen: Boolean = false,
  @SerializedName("repeatNotification")
  var repeatNotification: Boolean = false
)
