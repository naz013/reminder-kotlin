package com.github.naz013.logic.demodata

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.ShopItemV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.logic.birthday.SaveBirthdayUseCase
import com.github.naz013.logic.note.InsertDemoNotesUseCase
import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
import com.github.naz013.repository.GroupV2Repository
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Seeds a fresh install with a small set of showcase content - a few reminders, one birthday,
 * and a few notes - via the same use cases a real user action would go through, so the content
 * is genuinely executable (scheduled alarms, occurrence calculation, sync) rather than the
 * preview-only `.save()` shortcuts the Developer Settings debug tools historically used.
 */
class InsertDemoDataUseCase(
  private val activateReminderUseCase: ActivateReminderUseCase,
  private val saveBirthdayUseCase: SaveBirthdayUseCase,
  private val insertDemoNotesUseCase: InsertDemoNotesUseCase,
  private val groupV2Repository: GroupV2Repository,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke() {
    runCatching { insertDemoReminders() }
      .onFailure { Logger.e(TAG, "Failed to insert demo reminders", it) }
    runCatching { insertDemoBirthday() }
      .onFailure { Logger.e(TAG, "Failed to insert demo birthday", it) }
    runCatching { insertDemoNotesUseCase() }
      .onFailure { Logger.e(TAG, "Failed to insert demo notes", it) }
  }

  private suspend fun insertDemoReminders() {
    val now = LocalDateTime.now()
    val groupId = groupV2Repository.defaultGroup()?.uuId
    val startDateTime = dateTimeManager.getCurrentDateTime()

    fun schedule(dateTime: LocalDateTime) =
      ReminderSchedule(startDateTime = startDateTime, eventDateTime = dateTimeManager.localToUtc(dateTime))

    val nextNineAm = if (now.toLocalTime().isBefore(LocalTime.of(9, 0))) {
      now.toLocalDate()
    } else {
      now.toLocalDate().plusDays(1)
    }

    val reminders =
      listOf(
        ReminderV2(
          summary = "Team standup meeting",
          schedule = schedule(LocalDateTime.of(nextNineAm, LocalTime.of(9, 0))),
          groupId = groupId,
        ),
        ReminderV2(
          summary = "Weekly grocery shopping",
          schedule = schedule(now.plusHours(SHOPPING_REMINDER_HOURS_AHEAD)),
          action = ReminderAction.Shopping,
          shoppingItems =
            listOf(
              ShopItemV2(summary = "Milk", isChecked = false, createdAt = now),
              ShopItemV2(summary = "Fresh vegetables", isChecked = false, createdAt = now),
              ShopItemV2(summary = "Coffee beans", isChecked = false, createdAt = now),
              ShopItemV2(summary = "Birthday candles", isChecked = true, createdAt = now),
            ),
          groupId = groupId,
        ),
        ReminderV2(
          summary = "Call a friend",
          schedule = schedule(now.plusDays(CALL_REMINDER_DAYS_AHEAD)),
          action = ReminderAction.Call(target = "+1234567890"),
          groupId = groupId,
        ),
        ReminderV2(
          summary = "Daily vitamins",
          schedule = schedule(now.plusHours(VITAMINS_REMINDER_HOURS_AHEAD)),
          recurrence = RecurrenceRule.Daily(repeatInterval = 24 * 60 * 60 * 1000L),
          groupId = groupId,
        ),
      )
    reminders.forEach { activateReminderUseCase(it, startAnyway = true) }
  }

  private suspend fun insertDemoBirthday() {
    val today = LocalDate.now()
    val upcoming = today.plusDays(BIRTHDAY_DAYS_AHEAD)
    val date = LocalDate.of(BIRTHDAY_BIRTH_YEAR, upcoming.monthValue, upcoming.dayOfMonth)
    saveBirthdayUseCase(
      Birthday(
        name = "Mom",
        date = dateTimeManager.formatBirthdayDate(date),
        day = date.dayOfMonth,
        month = date.monthValue - 1,
        dayMonth = "${date.dayOfMonth}|${date.monthValue - 1}",
        syncState = SyncState.Synced,
      ),
    )
  }

  companion object {
    private const val TAG = "InsertDemoDataUseCase"
    private const val SHOPPING_REMINDER_HOURS_AHEAD = 3L
    private const val CALL_REMINDER_DAYS_AHEAD = 2L
    private const val VITAMINS_REMINDER_HOURS_AHEAD = 12L
    private const val BIRTHDAY_DAYS_AHEAD = 3L
    private const val BIRTHDAY_BIRTH_YEAR = 1962
  }
}
