package com.elementary.tasks.calendar.data

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import com.elementary.tasks.calendar.occurrence.GetOccurrencesByDayUseCase
import com.elementary.tasks.core.data.adapter.UiReminderListAdapter
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayListAdapter
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.occurance.OccurrenceType
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class DayLiveData(
  private val dispatcherProvider: DispatcherProvider,
  private val getOccurrencesByDayUseCase: GetOccurrencesByDayUseCase,
  private val birthdayRepository: BirthdayRepository,
  private val reminderRepository: ReminderRepository,
  private val uiBirthdayListAdapter: UiBirthdayListAdapter,
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val dateTimeManager: DateTimeManager
) : LiveData<List<EventModel>>() {

  private val scope: CoroutineScope = CoroutineScope(Job())
  private var lastDate: LocalDate? = null

  @MainThread
  fun onDateChanged(date: LocalDate) {
    this.lastDate = date
    loadData(date)
  }

  override fun onActive() {
    super.onActive()
    lastDate?.also { loadData(it) }
  }

  private fun loadData(date: LocalDate) {
    scope.launch(dispatcherProvider.default()) {
      val occurrences = getOccurrencesByDayUseCase(date)
      val birthdays = birthdayRepository.getAll().associateBy { it.uuId }
      val reminders = reminderRepository.getActive().associateBy { it.uuId }
      val mappedData = occurrences.mapNotNull {
        when (it.type) {
          OccurrenceType.Birthday -> {
            val birthday = birthdays[it.eventId] ?: return@mapNotNull null
            val dateTime = LocalDateTime.of(it.date, it.time)
            uiBirthdayListAdapter.convert(
              birthday = birthday,
              nowDateTime = dateTime
            ).toEventModel(LocalDateTime.of(it.date, it.time))
          }
          OccurrenceType.Reminder -> {
            val reminder = reminders[it.eventId] ?: return@mapNotNull null
            reminder.eventTime = dateTimeManager.getGmtFromDateTime(LocalDateTime.of(it.date, it.time))
            uiReminderListAdapter.create(reminder).toEventModel(LocalDateTime.of(it.date, it.time))
          }
          else -> null
        }
      }

      Logger.d(TAG, "Mapped data for $date: ${mappedData.size} events")
      launch(dispatcherProvider.main()) {
        value = mappedData
      }
    }
  }

  private fun UiReminderListData.toEventModel(dateTime: LocalDateTime): ReminderEventModel {
    return ReminderEventModel(
      model = this,
      day = dateTime.dayOfMonth,
      monthValue = dateTime.monthValue,
      year = dateTime.year
    )
  }

  private fun UiBirthdayList.toEventModel(localDateTime: LocalDateTime): BirthdayEventModel {
    return BirthdayEventModel(
      model = this,
      day = localDateTime.dayOfMonth,
      monthValue = localDateTime.monthValue,
      year = localDateTime.year
    )
  }

  companion object {
    private const val TAG = "DayLiveData"
  }
}
