package com.elementary.tasks.notes.preview

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.note.UiNoteNotificationAdapter
import com.elementary.tasks.core.data.adapter.note.UiNotePreviewAdapter
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.notes.preview.reminders.ReminderToUiNoteAttachedReminder
import com.elementary.tasks.notes.usecase.ChangeNoteArchiveStateUseCase
import com.elementary.tasks.notes.usecase.CreateSharedNoteFileUseCase
import com.elementary.tasks.notes.usecase.DeleteNoteUseCase
import com.elementary.tasks.reminder.usecase.SaveReminderUseCase
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.toSingleEvent
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.ui.common.isAlmostTransparent
import com.github.naz013.ui.common.isColorDark
import com.github.naz013.ui.common.theme.ThemeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class PreviewNoteViewModel(
  val key: String,
  dispatcherProvider: DispatcherProvider,
  private val noteRepository: NoteRepository,
  private val reminderRepository: ReminderRepository,
  private val uiNotePreviewAdapter: UiNotePreviewAdapter,
  private val textProvider: TextProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  private val uiNoteNotificationAdapter: UiNoteNotificationAdapter,
  private val notifier: Notifier,
  private val reminderToUiNoteAttachedReminder: ReminderToUiNoteAttachedReminder,
  private val deleteNoteUseCase: DeleteNoteUseCase,
  private val changeNoteArchiveStateUseCase: ChangeNoteArchiveStateUseCase,
  private val saveReminderUseCase: SaveReminderUseCase,
  private val createSharedNoteFileUseCase: CreateSharedNoteFileUseCase,
  private val themeProvider: ThemeProvider,
  private val imagesSingleton: ImagesSingleton,
) : BaseProgressViewModel(dispatcherProvider) {
  private val _state = MutableStateFlow(PreviewNoteState(id = key))
  val state: StateFlow<PreviewNoteState> = _state.asStateFlow()

  private val _sharedFile = mutableLiveDataOf<Pair<NoteWithImages, File>>()
  val sharedFile = _sharedFile.toSingleEvent()

  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  private var initStatusBarColor: Int = -1
  private var statusBarColorSaved: Boolean = false

  @ColorInt
  fun getStatusBarColor(): Int? =
    if (statusBarColorSaved) {
      initStatusBarColor.takeIf { it != -1 }
    } else {
      null
    }

  fun saveStatusBarColor(
    @ColorInt color: Int,
  ) {
    if (statusBarColorSaved) return
    initStatusBarColor = color
    statusBarColorSaved = true
  }

  /** Pure contrast math, ported from the previous Fragment implementation — kept here so the
   *  Fragment/Compose layer never has to know about [ThemeProvider] or color math itself. */
  fun colorsFor(state: PreviewNoteState): NotePreviewColors {
    val isBgDark =
      if (state.opacity.isAlmostTransparent()) {
        themeProvider.isDark
      } else {
        state.backgroundColor.isColorDark()
      }
    val contentColor = if (isBgDark) PURE_WHITE else PURE_BLACK
    return NotePreviewColors(
      background = Color(state.backgroundColor),
      statusBarColor = state.backgroundColor,
      content = Color(contentColor),
    )
  }

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    analyticsEventSender.send(ScreenUsedEvent(Screen.NOTE_PREVIEW))
  }

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    loadInternal()
  }

  private fun loadInternal() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = noteRepository.getById(key)
      if (noteWithImages != null) {
        val uiNotePreview = uiNotePreviewAdapter.convert(noteWithImages)
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
            backgroundColor = uiNotePreview.backgroundColor,
            opacity = uiNotePreview.opacity,
            isArchived = uiNotePreview.isArchived,
            showAdsBanner = !BuildParams.isPro && AdsProvider.hasAds(),
          )
        }
      }
      loadReminders()
    }
  }

  private suspend fun loadReminders() {
    val reminders =
      reminderRepository.getByNoteKey(key).map {
        reminderToUiNoteAttachedReminder(it)
      }
    _state.update { it.copy(reminders = reminders) }
  }

  fun onStatusClick() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = noteRepository.getById(key) ?: return@launch
      uiNoteNotificationAdapter.convert(noteWithImages).also {
        withUIContext { notifier.showNoteNotification(it) }
      }
    }
  }

  fun onArchiveClick() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = noteRepository.getById(key)
      val note = noteWithImages?.note
      if (note == null) {
        postInProgress(false)
        postError(textProvider.getText(R.string.notes_failed_to_update))
        return@launch
      }

      changeNoteArchiveStateUseCase(key, !note.archived)

      loadInternal()

      postInProgress(false)
      postCommand(Commands.UPDATED)
    }
  }

  fun onDeleteClick() {
    _state.update { it.copy(activeDialog = PreviewNoteDialog.DELETE) }
  }

  fun onDialogDismiss() {
    _state.update { it.copy(activeDialog = null) }
  }

  fun onDeleteConfirmed() {
    _state.update { it.copy(activeDialog = null) }
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      deleteNoteUseCase(key)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  fun onShareClick() {
    viewModelScope.launch(dispatcherProvider.default()) {
      postInProgress(true)
      val noteWithImages = noteRepository.getById(key)
      if (noteWithImages == null) {
        postInProgress(false)
        postCommand(Commands.FAILED)
        return@launch
      }
      val file = createSharedNoteFileUseCase(noteWithImages)
      Logger.i(TAG, "Share note file created: ${file?.absolutePath}")

      postInProgress(false)
      if (file != null) {
        _sharedFile.postValue(Pair(noteWithImages, file))
      } else {
        postError(textProvider.getText(R.string.failed_to_send_note))
      }
    }
  }

  fun onEditClick() {
    navigationEvent.value = Event(NavigationEvent.EditNote(key))
  }

  fun onReminderEditClick(id: String) {
    navigationEvent.value = Event(NavigationEvent.EditReminder(id))
  }

  fun onImageOpen(position: Int) {
    val s = _state.value
    imagesSingleton.setCurrent(images = s.images, backgroundColor = s.backgroundColor)
    navigationEvent.value = Event(NavigationEvent.OpenImagePreview(position))
  }

  fun onReminderDetachClick(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderRepository.getById(id)

      if (reminder == null) {
        postInProgress(false)
        return@launch
      }

      saveReminderUseCase(
        reminder.copy(
          noteId = "",
          version = reminder.version + 1,
          syncState = SyncState.WaitingForUpload,
        ),
      )

      loadReminders()
      postInProgress(false)
      postCommand(Commands.UPDATED)
    }
  }

  sealed interface NavigationEvent {
    data class EditNote(val id: String) : NavigationEvent
    data class EditReminder(val id: String) : NavigationEvent
    data class OpenImagePreview(val position: Int) : NavigationEvent
  }

  companion object {
    private const val TAG = "PreviewNoteViewModel"
    private const val PURE_WHITE = android.graphics.Color.WHITE
    private const val PURE_BLACK = android.graphics.Color.BLACK
  }
}
