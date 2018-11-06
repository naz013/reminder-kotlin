package com.elementary.tasks.core.viewModels.dayVew

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.elementary.tasks.R
import com.elementary.tasks.dayView.DayViewProvider
import com.elementary.tasks.dayView.EventsDataSingleton
import com.elementary.tasks.dayView.EventModel
import com.elementary.tasks.dayView.EventsPagerItem
import com.elementary.tasks.birthdays.work.DeleteBackupWorker
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.viewModels.BaseDbViewModel
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.reminder.work.SingleBackupWorker
import kotlinx.coroutines.experimental.CommonPool
import com.elementary.tasks.core.utils.temp.UI
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
    var events: LiveData<List<EventModel>> = liveData
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
                    .setInputData(Data.Builder().putString(Constants.INTENT_ID, birthday.uuId).build())
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
            val work = OneTimeWorkRequest.Builder(SingleBackupWorker::class.java)
                    .setInputData(Data.Builder().putString(Constants.INTENT_ID, reminder.uuId).build())
                    .addTag(reminder.uuId)
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }

    private inner class DayViewLiveData internal constructor() : LiveData<List<EventModel>>(), DayViewProvider.Callback, DayViewProvider.InitCallback {

        init {
            val provider = EventsDataSingleton.getInstance().provider
            provider?.addObserver(this)
        }

        internal fun update() {
            val provider = EventsDataSingleton.getInstance().provider
            val model = item ?: return
            provider?.findMatches(model.day, model.month, model.year, true, this)
        }

        override fun apply(list: List<EventModel>) {
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
