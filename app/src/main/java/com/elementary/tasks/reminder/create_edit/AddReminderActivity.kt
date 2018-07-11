package com.elementary.tasks.reminder.create_edit

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast

import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.viewModels.reminders.ReminderViewModel
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.databinding.ActivityAddReminderBinding
import com.google.android.material.snackbar.Snackbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders

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

    private var binding: ActivityAddReminderBinding? = null
    private var viewModel: ReminderViewModel? = null

    private val mActionListener = object : ActionView.OnActionListener {
        override fun onActionChange(hasAction: Boolean) {
            if (!hasAction) {
                binding!!.taskText.setText(getString(R.string.remind_me))
            }
        }

        override fun onTypeChange(isMessageType: Boolean) {
            if (isMessageType) {
                binding!!.taskText.setText(getString(R.string.message))
            } else {
                binding!!.taskText.setText(getString(R.string.remind_me))
            }
        }
    }

    private val isExportToCalendar: Boolean
        get() = prefs!!.isCalendarEnabled || prefs!!.isStockCalendarEnabled

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_add_reminder, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                save()
                return true
            }
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_reminder)
        initActionBar()
        val date = intent.getLongExtra(Constants.INTENT_DATE, 0)
        binding!!.repeatView.enablePrediction(true)
        binding!!.dateView.setEventListener(binding!!.repeatView.eventListener)
        binding!!.actionView.setListener(mActionListener)
        binding!!.actionView.setActivity(this)
        binding!!.actionView.setContactClickListener { view -> selectContact() }
        if (isExportToCalendar) {
            binding!!.exportToCalendar.visibility = View.VISIBLE
        } else {
            binding!!.exportToCalendar.visibility = View.GONE
        }
        if (Google.getInstance(this) != null) {
            binding!!.exportToTasks.visibility = View.VISIBLE
        } else {
            binding!!.exportToTasks.visibility = View.GONE
        }

        if (date != 0L) {
            binding!!.dateView.dateTime = date
        }

        initViewModel()
    }

    private fun initViewModel() {
        val factory = ReminderViewModel.Factory(application, 0)
        viewModel = ViewModelProviders.of(this, factory).get(ReminderViewModel::class.java)
        viewModel!!.result.observe(this, { commands ->
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
        setSupportActionBar(binding!!.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
    }

    private fun save() {
        val summary = binding!!.taskText.text!!.toString()
        var type = Reminder.BY_DATE
        val isAction = binding!!.actionView.hasAction()
        if (TextUtils.isEmpty(summary) && !isAction) {
            Snackbar.make(binding!!.root, getString(R.string.task_summary_is_empty), Snackbar.LENGTH_SHORT).show()
            return
        }
        var number: String? = null
        if (isAction) {
            number = binding!!.actionView.number
            if (TextUtils.isEmpty(number)) {
                Snackbar.make(binding!!.root, getString(R.string.you_dont_insert_number), Snackbar.LENGTH_SHORT).show()
                return
            }
            if (binding!!.actionView.type == ActionView.TYPE_CALL) {
                type = Reminder.BY_DATE_CALL
            } else {
                type = Reminder.BY_DATE_SMS
            }
        }
        val startTime = binding!!.dateView.dateTime
        val before = binding!!.beforeView.beforeValue
        if (before > 0 && startTime - before < System.currentTimeMillis()) {
            Toast.makeText(this, R.string.invalid_remind_before_parameter, Toast.LENGTH_SHORT).show()
            return
        }
        val reminder = Reminder()
        reminder.target = number
        reminder.type = type
        reminder.repeatInterval = binding!!.repeatView.repeat
        reminder.isExportToCalendar = binding!!.exportToCalendar.isChecked
        reminder.isExportToTasks = binding!!.exportToTasks.isChecked
        reminder.summary = summary
        val item = viewModel!!.defaultGroup.value
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
        viewModel!!.saveAndStartReminder(reminder)
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
            binding!!.actionView.number = number
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        binding!!.actionView.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size == 0) return
        when (requestCode) {
            CONTACTS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectContact()
            }
        }
    }

    companion object {

        private val TAG = "AddReminderActivity"
        private val CONTACTS = 112
    }
}
