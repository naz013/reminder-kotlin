package com.elementary.tasks.reminder.create.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.apps.SelectApplicationActivity
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.onChanged
import kotlinx.android.synthetic.main.fragment_reminder_application.*
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
class ApplicationFragment : RepeatableTypeFragment() {

    private val type: Int
        get() = if (application.isChecked) {
            Reminder.BY_DATE_APP
        } else {
            Reminder.BY_DATE_LINK
        }
    private val appName: String
        get() {
            val packageManager = context!!.packageManager
            var applicationInfo: ApplicationInfo? = null
            try {
                applicationInfo = packageManager.getApplicationInfo(iFace.state.app, 0)
            } catch (ignored: Exception) {
            }
            return (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "???") as String
        }

    override fun prepare(): Reminder? {
        val type = type
        var number: String
        val reminder = iFace.state.reminder
        if (Reminder.isSame(type, Reminder.BY_DATE_APP)) {
            number = iFace.state.app
            if (TextUtils.isEmpty(number)) {
                iFace.showSnackbar(getString(R.string.you_dont_select_application))
                return null
            }
        } else {
            number = urlField.text.toString().trim()
            if (TextUtils.isEmpty(number) || number.matches(".*https?://".toRegex())) {
                iFace.showSnackbar(getString(R.string.you_dont_insert_link))
                return null
            }
            if (!number.startsWith("http://") && !number.startsWith("https://"))
                number = "http://$number"
        }
        val startTime = dateView.dateTime
        if (reminder.remindBefore > 0 && startTime - reminder.remindBefore < System.currentTimeMillis()) {
            iFace.showSnackbar(getString(R.string.invalid_remind_before_parameter))
            return null
        }
        reminder.target = number
        reminder.type = type
        reminder.startTime = reminder.eventTime
        Timber.d("EVENT_TIME %s", TimeUtil.getFullDateTime(startTime, true))
        if (!TimeCount.isCurrent(reminder.eventTime)) {
            iFace.showSnackbar(getString(R.string.reminder_is_outdated))
            return null
        }
        return reminder
    }

    override fun layoutRes(): Int = R.layout.fragment_reminder_application

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
                dateTimeView = dateView,
                loudnessPickerView = loudnessView,
                priorityPickerView = priorityView,
                repeatLimitView = repeatLimitView,
                repeatView = repeatView,
                windowTypeView = windowTypeView
        )
    }

    override fun onNewHeader(newHeader: String) {
        cardSummary?.text = newHeader
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tuneExtraView.hasAutoExtra = true
        tuneExtraView.hint = getString(R.string.enable_launching_application_automatically)

        pickApplication.setOnClickListener {
            activity?.startActivityForResult(Intent(activity, SelectApplicationActivity::class.java), Constants.REQUEST_CODE_APPLICATION)
        }
        urlLayout.visibility = View.GONE
        urlField.setText(iFace.state.link)
        urlField.onChanged {
            iFace.state.link = it
        }
        application.setOnCheckedChangeListener { _, b ->
            iFace.state.isLink = !b
            if (!b) {
                applicationLayout.visibility = View.GONE
                urlLayout.visibility = View.VISIBLE
            } else {
                urlLayout.visibility = View.GONE
                applicationLayout.visibility = View.VISIBLE
            }
        }
        editReminder()
    }

    private fun editReminder() {
        val reminder = iFace.state.reminder
        if (reminder.target != "") {
            if (!iFace.state.isLink && Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)) {
                application.isChecked = true
                iFace.state.app = reminder.target
                iFace.state.isLink = false
                applicationName.text = appName
            } else {
                browser.isChecked = true
                iFace.state.link = reminder.target
                iFace.state.isLink = true
                urlField.setText(reminder.target)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_CODE_APPLICATION && resultCode == Activity.RESULT_OK) {
            iFace.state.app = data?.getStringExtra(Constants.SELECTED_APPLICATION) ?: ""
            applicationName.text = appName
        }
    }
}
