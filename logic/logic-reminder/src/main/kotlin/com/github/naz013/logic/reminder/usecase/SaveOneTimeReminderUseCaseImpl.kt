package com.github.naz013.logic.reminder.usecase

import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.SaveOneTimeReminderUseCase
import org.threeten.bp.LocalDateTime

class SaveOneTimeReminderUseCaseImpl(
  private val activateReminderUseCase: ActivateReminderUseCase,
  private val dateTimeManager: DateTimeManager,
  private val appWidgetUpdater: AppWidgetUpdater,
) : SaveOneTimeReminderUseCase {

  override suspend fun invoke(
    uuId: String,
    summary: String?,
    dateTime: LocalDateTime
  ): String {
    val startDateTime = dateTimeManager.localToUtc(dateTime)
    val reminder = ReminderV2(
      uuId = uuId,
      summary = summary?.normalizeSummary() ?: "",
      schedule = ReminderSchedule(startDateTime = startDateTime, eventDateTime = startDateTime),
    )
    activateReminderUseCase(
      reminder = reminder,
      startAnyway = true,
      skipGoogleTaskExport = true,
    )
    appWidgetUpdater.updateScheduleWidget()
    return reminder.uuId
  }

  private fun String.normalizeSummary(): String =
    if (length > MAX_REMINDER_SUMMARY_LENGTH) {
      substring(0, MAX_REMINDER_SUMMARY_LENGTH)
    } else {
      this
    }

  companion object {
    private const val MAX_REMINDER_SUMMARY_LENGTH = 500
  }
}
