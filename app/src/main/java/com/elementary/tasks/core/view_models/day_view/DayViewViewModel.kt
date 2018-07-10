package com.elementary.tasks.core.view_models.day_view

import android.app.Application
import android.widget.Toast

import com.elementary.tasks.R
import com.elementary.tasks.birthdays.DayViewProvider
import com.elementary.tasks.birthdays.EventsDataSingleton
import com.elementary.tasks.birthdays.EventsItem
import com.elementary.tasks.birthdays.EventsPagerItem
import com.elementary.tasks.birthdays.work.DeleteBirthdayFilesAsync
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.reminder.work.UpdateFilesAsync
import androidx.lifecycle.LiveData

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
        run {
            appDb!!.birthdaysDao().delete(birthday)

            end {
                isInProgress.postValue(false)
                result.postValue(Commands.DELETED)
                liveData.update()
            }
            DeleteBirthdayFilesAsync(getApplication()).execute(birthday.uuId)
        }
    }

    fun moveToTrash(reminder: Reminder) {
        isInProgress.postValue(true)
        run {
            reminder.isRemoved = true
            EventControlFactory.getController(reminder).stop()
            appDb!!.reminderDao().insert(reminder)
            end {
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
