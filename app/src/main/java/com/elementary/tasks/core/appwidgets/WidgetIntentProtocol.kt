package com.elementary.tasks.core.appwidgets

import java.io.Serializable

data class WidgetIntentProtocol(
  val extra: Map<String, Any?>
) : Serializable

enum class Direction : Serializable {
  REMINDER,
  BIRTHDAY,
  NOTE,
  GOOGLE_TASK
}
