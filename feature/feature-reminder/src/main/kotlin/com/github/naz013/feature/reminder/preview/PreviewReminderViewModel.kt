package com.github.naz013.feature.reminder.preview

import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.UiReminderCommonAdapter
import com.github.naz013.feature.reminder.UiReminderPlaceAdapter
import com.github.naz013.ui.group.UiGroupListAdapter
import com.github.naz013.feature.reminder.note.UiNoteListAdapter
import com.github.naz013.ui.reminder.UiReminderType
import com.github.naz013.files.BackupTool
import com.github.naz013.feature.reminder.build.valuedialog.controller.attachments.UriToAttachmentFileAdapter
import com.github.naz013.feature.reminder.preview.data.UiCalendarEventList
import com.github.naz013.logic.reminder.usecase.ToggleReminderStateUseCase
import com.github.naz013.logic.reminder.usecase.MoveReminderToArchiveUseCase
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.googlecalendar.GoogleCalendarApi
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
import com.github.naz013.logic.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.logic.reminder.usecase.SaveReminderUseCase
import com.github.naz013.repository.CalendarEventRepository
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.observer.TableChangeListenerFactory
import com.github.naz013.repository.table.Table
import com.github.naz013.ui.googletask.GoogleTaskItemStateAdapter
import com.github.naz013.ui.tag.TagChipStateAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.io.File
import java.util.UUID

internal class PreviewReminderViewModel(
  private val id: String,
  private val reminderV2Repository: ReminderV2Repository,
  private val googleCalendarApi: GoogleCalendarApi,
  private val dispatcherProvider: DispatcherProvider,
  private val uiReminderPlaceAdapter: UiReminderPlaceAdapter,
  private val uiReminderCommonAdapter: UiReminderCommonAdapter,
  private val uiGroupListAdapter: UiGroupListAdapter,
  private val uiNoteListAdapter: UiNoteListAdapter,
  private val googleTaskItemStateAdapter: GoogleTaskItemStateAdapter,
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
  private val saveReminderUseCase: SaveReminderUseCase,
  private val tableChangeListenerFactory: TableChangeListenerFactory,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val tagChipStateAdapter: TagChipStateAdapter,
) : ViewModel() {

  private val _state = MutableStateFlow(PreviewReminderState())
  val state = _state.stateInWhileSubscribed(PreviewReminderState())
    .onStart { load() }

  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  // The reminder can also be completed/snoozed/toggled from the system notification's action
  // buttons, which run through a BroadcastReceiver rather than an Activity - so this screen
  // never gets an ON_RESUME to hook into. Listen for the underlying table write directly so the
  // status shown here stays correct even when that happens while this screen is in the foreground.
  private val reminderTableChangeListener = tableChangeListenerFactory.create(Table.ReminderV2) { refresh() }

  init {
    reminderTableChangeListener.register()
    viewModelScope.launch(dispatcherProvider.default()) {
      tagAssignmentRepository.observeTagsForItem(id, TaggedItemType.REMINDER)
        .map { tags -> tags.map { tagChipStateAdapter(it) } }
        .collect { tagChips ->
          _state.update { it.copy(tags = tagChips) }
        }
    }
  }

  override fun onCleared() {
    reminderTableChangeListener.unregister()
  }

  fun refresh() {
    load()
  }

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
        reminderV2Repository.getById(id)
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
        reminderV2Repository.getById(id)
      } ?: return@launch
      val subTasks = withContext(dispatcherProvider.default()) {
        reminder.shoppingItems.toMutableList().let { list ->
          val index = list.indexOfFirst { subtask -> subtask.uuId == subTaskId }
          if (index != -1) {
            list.removeAt(index)
            Logger.i(TAG, "Subtask removed, at index: $index, id: $subTaskId")
          }
          list
        }

      }
      saveReminder(reminder.copy(shoppingItems = subTasks.toList()))
    }
  }

  fun onSubTaskChecked(subTaskId: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderV2Repository.getById(id) ?: return@launch
      val subTasks = reminder.shoppingItems
      val index = subTasks.indexOfFirst { it.uuId == subTaskId }
      if (index != -1) {
        val updated = subTasks.toMutableList()
        updated[index] = updated[index].copy(isChecked = !updated[index].isChecked)
        saveReminder(reminder.copy(shoppingItems = updated.toList()))
      }
    }
  }

  fun deleteEvent(eventItem: UiCalendarEventList) {
    Logger.i(TAG, "Deleting calendar event, id: ${eventItem.id}")
    viewModelScope.launch(dispatcherProvider.default()) {
      if (eventItem.localId.isNotBlank()) {
        calendarEventRepository.delete(eventItem.localId)
      }
      googleCalendarApi.deleteEvent(eventItem.id)
      load()
    }
  }

  fun copyReminder(time: LocalTime) {
    Logger.i(TAG, "Copying reminder, id: $id, time: $time")
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderV2Repository.getById(id) ?: return@launch

      val date =
        reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) }?.toLocalDate()
          ?: LocalDate.now()
      var dateTime = LocalDateTime.of(date, time)

      while (dateTime < LocalDateTime.now()) {
        dateTime = dateTime.plusDays(1)
      }
      val eventDateTime = dateTimeManager.localToUtc(dateTime)

      val newItem =
        reminder.copy(
          uuId = UUID.randomUUID().toString(),
          summary = textProvider.getText(R.string.copy_of, reminder.summary),
          schedule = ReminderSchedule(startDateTime = eventDateTime, eventDateTime = eventDateTime),
        )
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
      val reminder = reminderV2Repository.getById(id) ?: return@launch
      val file = backupTool.reminderToFile(reminder) ?: return@launch
      Logger.i(TAG, "Sharing reminder ${file.name}")
      withContext(dispatcherProvider.main()) {
        event.emit(
          ViewModelEvent.ShareData(
            file = file,
            title = reminder.summary,
          )
        )
      }
    }
  }

  private fun deleteReminder() {
    Logger.i(TAG, "Deleting reminder, id: $id")
    viewModelScope.launch(dispatcherProvider.io()) {
      reminderV2Repository.getById(id)?.also { reminder ->
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

  private fun saveReminder(reminder: ReminderV2) {
    Logger.i(TAG, "Saving reminder, id: ${reminder.uuId}")
    viewModelScope.launch(dispatcherProvider.main()) {
      withContext(dispatcherProvider.io()) {
        saveReminderUseCase(reminder)
      }
      load()
    }
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = withContext(dispatcherProvider.io()) {
        reminderV2Repository.getById(id)
      } ?: return@launch
      val reminderGroup = withContext(dispatcherProvider.io()) {
        reminder.groupId?.let { groupV2Repository.getById(it) }
      }

      val status = uiReminderCommonAdapter.getReminderStatus(reminder.isActive, reminder.isRemoved)
      val due = uiReminderCommonAdapter.getDueV2(reminder)
      val target = uiReminderCommonAdapter.getTargetV2(reminder)
      val group = reminderGroup?.let { uiGroupListAdapter.convert(it) }
      val places = reminder.places.map { uiReminderPlaceAdapter.create(it) }
      val attachments = reminder.attachmentFiles.map { uriToAttachmentFileAdapter(it.toUri()) }
      val subTasks =
        if (reminder.action is ReminderAction.Shopping) {
          reminder.shoppingItems
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
            priorityTitle =
              uiReminderCommonAdapter.getPriorityTitle(
                (reminder.notification.priority ?: ReminderPriority.NORMAL).ordinal,
              ),
            target = target,
            targetType = targetTypeV2(reminder.action),
            rawTarget = rawTargetV2(reminder.action),
            attachments = attachments,
            subTasks = subTasks,
            places = places,
            placesHeader = placesHeaderV2(reminder.recurrence, places.size),
            canCopy = canCopyV2(reminder.recurrence),
            canDelete = reminder.isRemoved,
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
          _state.update { it.copy(googleTask = googleTaskItemStateAdapter.convert(googleTask, list)) }
        }
      }

      val events = withContext(dispatcherProvider.io()) {
        googleCalendarApi.loadEvents(reminder.uuId)
      }
      if (events.isNotEmpty()) {
        val calendarsMap = googleCalendarApi.getCalendarsList().associateBy { it.id }
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

  private fun placesHeaderV2(
    recurrence: RecurrenceRule,
    placesCount: Int,
  ): String =
    when (placesCount) {
      0 -> ""
      1 if recurrence is RecurrenceRule.LocationEnter ->
        textProvider.getText(R.string.builder_arriving_destination)

      1 -> textProvider.getText(R.string.builder_leaving_place)
      else -> textProvider.getText(R.string.places)
    }

  private fun canCopyV2(recurrence: RecurrenceRule): Boolean =
    recurrence is RecurrenceRule.Once || recurrence is RecurrenceRule.Daily

  private fun targetTypeV2(action: ReminderAction): UiReminderType? =
    when (action) {
      is ReminderAction.Call -> UiReminderType(UiReminderType.Base.DATE, UiReminderType.Kind.CALL)
      is ReminderAction.Sms -> UiReminderType(UiReminderType.Base.DATE, UiReminderType.Kind.SMS)
      is ReminderAction.Link -> UiReminderType(UiReminderType.Base.DATE, UiReminderType.Kind.LINK)
      is ReminderAction.App -> UiReminderType(UiReminderType.Base.DATE, UiReminderType.Kind.APP)
      is ReminderAction.Email -> UiReminderType(UiReminderType.Base.DATE, UiReminderType.Kind.EMAIL)
      ReminderAction.Shopping, ReminderAction.None -> null
    }

  private fun rawTargetV2(action: ReminderAction): String =
    when (action) {
      is ReminderAction.Call -> action.target
      is ReminderAction.Sms -> action.target
      is ReminderAction.Link -> action.target
      is ReminderAction.App -> action.target
      is ReminderAction.Email -> action.target
      ReminderAction.Shopping, ReminderAction.None -> ""
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
