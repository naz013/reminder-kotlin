package com.elementary.tasks.core.view_models.reminders

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.analytics.AnalyticsEventSender
import com.elementary.tasks.core.analytics.Feature
import com.elementary.tasks.core.analytics.FeatureUsedEvent
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.contacts.ContactsReader
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import timber.log.Timber

class FollowReminderViewModel(
  private val reminderDao: ReminderDao,
  private val reminderGroupDao: ReminderGroupDao,
  private val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val dateTimeManager: DateTimeManager,
  private val featureManager: FeatureManager,
  private val gTasks: GTasks,
  private val contactsReader: ContactsReader,
  private val analyticsEventSender: AnalyticsEventSender
) : BaseProgressViewModel(dispatcherProvider) {

  private val _contactInfo = mutableLiveDataOf<String>()
  val contactInfo = _contactInfo.toLiveData()

  private val _contactPhoto = mutableLiveDataOf<Uri>()
  val contactPhoto = _contactPhoto.toLiveData()

  private val _state = mutableLiveDataOf<TimeState>()
  val state = _state.toLiveData()

  var customDate: LocalDate = LocalDate.now()
  var customTime: LocalTime = LocalTime.now()

  private var number: String = ""
  private var defGroup: ReminderGroup? = null
  private var initDateTime: LocalDateTime = LocalDateTime.now()
  var nextWorkDateTime: LocalDateTime = LocalDateTime.now()
  var tomorrowDateTime: LocalDateTime = LocalDateTime.now()

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderGroupDao.defaultGroup(true)?.also {
        defGroup = it
      }
    }
  }

  fun getState(): TimeState {
    return state.value ?: TimeState.CUSTOM
  }

  fun onNewState(state: TimeState) {
    _state.postValue(state)
  }

  fun getAfterDateTime(minutes: Int): LocalDateTime {
    return LocalDateTime.now().plusMinutes(minutes.toLong())
  }

  fun getCustomDateTime(): LocalDateTime {
    return LocalDateTime.of(customDate, customTime)
  }

  fun initDateTime(millis: Long) {
    this.initDateTime = dateTimeManager.fromMillis(millis)
  }

  fun initNextBusinessDateTime(): String {
    nextWorkDateTime = when (initDateTime.dayOfWeek) {
      DayOfWeek.FRIDAY -> initDateTime.plusDays(3)
      DayOfWeek.SATURDAY -> initDateTime.plusDays(2)
      else -> initDateTime.plusDays(1)
    }
    return dateTimeManager.getDateTime(nextWorkDateTime)
  }

  fun initTomorrowDateTime(): String {
    tomorrowDateTime = initDateTime.plusDays(1)
    return dateTimeManager.getDateTime(tomorrowDateTime)
  }

  fun updateCustomDate(localDate: LocalDate): String {
    this.customDate = localDate
    return dateTimeManager.getDate(localDate)
  }

  fun updateCustomTime(localTime: LocalTime): String {
    this.customTime = localTime
    return dateTimeManager.getTime(localTime)
  }

  fun saveDateTask(
    text: String,
    type: Int,
    due: LocalDateTime,
    exportToTasks: Boolean,
    exportToCalendar: Boolean
  ) {
    val reminder = Reminder()
    val def = defGroup
    if (def != null) {
      reminder.groupUuId = def.groupUuId
    }
    reminder.eventTime = dateTimeManager.getGmtFromDateTime(due)
    reminder.startTime = dateTimeManager.getGmtFromDateTime(due)
    reminder.type = type
    reminder.summary = text
    reminder.target = number
    reminder.exportToTasks = exportToTasks
    reminder.exportToCalendar = exportToCalendar
    saveAndStartReminder(reminder)
  }

  fun onNumberReceived(number: String) {
    this.number = number
    viewModelScope.launch(dispatcherProvider.default()) {
      contactsReader.getNameFromNumber(number)?.let {
        _contactInfo.postValue("$it\n$number")
      } ?: run {
        _contactInfo.postValue(number)
      }
      contactsReader.getIdFromNumber(number).takeIf { it != 0L }
        ?.let { contactsReader.getPhoto(it) }
        ?.let { _contactPhoto.postValue(it) }
    }
  }

  fun canExportToTasks(): Boolean {
    return featureManager.isFeatureEnabled(FeatureManager.Feature.GOOGLE_TASKS) && gTasks.isLogged
  }

  private fun saveAndStartReminder(reminder: Reminder) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      runBlocking {
        Timber.d("saveAndStartReminder: save START")
        if (reminder.groupUuId == "") {
          val group = reminderGroupDao.defaultGroup()
          if (group != null) {
            reminder.groupColor = group.groupColor
            reminder.groupTitle = group.groupTitle
            reminder.groupUuId = group.groupUuId
          }
        }
        reminderDao.insert(reminder)
        eventControlFactory.getController(reminder).start()
        Timber.d("saveAndStartReminder: save DONE")
      }
      workerLauncher.startWork(ReminderSingleBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
      postInProgress(false)
      analyticsEventSender.send(FeatureUsedEvent(Feature.AFTER_CALL))
      postCommand(Commands.SAVED)
    }
  }

  enum class TimeState {
    CUSTOM,
    TOMORROW,
    NEXT_BUSINESS,
    AFTER
  }
}
