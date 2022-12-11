package com.elementary.tasks.core.view_models.notes

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.notes.work.DeleteNoteBackupWorker
import com.elementary.tasks.reminder.work.ReminderDeleteBackupWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class NotePreviewViewModel(
  key: String,
  prefs: Prefs,
  private val calendarUtils: CalendarUtils,
  private val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  private val backupTool: BackupTool,
  notesDao: NotesDao,
  private val reminderDao: ReminderDao
) : BaseNotesViewModel(
  prefs,
  dispatcherProvider,
  workManagerProvider,
  notesDao
) {

  private val _sharedFile = mutableLiveDataOf<Pair<NoteWithImages, File>>()
  val sharedFile = _sharedFile.toLiveData()

  val note = notesDao.loadById(key)
  val reminder = reminderDao.loadByNoteKey(if (key == "") "1" else key)

  var hasSameInDb: Boolean = false

  fun deleteNote() {
    val noteWithImages = note.value ?: return
    val note = noteWithImages.note ?: return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      runBlocking(dispatcherProvider.io()) {
        notesDao.delete(note)
        for (image in noteWithImages.images) {
          notesDao.delete(image)
        }
      }
      startWork(DeleteNoteBackupWorker::class.java, Constants.INTENT_ID, note.key)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  fun shareNote() {
    val note = note.value ?: return
    viewModelScope.launch(dispatcherProvider.default()) {
      postInProgress(true)
      val file = runBlocking {
        backupTool.noteToFile(note)
      }
      postInProgress(false)
      if (file != null) {
        _sharedFile.postValue(Pair(note, file))
      } else {
        postError("Failed to send Note")
      }
    }
  }

  fun deleteReminder(reminder: Reminder) {
    postInProgress(true)
    launchDefault {
      eventControlFactory.getController(reminder).stop()
      reminderDao.delete(reminder)
      calendarUtils.deleteEvents(reminder.uuId)
      startWork(ReminderDeleteBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
      postInProgress(false)
      postCommand(Commands.UPDATED)
    }
  }
}
