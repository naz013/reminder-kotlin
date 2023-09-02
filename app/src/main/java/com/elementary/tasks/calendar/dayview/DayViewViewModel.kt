package com.elementary.tasks.calendar.dayview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.calendar.CalendarDataProvider
import com.elementary.tasks.calendar.EventModel
import com.elementary.tasks.calendar.dayview.weekheader.WeekDay
import com.elementary.tasks.calendar.dayview.weekheader.WeekHeaderController
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.arch.OneWayLiveData
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate

class DayViewViewModel(
  private val eventControlFactory: EventControlFactory,
  private val calendarDataProvider: CalendarDataProvider,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val reminderDao: ReminderDao,
  private val birthdaysDao: BirthdaysDao,
  private val prefs: Prefs,
  private val weekHeaderController: WeekHeaderController
) : BaseProgressViewModel(dispatcherProvider), CalendarDataProvider.DataChangeObserver {

  private var _events: MutableLiveData<Pair<DayPagerItem, List<EventModel>>> = MutableLiveData()
  var events: LiveData<Pair<DayPagerItem, List<EventModel>>> = _events

  val week = OneWayLiveData<List<WeekDay>>()
  private var prevSearchParams: Pair<DayPagerItem, (List<EventModel>) -> Unit>? = null

  init {
    calendarDataProvider.observe(javaClass, this)
  }

  fun onDateSelected(date: LocalDate) {
    week.viewModelPost(weekHeaderController.calculateWeek(date))
  }

  fun findEvents(item: DayPagerItem, callback: (List<EventModel>) -> Unit) {
    prevSearchParams = Pair(item, callback)
    viewModelScope.launch(dispatcherProvider.default()) {
      val events = calendarDataProvider.getByDateRange(
        dateStart = item.date,
        dateEnd = item.date,
        reminderMode = calendarDataProvider.getReminderMode(
          prefs.isRemindersInCalendarEnabled,
          prefs.isFutureEventEnabled
        )
      )
      withContext(dispatcherProvider.main()) {
        callback(events)
      }
    }
  }

  fun deleteBirthday(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      birthdaysDao.delete(id)
      postInProgress(false)
      postCommand(Commands.DELETED)
      workerLauncher.startWork(BirthdayDeleteBackupWorker::class.java, Constants.INTENT_ID, id)
    }
  }

  fun moveToTrash(reminder: UiReminderListData) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val fromDb = reminderDao.getById(reminder.id)
      if (fromDb != null) {
        fromDb.isRemoved = true
        eventControlFactory.getController(fromDb).stop()
        reminderDao.insert(fromDb)
        postInProgress(false)
        postCommand(Commands.DELETED)
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          Constants.INTENT_ID,
          fromDb.uuId
        )
      } else {
        postCommand(Commands.FAILED)
      }
    }
  }

  fun skip(reminder: UiReminderListData) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val fromDb = reminderDao.getById(reminder.id)
      if (fromDb != null) {
        eventControlFactory.getController(fromDb).skip()
        postInProgress(false)
        postCommand(Commands.DELETED)
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          Constants.INTENT_ID,
          fromDb.uuId
        )
      } else {
        postCommand(Commands.FAILED)
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    calendarDataProvider.removeObserver(javaClass)
  }

  override fun onCalendarDataChanged() {
    prevSearchParams?.also {
      findEvents(it.first, it.second)
    }
  }
}
