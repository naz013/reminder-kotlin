package com.github.naz013.appwidgets

import java.io.Serializable

internal data class WidgetIntentProtocol(
  val extra: Map<String, Any?>
) : Serializable

internal enum class Direction : Serializable {
  REMINDER_PREVIEW,
  ADD_REMINDER,
  BIRTHDAY_PREVIEW,
  ADD_BIRTHDAY,
  NOTE_PREVIEW,
  ADD_NOTE,
  GOOGLE_TASK,
  HOME
}
