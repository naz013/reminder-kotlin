package com.elementary.tasks.notes.preview

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.note.UiNotePreviewAdapter
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.note.UiNotePreview
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.notes.work.DeleteNoteBackupWorker
import com.elementary.tasks.reminder.work.ReminderDeleteBackupWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class NotePreviewViewModel(
  val key: String,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val backupTool: BackupTool,
  private val notesDao: NotesDao,
  private val reminderDao: ReminderDao,
  private val uiNotePreviewAdapter: UiNotePreviewAdapter,
  private val textProvider: TextProvider
) : BaseProgressViewModel(dispatcherProvider) {

  private val _sharedFile = mutableLiveDataOf<Pair<NoteWithImages, File>>()
  val sharedFile = _sharedFile.toLiveData()

  private val _note = mutableLiveDataOf<UiNotePreview>()
  val note = _note.toLiveData()

  val reminder = reminderDao.loadByNoteKey(if (key == "") "1" else key)

  var hasSameInDb: Boolean = false

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = notesDao.getById(key)
      if (noteWithImages != null) {
        _note.postValue(uiNotePreviewAdapter.convert(noteWithImages))
      }
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

  fun deleteReminder(reminder: Reminder) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      eventControlFactory.getController(reminder).stop()
      reminderDao.delete(reminder)
      googleCalendarUtils.deleteEvents(reminder.uuId)
      workerLauncher.startWork(
        ReminderDeleteBackupWorker::class.java,
        Constants.INTENT_ID,
        reminder.uuId
      )
      postInProgress(false)
      postCommand(Commands.UPDATED)
    }
  }
}