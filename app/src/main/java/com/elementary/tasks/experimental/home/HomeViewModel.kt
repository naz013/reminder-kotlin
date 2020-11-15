package com.elementary.tasks.experimental.home

import android.content.Context
import androidx.lifecycle.LiveData
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.PrefsConstants
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.reminders.BaseRemindersViewModel
import timber.log.Timber

class HomeViewModel(
  appDb: AppDb,
  prefs: Prefs,
  calendarUtils: CalendarUtils,
  eventControlFactory: EventControlFactory,
  private val context: Context
) : BaseRemindersViewModel(appDb, prefs, calendarUtils, eventControlFactory), (String) -> Unit {

  private val _reminders = mutableLiveDataOf<List<Reminder>>()
  private var liveData: LiveData<List<Reminder>>? = null
  val reminders: LiveData<List<Reminder>> = _reminders
  val birthdays = appDb.birthdaysDao().findAll(
    TimeUtil.getBirthdayDayMonthList(duration = prefs.birthdayDurationInDays + 1)
  )

  init {
    prefs.addObserver(PrefsConstants.SHOW_PERMANENT_REMINDERS, this)
    initReminders()
  }

  private fun initReminders() {
    val remindersLiveData = if (prefs.showPermanentOnHome) {
      appDb.reminderDao().loadAllTypesInRangeWithPermanent(
        fromTime = TimeUtil.getDayStart(),
        toTime = TimeUtil.getDayEnd()
      )
    } else {
      appDb.reminderDao().loadAllTypesInRange(
        fromTime = TimeUtil.getDayStart(),
        toTime = TimeUtil.getDayEnd()
      )
    }
    remindersLiveData.observeForever {
      _reminders.postValue(it)
    }
    liveData = remindersLiveData
  }

  fun deleteBirthday(birthday: Birthday) {
    postInProgress(true)
    launchDefault {
      appDb.birthdaysDao().delete(birthday)
      updateBirthdayPermanent()
      startWork(BirthdayDeleteBackupWorker::class.java, Constants.INTENT_ID, birthday.uuId, context)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  private fun updateBirthdayPermanent() {
    if (prefs.isBirthdayPermanentEnabled) {
      Notifier.showBirthdayPermanent(context, prefs)
    }
  }

  override fun invoke(p1: String) {
    Timber.d("invoke: $p1")
    if (p1 == PrefsConstants.SHOW_PERMANENT_REMINDERS) {
      initReminders()
    }
  }
}
