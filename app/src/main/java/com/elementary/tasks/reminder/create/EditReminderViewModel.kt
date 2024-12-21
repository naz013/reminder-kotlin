package com.elementary.tasks.reminder.create

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.analytics.ReminderAnalyticsTracker
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.observeTable
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.reminder.work.ReminderDeleteBackupWorker
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.logging.Logger
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.repository.observer.TableChangeListenerFactory
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Deprecated("Replaced by new Builder")
class EditReminderViewModel(
  id: String,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val reminderGroupRepository: ReminderGroupRepository,
  private val reminderRepository: ReminderRepository,
  private val placeRepository: PlaceRepository,
  private val analyticsEventSender: AnalyticsEventSender,
  private val reminderAnalyticsTracker: ReminderAnalyticsTracker,
  private val tableChangeListenerFactory: TableChangeListenerFactory
) : BaseProgressViewModel(dispatcherProvider) {

  private val _googleTask = mutableLiveDataOf<Pair<GoogleTaskList?, GoogleTask?>>()
  val googleTask = _googleTask.toLiveData()

  val reminder = viewModelScope.observeTable(
    table = Table.Reminder,
    tableChangeListenerFactory = tableChangeListenerFactory,
    queryProducer = { reminderRepository.getById(id) }
  )
  var hasSameInDb: Boolean = false

  private var _allGroups = viewModelScope.observeTable(
    table = Table.ReminderGroup,
    tableChangeListenerFactory = tableChangeListenerFactory,
    queryProducer = { reminderGroupRepository.getAll() }
  )
  var allGroups: LiveData<List<ReminderGroup>> = _allGroups

  fun getGroups(): List<ReminderGroup> {
    return _allGroups.value ?: emptyList()
  }

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    reminderAnalyticsTracker.startTracking()
  }

  fun saveAndStartReminder(reminder: Reminder, isEdit: Boolean = true) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      runBlocking {
        Logger.d("saveAndStartReminder: save START")
        if (reminder.groupUuId == "") {
          val group = reminderGroupRepository.defaultGroup()
          if (group != null) {
            reminder.groupColor = group.groupColor
            reminder.groupTitle = group.groupTitle
            reminder.groupUuId = group.groupUuId
          }
        }
        reminderRepository.save(reminder)
        if (!isEdit) {
          if (Reminder.isGpsType(reminder.type)) {
            val places = reminder.places
            if (places.isNotEmpty()) {
              placeRepository.save(places[0])
            }
          }
        }
        eventControlFactory.getController(reminder).enable()
        Logger.d("saveAndStartReminder: save DONE")
        analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_REMINDER))
        reminderAnalyticsTracker.sendEvent(UiReminderType(reminder.type).getEventType())
        Logger.logEvent("Reminder saved, type = ${reminder.type}")
      }
      backupReminder(reminder.uuId)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  fun pauseReminder(reminder: Reminder) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      eventControlFactory.getController(reminder).pause()
      postInProgress(false)
    }
  }

  fun resumeReminder(reminder: Reminder) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      eventControlFactory.getController(reminder).resume()
      postInProgress(false)
    }
  }

  fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderRepository.getById(id)
      hasSameInDb = reminder != null
    }
  }

  fun moveToTrash(reminder: Reminder) {
    withResultSuspend {
      reminder.isRemoved = true
      eventControlFactory.getController(reminder).disable()
      reminderRepository.save(reminder)
      backupReminder(reminder.uuId)
      Commands.DELETED
    }
  }

  fun deleteReminder(reminder: Reminder, showMessage: Boolean) {
    if (showMessage) {
      withResultSuspend {
        eventControlFactory.getController(reminder).disable()
        reminderRepository.delete(reminder.uuId)
        googleCalendarUtils.deleteEvents(reminder.uuId)
        workerLauncher.startWork(
          ReminderDeleteBackupWorker::class.java,
          Constants.INTENT_ID,
          reminder.uuId
        )
        Commands.DELETED
      }
    } else {
      withProgressSuspend {
        eventControlFactory.getController(reminder).disable()
        reminderRepository.delete(reminder.uuId)
        googleCalendarUtils.deleteEvents(reminder.uuId)
        workerLauncher.startWork(
          ReminderDeleteBackupWorker::class.java,
          Constants.INTENT_ID,
          reminder.uuId
        )
      }
    }
  }

  private fun backupReminder(uuId: String) {
    Logger.d("backupReminder: start backup")
    workerLauncher.startWork(ReminderSingleBackupWorker::class.java, Constants.INTENT_ID, uuId)
  }
}
