package com.elementary.tasks.core.deeplink

import com.github.naz013.navigation.DeepLinkData
import kotlinx.parcelize.Parcelize
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

@Parcelize
data class ReminderDatetimeTypeDeepLinkData(
  val type: Int,
  val dateTime: LocalDateTime
) : DeepLinkData(IntentKey.REMINDER_DATETIME_TYPE)

@Parcelize
data object ReminderTodoTypeDeepLinkData : DeepLinkData(IntentKey.REMINDER_TODO_TYPE)

@Parcelize
data class ReminderTextDeepLinkData(
  val text: String
) : DeepLinkData(IntentKey.REMINDER_TEXT)

@Parcelize
data class BirthdayDateDeepLinkData(
  val date: LocalDate
) : DeepLinkData(IntentKey.BIRTHDAY_DATE)

@Parcelize
data class GoogleTaskDateTimeDeepLinkData(
  val date: LocalDate,
  val time: LocalTime?
) : DeepLinkData(IntentKey.GOOGLE_TASK_DATE_TIME)

// Name structure, first word: prefix, second and others: parameters
object IntentKey {
  const val REMINDER_DATETIME_TYPE = "reminder_datetime_type"
  const val REMINDER_TODO_TYPE = "reminder_todo_type"
  const val REMINDER_TEXT = "reminder_text"
  const val BIRTHDAY_DATE = "birthday_date"
  const val GOOGLE_TASK_DATE_TIME = "google_task_date_time"
}
