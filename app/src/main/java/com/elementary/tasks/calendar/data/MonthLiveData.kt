package com.elementary.tasks.calendar.data

import androidx.lifecycle.LiveData
import com.elementary.tasks.calendar.occurrence.GetOccurrencesByDateRangeUseCase
import com.elementary.tasks.core.calendar.EventsCursor
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.ContextProvider
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.occurance.EventOccurrence
import com.github.naz013.domain.occurance.OccurrenceType
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.ui.common.theme.ThemeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate

class MonthLiveData(
  private val context: ContextProvider,
  private val dispatcherProvider: DispatcherProvider,
  private val prefs: Prefs,
  private val getOccurrencesByDateRangeUseCase: GetOccurrencesByDateRangeUseCase,
  private val birthdayRepository: BirthdayRepository,
  private val reminderRepository: ReminderRepository,
) : LiveData<Map<LocalDate, EventsCursor>>() {

  private val scope: CoroutineScope = CoroutineScope(Job())
  private var lastDate: LocalDate? = null

  fun onDateChanged(date: LocalDate) {
    this.lastDate = date
    loadData(date)
  }

  override fun onActive() {
    super.onActive()
    lastDate?.let {
      loadData(it)
    }
  }

  private fun loadData(date: LocalDate) {
    val startOfTheMonth = date.withDayOfMonth(1)
    val endOfTheMonth = date.withDayOfMonth(date.lengthOfMonth())
    scope.launch(dispatcherProvider.default()) {
      val occurrences = getOccurrencesByDateRangeUseCase(
        startDate = startOfTheMonth,
        endDate = endOfTheMonth
      )
      val birthdays = birthdayRepository.getAll().associateBy { it.uuId }
      val reminders = reminderRepository.getActive().associateBy { it.uuId }
      val mappedData = mapData(occurrences, birthdays, reminders)
      Logger.d(TAG, "Mapped data for $date: ${mappedData.size} days with events")

      withContext(dispatcherProvider.main()) {
        value = mappedData
      }
    }
  }

  private fun mapData(
    list: List<EventOccurrence>,
    birthdaysMap: Map<String, Birthday>,
    remindersMap: Map<String, Reminder>
  ): Map<LocalDate, EventsCursor> {
    val birthdayColor = birthdayColor()
    val reminderColor = reminderColor()

    val map = mutableMapOf<LocalDate, EventsCursor>()
    for (model in list) {
      when (model.type) {
        OccurrenceType.Birthday -> {
          val birthday = birthdaysMap[model.eventId] ?: continue
          setEvent(
            model.date,
            birthday.name,
            birthdayColor,
            EventsCursor.Type.BIRTHDAY,
            map
          )
        }

        OccurrenceType.Reminder -> {
          val reminder = remindersMap[model.eventId] ?: continue
          setEvent(
            model.date,
            reminder.summary,
            reminderColor,
            EventsCursor.Type.REMINDER,
            map
          )
        }

        OccurrenceType.CalendarEvent -> {
          // TODO: Add calendar event handling
        }
      }
    }
    return map
  }

  private fun setEvent(
    date: LocalDate,
    summary: String,
    color: Int,
    type: EventsCursor.Type,
    map: MutableMap<LocalDate, EventsCursor>
  ) {
    if (map.containsKey(date)) {
      val eventsCursor = map[date] ?: EventsCursor()
      eventsCursor.addEvent(summary, color, type, date)
      map[date] = eventsCursor
    } else {
      val eventsCursor = EventsCursor(summary, color, type, date)
      map[date] = eventsCursor
    }
  }

  private fun birthdayColor(): Int {
    return ThemeProvider.colorBirthdayCalendar(context.themedContext, prefs.birthdayLedColor)
  }

  private fun reminderColor(): Int {
    return ThemeProvider.colorReminderCalendar(context.themedContext, prefs.reminderColor)
  }

  companion object {
    private const val TAG = "MonthLiveData"
  }
}
