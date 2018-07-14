package com.elementary.tasks.core.viewModels.notes

import android.app.Application

import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.viewModels.BaseDbViewModel
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.notes.work.DeleteNoteFilesAsync
import com.elementary.tasks.reminder.work.DeleteFilesAsync

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
internal abstract class BaseNotesViewModel(application: Application) : BaseDbViewModel(application) {

    fun deleteNote(note: Note) {
        isInProgress.postValue(true)
        run {
            appDb!!.notesDao().delete(note)
            end {
                isInProgress.postValue(false)
                result.postValue(Commands.DELETED)
            }
            DeleteNoteFilesAsync(getApplication()).execute(note.key)
        }
    }

    fun saveNote(note: Note) {
        isInProgress.postValue(true)
        run {
            appDb!!.notesDao().insert(note)
            end {
                isInProgress.postValue(false)
                result.postValue(Commands.SAVED)
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        isInProgress.postValue(true)
        run {
            EventControlFactory.getController(reminder).stop()
            appDb!!.reminderDao().delete(reminder)
            end {
                isInProgress.postValue(false)
                result.postValue(Commands.UPDATED)
            }
            CalendarUtils.deleteEvents(getApplication(), reminder.uniqueId)
            DeleteFilesAsync(getApplication()).execute(reminder.uuId)
        }
    }
}