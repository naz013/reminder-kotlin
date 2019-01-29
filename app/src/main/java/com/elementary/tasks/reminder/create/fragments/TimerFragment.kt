package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.UsedTime
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.bindProperty
import com.elementary.tasks.core.view_models.used_time.UsedTimeViewModel
import com.elementary.tasks.core.views.ActionView
import kotlinx.android.synthetic.main.fragment_reminder_timer.*
import kotlinx.android.synthetic.main.list_item_used_time.view.*
import timber.log.Timber

/**
 * Copyright 2016 Nazar Suhovich
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
class TimerFragment : RepeatableTypeFragment() {

    private val timesAdapter = TimesAdapter()
    lateinit var viewModel: UsedTimeViewModel

    override fun prepare(): Reminder? {
        val reminder = iFace.state.reminder
        val after = timerPickerView.timerValue
        if (after == 0L) {
            iFace.showSnackbar(getString(R.string.you_dont_insert_timer_time))
            return null
        }
        var type = Reminder.BY_TIME
        val isAction = actionView.hasAction()
        if (TextUtils.isEmpty(reminder.summary) && !isAction) {
            taskLayout.error = getString(R.string.task_summary_is_empty)
            taskLayout.isErrorEnabled = true
            return null
        }
        var number = ""
        if (isAction) {
            number = actionView.number
            if (TextUtils.isEmpty(number)) {
                iFace.showSnackbar(getString(R.string.you_dont_insert_number))
                return null
            }
            type = if (actionView.type == ActionView.TYPE_CALL) {
                Reminder.BY_TIME_CALL
            } else {
                Reminder.BY_TIME_SMS
            }
        }

        viewModel.saveTime(after)

        reminder.target = number
        reminder.type = type
        reminder.after = after
        val startTime = TimeCount.generateNextTimer(reminder, true)
        reminder.startTime = TimeUtil.getGmtFromDateTime(startTime)
        reminder.eventTime = TimeUtil.getGmtFromDateTime(startTime)
        Timber.d("EVENT_TIME %s", TimeUtil.getFullDateTime(startTime, true))
        if (!TimeCount.isCurrent(reminder.eventTime)) {
            iFace.showSnackbar(getString(R.string.reminder_is_outdated))
            return null
        }
        return reminder
    }

    override fun layoutRes(): Int = R.layout.fragment_reminder_timer

    override fun provideViews() {
        setViews(
                scrollView = scrollView,
                expansionLayout = moreLayout,
                ledPickerView = ledView,
                calendarCheck = exportToCalendar,
                tasksCheck = exportToTasks,
                extraView = tuneExtraView,
                melodyView = melodyView,
                attachmentView = attachmentView,
                groupView = groupView,
                summaryView = taskSummary,
                beforePickerView = beforeView,
                loudnessPickerView = loudnessView,
                priorityPickerView = priorityView,
                repeatLimitView = repeatLimitView,
                repeatView = repeatView,
                windowTypeView = windowTypeView,
                actionView = actionView
        )
    }

    override fun onNewHeader(newHeader: String) {
        cardSummary?.text = newHeader
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMostUsedList()
        tuneExtraView.hasAutoExtra = false

        exclusionView.dialogues = dialogues
        exclusionView.prefs = prefs
        exclusionView.themeUtil = themeUtil

        exclusionView.bindProperty(iFace.state.reminder.hours, iFace.state.reminder.from,
                iFace.state.reminder.to) { hours, from, to ->
            iFace.state.reminder.hours = hours
            iFace.state.reminder.from = from
            iFace.state.reminder.to = to
        }

        editReminder()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(UsedTimeViewModel::class.java)
        viewModel.usedTimeList.observe(this, Observer {
            if (it != null) {
                timesAdapter.updateData(it)
                if (it.isEmpty()) {
                    mostUserTimes.visibility = View.GONE
                } else {
                    mostUserTimes.visibility = View.VISIBLE
                }
            } else {
                mostUserTimes.visibility = View.GONE
            }
        })
    }

    private fun initMostUsedList() {
        mostUserTimes.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        timesAdapter.listener = {
            timerPickerView.timerValue = it.timeMills
        }
        mostUserTimes.adapter = timesAdapter
    }

    override fun updateActions() {
        if (actionView.hasAction()) {
            tuneExtraView.hasAutoExtra = true
            if (actionView.type == ActionView.TYPE_MESSAGE) {
                tuneExtraView.hint = getString(R.string.enable_sending_sms_automatically)
            } else {
                tuneExtraView.hint = getString(R.string.enable_making_phone_calls_automatically)
            }
        } else {
            tuneExtraView.hasAutoExtra = false
        }
    }

    private fun editReminder() {
        timerPickerView.timerValue = iFace.state.reminder.after
    }

    inner class TimesAdapter : RecyclerView.Adapter<TimesAdapter.TimeHolder>() {

        private val data: MutableList<UsedTime> = mutableListOf()
        var listener: ((UsedTime) -> Unit)? = null

        fun updateData(list: List<UsedTime>) {
            this.data.clear()
            this.data.addAll(list)
            notifyDataSetChanged()
        }

        override fun onBindViewHolder(holder: TimeHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeHolder {
            return TimeHolder(parent)
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class TimeHolder(viewGroup: ViewGroup) :
                RecyclerView.ViewHolder(LayoutInflater.from(viewGroup.context).inflate(R.layout.list_item_used_time, viewGroup, false)) {

            init {
                itemView.chipItem.setOnClickListener {
                    listener?.invoke(data[adapterPosition])
                }
            }

            fun bind(usedTime: UsedTime) {
                itemView.chipItem.text = usedTime.timeString
            }
        }
    }
}
