package com.elementary.tasks.core.viewModels.monthView

import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.viewModels.BaseDbViewModel
import com.elementary.tasks.dayView.DayViewProvider
import com.elementary.tasks.dayView.day.EventModel
import com.elementary.tasks.monthView.MonthPagerItem
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
class MonthViewViewModel private constructor(application: Application,
                                             private val addReminders: Boolean,
                                             private val calculateFuture: Boolean,
                                             private val birthTime: Long = 0) : BaseDbViewModel(application) {

    private var liveData: MonthViewLiveData
    var events: MutableLiveData<Pair<MonthPagerItem, List<EventModel>>> = MutableLiveData()

    init {
        liveData = MonthViewLiveData()
    }

    fun findEvents(item: MonthPagerItem) {
        try {
            liveData.findEvents(item, false) { eventsPagerItem, list ->
                events.postValue(Pair(eventsPagerItem, list))
            }
        } catch (e: UninitializedPropertyAccessException) {
        }
    }

    private inner class MonthViewLiveData internal constructor() : LiveData<Pair<MonthPagerItem, List<EventModel>>>() {

        private val reminderData = ArrayList<EventModel>()
        private val birthdayData = ArrayList<EventModel>()
        private val birthdays = appDb.birthdaysDao().loadAll()
        private val reminders = appDb.reminderDao().loadType(true, false)

        private var monthPagerItem: MonthPagerItem? = null
        private var job: Job? = null
        private var listener: ((MonthPagerItem, List<EventModel>) -> Unit)? = null
        private var sort = false

        private val birthdayObserver: Observer<in List<Birthday>> = Observer {
            launchDefault {
                if (it != null) {
                    birthdayData.clear()
                    birthdayData.addAll(DayViewProvider.loadBirthdays(birthTime, it))
                    repeatSearch()
                }
            }
        }
        private val reminderObserver: Observer<in List<Reminder>> = Observer {
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
            if (addReminders) {
                reminders.observeForever(reminderObserver)
            }
        }

        fun findEvents(monthPagerItem: MonthPagerItem, sort: Boolean, listener: ((MonthPagerItem, List<EventModel>) -> Unit)) {
            this.listener = listener
            this.monthPagerItem = monthPagerItem
            this.sort = sort
            val toSearch = mutableListOf<EventModel>()
            toSearch.addAll(birthdayData)
            if (addReminders) {
                toSearch.addAll(reminderData)
            }
            findMatches(toSearch, monthPagerItem, sort)
        }

        override fun onInactive() {
            super.onInactive()
            birthdays.observeForever(birthdayObserver)
            if (addReminders) {
                reminders.observeForever(reminderObserver)
            }
        }

        override fun onActive() {
            super.onActive()
            birthdays.removeObserver(birthdayObserver)
            if (addReminders) {
                reminders.removeObserver(reminderObserver)
            }
        }

        private fun notifyObserver(monthPagerItem: MonthPagerItem, list: List<EventModel>) {
            listener?.invoke(monthPagerItem, list)
        }

        private fun repeatSearch() {
            val item = monthPagerItem ?: return
            val callback = listener?: return
            findEvents(item, this.sort, callback)
        }

        private fun findMatches(list: List<EventModel>, monthPagerItem: MonthPagerItem, sort: Boolean) {
            this.job?.cancel()
            this.job = launchDefault {
                val res = ArrayList<EventModel>()
                Timber.d("Search events: $monthPagerItem")
                for (item in list) {
                    val mMonth = item.month
                    val mYear = item.year
                    val type = item.viewType
                    if (type == EventModel.BIRTHDAY && mMonth == monthPagerItem.month) {
                        res.add(item)
                    } else if (mMonth == monthPagerItem.month && mYear == monthPagerItem.year) {
                        res.add(item)
                    }
                }
                Timber.d("Search events: found -> %d", res.size)
                if (!sort) {
                    withUIContext { notifyObserver(monthPagerItem, res) }
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
                    withUIContext { notifyObserver(monthPagerItem, res) }
                }
            }
        }
    }

    class Factory(private val application: Application,
                  private val addReminders: Boolean,
                  private val calculateFuture: Boolean,
                  private val birthTime: Long = 0) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MonthViewViewModel(application, addReminders, calculateFuture, birthTime) as T
        }
    }
}
