package com.elementary.tasks.core.view_models.notes

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.notes.work.NoteSingleBackupWorker
import com.elementary.tasks.reminder.work.ReminderDeleteBackupWorker
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import kotlinx.coroutines.launch
import timber.log.Timber

class NoteViewModel(
  key: String,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  workerLauncher: WorkerLauncher,
  notesDao: NotesDao,
  private val reminderDao: ReminderDao,
  private val reminderGroupDao: ReminderGroupDao
) : BaseNotesViewModel(dispatcherProvider, workerLauncher, notesDao) {

  val note = notesDao.loadById(key)
  val reminder = reminderDao.loadByNoteKey(if (key == "") "1" else key)

  var hasSameInDb: Boolean = false

  fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val note = notesDao.getById(id)
      hasSameInDb = note?.note != null
    }
  }

  fun saveNote(note: NoteWithImages, reminder: Reminder?) {
    val v = note.note ?: return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      v.updatedAt = DateTimeManager.gmtDateTime
      Timber.d("saveNote: %s", note)
      saveImages(note.images, v.key)
      notesDao.insert(v)
      workerLauncher.startWork(NoteSingleBackupWorker::class.java, Constants.INTENT_ID, v.key)
      postInProgress(false)
      postCommand(Commands.SAVED)
      if (reminder != null) {
        saveReminder(reminder)
      }
    }
  }

  private fun saveReminder(reminder: Reminder) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val group = reminderGroupDao.defaultGroup()
      if (group != null) {
        reminder.groupColor = group.groupColor
        reminder.groupTitle = group.groupTitle
        reminder.groupUuId = group.groupUuId
        reminderDao.insert(reminder)
      }
      if (reminder.groupUuId != "") {
        eventControlFactory.getController(reminder).start()
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          Constants.INTENT_ID, reminder.uuId
        )
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
