package com.elementary.tasks.notes.preview

import androidx.annotation.ColorInt
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.note.UiNoteNotificationAdapter
import com.elementary.tasks.core.data.adapter.note.UiNotePreviewAdapter
import com.elementary.tasks.core.data.ui.note.UiNotePreview
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.notes.preview.reminders.ReminderToUiNoteAttachedReminder
import com.elementary.tasks.notes.preview.reminders.UiNoteAttachedReminder
import com.elementary.tasks.notes.usecase.ChangeNoteArchiveStateUseCase
import com.elementary.tasks.notes.usecase.DeleteNoteUseCase
import com.elementary.tasks.reminder.usecase.SaveReminderUseCase
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.livedata.toSingleEvent
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.ReminderRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class PreviewNoteViewModel(
  val key: String,
  dispatcherProvider: DispatcherProvider,
  private val backupTool: BackupTool,
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
  private val saveReminderUseCase: SaveReminderUseCase
) : BaseProgressViewModel(dispatcherProvider) {

  private val _sharedFile = mutableLiveDataOf<Pair<NoteWithImages, File>>()
  val sharedFile = _sharedFile.toSingleEvent()

  private val _note = mutableLiveDataOf<UiNotePreview>()
  val note = _note.toLiveData()

  private val _reminders = mutableLiveDataOf<List<UiNoteAttachedReminder>>()
  val reminders = _reminders.toLiveData()

  var hasSameInDb: Boolean = false
  var isBgDark: Boolean = false
  private var initStatusBarColor: Int = -1
  private var statusBarColorSaved: Boolean = false

  @ColorInt
  fun getStatusBarColor(): Int? {
    return if (statusBarColorSaved) {
      initStatusBarColor.takeIf { it != -1 }
    } else {
      null
    }
  }

  fun saveStatusBarColor(@ColorInt color: Int) {
    if (statusBarColorSaved) return
    initStatusBarColor = color
    statusBarColorSaved = true
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
        _note.postValue(uiNotePreviewAdapter.convert(noteWithImages))
      }
      loadReminders()
    }
  }

  private suspend fun loadReminders() {
    val reminders = reminderRepository.getByNoteKey(key).map {
      reminderToUiNoteAttachedReminder(it)
    }
    _reminders.postValue(reminders)
  }

  fun showNoteInNotification(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = noteRepository.getById(id) ?: return@launch
      uiNoteNotificationAdapter.convert(noteWithImages).also {
        withUIContext { notifier.showNoteNotification(it) }
      }
    }
  }

  fun toggleArchiveFlag() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = noteRepository.getById(key)
      if (noteWithImages == null) {
        postInProgress(false)
        postError(textProvider.getText(R.string.notes_failed_to_update))
        return@launch
      }

      val note = noteWithImages.note
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

  fun deleteNote() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      deleteNoteUseCase(key)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  fun shareNote() {
    viewModelScope.launch(dispatcherProvider.default()) {
      postInProgress(true)
      val noteWithImages = noteRepository.getById(key)
      if (noteWithImages == null) {
        postInProgress(false)
        postCommand(Commands.FAILED)
        return@launch
      }
      val file = runBlocking {
        backupTool.noteToFile(noteWithImages)
      }
      postInProgress(false)
      if (file != null) {
        _sharedFile.postValue(Pair(noteWithImages, file))
      } else {
        postError(textProvider.getText(R.string.failed_to_send_note))
      }
    }
  }

  fun detachReminder(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderRepository.getById(id)

      if (reminder == null) {
        postInProgress(false)
        return@launch
      }

      reminder.noteId = ""

      saveReminderUseCase(reminder)

      loadReminders()
      postInProgress(false)
      postCommand(Commands.UPDATED)
    }
  }
}
