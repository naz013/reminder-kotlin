package com.elementary.tasks.core.viewModels.notes

import android.app.Application
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.toWorkData
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.viewModels.BaseDbViewModel
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.notes.work.DeleteNoteBackupWorker
import com.elementary.tasks.notes.work.SingleBackupWorker
import com.elementary.tasks.reminder.work.DeleteBackupWorker
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

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

    fun deleteNote(note: Note) {
        isInProgress.postValue(true)
        launch(CommonPool) {
            appDb.notesDao().delete(note)
            withUIContext {
                isInProgress.postValue(false)
                result.postValue(Commands.DELETED)
            }
            val work = OneTimeWorkRequest.Builder(DeleteNoteBackupWorker::class.java)
                    .setInputData(mapOf(Constants.INTENT_ID to note.key).toWorkData())
                    .addTag(note.key)
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }

    fun saveNote(note: Note) {
        isInProgress.postValue(true)
        launch(CommonPool) {
            appDb.notesDao().insert(note)
            withUIContext {
                isInProgress.postValue(false)
                result.postValue(Commands.SAVED)
            }
            val work = OneTimeWorkRequest.Builder(SingleBackupWorker::class.java)
                    .setInputData(mapOf(Constants.INTENT_ID to note.key).toWorkData())
                    .addTag(note.key)
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        isInProgress.postValue(true)
        launch(CommonPool) {
            EventControlFactory.getController(reminder).stop()
            appDb.reminderDao().delete(reminder)
            withUIContext {
                isInProgress.postValue(false)
                result.postValue(Commands.UPDATED)
            }
            calendarUtils.deleteEvents(reminder.uniqueId)
            val work = OneTimeWorkRequest.Builder(DeleteBackupWorker::class.java)
                    .setInputData(mapOf(Constants.INTENT_ID to reminder.uuId).toWorkData())
                    .addTag(reminder.uuId)
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }
}
