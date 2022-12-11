package com.elementary.tasks.core.view_models.notes

import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.notes.work.NoteSingleBackupWorker
import com.elementary.tasks.reminder.work.ReminderDeleteBackupWorker
import timber.log.Timber

class NoteViewModel(
  key: String,
  prefs: Prefs,
  private val calendarUtils: CalendarUtils,
  private val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  notesDao: NotesDao,
  private val reminderDao: ReminderDao,
  private val reminderGroupDao: ReminderGroupDao
) : BaseNotesViewModel(
  prefs,
  dispatcherProvider,
  workManagerProvider,
  notesDao
) {

  val note = notesDao.loadById(key)
  val reminder = reminderDao.loadByNoteKey(if (key == "") "1" else key)

  var hasSameInDb: Boolean = false

  fun findSame(id: String) {
    launchDefault {
      val note = notesDao.getById(id)
      hasSameInDb = note?.note != null
    }
  }

  fun saveNote(note: NoteWithImages, reminder: Reminder?) {
    val v = note.note ?: return
    postInProgress(true)
    launchDefault {
      v.updatedAt = TimeUtil.gmtDateTime
      Timber.d("saveNote: %s", note)
      saveImages(note.images, v.key)
      notesDao.insert(v)
      startWork(NoteSingleBackupWorker::class.java, Constants.INTENT_ID, v.key)
      postInProgress(false)
      postCommand(Commands.SAVED)
      if (reminder != null) {
        saveReminder(reminder)
      }
    }
  }

  private fun saveReminder(reminder: Reminder) {
    launchDefault {
      val group = reminderGroupDao.defaultGroup()
      if (group != null) {
        reminder.groupColor = group.groupColor
        reminder.groupTitle = group.groupTitle
        reminder.groupUuId = group.groupUuId
        reminderDao.insert(reminder)
      }
      if (reminder.groupUuId != "") {
        eventControlFactory.getController(reminder).start()
        startWork(com.elementary.tasks.reminder.work.ReminderSingleBackupWorker::class.java,
          Constants.INTENT_ID, reminder.uuId)
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
