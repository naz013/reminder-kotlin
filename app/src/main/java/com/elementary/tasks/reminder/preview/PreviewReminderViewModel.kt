package com.elementary.tasks.reminder.preview

import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.UiReminderCommonAdapter
import com.elementary.tasks.core.data.adapter.UiReminderPlaceAdapter
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskListAdapter
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteListAdapter
import com.elementary.tasks.core.data.ui.UiShareData
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.util.UUID

class PreviewReminderViewModel(
  private val id: String,
  private val reminderRepository: ReminderRepository,
  private val googleCalendarUtils: GoogleCalendarUtils,
  dispatcherProvider: DispatcherProvider,
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
  private val reminderGroupRepository: ReminderGroupRepository,
  private val dateTimeManager: DateTimeManager,
  private val textProvider: TextProvider,
  private val deleteReminderUseCase: DeleteReminderUseCase,
  private val moveReminderToArchiveUseCase: MoveReminderToArchiveUseCase,
  private val activateReminderUseCase: ActivateReminderUseCase,
  private val toggleReminderStateUseCase: ToggleReminderStateUseCase,
) : BaseProgressViewModel(dispatcherProvider) {
  val state: StateFlow<PreviewReminderState> field = MutableStateFlow(PreviewReminderState())

  private val _sharedFile = mutableLiveDataOf<UiShareData>()
  val sharedFile = _sharedFile.toLiveData()

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    load()
  }

  fun onToggleClick() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderRepository.getById(id) ?: return@launch
      if (reminder.isRemoved) return@launch
      postInProgress(true)
      Logger.i(TAG, "Toggling reminder, id: ${reminder.uuId}")
      val updatedReminder = toggleReminderStateUseCase(reminder)
      postInProgress(false)
      if (updatedReminder.first) {
        postCommand(Commands.SAVED)
      } else {
        postCommand(Commands.OUTDATED)
      }
      load()
    }
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
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderRepository.getById(id)?.also { reminder ->
        postInProgress(true)
        if (reminder.groupUuId == "") {
          val group = reminderGroupRepository.defaultGroup()
          if (group != null) {
            reminder.groupColor = group.groupColor
            reminder.groupTitle = group.groupTitle
            reminder.groupUuId = group.groupUuId
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
        postInProgress(false)
        postCommand(Commands.SAVED)
      }
    }
  }

  fun onDeleteClick() {
    state.update { it.copy(showDeleteConfirm = true) }
  }

  fun onDeleteDismiss() {
    state.update { it.copy(showDeleteConfirm = false) }
  }

  fun onDeleteConfirmed() {
    val canDelete = state.value.canDelete
    state.update { it.copy(showDeleteConfirm = false) }
    if (canDelete) {
      deleteReminder()
    } else {
      moveToTrash()
    }
  }

  fun shareReminder() {
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderRepository
        .getById(id)
        ?.let {
          UiShareData(
            file = backupTool.reminderToFile(it),
            name = it.summary,
          )
        }?.also {
          Logger.i(TAG, "Sharing reminder ${it.name}")
          _sharedFile.postValue(it)
        }
    }
  }

  private fun deleteReminder() {
    Logger.i(TAG, "Deleting reminder, id: $id")
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderRepository.getById(id)?.also { reminder ->
        withResultSuspend {
          deleteReminderUseCase(reminder)
          Commands.DELETED
        }
      }
    }
  }

  private fun moveToTrash() {
    Logger.i(TAG, "Moving reminder to trash, id: $id")
    viewModelScope.launch(dispatcherProvider.default()) {
      moveReminderToArchiveUseCase(id)
      postCommand(Commands.DELETED)
    }
  }

  private fun saveReminder(reminder: Reminder) {
    postInProgress(true)
    Logger.i(TAG, "Saving reminder, id: ${reminder.uuId}")
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderRepository.save(reminder)
      postInProgress(false)
      postCommand(Commands.SAVED)
      load()
    }
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderRepository.getById(id) ?: return@launch
      val reminderGroup = reminderGroupRepository.getById(reminder.groupUuId)
      val type = UiReminderType(reminder.type)

      val status = uiReminderCommonAdapter.getReminderStatus(reminder.isActive, reminder.isRemoved)
      val due = uiReminderCommonAdapter.getDue(reminder, type)
      val target = uiReminderCommonAdapter.getTarget(reminder, type)
      val group =
        reminderGroup?.let { uiGroupListAdapter.convert(it) }
          ?: uiGroupListAdapter.convert(reminder.groupUuId, reminder.groupColor, reminder.groupTitle)
      val places = if (type.isGpsType()) reminder.places.map { uiReminderPlaceAdapter.create(it) } else emptyList()
      val attachments = reminder.attachmentFiles.map { uriToAttachmentFileAdapter(Uri.parse(it)) }
      val subTasks =
        if (type.isSubTasks()) {
          reminder.shoppings
            .filterNot { it.isDeleted }
            .sortedByDescending { !it.isChecked }
            .map { UiPreviewSubTask(id = it.uuId, text = it.summary, isChecked = it.isChecked) }
        } else {
          emptyList()
        }

      state.update {
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
        )
      }

      noteRepository.getById(reminder.noteId)?.let { note ->
        state.update { it.copy(note = uiNoteListAdapter.convert(note)) }
      }

      googleTaskRepository.getByReminderId(reminder.uuId)?.let { googleTask ->
        val list = googleTaskListRepository.getById(googleTask.listId)
        state.update { it.copy(googleTask = uiGoogleTaskListAdapter.convert(googleTask, list)) }
      }

      val events = googleCalendarUtils.loadEvents(reminder.uuId)
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
        state.update { it.copy(calendarEvents = calendarEvents) }
      }
    }
  }

  private fun placesHeader(
    type: UiReminderType,
    placesCount: Int,
  ): String =
    when {
      placesCount == 0 -> ""
      placesCount == 1 && type.isBase(UiReminderType.Base.LOCATION_IN) ->
        textProvider.getText(R.string.builder_arriving_destination)

      placesCount == 1 -> textProvider.getText(R.string.builder_leaving_place)
      else -> textProvider.getText(R.string.places)
    }

  companion object {
    private const val TAG = "PreviewReminderViewModel"
  }
}
