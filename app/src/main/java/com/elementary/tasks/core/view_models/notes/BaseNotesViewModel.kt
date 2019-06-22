package com.elementary.tasks.core.view_models.notes

import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.notes.work.DeleteNoteBackupWorker
import com.elementary.tasks.notes.work.SingleBackupWorker
import com.elementary.tasks.reminder.work.DeleteBackupWorker
import kotlinx.coroutines.runBlocking
import org.koin.core.inject
import timber.log.Timber

abstract class BaseNotesViewModel : BaseDbViewModel() {

    protected val calendarUtils: CalendarUtils by inject()

    fun deleteNote(noteWithImages: NoteWithImages) {
        val note = noteWithImages.note ?: return
        postInProgress(true)
        launchDefault {
            runBlocking {
                appDb.notesDao().delete(note)
                for (image in noteWithImages.images) {
                    appDb.notesDao().delete(image)
                }
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
            runBlocking {
                appDb.notesDao().insert(v)
            }
            startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, v.key)
            postInProgress(false)
            postCommand(Commands.SAVED)
        }
    }

    fun saveNote(note: NoteWithImages) {
        val v = note.note ?: return
        postInProgress(true)
        launchDefault {
            v.updatedAt = TimeUtil.gmtDateTime
            runBlocking {
                saveImages(note.images, v.key)
                appDb.notesDao().insert(v)
            }
            startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, v.key)
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
            runBlocking {
                saveImages(note.images, v.key)
                appDb.notesDao().insert(v)
            }
            startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, v.key)
            postInProgress(false)
            postCommand(Commands.SAVED)
            if (reminder != null) {
                saveReminder(reminder)
            }
        }
    }

    private fun saveReminder(reminder: Reminder) {
        launchDefault {
            runBlocking {
                val group = appDb.reminderGroupDao().defaultGroup()
                if (group != null) {
                    reminder.groupColor = group.groupColor
                    reminder.groupTitle = group.groupTitle
                    reminder.groupUuId = group.groupUuId
                }
                appDb.reminderDao().insert(reminder)
            }
            EventControlFactory.getController(reminder).start()
            startWork(com.elementary.tasks.reminder.work.SingleBackupWorker::class.java,
                    Constants.INTENT_ID, reminder.uuId)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        postInProgress(true)
        launchDefault {
            runBlocking {
                EventControlFactory.getController(reminder).stop()
                appDb.reminderDao().delete(reminder)
                calendarUtils.deleteEvents(reminder.uuId)
            }
            startWork(DeleteBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
            postInProgress(false)
            postCommand(Commands.UPDATED)
        }
    }
}
