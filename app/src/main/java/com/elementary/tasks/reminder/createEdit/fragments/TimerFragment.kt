package com.elementary.tasks.reminder.createEdit.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
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
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.data.models.UsedTime
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.usedTime.UsedTimeViewModel
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
        val reminder = reminderInterface.reminder
        val after = timerPickerView.timerValue
        if (after == 0L) {
            reminderInterface.showSnackbar(getString(R.string.you_dont_insert_timer_time))
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
                reminderInterface.showSnackbar(getString(R.string.you_dont_insert_number))
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
        val startTime = timeCount.generateNextTimer(reminder, true)
        reminder.startTime = TimeUtil.getGmtFromDateTime(startTime)
        reminder.eventTime = TimeUtil.getGmtFromDateTime(startTime)
        Timber.d("EVENT_TIME %s", TimeUtil.getFullDateTime(startTime, true, true))
        if (!TimeCount.isCurrent(reminder.eventTime)) {
            reminderInterface.showSnackbar(getString(R.string.reminder_is_outdated))
            return null
        }
        return reminder
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reminder_timer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMostUsedList()

        ViewUtils.listenScrollableView(scrollView) {
            reminderInterface.updateScroll(it)
        }
        moreLayout.isNestedScrollingEnabled = false

        if (Module.isPro) {
            ledView.visibility = View.VISIBLE
        } else {
            ledView.visibility = View.GONE
        }

        tuneExtraView.dialogues = dialogues
        tuneExtraView.hasAutoExtra = false

        exclusionView.dialogues = dialogues
        exclusionView.prefs = prefs
        exclusionView.themeUtil = themeUtil

        actionView.setActivity(activity!!)
        actionView.setContactClickListener(View.OnClickListener { selectContact() })

        melodyView.onFileSelectListener = {
            reminderInterface.selectMelody()
        }
        attachmentView.onFileSelectListener = {
            reminderInterface.attachFile()
        }
        groupView.onGroupSelectListener = {
            reminderInterface.selectGroup()
        }

        initScreenState()
        initPropertyFields()
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

    private fun initPropertyFields() {
        val reminder = reminderInterface.reminder
        taskSummary.bindProperty(reminder.summary) {
            reminder.summary = it.trim()
        }
        beforeView.bindProperty(reminder.remindBefore) {
            reminder.remindBefore = it
            updateHeader()
        }
        repeatView.bindProperty(reminder.repeatInterval) {
            reminder.repeatInterval = it
        }
        exportToCalendar.bindProperty(reminder.exportToCalendar) {
            reminder.exportToCalendar = it
        }
        exportToTasks.bindProperty(reminder.exportToTasks) {
            reminder.exportToTasks = it
        }
        priorityView.bindProperty(reminder.priority) {
            reminder.priority = it
            updateHeader()
        }
        actionView.bindProperty(reminder.target) {
            reminder.target = it
            updateActions()
        }
        melodyView.bindProperty(reminder.melodyPath) {
            reminder.melodyPath = it
        }
        attachmentView.bindProperty(reminder.attachmentFile) {
            reminder.attachmentFile = it
        }
        loudnessView.bindProperty(reminder.volume) {
            reminder.volume = it
        }
        repeatLimitView.bindProperty(reminder.repeatLimit) {
            reminder.repeatLimit = it
        }
        windowTypeView.bindProperty(reminder.windowType) {
            reminder.windowType = it
        }
        tuneExtraView.bindProperty(reminder) {
            reminder.copyExtra(it)
        }
        exclusionView.bindProperty(reminder.hours, reminder.from, reminder.to) { hours, from, to ->
            reminder.hours = hours
            reminder.from = from
            reminder.to = to
        }
        if (Module.isPro) {
            ledView.bindProperty(reminder.color) {
                reminder.color = it
            }
        }
    }

    private fun updateActions() {
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

    private fun updateHeader() {
        cardSummary.text = getSummary()
    }

    private fun initScreenState() {
        if (reminderInterface.canExportToCalendar) {
            exportToCalendar.visibility = View.VISIBLE
        } else {
            exportToCalendar.visibility = View.GONE
        }
        if (reminderInterface.canExportToTasks) {
            exportToTasks.visibility = View.VISIBLE
        } else {
            exportToTasks.visibility = View.GONE
        }
    }

    private fun editReminder() {
        val reminder = reminderInterface.reminder
        groupView.reminderGroup = ReminderGroup().apply {
            this.groupColor = reminder.groupColor
            this.groupTitle = reminder.groupTitle
            this.groupUuId = reminder.groupUuId
        }
        timerPickerView.timerValue = reminder.after
        if (reminder.target != "") {
            actionView.setAction(true)
            if (Reminder.isKind(reminder.type, Reminder.Kind.CALL)) {
                actionView.type = ActionView.TYPE_CALL
            } else if (Reminder.isKind(reminder.type, Reminder.Kind.SMS)) {
                actionView.type = ActionView.TYPE_MESSAGE
            }
        }
    }

    private fun selectContact() {
        if (Permissions.checkPermission(activity!!, Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
            SuperUtil.selectContact(activity!!, Constants.REQUEST_CODE_CONTACTS)
        } else {
            Permissions.requestPermission(activity!!, CONTACTS, Permissions.READ_CONTACTS, Permissions.READ_CALLS)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_CODE_CONTACTS && resultCode == Activity.RESULT_OK) {
            val number = data?.getStringExtra(Constants.SELECTED_CONTACT_NUMBER) ?: ""
            actionView.number = number
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        actionView.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) return
        when (requestCode) {
            CONTACTS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectContact()
            }
        }
    }

    override fun onGroupUpdate(reminderGroup: ReminderGroup) {
        super.onGroupUpdate(reminderGroup)
        groupView.reminderGroup = reminderGroup
        updateHeader()
    }

    override fun onMelodySelect(path: String) {
        super.onMelodySelect(path)
        melodyView.file = path
    }

    override fun onAttachmentSelect(path: String) {
        super.onAttachmentSelect(path)
        attachmentView.file = path
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

    companion object {

        private const val CONTACTS = 112
    }
}
