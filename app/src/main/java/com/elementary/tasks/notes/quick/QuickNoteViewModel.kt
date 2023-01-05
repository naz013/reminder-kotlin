package com.elementary.tasks.notes.quick

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.notes.work.NoteSingleBackupWorker
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import kotlinx.coroutines.launch
import timber.log.Timber

class QuickNoteViewModel(
  private val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val notesDao: NotesDao,
  private val reminderDao: ReminderDao,
  private val reminderGroupDao: ReminderGroupDao
) : BaseProgressViewModel(dispatcherProvider) {

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

  private fun saveImages(list: List<ImageFile>, id: String) {
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
}
