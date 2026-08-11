package com.elementary.tasks.reminder.build

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface BuildReminderNavKey : NavKey {
  @Serializable
  data class Main(
    val id: String = "",
    val fromIntentItem: Boolean = false,
    val deepLinkDateTimeType: DateTimeType? = null,
    val deepLinkDateTimeMillis: Long? = null,
    val deepLinkTodo: Boolean = false,
    val deepLinkText: String? = null,
    val groupUuId: String? = null,
    val seedFromTodoEdit: Boolean = false,
    val isEditingExtend: Boolean = false,
  ) : BuildReminderNavKey {

    enum class DateTimeType {
      Date
    }
  }

  @Serializable
  data object Help : BuildReminderNavKey

  @Serializable
  data object RecurHelp : BuildReminderNavKey

  @Serializable
  data object SelectApplication : BuildReminderNavKey
}
