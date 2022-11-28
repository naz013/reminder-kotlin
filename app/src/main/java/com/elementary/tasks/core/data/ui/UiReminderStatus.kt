package com.elementary.tasks.core.data.ui

data class UiReminderStatus(
  val title: String,
  val active: Boolean,
  val removed: Boolean
) {
  val canToggle: Boolean = !removed
  val canMakeAction: Boolean = canToggle && active
}
