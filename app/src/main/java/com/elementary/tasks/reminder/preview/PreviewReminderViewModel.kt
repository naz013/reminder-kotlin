package com.elementary.tasks.reminder.preview

import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.data.adapter.UiReminderCommonAdapter
import com.elementary.tasks.core.data.adapter.UiReminderPlaceAdapter
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskListAdapter
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteListAdapter
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.reminder.build.valuedialog.controller.attachments.UriToAttachmentFileAdapter
import com.elementary.tasks.reminder.preview.data.UiCalendarEventList
import com.elementary.tasks.reminder.scheduling.usecase.ActivateReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.ToggleReminderStateUseCase
import com.elementary.tasks.reminder.usecase.DeleteReminderUseCase
import com.elementary.tasks.reminder.usecase.MoveReminderToArchiveUseCase
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.repository.CalendarEventRepository
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.io.File
import java.util.UUID

class PreviewReminderViewModel(
  private val id: String,
  private val reminderRepository: ReminderRepository,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val dispatcherProvider: DispatcherProvider,
  private val uiReminderPlaceAdapter: UiReminderPlaceAdapter,
  private val uiReminderCommonAdapter: UiReminderCommonAdapter,
  private val uiGroupListAdapter: UiGroupListAdapter,
  private val uiNoteListAdapter: UiNoteListAdapter,
  private val uiGoogleTaskListAdapter: UiGoogleTaskListAdapter,
  private val uriToAttachmentFileAdapter: UriToAttachmentFileAdapter,
  private val backupTool: BackupTool,
  private val noteRepository: NoteRepository,
  private val googleTaskRepository: GoogleTaskRepository,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val calendarEventRepository: CalendarEventRepository,
  private val groupV2Repository: GroupV2Repository,
  private val dateTimeManager: DateTimeManager,
  private val textProvider: TextProvider,
  private val deleteReminderUseCase: DeleteReminderUseCase,
  private val moveReminderToArchiveUseCase: MoveReminderToArchiveUseCase,
  private val activateReminderUseCase: ActivateReminderUseCase,
  private val toggleReminderStateUseCase: ToggleReminderStateUseCase,
  private val buildInfo: BuildInfo,
) : ViewModel() {

  private val _state = MutableStateFlow(PreviewReminderState())
  val state = _state.stateInWhileSubscribed(PreviewReminderState())
    .onStart { load() }

  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  fun onCopyClicked() {
    viewModelScope.launch(dispatcherProvider.default()) {
      var time = LocalTime.of(0, 0)
      val list = mutableListOf<LocalTime>()
      val times = mutableListOf<String>()
      var isRunning = true
      do {
        if (time.hour == 23 && time.minute == 30) {
          isRunning = false
        } else {
          list.add(time)
          times.add(dateTimeManager.getTime(time))
          time = time.plusMinutes(30)
        }
      } while (isRunning)

      withContext(dispatcherProvider.main()) {
        event.emit(
          ViewModelEvent.ShowCopyTimeDialog(
            times = list,
            titles = times,
          )
        )
      }
    }
  }

  fun onOpenCalendarClicked(id: Long) {
    if (id <= 0L) return
    val uri = "content://com.android.calendar/events/$id".toUri()
    val intent = Intent(Intent.ACTION_VIEW, uri)
    event.emit(ViewModelEvent.OpenCalendar(intent, textProvider.getString(R.string.calendar)))
  }

  fun onToggleClick() {
    viewModelScope.launch(dispatcherProvider.main()) {
      val reminder = withContext(dispatcherProvider.io()) {
        reminderRepository.getById(id)
      } ?: run {
        event.emit(ViewModelEvent.ShowError(textProvider.getString(R.string.error_reminder_not_found)))
        event.emit(ViewModelEvent.MoveBack)
        return@launch
      }
      if (reminder.isRemoved) {
        event.emit(
          ViewModelEvent.ShowError(textProvider.getString(R.string.error_reminder_is_already_removed_edit_it_first))
        )
        return@launch
      }
      Logger.i(TAG, "Toggling reminder, id: ${reminder.uuId}")
      val result = withContext(dispatcherProvider.io()) {
        toggleReminderStateUseCase(reminder)
      }
      if (!result.success) {
        event.emit(ViewModelEvent.ShowError(textProvider.getString(R.string.reminder_is_outdated)))
      }
      load()
    }
  }

  fun onSubTaskRemoved(subTaskId: String) {
    viewModelScope.launch(dispatcherProvider.main()) {
      val reminder = withContext(dispatcherProvider.io()) {
        reminderRepository.getById(id)
      } ?: return@launch
      val subTasks = withContext(dispatcherProvider.default()) {
        reminder.shoppings.toMutableList().let { list ->
          val index = list.indexOfFirst { subtask -> subtask.uuId == subTaskId }
          if (index != -1) {
            list.removeAt(index)
            Logger.i(TAG, "Subtask removed, at index: $index, id: $subTaskId")
          }
          list
        }

      }
      saveReminder(reminder.copy(shoppings = subTasks.toList()))
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
      }
    }
  }

  fun deleteEvent(eventItem: UiCalendarEventList) {
    Logger.i(TAG, "Deleting calendar event, id: ${eventItem.id}")
    viewModelScope.launch(dispatcherProvider.default()) {
      if (eventItem.localId.isNotBlank()) {
        calendarEventRepository.delete(eventItem.localId)
      }
      googleCalendarUtils.deleteEvent(eventItem.id)
      load()
    }
  }

  fun copyReminder(time: LocalTime) {
    Logger.i(TAG, "Copying reminder, id: $id, time: $time")
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderRepository.getById(id) ?: return@launch

      if (reminder.groupUuId == "") {
        val group = groupV2Repository.defaultGroup()
        if (group != null) {
          reminder.groupColor = group.color
          reminder.groupTitle = group.title
          reminder.groupUuId = group.uuId
        }
      }
      val newItem =
        reminder.copy().apply {
          this.uuId = UUID.randomUUID().toString()
        }
      newItem.summary = textProvider.getText(R.string.copy_of, reminder.summary)

      val date =
        dateTimeManager.fromGmtToLocal(newItem.eventTime)?.toLocalDate()
          ?: LocalDate.now()
      var dateTime = LocalDateTime.of(date, time)

      while (dateTime < LocalDateTime.now()) {
        dateTime = dateTime.plusDays(1)
      }
      newItem.eventTime = dateTimeManager.getGmtFromDateTime(dateTime)
      newItem.startTime = dateTimeManager.getGmtFromDateTime(dateTime)
      activateReminderUseCase(newItem)
    }
  }

  fun onDeleteClick() {
    _state.update { it.copy(showDeleteConfirm = true) }
  }

  fun onDeleteDismiss() {
    _state.update { it.copy(showDeleteConfirm = false) }
  }

  fun onDeleteConfirmed() {
    val canDelete = _state.value.canDelete
    _state.update { it.copy(showDeleteConfirm = false) }
    if (canDelete) {
      deleteReminder()
    } else {
      moveToTrash()
    }
  }

  fun shareReminder() {
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderRepository.getById(id) ?: return@launch
      val file = backupTool.reminderToFile(reminder) ?: return@launch
      Logger.i(TAG, "Sharing reminder ${file.name}")
      event.emit(
        ViewModelEvent.ShareData(
          file = file,
          title = reminder.summary,
        )
      )
    }
  }

  private fun deleteReminder() {
    Logger.i(TAG, "Deleting reminder, id: $id")
    viewModelScope.launch(dispatcherProvider.io()) {
      reminderRepository.getById(id)?.also { reminder ->
        deleteReminderUseCase(reminder)
        withContext(dispatcherProvider.main()) {
          event.emit(ViewModelEvent.MoveBack)
        }
      }
    }
  }

  private fun moveToTrash() {
    Logger.i(TAG, "Moving reminder to trash, id: $id")
    viewModelScope.launch(dispatcherProvider.io()) {
      moveReminderToArchiveUseCase(id)
      withContext(dispatcherProvider.main()) {
        event.emit(ViewModelEvent.MoveBack)
      }
    }
  }

  private fun saveReminder(reminder: Reminder) {
    Logger.i(TAG, "Saving reminder, id: ${reminder.uuId}")
    viewModelScope.launch(dispatcherProvider.main()) {
      withContext(dispatcherProvider.io()) {
        reminderRepository.save(reminder)
      }
      load()
    }
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = withContext(dispatcherProvider.io()) {
        reminderRepository.getById(id)
      } ?: return@launch
      val reminderGroup = withContext(dispatcherProvider.io()) {
        groupV2Repository.getById(reminder.groupUuId)
      }
      val type = UiReminderType(reminder.type)

      val status = uiReminderCommonAdapter.getReminderStatus(reminder.isActive, reminder.isRemoved)
      val due = uiReminderCommonAdapter.getDue(reminder, type)
      val target = uiReminderCommonAdapter.getTarget(reminder, type)
      val group =
        reminderGroup?.let { uiGroupListAdapter.convert(it) }
          ?: uiGroupListAdapter.convert(reminder.groupUuId, reminder.groupColor, reminder.groupTitle)
      val places = if (type.isGpsType()) reminder.places.map { uiReminderPlaceAdapter.create(it) } else emptyList()
      val attachments = reminder.attachmentFiles.map { uriToAttachmentFileAdapter(it.toUri()) }
      val subTasks =
        if (type.isSubTasks()) {
          reminder.shoppings
            .filterNot { it.isDeleted }
            .sortedByDescending { !it.isChecked }
            .map { UiPreviewSubTask(id = it.uuId, text = it.summary, isChecked = it.isChecked) }
        } else {
          emptyList()
        }

      withContext(dispatcherProvider.main()) {
        _state.update {
          it.copy(
            id = reminder.uuId,
            isLoading = false,
            status = status,
            summary = reminder.summary,
            description = reminder.description?.takeIf { d -> d.isNotEmpty() },
            dueDateTime = due.formattedDateTime,
            before = due.before,
            repeat = due.repeat,
            remaining = due.remaining,
            groupTitle = group?.title,
            priorityTitle = uiReminderCommonAdapter.getPriorityTitle(reminder.priority),
            target = target,
            targetType =
              type.takeIf { t ->
                t.isCall() || t.isSms() || t.isApp() || t.isLink() || t.isEmail()
              },
            rawTarget = reminder.target,
            attachments = attachments,
            subTasks = subTasks,
            places = places,
            placesHeader = placesHeader(type, places.size),
            canCopy = type.isBase(UiReminderType.Base.DATE),
            canDelete = reminder.isRemoved,
            hasAds = !buildInfo.isPro && AdsProvider.hasAds()
          )
        }
      }

      withContext(dispatcherProvider.io()) {
        noteRepository.getById(reminder.noteId)
      }?.let { note ->
        withContext(dispatcherProvider.main()) {
          _state.update { it.copy(note = uiNoteListAdapter.convert(note)) }
        }
      }

      withContext(dispatcherProvider.io()) {
        googleTaskRepository.getByReminderId(reminder.uuId)
      }?.let { googleTask ->
        val list = withContext(dispatcherProvider.io()) {
          googleTaskListRepository.getById(googleTask.listId)
        }
        withContext(dispatcherProvider.main()) {
          _state.update { it.copy(googleTask = uiGoogleTaskListAdapter.convert(googleTask, list)) }
        }
      }

      val events = withContext(dispatcherProvider.io()) {
        googleCalendarUtils.loadEvents(reminder.uuId)
      }
      if (events.isNotEmpty()) {
        val calendarsMap = googleCalendarUtils.getCalendarsList().associateBy { it.id }
        val calendarEvents =
          events.map { item ->
            UiCalendarEventList(
              id = item.id,
              localId = item.localId,
              title = item.title,
              description = item.description,
              calendarName = calendarsMap[item.calendarId]?.name,
              dateStartFormatted = dateTimeManager.getFullDateTime(item.dtStart).takeIf { item.dtStart != 0L },
              dateEndFormatted = dateTimeManager.getFullDateTime(item.dtEnd).takeIf { item.dtEnd != 0L },
            )
          }
        withContext(dispatcherProvider.main()) {
          _state.update { it.copy(calendarEvents = calendarEvents) }
        }
      }
    }
  }

  private fun placesHeader(
    type: UiReminderType,
    placesCount: Int,
  ): String =
    when (placesCount) {
      0 -> ""
      1 if type.isBase(UiReminderType.Base.LOCATION_IN) ->
        textProvider.getText(R.string.builder_arriving_destination)

      1 -> textProvider.getText(R.string.builder_leaving_place)
      else -> textProvider.getText(R.string.places)
    }

  sealed interface ViewModelEvent {
    data object MoveBack : ViewModelEvent

    data class ShowError(
      val message: String
    ) : ViewModelEvent

    data class ShareData(
      val file: File,
      val title: String,
    ) : ViewModelEvent

    data class OpenCalendar(
      val intent: Intent,
      val title: String,
    ) : ViewModelEvent

    data class ShowCopyTimeDialog(
      val times: List<LocalTime>,
      val titles: List<String>,
    ) : ViewModelEvent
  }

  companion object {
    private const val TAG = "PreviewReminderViewModel"
  }
}
