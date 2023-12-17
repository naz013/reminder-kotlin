package com.elementary.tasks.core.deeplink

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

sealed class DeepLinkData(
  val intentKey: IntentKey
) : Parcelable

@Parcelize
data class ReminderDatetimeTypeDeepLinkData(
  val type: Int,
  val dateTime: LocalDateTime
) : DeepLinkData(IntentKey.REMINDER_DATETIME_TYPE)

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
enum class IntentKey(val value: String) {
  REMINDER_DATETIME_TYPE("reminder_datetime_type"),
  BIRTHDAY_DATE("birthday_date"),
  GOOGLE_TASK_DATE_TIME("google_task_date_time")
}
