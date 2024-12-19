package com.elementary.tasks.notes.preview

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.note.UiNoteNotificationAdapter
import com.elementary.tasks.core.data.adapter.note.UiNotePreviewAdapter
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.repository.NoteImageRepository
import com.elementary.tasks.core.data.ui.note.UiNotePreview
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.notes.preview.reminders.ReminderToUiNoteAttachedReminder
import com.elementary.tasks.notes.preview.reminders.UiNoteAttachedReminder
import com.elementary.tasks.notes.work.DeleteNoteBackupWorker
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class NotePreviewViewModel(
    val key: String,
    dispatcherProvider: DispatcherProvider,
    private val workerLauncher: WorkerLauncher,
    private val backupTool: BackupTool,
    private val notesDao: NotesDao,
    private val reminderDao: ReminderDao,
    private val uiNotePreviewAdapter: UiNotePreviewAdapter,
    private val textProvider: TextProvider,
    private val analyticsEventSender: AnalyticsEventSender,
    private val noteImageRepository: NoteImageRepository,
    private val uiNoteNotificationAdapter: UiNoteNotificationAdapter,
    private val notifier: Notifier,
    private val reminderToUiNoteAttachedReminder: ReminderToUiNoteAttachedReminder
) : BaseProgressViewModel(dispatcherProvider) {

  private val _sharedFile = mutableLiveDataOf<Pair<NoteWithImages, File>>()
  val sharedFile = _sharedFile.toLiveData()

  private val _note = mutableLiveDataOf<UiNotePreview>()
  val note = _note.toLiveData()

  private val _reminders = mutableLiveDataOf<List<UiNoteAttachedReminder>>()
  val reminders = _reminders.toLiveData()

  var hasSameInDb: Boolean = false

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
      val noteWithImages = notesDao.getById(key)
      if (noteWithImages != null) {
        _note.postValue(uiNotePreviewAdapter.convert(noteWithImages))
      }
      loadReminders()
    }
  }

  private fun loadReminders() {
    val reminders = reminderDao.getByNoteKey(key).map {
      reminderToUiNoteAttachedReminder(it)
    }
    _reminders.postValue(reminders)
  }

  fun showNoteInNotification(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = notesDao.getById(id) ?: return@launch
      uiNoteNotificationAdapter.convert(noteWithImages).also {
        withUIContext { notifier.showNoteNotification(it) }
      }
    }
  }

  fun toggleArchiveFlag() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = notesDao.getById(key)
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

      note.archived = !note.archived
      notesDao.insert(note)

      workerLauncher.startWork(DeleteNoteBackupWorker::class.java, Constants.INTENT_ID, note.key)

      loadInternal()

      postInProgress(false)
      postCommand(Commands.UPDATED)
    }
  }

  fun deleteNote() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = notesDao.getById(key)
      if (noteWithImages == null) {
        postInProgress(false)
        postCommand(Commands.FAILED)
        return@launch
      }
      val note = noteWithImages.note
      if (note == null) {
        postInProgress(false)
        postCommand(Commands.FAILED)
        return@launch
      }
      notesDao.delete(note)
      for (image in noteWithImages.images) {
        notesDao.delete(image)
      }
      noteImageRepository.clearFolder(note.key)
      workerLauncher.startWork(DeleteNoteBackupWorker::class.java, Constants.INTENT_ID, note.key)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  fun shareNote() {
    viewModelScope.launch(dispatcherProvider.default()) {
      postInProgress(true)
      val noteWithImages = notesDao.getById(key)
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
      val reminder = reminderDao.getById(id)

      if (reminder == null) {
        postInProgress(false)
        return@launch
      }

      reminder.noteId = ""

      reminderDao.insert(reminder)
      workerLauncher.startWork(
        ReminderSingleBackupWorker::class.java,
        Constants.INTENT_ID,
        reminder.uuId
      )

      loadReminders()
      postInProgress(false)
      postCommand(Commands.UPDATED)
    }
  }
}
