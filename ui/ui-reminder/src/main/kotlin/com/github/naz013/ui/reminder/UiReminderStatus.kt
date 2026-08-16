package com.github.naz013.ui.reminder

data class UiReminderStatus(
  val title: String,
  val active: Boolean,
  val removed: Boolean,
) {
  val canToggle: Boolean = !removed
  val canMakeAction: Boolean = canToggle && active
}
