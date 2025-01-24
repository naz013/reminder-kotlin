package com.elementary.tasks.reminder.preview

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.UiShareData
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.reminder.preview.data.UiCalendarEventList
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewData
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewDataAdapter
import com.elementary.tasks.reminder.work.ReminderDeleteBackupWorker
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.logging.Logger
import com.github.naz013.repository.CalendarEventRepository
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.ReminderRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class PreviewReminderViewModel(
  arguments: Bundle?,
  private val reminderRepository: ReminderRepository,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val uiReminderPreviewDataAdapter: UiReminderPreviewDataAdapter,
  private val backupTool: BackupTool,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val noteRepository: NoteRepository,
  private val googleTaskRepository: GoogleTaskRepository,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val calendarEventRepository: CalendarEventRepository,
  private val reminderGroupRepository: ReminderGroupRepository,
  private val dateTimeManager: DateTimeManager,
  private val googleTaskToUiReminderPreviewGoogleTask: GoogleTaskToUiReminderPreviewGoogleTask,
  private val noteToUiReminderPreviewNote: NoteToUiReminderPreviewNote,
  private val textProvider: TextProvider,
  private val eventToUiReminderPreview: EventToUiReminderPreview
) : BaseProgressViewModel(dispatcherProvider) {

  private val _note = mutableLiveDataOf<UiNoteList>()
  val note = _note.toLiveData()

  private val _googleTask = mutableLiveDataOf<UiGoogleTaskList>()
  val googleTask = _googleTask.toLiveData()

  private val _sharedFile = mutableLiveDataOf<UiShareData>()
  val sharedFile = _sharedFile.toLiveData()

  private val _reminderData = mutableLiveDataOf<List<UiReminderPreviewData>>()
  val reminderData = _reminderData.toLiveData()

  var canCopy = false
    private set
  var canDelete = false
    private set
  var id: String = ""
    private set

  init {
    id = arguments?.getString(IntentKeys.INTENT_ID) ?: ""
  }

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    loadReminder()
  }

  fun onSubTaskRemoved(subTaskId: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderRepository.getById(id) ?: return@launch
      val subTasks = reminder.shoppings.toMutableList()
      val index = subTasks.indexOfFirst { it.uuId == subTaskId }

      if (index != -1) {
        subTasks.removeAt(index)

        Logger.i(TAG, "Subtask removed, at index: $index, id: $subTaskId")

        saveReminder(reminder.copy(shoppings = subTasks.toList()))
        loadReminder()
      }
    }
  }

  fun onSubTaskChecked(subTaskId: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderRepository.getById(id) ?: return@launch
      val subTasks = reminder.shoppings
      val index = subTasks.indexOfFirst { it.uuId == subTaskId }

      if (index != -1) {
        subTasks[index].isChecked = !subTasks[index].isChecked
        saveReminder(reminder.copy(shoppings = subTasks.toList()))
        loadReminder()
      }
    }
  }

  fun switchClick() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderRepository.getById(id) ?: return@launch

      if (reminder.isRemoved) return@launch

      toggleReminder(reminder)
    }
  }

  private fun loadReminder() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderRepository.getById(id) ?: return@launch
      val reminderGroup = reminderGroupRepository.getById(reminder.groupUuId)

      val type = UiReminderType(reminder.type)

      canCopy = type.isBase(UiReminderType.Base.DATE)
      canDelete = reminder.isRemoved

      val data = uiReminderPreviewDataAdapter.create(reminder, reminderGroup).toMutableList()
      _reminderData.postValue(data)

      noteRepository.getById(reminder.noteId)?.let { noteToUiReminderPreviewNote(it) }
        ?.also { data.addAll(it) }

      googleTaskRepository.getByReminderId(reminder.uuId)?.let {
        googleTaskToUiReminderPreviewGoogleTask(it, googleTaskListRepository.getById(it.listId))
      }?.also { data.addAll(it) }

      googleCalendarUtils.loadEvents(reminder.uuId).takeIf { it.isNotEmpty() }?.let {
        eventToUiReminderPreview(it, googleCalendarUtils.getCalendarsList())
      }?.also { data.addAll(it) }

      _reminderData.postValue(data)
    }
  }

  private fun saveReminder(reminder: Reminder) {
    postInProgress(true)
    Logger.i(TAG, "Saving reminder, id: ${reminder.uuId}")
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderRepository.save(reminder)
      appWidgetUpdater.updateScheduleWidget()
      workerLauncher.startWork(
        ReminderSingleBackupWorker::class.java,
        IntentKeys.INTENT_ID,
        reminder.uuId
      )
      postInProgress(false)
      postCommand(Commands.SAVED)
      loadReminder()
    }
  }

  fun deleteEvent(eventItem: UiCalendarEventList) {
    Logger.i(TAG, "Deleting calendar event, id: ${eventItem.id}")
    viewModelScope.launch(dispatcherProvider.default()) {
      if (eventItem.localId.isNotBlank()) {
        calendarEventRepository.delete(eventItem.localId)
      }
      googleCalendarUtils.deleteEvent(eventItem.id)
      loadReminder()
    }
  }

  private suspend fun toggleReminder(reminder: Reminder) {
    postInProgress(true)
    Logger.i(TAG, "Toggling reminder, id: ${reminder.uuId}")
    if (!eventControlFactory.getController(reminder).onOff()) {
      postInProgress(false)
      postCommand(Commands.OUTDATED)
    } else {
      workerLauncher.startWork(
        ReminderSingleBackupWorker::class.java,
        IntentKeys.INTENT_ID,
        reminder.uuId
      )
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
    loadReminder()
  }

  fun copyReminder(time: LocalTime) {
    Logger.i(TAG, "Copying reminder, id: $id, time: $time")
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderRepository.getById(id)?.also { reminder ->
        postInProgress(true)
        runBlocking {
          if (reminder.groupUuId == "") {
            val group = reminderGroupRepository.defaultGroup()
            if (group != null) {
              reminder.groupColor = group.groupColor
              reminder.groupTitle = group.groupTitle
              reminder.groupUuId = group.groupUuId
            }
          }
          val newItem = reminder.copy()
          newItem.summary = reminder.summary + " - " + textProvider.getText(R.string.copy)

          val date = dateTimeManager.fromGmtToLocal(newItem.eventTime)?.toLocalDate()
            ?: LocalDate.now()
          var dateTime = LocalDateTime.of(date, time)

          while (dateTime < LocalDateTime.now()) {
            dateTime = dateTime.plusDays(1)
          }
          newItem.eventTime = dateTimeManager.getGmtFromDateTime(dateTime)
          newItem.startTime = dateTimeManager.getGmtFromDateTime(dateTime)
          reminderRepository.save(newItem)
          eventControlFactory.getController(newItem).enable()
        }
        postCommand(Commands.SAVED)
      }
    }
  }

  fun deleteReminder() {
    Logger.i(TAG, "Deleting reminder, id: $id")
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderRepository.getById(id)?.also { reminder ->
        withResultSuspend {
          eventControlFactory.getController(reminder).disable()
          reminderRepository.delete(reminder.uuId)
          googleCalendarUtils.deleteEvents(reminder.uuId)
          workerLauncher.startWork(
            ReminderDeleteBackupWorker::class.java,
            IntentKeys.INTENT_ID,
            reminder.uuId
          )
          Commands.DELETED
        }
      }
    }
  }

  fun moveToTrash() {
    Logger.i(TAG, "Moving reminder to trash, id: $id")
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderRepository.getById(id)?.also {
        it.isRemoved = true
        eventControlFactory.getController(it).disable()
        reminderRepository.save(it)
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          IntentKeys.INTENT_ID,
          it.uuId
        )
        postCommand(Commands.DELETED)
      }
    }
  }

  fun shareReminder() {
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderRepository.getById(id)?.let {
        UiShareData(
          file = backupTool.reminderToFile(it),
          name = it.summary
        )
      }?.also {
        Logger.i(TAG, "Sharing reminder ${it.name}")
        _sharedFile.postValue(it)
      }
    }
  }

  companion object {
    private const val TAG = "PreviewReminderViewModel"
  }
}
