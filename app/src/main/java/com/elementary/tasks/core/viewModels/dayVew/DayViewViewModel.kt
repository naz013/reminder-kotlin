package com.elementary.tasks.core.viewModels.dayVew

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.work.DeleteBackupWorker
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.viewModels.BaseDbViewModel
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.dayView.DayViewProvider
import com.elementary.tasks.dayView.EventsPagerItem
import com.elementary.tasks.dayView.day.EventModel
import com.elementary.tasks.reminder.work.SingleBackupWorker
import kotlinx.coroutines.Job
import timber.log.Timber
import java.util.*

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
class DayViewViewModel private constructor(application: Application,
                                           private val calculateFuture: Boolean,
                                           private val birthTime: Long = 0) : BaseDbViewModel(application) {

    private var liveData: DayViewLiveData

    private var _events: MutableLiveData<Pair<EventsPagerItem, List<EventModel>>> = MutableLiveData()
    var events: LiveData<Pair<EventsPagerItem, List<EventModel>>> = _events

    private var _groups: MutableList<ReminderGroup> = mutableListOf()
    var groups: List<ReminderGroup> = listOf()
        get() = _groups

    init {
        appDb.reminderGroupDao().loadAll().observeForever{
            if (it != null) {
                _groups.clear()
                _groups.addAll(it)
            }
        }
        liveData = DayViewLiveData()
    }

    fun findEvents(item: EventsPagerItem) {
        try {
            liveData.findEvents(item, true) { eventsPagerItem, list ->
                _events.postValue(Pair(eventsPagerItem, list))
            }
        } catch (e: UninitializedPropertyAccessException) {
        }
    }

    fun saveReminder(reminder: Reminder) {
        postInProgress(true)
        launchDefault {
            appDb.reminderDao().insert(reminder)
            withUIContext {
                postInProgress(false)
                postCommand(Commands.SAVED)
            }
            startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
        }
    }

    fun deleteBirthday(birthday: Birthday) {
        postInProgress(true)
        launchDefault {
            appDb.birthdaysDao().delete(birthday)
            withUIContext {
                postInProgress(false)
                postCommand(Commands.DELETED)
            }
            startWork(DeleteBackupWorker::class.java, Constants.INTENT_ID, birthday.uuId)
        }
    }

    fun moveToTrash(reminder: Reminder) {
        postInProgress(true)
        launchDefault {
            reminder.isRemoved = true
            EventControlFactory.getController(reminder).stop()
            appDb.reminderDao().insert(reminder)
            withUIContext {
                postInProgress(false)
                postCommand(Commands.DELETED)
                Toast.makeText(getApplication(), R.string.deleted, Toast.LENGTH_SHORT).show()
            }
            startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
        }
    }

    private inner class DayViewLiveData internal constructor() : LiveData<Pair<EventsPagerItem, List<EventModel>>>() {

        private val reminderData = ArrayList<EventModel>()
        private val birthdayData = ArrayList<EventModel>()
        private val birthdays = appDb.birthdaysDao().loadAll()
        private val reminders = appDb.reminderDao().loadType(true, false)

        private var eventsPagerItem: EventsPagerItem? = null
        private var job: Job? = null
        private var listener: ((EventsPagerItem, List<EventModel>) -> Unit)? = null
        private var sort = false

        private val birthdayObserver: Observer<in List<Birthday>> = Observer {
            Timber.d("birthdaysChanged: ")
            launchDefault {
                if (it != null) {
                    birthdayData.clear()
                    birthdayData.addAll(DayViewProvider.loadBirthdays(birthTime, it))
                    repeatSearch()
                }
            }
        }
        private val reminderObserver: Observer<in List<Reminder>> = Observer {
            Timber.d("remindersChanged: ")
            launchDefault {
                if (it != null) {
                    reminderData.clear()
                    reminderData.addAll(DayViewProvider.loadReminders(calculateFuture, it))
                    repeatSearch()
                }
            }
        }

        init {
            birthdays.observeForever(birthdayObserver)
            reminders.observeForever(reminderObserver)
        }

        fun findEvents(eventsPagerItem: EventsPagerItem, sort: Boolean, listener: ((EventsPagerItem, List<EventModel>) -> Unit)?) {
            if (listener == null) return
            this.listener = listener
            this.eventsPagerItem = eventsPagerItem
            this.sort = sort
            val toSearch = mutableListOf<EventModel>()
            toSearch.addAll(birthdayData)
            toSearch.addAll(reminderData)
            findMatches(toSearch, eventsPagerItem, sort)
        }

        override fun onInactive() {
            super.onInactive()
            Timber.d("onInactive: ")
            birthdays.observeForever(birthdayObserver)
            reminders.observeForever(reminderObserver)
            this.eventsPagerItem = null
        }

        override fun onActive() {
            super.onActive()
            Timber.d("onActive: ")
            birthdays.removeObserver(birthdayObserver)
            reminders.removeObserver(reminderObserver)
        }

        private fun notifyObserver(eventsPagerItem: EventsPagerItem, list: List<EventModel>) {
            listener?.invoke(eventsPagerItem, list)
        }

        private fun repeatSearch() {
            val item = eventsPagerItem ?: return
            findEvents(item, this.sort, listener)
        }

        private fun findMatches(list: List<EventModel>, eventsPagerItem: EventsPagerItem, sort: Boolean) {
            this.job?.cancel()
            this.job = launchDefault {
                val res = ArrayList<EventModel>()
                Timber.d("Search events: $eventsPagerItem")
                for (item in list) {
                    val mDay = item.day
                    val mMonth = item.month
                    val mYear = item.year
                    val type = item.viewType
                    if (type == EventModel.BIRTHDAY && mDay == eventsPagerItem.day && mMonth == eventsPagerItem.month) {
                        res.add(item)
                    } else {
                        if (mDay == eventsPagerItem.day && mMonth == eventsPagerItem.month && mYear == eventsPagerItem.year) {
                            res.add(item)
                        }
                    }
                }
                Timber.d("Search events: found -> %d", res.size)
                if (!sort) {
                    withUIContext { notifyObserver(eventsPagerItem, res) }
                } else {
                    res.sortWith(Comparator { eventsItem, t1 ->
                        var time1: Long = 0
                        var time2: Long = 0
                        if (eventsItem.model is Birthday) {
                            val item = eventsItem.model as Birthday
                            val dateItem = TimeUtil.getFutureBirthdayDate(birthTime, item.date)
                            if (dateItem != null) {
                                val calendar = dateItem.calendar
                                time1 = calendar.timeInMillis
                            }
                        } else if (eventsItem.model is Reminder) {
                            val reminder = eventsItem.model as Reminder
                            time1 = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
                        }
                        if (t1.model is Birthday) {
                            val item = t1.model as Birthday
                            val dateItem = TimeUtil.getFutureBirthdayDate(birthTime, item.date)
                            if (dateItem != null) {
                                val calendar = dateItem.calendar
                                time2 = calendar.timeInMillis
                            }
                        } else if (t1.model is Reminder) {
                            val reminder = t1.model as Reminder
                            time2 = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
                        }
                        (time1 - time2).toInt()
                    })
                    withUIContext { notifyObserver(eventsPagerItem, res) }
                }
            }
        }
    }

    class Factory(private val application: Application,
                  private val calculateFuture: Boolean,
                  private val birthTime: Long = 0) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DayViewViewModel(application, calculateFuture, birthTime) as T
        }
    }
}
