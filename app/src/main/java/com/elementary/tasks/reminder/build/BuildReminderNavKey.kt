package com.elementary.tasks.reminder.build

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface BuildReminderNavKey : NavKey {
  @Serializable
  data object Main : BuildReminderNavKey

  @Serializable
  data object Configure : BuildReminderNavKey

  @Serializable
  data object Help : BuildReminderNavKey

  @Serializable
  data object RecurHelp : BuildReminderNavKey
}
