package com.elementary.tasks.reminder.createEdit

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.reminders.ReminderViewModel
import com.elementary.tasks.core.views.ActionView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_add_reminder.*

/**
 * Copyright 2017 Nazar Suhovich
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
class AddReminderActivity : ThemedActivity() {

    private lateinit var viewModel: ReminderViewModel

    private val mActionListener = object : ActionView.OnActionListener {
        override fun onActionChange(hasAction: Boolean) {
            if (!hasAction) {
                task_text.setText(getString(R.string.remind_me))
            }
        }

        override fun onTypeChange(isMessageType: Boolean) {
            if (isMessageType) {
                task_text.setText(getString(R.string.message))
            } else {
                task_text.setText(getString(R.string.remind_me))
            }
        }
    }

    private val isExportToCalendar: Boolean
        get() = prefs.isCalendarEnabled || prefs.isStockCalendarEnabled

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add_reminder, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                save()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reminder)
        initActionBar()
        val date = intent.getLongExtra(Constants.INTENT_DATE, 0)
        repeatView.enablePrediction(true)
        dateView.setEventListener(repeatView.eventListener)
        actionView.setListener(mActionListener)
        actionView.setActivity(this)
        actionView.setContactClickListener(View.OnClickListener { selectContact() })
        if (isExportToCalendar) {
            exportToCalendar.visibility = View.VISIBLE
        } else {
            exportToCalendar.visibility = View.GONE
        }
        if (Google.getInstance() != null) {
            exportToTasks.visibility = View.VISIBLE
        } else {
            exportToTasks.visibility = View.GONE
        }

        if (date != 0L) {
            dateView.dateTime = date
        }

        initViewModel()
    }

    private fun initViewModel() {
        val factory = ReminderViewModel.Factory(application, "")
        viewModel = ViewModelProviders.of(this, factory).get(ReminderViewModel::class.java)
        viewModel.result.observe(this, Observer{ commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED, Commands.SAVED -> {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
        })
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
    }

    private fun save() {
        val summary = task_text.text.toString().trim()
        var type = Reminder.BY_DATE
        val isAction = actionView.hasAction()
        if (TextUtils.isEmpty(summary) && !isAction) {
            Snackbar.make(rootView, getString(R.string.task_summary_is_empty), Snackbar.LENGTH_SHORT).show()
            return
        }
        var number = ""
        if (isAction) {
            number = actionView.number
            if (TextUtils.isEmpty(number)) {
                Snackbar.make(rootView, getString(R.string.you_dont_insert_number), Snackbar.LENGTH_SHORT).show()
                return
            }
            type = if (actionView.type == ActionView.TYPE_CALL) {
                Reminder.BY_DATE_CALL
            } else {
                Reminder.BY_DATE_SMS
            }
        }
        val startTime = dateView.dateTime
        val before = before_view.beforeValue
        if (before > 0 && startTime - before < System.currentTimeMillis()) {
            Toast.makeText(this, R.string.invalid_remind_before_parameter, Toast.LENGTH_SHORT).show()
            return
        }
        val reminder = Reminder()
        reminder.target = number
        reminder.type = type
        reminder.repeatInterval = repeatView.repeat
        reminder.exportToCalendar = exportToCalendar.isChecked
        reminder.exportToTasks = exportToTasks.isChecked
        reminder.summary = summary
        val item = viewModel.defaultGroup.value
        if (item != null) {
            reminder.groupUuId = item.uuId
        }
        LogUtil.d(TAG, "prepare: $type")
        reminder.remindBefore = before
        reminder.startTime = TimeUtil.getGmtFromDateTime(startTime)
        reminder.eventTime = TimeUtil.getGmtFromDateTime(startTime)
        LogUtil.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true, true))
        if (!TimeCount.isCurrent(reminder.eventTime)) {
            Toast.makeText(this, R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.saveAndStartReminder(reminder)
    }

    private fun selectContact() {
        if (Permissions.checkPermission(this, Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
            SuperUtil.selectContact(this, Constants.REQUEST_CODE_CONTACTS)
        } else {
            Permissions.requestPermission(this, CONTACTS, Permissions.READ_CONTACTS, Permissions.READ_CALLS)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_CODE_CONTACTS && resultCode == Activity.RESULT_OK) {
            val number = data!!.getStringExtra(Constants.SELECTED_CONTACT_NUMBER)
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

    companion object {

        private const val TAG = "AddReminderActivity"
        private const val CONTACTS = 112
    }
}
