package com.elementary.tasks.core.viewModels.notes

import android.app.Application
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.viewModels.BaseDbViewModel
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.notes.work.DeleteNoteBackupWorker
import com.elementary.tasks.notes.work.SingleBackupWorker
import com.elementary.tasks.reminder.work.DeleteBackupWorker
import timber.log.Timber

/**
 * Copyright 2018 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
abstract class BaseNotesViewModel(application: Application) : BaseDbViewModel(application) {

    fun deleteNote(noteWithImages: NoteWithImages) {
        val note = noteWithImages.note ?: return
        isInProgress.postValue(true)
        launchDefault {
            appDb.notesDao().delete(note)
            appDb.notesDao().deleteAllImages(note.key)
            withUIContext {
                isInProgress.postValue(false)
                result.postValue(Commands.DELETED)
            }
            val work = OneTimeWorkRequest.Builder(DeleteNoteBackupWorker::class.java)
                    .setInputData(Data.Builder().putString(Constants.INTENT_ID, note.key).build())
                    .addTag(note.key)
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }

    fun saveNote(note: NoteWithImages) {
        val v = note.note ?: return
        isInProgress.postValue(true)
        launchDefault {
            appDb.notesDao().insert(v)
            if (note.images.isNotEmpty()) {
                note.images = note.images.map {
                    it.noteId = v.key
                    it
                }
                appDb.notesDao().insertAll(note.images)
            }
            withUIContext {
                isInProgress.postValue(false)
                result.postValue(Commands.SAVED)
            }
            val work = OneTimeWorkRequest.Builder(SingleBackupWorker::class.java)
                    .setInputData(Data.Builder().putString(Constants.INTENT_ID, v.key).build())
                    .addTag(v.key)
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }

    fun saveNote(note: NoteWithImages, reminder: Reminder?) {
        val v = note.note ?: return
        isInProgress.postValue(true)
        launchDefault {
            if (note.images.isNotEmpty()) {
                note.images = note.images.map {
                    it.noteId = v.key
                    it
                }
                appDb.notesDao().insertAll(note.images)
            }
            Timber.d("saveNote: %s", note)
            appDb.notesDao().insert(v)
            withUIContext {
                if (reminder != null) {
                    saveReminder(reminder)
                }
                isInProgress.postValue(false)
                result.postValue(Commands.SAVED)
            }
            val work = OneTimeWorkRequest.Builder(SingleBackupWorker::class.java)
                    .setInputData(Data.Builder().putString(Constants.INTENT_ID, v.key).build())
                    .addTag(v.key)
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }

    private fun saveReminder(reminder: Reminder) {
        launchDefault {
            val group = appDb.reminderGroupDao().defaultGroup()
            if (group != null) {
                reminder.groupColor = group.groupColor
                reminder.groupTitle = group.groupTitle
                reminder.groupUuId = group.groupUuId
            }

            appDb.reminderDao().insert(reminder)
            EventControlFactory.getController(reminder).start()
            val work = OneTimeWorkRequest.Builder(com.elementary.tasks.reminder.work.SingleBackupWorker::class.java)
                    .setInputData(Data.Builder().putString(Constants.INTENT_ID, reminder.uuId).build())
                    .addTag(reminder.uuId)
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        isInProgress.postValue(true)
        launchDefault {
            EventControlFactory.getController(reminder).stop()
            appDb.reminderDao().delete(reminder)
            withUIContext {
                isInProgress.postValue(false)
                result.postValue(Commands.UPDATED)
            }
            calendarUtils.deleteEvents(reminder.uniqueId)
            val work = OneTimeWorkRequest.Builder(DeleteBackupWorker::class.java)
                    .setInputData(Data.Builder().putString(Constants.INTENT_ID, reminder.uuId).build())
                    .addTag(reminder.uuId)
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }
}
