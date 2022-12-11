package com.elementary.tasks.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.elementary.tasks.birthdays.list.BirthdayModelAdapter
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.PrefsConstants
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.core.view_models.reminders.BaseRemindersViewModel
import timber.log.Timber

class HomeViewModel(
  appDb: AppDb,
  private val currentStateHolder: CurrentStateHolder,
  calendarUtils: CalendarUtils,
  eventControlFactory: EventControlFactory,
  private val birthdayModelAdapter: BirthdayModelAdapter,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  updatesHelper: UpdatesHelper,
  private val birthdaysDao: BirthdaysDao
) : BaseRemindersViewModel(
  currentStateHolder.preferences,
  calendarUtils,
  eventControlFactory,
  dispatcherProvider,
  workManagerProvider,
  updatesHelper,
  appDb.reminderDao(),
  appDb.reminderGroupDao(),
  appDb.placesDao()
), (String) -> Unit {

  private val _reminders = mutableLiveDataOf<List<Reminder>>()
  private var liveData: LiveData<List<Reminder>>? = null
  val reminders: LiveData<List<Reminder>> = _reminders
  val birthdays = appDb.birthdaysDao().findAll(
    TimeUtil.getBirthdayDayMonthList(duration = prefs.birthdayDurationInDays + 1)
  ).map { list -> list.map { birthdayModelAdapter.convert(it) } }
  var topScrollX = 0

  init {
    prefs.addObserver(PrefsConstants.SHOW_PERMANENT_REMINDERS, this)
    initReminders()
  }

  private fun initReminders() {
    val remindersLiveData = if (prefs.showPermanentOnHome) {
      reminderDao.loadAllTypesInRangeWithPermanent(
        fromTime = TimeUtil.getDayStart(),
        toTime = TimeUtil.getDayEnd()
      )
    } else {
      reminderDao.loadAllTypesInRange(
        fromTime = TimeUtil.getDayStart(),
        toTime = TimeUtil.getDayEnd()
      )
    }
    remindersLiveData.observeForever {
      _reminders.postValue(it)
    }
    liveData = remindersLiveData
  }

  fun deleteBirthday(id: String) {
    postInProgress(true)
    launchDefault {
      birthdaysDao.delete(id)
      updateBirthdayPermanent()
      startWork(BirthdayDeleteBackupWorker::class.java, Constants.INTENT_ID, id)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  private fun updateBirthdayPermanent() {
    if (prefs.isBirthdayPermanentEnabled) {
      currentStateHolder.notifier.showBirthdayPermanent()
    }
  }

  override fun invoke(p1: String) {
    Timber.d("invoke: $p1")
    if (p1 == PrefsConstants.SHOW_PERMANENT_REMINDERS) {
      initReminders()
    }
  }
}
