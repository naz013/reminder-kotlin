package com.elementary.tasks.core.services.action

/** A single extra action button shown on an alert notification. */
data class NotificationAction(
  val icon: Int,
  val label: String,
  val actionKey: String,
)
