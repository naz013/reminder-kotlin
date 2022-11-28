package com.elementary.tasks.core.data.ui

sealed class UiReminderTarget

data class UiSmsTarget(
  val summary: String,
  val target: String
) : UiReminderTarget()

data class UiCallTarget(val target: String) : UiReminderTarget()

data class UiAppTarget(val target: String) : UiReminderTarget()

data class UiLinkTarget(val target: String) : UiReminderTarget()

data class UiEmailTarget(
  val summary: String,
  val target: String,
  val subject: String,
  val attachmentFile: String
) : UiReminderTarget()
