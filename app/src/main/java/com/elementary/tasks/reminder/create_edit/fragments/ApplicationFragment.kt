package com.elementary.tasks.reminder.create_edit.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.elementary.tasks.R
import com.elementary.tasks.core.apps.ApplicationActivity
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.FragmentReminderApplicationBinding
import com.elementary.tasks.core.data.models.Reminder

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

    private var binding: FragmentReminderApplicationBinding? = null
    private var selectedPackage: String? = null
    var appClick = { v -> activity!!.startActivityForResult(Intent(activity, ApplicationActivity::class.java), Constants.REQUEST_CODE_APPLICATION) }

    private val type: Int
        get() = if (binding!!.application.isChecked) {
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
        if (`interface` == null) return null
        var reminder: Reminder? = `interface`!!.reminder
        val type = type
        var number: String?
        if (Reminder.isSame(type, Reminder.BY_DATE_APP)) {
            number = selectedPackage
            if (TextUtils.isEmpty(number)) {
                `interface`!!.showSnackbar(getString(R.string.you_dont_select_application))
                return null
            }
        } else {
            number = binding!!.phoneNumber.text!!.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(number) || number.matches(".*https?://".toRegex())) {
                `interface`!!.showSnackbar(getString(R.string.you_dont_insert_link))
                return null
            }
            if (!number.startsWith("http://") && !number.startsWith("https://"))
                number = "http://$number"
        }
        val startTime = binding!!.dateView.dateTime
        val before = binding!!.beforeView.beforeValue
        if (before > 0 && startTime - before < System.currentTimeMillis()) {
            Toast.makeText(context, R.string.invalid_remind_before_parameter, Toast.LENGTH_SHORT).show()
            return null
        }
        if (reminder == null) {
            reminder = Reminder()
        }
        reminder.target = number
        reminder.type = type
        val repeat = binding!!.repeatView.repeat
        reminder.repeatInterval = repeat
        reminder.isExportToCalendar = binding!!.exportToCalendar.isChecked
        reminder.isExportToTasks = binding!!.exportToTasks.isChecked
        reminder.setClear(`interface`)
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
        binding = FragmentReminderApplicationBinding.inflate(inflater, container, false)
        binding!!.pickApplication.setOnClickListener(appClick)
        binding!!.repeatView.enablePrediction(true)
        binding!!.dateView.setEventListener(binding!!.repeatView.eventListener)
        `interface`!!.setEventHint(getString(R.string.subject))
        `interface`!!.setHasAutoExtra(true, getString(R.string.enable_launching_application_automatically))
        if (`interface`!!.isExportToCalendar) {
            binding!!.exportToCalendar.visibility = View.VISIBLE
        } else {
            binding!!.exportToCalendar.visibility = View.GONE
        }
        if (`interface`!!.isExportToTasks) {
            binding!!.exportToTasks.visibility = View.VISIBLE
        } else {
            binding!!.exportToTasks.visibility = View.GONE
        }
        binding!!.phoneNumber.visibility = View.GONE
        binding!!.application.setOnCheckedChangeListener { compoundButton, b ->
            if (!b) {
                ViewUtils.collapse(binding!!.applicationLayout)
                ViewUtils.expand(binding!!.phoneNumber)
            } else {
                ViewUtils.collapse(binding!!.phoneNumber)
                ViewUtils.expand(binding!!.applicationLayout)
            }
        }
        editReminder()
        return binding!!.root
    }

    private fun editReminder() {
        if (`interface`!!.reminder == null) return
        val reminder = `interface`!!.reminder
        binding!!.exportToCalendar.isChecked = reminder.isExportToCalendar
        binding!!.exportToTasks.isChecked = reminder.isExportToTasks
        binding!!.dateView.setDateTime(reminder.eventTime)
        binding!!.repeatView.setDateTime(reminder.eventTime)
        binding!!.repeatView.repeat = reminder.repeatInterval
        binding!!.beforeView.setBefore(reminder.remindBefore)
        if (reminder.target != null) {
            if (Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)) {
                binding!!.application.isChecked = true
                selectedPackage = reminder.target
                binding!!.applicationName.text = appName
            } else {
                binding!!.browser.isChecked = true
                binding!!.phoneNumber.setText(reminder.target)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_CODE_APPLICATION && resultCode == Activity.RESULT_OK) {
            selectedPackage = data!!.getStringExtra(Constants.SELECTED_APPLICATION)
            binding!!.applicationName.text = appName
        }
    }

    companion object {

        private val TAG = "DateFragment"
    }
}
