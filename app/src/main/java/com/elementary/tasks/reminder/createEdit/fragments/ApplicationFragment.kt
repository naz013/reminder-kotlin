package com.elementary.tasks.reminder.createEdit.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.core.apps.ApplicationActivity
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.*
import kotlinx.android.synthetic.main.fragment_reminder_application.*

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

    private var selectedPackage: String? = null

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
                applicationInfo = packageManager.getApplicationInfo(selectedPackage, 0)
            } catch (ignored: PackageManager.NameNotFoundException) {
            }

            return (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "???") as String
        }

    override fun prepare(): Reminder? {
        if (reminderInterface == null) return null
        val type = type
        var number: String
        if (Reminder.isSame(type, Reminder.BY_DATE_APP)) {
            number = selectedPackage ?: ""
            if (TextUtils.isEmpty(number)) {
                reminderInterface!!.showSnackbar(getString(R.string.you_dont_select_application))
                return null
            }
        } else {
            number = phoneNumber.text!!.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(number) || number.matches(".*https?://".toRegex())) {
                reminderInterface!!.showSnackbar(getString(R.string.you_dont_insert_link))
                return null
            }
            if (!number.startsWith("http://") && !number.startsWith("https://"))
                number = "http://$number"
        }
        val startTime = dateView.dateTime
        val before = before_view.beforeValue
        if (before > 0 && startTime - before < System.currentTimeMillis()) {
            Toast.makeText(context, R.string.invalid_remind_before_parameter, Toast.LENGTH_SHORT).show()
            return null
        }
        var reminder = reminderInterface!!.reminder
        if (reminder == null) {
            reminder = Reminder()
        }
        reminder.target = number
        reminder.type = type
        val repeat = repeatView.repeat
        reminder.repeatInterval = repeat
        reminder.exportToCalendar = exportToCalendar.isChecked
        reminder.exportToTasks = exportToTasks.isChecked
        if (reminderInterface != null) {
            reminder.setClear(reminderInterface!!)
        }
        reminder.remindBefore = before
        reminder.startTime = TimeUtil.getGmtFromDateTime(startTime)
        reminder.eventTime = TimeUtil.getGmtFromDateTime(startTime)
        LogUtil.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true, true))
        if (!TimeCount.isCurrent(reminder.eventTime)) {
            Toast.makeText(context, R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show()
            return null
        }
        return reminder
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.fragment_date_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_limit -> changeLimit()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reminder_application, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pickApplication.setOnClickListener { activity!!.startActivityForResult(Intent(activity, ApplicationActivity::class.java), Constants.REQUEST_CODE_APPLICATION) }
        repeatView.enablePrediction(true)
        dateView.setEventListener(repeatView.eventListener)
        initScreenState()
        phoneNumber.visibility = View.GONE
        application.setOnCheckedChangeListener { _, b ->
            if (!b) {
                ViewUtils.collapse(applicationLayout)
                ViewUtils.expand(phoneNumber)
            } else {
                ViewUtils.collapse(phoneNumber)
                ViewUtils.expand(applicationLayout)
            }
        }
        editReminder()
    }

    private fun initScreenState() {
        val reminderIface = reminderInterface ?: return
        reminderIface.setEventHint(getString(R.string.subject))
        reminderIface.setHasAutoExtra(true, getString(R.string.enable_launching_application_automatically))
        if (reminderIface.isExportToCalendar) {
            exportToCalendar.visibility = View.VISIBLE
        } else {
            exportToCalendar.visibility = View.GONE
        }
        if (reminderIface.isExportToTasks) {
            exportToTasks.visibility = View.VISIBLE
        } else {
            exportToTasks.visibility = View.GONE
        }
    }

    private fun editReminder() {
        val reminder = reminderInterface?.reminder ?: return
        exportToCalendar.isChecked = reminder.exportToCalendar
        exportToTasks.isChecked = reminder.exportToTasks
        dateView.setDateTime(reminder.eventTime)
        repeatView.setDateTime(reminder.eventTime)
        repeatView.repeat = reminder.repeatInterval
        before_view.setBefore(reminder.remindBefore)
        if (reminder.target != "") {
            if (Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)) {
                application.isChecked = true
                selectedPackage = reminder.target
                applicationName.text = appName
            } else {
                browser.isChecked = true
                phoneNumber.setText(reminder.target)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_CODE_APPLICATION && resultCode == Activity.RESULT_OK) {
            selectedPackage = data?.getStringExtra(Constants.SELECTED_APPLICATION)
            applicationName.text = appName
        }
    }

    companion object {
        private const val TAG = "DateFragment"
    }
}
