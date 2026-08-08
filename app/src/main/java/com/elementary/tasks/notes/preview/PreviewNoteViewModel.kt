package com.elementary.tasks.notes.preview

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.data.adapter.note.UiNoteNotificationAdapter
import com.elementary.tasks.core.data.adapter.note.UiNotePreviewAdapter
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.notes.NoteColorEngine
import com.elementary.tasks.notes.preview.reminders.ReminderToUiNoteAttachedReminder
import com.elementary.tasks.notes.usecase.ChangeNoteArchiveStateUseCase
import com.elementary.tasks.notes.usecase.CreateSharedNoteFileUseCase
import com.elementary.tasks.notes.usecase.DeleteNoteUseCase
import com.github.naz013.logic.reminder.usecase.SaveReminderUseCase
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.ReminderV2Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PreviewNoteViewModel(
  val key: String,
  private val dispatcherProvider: DispatcherProvider,
  private val noteRepository: NoteRepository,
  private val reminderV2Repository: ReminderV2Repository,
  private val uiNotePreviewAdapter: UiNotePreviewAdapter,
  private val textProvider: TextProvider,
  analyticsEventSender: AnalyticsEventSender,
  private val uiNoteNotificationAdapter: UiNoteNotificationAdapter,
  private val notifier: Notifier,
  private val reminderToUiNoteAttachedReminder: ReminderToUiNoteAttachedReminder,
  private val deleteNoteUseCase: DeleteNoteUseCase,
  private val changeNoteArchiveStateUseCase: ChangeNoteArchiveStateUseCase,
  private val saveReminderUseCase: SaveReminderUseCase,
  private val createSharedNoteFileUseCase: CreateSharedNoteFileUseCase,
  private val imagesSingleton: ImagesSingleton,
  private val noteColorEngine: NoteColorEngine,
) : ViewModel() {

  private val _state = MutableStateFlow(PreviewNoteState(id = key))
  val state = _state.stateInWhileSubscribed(PreviewNoteState(id = key))
    .onStart { loadInternal() }

  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.NOTE_PREVIEW))
  }

  private fun loadInternal() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = noteRepository.getById(key)
      if (noteWithImages != null) {
        val uiNotePreview = uiNotePreviewAdapter.convert(noteWithImages)
        val noteColors = noteColorEngine.colorsForLegacy(
          code = noteWithImages.getColor(),
          palette = noteWithImages.getPalette(),
          opacity = noteWithImages.getOpacity(),
        )
        withContext(dispatcherProvider.main()) {
          _state.update {
            it.copy(
              id = uiNotePreview.id,
              title = uiNotePreview.title,
              text = uiNotePreview.text,
              titleTypeface = uiNotePreview.titleTypeface,
              typeface = uiNotePreview.typeface,
              titleTextSize = uiNotePreview.titleTextSize,
              textSize = uiNotePreview.textSize,
              images = uiNotePreview.images,
              isArchived = uiNotePreview.isArchived,
              background = noteColors.background,
              content = noteColors.content,
            )
          }
        }
      }
      loadReminders()
    }
  }

  private suspend fun loadReminders() {
    val reminders =
      reminderV2Repository.getByNoteId(key).map {
        reminderToUiNoteAttachedReminder(it)
      }
    _state.update { it.copy(reminders = reminders) }
  }

  fun onStatusClick() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = noteRepository.getById(key) ?: return@launch
      uiNoteNotificationAdapter.convert(noteWithImages).also {
        withContext(dispatcherProvider.main()) { notifier.showNoteNotification(it) }
      }
    }
  }

  fun onArchiveClick() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = noteRepository.getById(key)
      val note = noteWithImages?.note
      if (note == null) {
        event.emit(ViewModelEvent.Message(textProvider.getText(R.string.notes_failed_to_update)))
        return@launch
      }

      changeNoteArchiveStateUseCase(key, !note.archived)
      val message = if (note.archived) {
        textProvider.getText(R.string.note_reverted_from_archive)
      } else {
        textProvider.getText(R.string.note_moved_to_archive)
      }

      loadInternal()

      withContext(dispatcherProvider.main()) {
        event.emit(ViewModelEvent.Message(message))
      }
    }
  }

  fun onDeleteClick() {
    event.emit(ViewModelEvent.Delete)
  }

  fun onDeleteConfirmed() {
    viewModelScope.launch(dispatcherProvider.default()) {
      deleteNoteUseCase(key)
      event.emit(ViewModelEvent.MoveBack)
    }
  }

  fun onShareClick() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = noteRepository.getById(key) ?: return@launch
      val file = createSharedNoteFileUseCase(noteWithImages)
      Logger.i(TAG, "Share note file created: ${file?.absolutePath}")
      if (file != null && file.exists() && file.canRead()) {
        event.emit(ViewModelEvent.ShareNote(noteWithImages.getTitle(), file))
      } else {
        event.emit(ViewModelEvent.Message(textProvider.getText(R.string.failed_to_send_note)))
      }
    }
  }

  fun onEditClick() {
    event.emit(ViewModelEvent.EditNote(key))
  }

  fun onReminderEditClick(id: String) {
    event.emit(ViewModelEvent.EditReminder(id))
  }

  fun onImageOpen(position: Int) {
    imagesSingleton.setCurrent(images = _state.value.images, backgroundColor = _state.value.background)
    event.emit(ViewModelEvent.OpenImagePreview(position))
  }

  fun onReminderDetachClick(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderV2Repository.getById(id) ?: return@launch

      saveReminderUseCase(
        reminder.copy(
          noteId = "",
          sync = reminder.sync.copy(version = reminder.sync.version + 1, syncState = SyncState.WaitingForUpload),
        ),
      )

      loadReminders()
    }
  }

  sealed interface ViewModelEvent {
    data class EditNote(
      val id: String,
    ) : ViewModelEvent

    data class EditReminder(
      val id: String,
    ) : ViewModelEvent

    data class OpenImagePreview(
      val position: Int,
    ) : ViewModelEvent

    data class ShareNote(
      val text: String,
      val file: File,
    ) : ViewModelEvent

    data class Message(
      val message: String,
    ) : ViewModelEvent

    data object MoveBack : ViewModelEvent

    data object Delete : ViewModelEvent
  }

  companion object {
    private const val TAG = "PreviewNoteViewModel"
  }
}
