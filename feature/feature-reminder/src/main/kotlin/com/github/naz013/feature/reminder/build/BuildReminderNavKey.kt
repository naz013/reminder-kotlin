package com.github.naz013.feature.reminder.build

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
    val deepLinkQuickAdd: QuickAddDeepLink? = null,
  ) : BuildReminderNavKey {

    enum class DateTimeType {
      Date
    }

    /** Carries what quick-add (REM-1221) understood but couldn't save directly - either the
     * schedule was recognized but no title text was left, or the phrase was fully understood and
     * the user chose "continue in full editor" anyway. [startDateTimeMillis] round-trips through
     * [com.github.naz013.datecalc.DateTimeManager.toMillis]/`fromMillis` like [deepLinkDateTimeMillis]
     * already does. Only the [RecurrenceType] values quick-add's parser can actually produce are
     * modeled here - anything else falls back to [RecurrenceType.ONCE]. */
    @Serializable
    data class QuickAddDeepLink(
      val startDateTimeMillis: Long,
      val recurrenceType: RecurrenceType = RecurrenceType.ONCE,
      val interval: Long = 1,
      val weekdays: List<Int> = emptyList(),
      val dayOfMonth: Int = 0,
      val monthOfYear: Int = 0,
    ) {
      enum class RecurrenceType {
        ONCE,
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY,
      }
    }
  }

  @Serializable
  data object Help : BuildReminderNavKey

  @Serializable
  data object RecurHelp : BuildReminderNavKey

  @Serializable
  data object SelectApplication : BuildReminderNavKey
}
