package com.elementary.tasks.reminder.create.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.core.views.DateTimeView
import kotlinx.android.synthetic.main.fragment_reminder_year.*
import timber.log.Timber
import java.util.*

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
class YearFragment : RepeatableTypeFragment() {

    private var mHour = 0
    private var mMinute = 0
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 1

    private val time: Long
        get() {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, mHour)
            calendar.set(Calendar.MINUTE, mMinute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }

    override fun prepare(): Reminder? {
        val reminder = reminderInterface.reminder
        var type = Reminder.BY_DAY_OF_YEAR
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
                Reminder.BY_DAY_OF_YEAR_CALL
            } else {
                Reminder.BY_DAY_OF_YEAR_SMS
            }
        }
        reminder.weekdays = listOf()
        reminder.target = number
        reminder.type = type
        reminder.dayOfMonth = mDay
        reminder.monthOfYear = mMonth
        reminder.repeatInterval = 0

        reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
        val startTime = TimeCount.getNextYearDayTime(reminder)

        if (reminder.remindBefore > 0 && startTime - reminder.remindBefore < System.currentTimeMillis()) {
            reminderInterface.showSnackbar(getString(R.string.invalid_remind_before_parameter))
            return null
        }

        reminder.startTime = TimeUtil.getGmtFromDateTime(startTime)
        reminder.eventTime = TimeUtil.getGmtFromDateTime(startTime)
        Timber.d("EVENT_TIME %s", TimeUtil.getFullDateTime(startTime, true))
        if (!TimeCount.isCurrent(reminder.eventTime)) {
            reminderInterface.showSnackbar(getString(R.string.reminder_is_outdated))
            return null
        }
        return reminder
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reminder_year, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(scrollView) {
            reminderInterface.updateScroll(it)
        }
        moreLayout?.isNestedScrollingEnabled = false

        if (prefs.isTelephonyAllowed) {
            actionView.visibility = View.VISIBLE
        } else {
            actionView.visibility = View.GONE
        }

        if (Module.isPro) {
            ledView.visibility = View.VISIBLE
        } else {
            ledView.visibility = View.GONE
        }

        tuneExtraView.dialogues = dialogues
        tuneExtraView.hasAutoExtra = false

        melodyView.onFileSelectListener = {
            reminderInterface.selectMelody()
        }
        attachmentView.onFileSelectListener = {
            reminderInterface.attachFile()
        }
        groupView.onGroupSelectListener = {
            reminderInterface.selectGroup()
        }

        dateView.setDateFormat(TimeUtil.simpleDate(prefs.appLanguage))
        dateView.setEventListener(object : DateTimeView.OnSelectListener {
            override fun onDateSelect(mills: Long, day: Int, month: Int, year: Int) {
                if (month == 1 && day > 28) {
                    reminderInterface.showSnackbar(getString(R.string.max_day_supported))
                    return
                }
                mDay = day
                mMonth = month
                mYear = year
            }

            override fun onTimeSelect(mills: Long, hour: Int, minute: Int) {
                mHour = hour
                mMinute = minute
            }
        })
        actionView.setActivity(activity!!)
        actionView.setContactClickListener(View.OnClickListener { selectContact() })
        initScreenState()
        initPropertyFields()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        mDay = calendar.get(Calendar.DAY_OF_MONTH)
        mMonth = calendar.get(Calendar.MONTH)
        mYear = calendar.get(Calendar.YEAR)
        mHour = calendar.get(Calendar.HOUR_OF_DAY)
        mMinute = calendar.get(Calendar.MINUTE)
        dateView.dateTime = System.currentTimeMillis()
        editReminder()
    }

    private fun initPropertyFields() {
        taskSummary.bindProperty(reminderInterface.reminder.summary) {
            reminderInterface.reminder.summary = it.trim()
        }
        beforeView.bindProperty(reminderInterface.reminder.remindBefore) {
            reminderInterface.reminder.remindBefore = it
            updateHeader()
        }
        exportToCalendar.bindProperty(reminderInterface.reminder.exportToCalendar) {
            reminderInterface.reminder.exportToCalendar = it
        }
        exportToTasks.bindProperty(reminderInterface.reminder.exportToTasks) {
            reminderInterface.reminder.exportToTasks = it
        }
        dateView.bindProperty(reminderInterface.reminder.eventTime) {
            reminderInterface.reminder.eventTime = it
        }
        priorityView.bindProperty(reminderInterface.reminder.priority) {
            reminderInterface.reminder.priority = it
            updateHeader()
        }
        actionView.bindProperty(reminderInterface.reminder.target) {
            reminderInterface.reminder.target = it
            updateActions()
        }
        melodyView.bindProperty(reminderInterface.reminder.melodyPath) {
            reminderInterface.reminder.melodyPath = it
        }
        attachmentView.bindProperty(reminderInterface.reminder.attachmentFile) {
            reminderInterface.reminder.attachmentFile = it
        }
        loudnessView.bindProperty(reminderInterface.reminder.volume) {
            reminderInterface.reminder.volume = it
        }
        repeatLimitView.bindProperty(reminderInterface.reminder.repeatLimit) {
            reminderInterface.reminder.repeatLimit = it
        }
        windowTypeView.bindProperty(reminderInterface.reminder.windowType) {
            reminderInterface.reminder.windowType = it
        }
        tuneExtraView.bindProperty(reminderInterface.reminder) {
            reminderInterface.reminder.copyExtra(it)
        }
        if (Module.isPro) {
            ledView.bindProperty(reminderInterface.reminder.color) {
                reminderInterface.reminder.color = it
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
        cardSummary?.text = getSummary()
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

    private fun updateDateTime(reminder: Reminder) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
        mHour = calendar.get(Calendar.HOUR_OF_DAY)
        mMinute = calendar.get(Calendar.MINUTE)
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.DAY_OF_MONTH, reminder.dayOfMonth)
        calendar.set(Calendar.MONTH, reminder.monthOfYear)
        calendar.set(Calendar.HOUR_OF_DAY, mHour)
        calendar.set(Calendar.MINUTE, mMinute)
        dateView.dateTime = calendar.timeInMillis
        mDay = reminder.dayOfMonth
        mMonth = reminder.monthOfYear
    }

    private fun editReminder() {
        val reminder = reminderInterface.reminder
        showGroup(groupView, reminder)
        updateDateTime(reminder)
        mDay = reminder.dayOfMonth
        mMonth = reminder.monthOfYear
        if (reminder.target != "") {
            actionView.setAction(true)
            actionView.number = reminder.target
            if (Reminder.isKind(reminder.type, Reminder.Kind.CALL)) {
                actionView.type = ActionView.TYPE_CALL
            } else if (Reminder.isKind(reminder.type, Reminder.Kind.SMS)) {
                actionView.type = ActionView.TYPE_MESSAGE
            }
        }
    }

    private fun selectContact() {
        if (Permissions.ensurePermissions(activity!!, CONTACTS, Permissions.READ_CONTACTS)) {
            SuperUtil.selectContact(activity!!, Constants.REQUEST_CODE_CONTACTS)
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
        groupView?.reminderGroup = reminderGroup
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

    companion object {

        private const val CONTACTS = 114
    }
}