package com.elementary.tasks.reminder.build

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface BuildReminderNavKey : NavKey {
  @Serializable
  data class Main(
    val id: String = "",
    val fromIntentItem: Boolean = false,
    val deepLinkDateTimeType: Int? = null,
    val deepLinkDateTimeMillis: Long? = null,
    val deepLinkTodo: Boolean = false,
    val deepLinkText: String? = null,
  ) : BuildReminderNavKey

  @Serializable
  data object Configure : BuildReminderNavKey

  @Serializable
  data object Help : BuildReminderNavKey

  @Serializable
  data object RecurHelp : BuildReminderNavKey

  @Serializable
  data object SelectApplication : BuildReminderNavKey
}
