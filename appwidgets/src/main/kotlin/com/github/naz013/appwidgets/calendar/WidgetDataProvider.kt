package com.github.naz013.appwidgets.calendar

import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.occurance.OccurrenceType
import com.github.naz013.feature.common.coroutine.invokeSuspend
import com.github.naz013.usecase.birthdays.GetAllBirthdaysUseCase
import com.github.naz013.usecase.reminders.GetActiveRemindersV2UseCase
import com.github.naz013.usecase.reminders.GetOccurrencesByDateRangeUseCase
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

/**
 * Reads pre-computed occurrence dates from the same model-agnostic `EventOccurrenceRepository`
 * table the app module's Calendar month view uses (via `CalculateReminderOccurrencesUseCase`,
 * still populated from V1 `Reminder` writes and already excluding GPS-type reminders at source)
 * instead of duplicating recurrence evaluation with its own `RecurrenceCalculator` pass — this
 * both migrates the widget to `ReminderV2` for its "still active" check and removes a second,
 * divergent occurrence-computation code path.
 */
internal class WidgetDataProvider(
  private val dateTimeManager: DateTimeManager,
  private val getOccurrencesByDateRangeUseCase: GetOccurrencesByDateRangeUseCase,
  private val getActiveRemindersV2UseCase: GetActiveRemindersV2UseCase,
  private val getAllBirthdaysUseCase: GetAllBirthdaysUseCase,
) {
  private val data: MutableList<Item> = ArrayList()
  private var birthdayTime: LocalTime = LocalTime.now()
  private var isFeature: Boolean = false

  enum class WidgetType {
    BIRTHDAY,
    REMINDER,
  }

  fun setTime(birthdayTime: LocalTime) {
    this.birthdayTime = birthdayTime
  }

  fun setFuture(isFeature: Boolean) {
    this.isFeature = isFeature
  }

  fun hasReminder(date: LocalDate): Boolean {
    var res = false
    for (item in data) {
      if (res) {
        break
      }
      res = item.date == date && item.type == WidgetType.REMINDER
    }
    return res
  }

  fun hasBirthday(
    day: Int,
    month: Int,
  ): Boolean {
    var res = false
    for (item in data) {
      if (item.date.dayOfMonth == day &&
        item.date.monthValue == month &&
        item.type == WidgetType.BIRTHDAY
      ) {
        res = true
        break
      }
    }
    return res
  }

  fun prepare() {
    data.clear()
    loadBirthdays()
    loadReminders()
  }

  private fun loadReminders() {
    val startDate = LocalDate.now()
    val endDate = startDate.plusDays(MAX_DAYS_COUNT)
    val activeIds = invokeSuspend { getActiveRemindersV2UseCase() }.mapTo(HashSet()) { it.uuId }
    val occurrences = invokeSuspend { getOccurrencesByDateRangeUseCase(startDate, endDate) }
      .filter { it.type == OccurrenceType.Reminder && it.eventId in activeIds }
      .sortedBy { it.date }

    if (isFeature) {
      occurrences.forEach { data.add(Item(it.date, WidgetType.REMINDER)) }
    } else {
      // Only the single next occurrence per reminder when future events are disabled, matching
      // the previous RecurrenceCalculator-based implementation's unconditional first-add.
      occurrences.distinctBy { it.eventId }.forEach { data.add(Item(it.date, WidgetType.REMINDER)) }
    }
  }

  private fun loadBirthdays() {
    val birthdays = invokeSuspend { getAllBirthdaysUseCase() }
    for (item in birthdays) {
      val date = dateTimeManager.parseBirthdayDate(item.date) ?: continue
      data.add(Item(date, WidgetType.BIRTHDAY))
    }
  }

  data class Item(
    val date: LocalDate,
    val type: WidgetType,
  )

  companion object {
    const val MAX_DAYS_COUNT: Long = 240
  }
}
