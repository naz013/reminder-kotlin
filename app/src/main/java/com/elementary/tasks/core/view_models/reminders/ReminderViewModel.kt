package com.elementary.tasks.core.view_models.reminders

import androidx.lifecycle.*
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.launchIo
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
class ReminderViewModel private constructor(id: String) : BaseRemindersViewModel() {

    private val _note = MutableLiveData<NoteWithImages>()
    val note: LiveData<NoteWithImages> = _note
    private val _googleTask = MutableLiveData<Pair<GoogleTaskList?, GoogleTask?>>()
    val googleTask: LiveData<Pair<GoogleTaskList?, GoogleTask?>> = _googleTask

    var reminder: LiveData<Reminder>
    private val mObserver = Observer<Reminder> {
        Timber.d("ReminderViewModel: $it")
    }

    init {
        reminder = appDb.reminderDao().loadById(id)
        reminder.observeForever(mObserver)
    }

    fun loadExtra(reminder: Reminder) {
        launchIo {
            _note.postValue(appDb.notesDao().getById(reminder.noteId))
            val googleTask = appDb.googleTasksDao().getByReminderId(reminder.uuId)
            if (googleTask != null) {
                _googleTask.postValue(Pair(appDb.googleTaskListsDao().getById(googleTask.listId), googleTask))
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        reminder.removeObserver(mObserver)
    }

    class Factory(private val id: String) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReminderViewModel(id) as T
        }
    }
}
