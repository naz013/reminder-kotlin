package com.elementary.tasks.core.viewModels.dayVew

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.toWorkData
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.DayViewProvider
import com.elementary.tasks.birthdays.EventsDataSingleton
import com.elementary.tasks.birthdays.EventsItem
import com.elementary.tasks.birthdays.EventsPagerItem
import com.elementary.tasks.birthdays.work.DeleteBackupWorker
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.viewModels.BaseDbViewModel
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.reminder.work.UpdateFilesAsync
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

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
class DayViewViewModel(application: Application) : BaseDbViewModel(application) {

    private val liveData = DayViewLiveData()
    var events: LiveData<List<EventsItem>> = liveData
    private var item: EventsPagerItem? = null

    fun setItem(item: EventsPagerItem?) {
        this.item = item
        liveData.update()
    }

    fun deleteBirthday(birthday: Birthday) {
        isInProgress.postValue(true)
        launch(CommonPool) {
            appDb.birthdaysDao().delete(birthday)
            withContext(UI) {
                isInProgress.postValue(false)
                result.postValue(Commands.DELETED)
                liveData.update()
            }
            val work = OneTimeWorkRequest.Builder(DeleteBackupWorker::class.java)
                    .setInputData(mapOf(Constants.INTENT_ID to birthday.uuId).toWorkData())
                    .addTag(birthday.uuId)
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }

    fun moveToTrash(reminder: Reminder) {
        isInProgress.postValue(true)
        launch(CommonPool) {
            reminder.isRemoved = true
            EventControlFactory.getController(reminder).stop()
            appDb.reminderDao().insert(reminder)
            withContext(UI) {
                isInProgress.postValue(false)
                result.postValue(Commands.DELETED)
                Toast.makeText(getApplication(), R.string.deleted, Toast.LENGTH_SHORT).show()
                liveData.update()
            }
            UpdateFilesAsync(getApplication()).execute(reminder)
        }
    }

    private inner class DayViewLiveData internal constructor() : LiveData<List<EventsItem>>(), DayViewProvider.Callback, DayViewProvider.InitCallback {

        init {
            val provider = EventsDataSingleton.getInstance().provider
            provider?.addObserver(this)
        }

        internal fun update() {
            val provider = EventsDataSingleton.getInstance().provider
            if (provider != null && item != null) {
                provider.findMatches(item!!.day, item!!.month, item!!.year, true, this)
            }
        }

        override fun apply(list: List<EventsItem>) {
            postValue(list)
        }

        override fun onInactive() {
            super.onInactive()
            val provider = EventsDataSingleton.getInstance().provider
            if (provider != null) {
                provider.removeCallback(this)
                provider.removeObserver(this)
            }
        }

        override fun onFinish() {
            update()
        }
    }
}
