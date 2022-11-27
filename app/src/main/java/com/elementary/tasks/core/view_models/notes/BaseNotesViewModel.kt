package com.elementary.tasks.core.view_models.notes

import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.notes.work.DeleteNoteBackupWorker
import com.elementary.tasks.notes.work.NoteSingleBackupWorker
import com.elementary.tasks.reminder.work.ReminderDeleteBackupWorker
import timber.log.Timber

abstract class BaseNotesViewModel(
  appDb: AppDb,
  prefs: Prefs,
  private val calendarUtils: CalendarUtils,
  private val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider
) : BaseDbViewModel(appDb, prefs, dispatcherProvider, workManagerProvider) {

  fun deleteNote(noteWithImages: NoteWithImages) {
    val note = noteWithImages.note ?: return
    postInProgress(true)
    launchDefault {
      appDb.notesDao().delete(note)
      for (image in noteWithImages.images) {
        appDb.notesDao().delete(image)
      }
      startWork(DeleteNoteBackupWorker::class.java, Constants.INTENT_ID, note.key)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  fun saveNoteColor(note: NoteWithImages, color: Int) {
    val v = note.note ?: return
    note.note?.color = color
    postInProgress(true)
    launchDefault {
      v.updatedAt = TimeUtil.gmtDateTime
      appDb.notesDao().insert(v)
      startWork(NoteSingleBackupWorker::class.java, Constants.INTENT_ID, v.key)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  fun saveNote(note: NoteWithImages) {
    val v = note.note ?: return
    postInProgress(true)
    launchDefault {
      v.updatedAt = TimeUtil.gmtDateTime
      saveImages(note.images, v.key)
      appDb.notesDao().insert(v)
      startWork(NoteSingleBackupWorker::class.java, Constants.INTENT_ID, v.key)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  private fun saveImages(list: List<ImageFile>, id: String) {
    val notesDao = appDb.notesDao()
    val oldList = notesDao.getImages(id)
    Timber.d("saveImages: ${oldList.size}")
    for (image in oldList) {
      Timber.d("saveImages: delete -> ${image.id}, ${image.noteId}")
      notesDao.delete(image)
    }
    if (list.isNotEmpty()) {
      val newList = list.map {
        it.noteId = id
        it
      }
      Timber.d("saveImages: new list -> $newList")
      notesDao.insertAll(newList)
    }
  }

  fun saveNote(note: NoteWithImages, reminder: Reminder?) {
    val v = note.note ?: return
    postInProgress(true)
    launchDefault {
      v.updatedAt = TimeUtil.gmtDateTime
      Timber.d("saveNote: %s", note)
      saveImages(note.images, v.key)
      appDb.notesDao().insert(v)
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
      val group = appDb.reminderGroupDao().defaultGroup()
      if (group != null) {
        reminder.groupColor = group.groupColor
        reminder.groupTitle = group.groupTitle
        reminder.groupUuId = group.groupUuId
        appDb.reminderDao().insert(reminder)
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
      appDb.reminderDao().delete(reminder)
      calendarUtils.deleteEvents(reminder.uuId)
      startWork(ReminderDeleteBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
      postInProgress(false)
      postCommand(Commands.UPDATED)
    }
  }
}
