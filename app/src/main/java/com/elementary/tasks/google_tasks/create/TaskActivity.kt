package com.elementary.tasks.google_tasks.create

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.google_tasks.GoogleTaskViewModel
import com.elementary.tasks.navigation.settings.security.PinLoginActivity
import kotlinx.android.synthetic.main.activity_create_google_task.*
import kotlinx.android.synthetic.main.view_progress.*
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
class TaskActivity : ThemedActivity() {

    private lateinit var stateViewModel: StateViewModel
    private lateinit var viewModel: GoogleTaskViewModel

    private var mIsLoading = false
    private var mItem: GoogleTask? = null

    private var mDateCallBack: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        val c = Calendar.getInstance()
        c.timeInMillis = System.currentTimeMillis()
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        c.set(Calendar.MONTH, monthOfYear)
        c.set(Calendar.YEAR, year)
        stateViewModel.date.postValue(c.timeInMillis)
    }

    private var mTimeCallBack: TimePickerDialog.OnTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        val c = Calendar.getInstance()
        c.timeInMillis = System.currentTimeMillis()
        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
        c.set(Calendar.MINUTE, minute)
        stateViewModel.time.postValue(c.timeInMillis)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stateViewModel = ViewModelProviders.of(this).get(StateViewModel::class.java)
        lifecycle.addObserver(stateViewModel)

        setContentView(R.layout.activity_create_google_task)
        initToolbar()
        initFields()

        progressMessageView.text = getString(R.string.please_wait)
        updateProgress(false)

        var tmp = intent.getStringExtra(Constants.INTENT_ID) ?: ""
        if (savedInstanceState == null) {
            stateViewModel.action = intent.getStringExtra(TasksConstants.INTENT_ACTION) ?: ""
            if (stateViewModel.action == "") stateViewModel.action = TasksConstants.CREATE
            initDefaults()
        } else {
            updateProgress(savedInstanceState.getBoolean(ARG_LOADING, false))
        }

        if (stateViewModel.action == TasksConstants.CREATE) {
            if (savedInstanceState != null) {
                tmp = savedInstanceState.getString(ARG_LIST, "")
            }
            initViewModel("", tmp)
        } else {
            initViewModel(tmp, "")
        }
    }

    override fun onStart() {
        super.onStart()
        observeStates()
        if (prefs.hasPinCode && !stateViewModel.isLogged) {
            PinLoginActivity.verify(this)
        }
    }

    private fun observeStates() {
        stateViewModel.time.observe(this, Observer {
            if (it != null) {
                switchDate()
            }
        })
        stateViewModel.date.observe(this, Observer {
            if (it != null) {
                switchDate()
            }
        })
        stateViewModel.isDateEnabled.observe(this, Observer {
            if (it != null) {
                switchDate(isDate = it)
            }
        })
        stateViewModel.isReminder.observe(this, Observer {
            if (it != null) {
                switchDate(isReminder = it)
            }
        })
        stateViewModel.reminderValue.observe(this, Observer {
            if (it != null) {
                showReminder(it)
            }
        })
    }

    private fun initDefaults() {
        stateViewModel.isLogged = intent.getBooleanExtra(ARG_LOGGED, false)
        stateViewModel.time.postValue(System.currentTimeMillis())
        stateViewModel.date.postValue(System.currentTimeMillis())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(ARG_LIST, stateViewModel.listId)
        outState.putBoolean(ARG_LOADING, mIsLoading)
        super.onSaveInstanceState(outState)
    }

    private fun updateProgress(b: Boolean) {
        mIsLoading = b
        if (b) {
            progressView.visibility = View.VISIBLE
        } else {
            progressView.visibility = View.GONE
        }
    }

    private fun initViewModel(taskId: String, listId: String) {
        stateViewModel.listId = listId
        viewModel = ViewModelProviders.of(this, GoogleTaskViewModel.Factory(taskId)).get(GoogleTaskViewModel::class.java)
        viewModel.isInProgress.observe(this, Observer { aBoolean ->
            if (aBoolean != null) {
                updateProgress(aBoolean)
            }
        })
        viewModel.result.observe(this, Observer { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.SAVED, Commands.DELETED -> onBackPressed()
                    else -> {
                    }
                }
            }
        })
        viewModel.googleTask.observe(this, Observer { googleTask ->
            if (googleTask != null) {
                editTask(googleTask)
            }
        })
        viewModel.googleTaskLists.observe(this, Observer { googleTaskLists ->
            if (googleTaskLists != null) {
                selectCurrent(googleTaskLists)
            }
        })
        viewModel.defaultTaskList.observe(this, Observer { googleTaskList ->
            if (googleTaskList != null && listId == "") {
                showTaskList(googleTaskList)
            }
        })
        viewModel.reminder.observe(this, Observer { reminder ->
            if (reminder != null) {
                if (!stateViewModel.isReminderEdited) {
                    stateViewModel.reminderValue.postValue(reminder)
                    stateViewModel.isReminderEdited = true
                }
            }
        })
    }

    private fun showReminder(reminder: Reminder) {
        stateViewModel.time.postValue(TimeUtil.getDateTimeFromGmt(reminder.eventTime))
        stateViewModel.isReminder.postValue(true)
    }

    private fun showTaskList(googleTaskList: GoogleTaskList) {
        stateViewModel.listId = googleTaskList.listId
        listText.text = googleTaskList.title
    }

    private fun selectCurrent(googleTaskLists: List<GoogleTaskList>) {
        for (googleTaskList in googleTaskLists) {
            if (googleTaskList.listId == stateViewModel.listId) {
                showTaskList(googleTaskList)
                break
            }
        }
    }

    private fun editTask(googleTask: GoogleTask) {
        this.mItem = googleTask
        stateViewModel.listId = googleTask.listId
        toolbar.setTitle(R.string.edit_task)
        if (!stateViewModel.isEdited) {
            editField.setText(googleTask.title)
            val note = googleTask.notes
            if (note != "") {
                detailsField.setText(note)
                detailsField.setSelection(detailsField.text.toString().trim().length)
            }
            val time = googleTask.dueDate
            if (time != 0L) {
                stateViewModel.date.postValue(time)
                stateViewModel.isDateEnabled.postValue(true)
            }
            if (viewModel.googleTaskLists.value != null) {
                for (googleTaskList in viewModel.googleTaskLists.value!!) {
                    if (googleTaskList.listId == googleTask.listId) {
                        showTaskList(googleTaskList)
                        break
                    }
                }
            }
            stateViewModel.isEdited = true
        }
        viewModel.loadReminder(googleTask.uuId)
    }

    private fun initFields() {
        listText.setOnClickListener { selectList(false) }
        dateField.setOnClickListener { selectDateAction(1) }
        timeField.setOnClickListener { selectDateAction(2) }
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.navigationIcon = ViewUtils.backIcon(this, isDark)
        toolbar.setTitle(R.string.new_task)
    }

    private fun selectDateAction(type: Int) {
        val builder = dialogues.getDialog(this)
        var types = arrayOf(getString(R.string.no_date), getString(R.string.select_date))
        if (type == 2) {
            types = arrayOf(getString(R.string.no_reminder), getString(R.string.select_time))
        }
        val adapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_single_choice, types)
        var selection = 0
        if (type == 1) {
            if (isDate()) selection = 1
        }
        if (type == 2) {
            if (isReminder()) selection = 1
        }
        builder.setSingleChoiceItems(adapter, selection) { dialog, which ->
            if (which != -1) {
                dialog.dismiss()
                if (type == 1) {
                    when (which) {
                        0 -> stateViewModel.isDateEnabled.postValue(false)
                        1 -> {
                            stateViewModel.isDateEnabled.postValue(true)
                            dateDialog()
                        }
                    }
                }
                if (type == 2) {
                    when (which) {
                        0 -> stateViewModel.isReminder.postValue(false)
                        1 -> {
                            stateViewModel.isReminder.postValue(true)
                            timeDialog()
                        }
                    }
                }
            }
        }
        builder.create().show()
    }

    private fun isDate(): Boolean = stateViewModel.isDateEnabled.value ?: false

    private fun isReminder(): Boolean = stateViewModel.isReminder.value ?: false

    private fun switchDate(isDate: Boolean = isDate(), isReminder: Boolean = isReminder()) {
        if (!isDate) {
            dateField.text = getString(R.string.no_date)
        } else {
            showDate()
        }
        if (!isReminder) {
            timeField.text = getString(R.string.no_reminder)
        } else {
            showTime()
        }
    }

    private fun showDate() {
        dateField.text = TimeUtil.getGoogleTaskDate(stateViewModel.date.value
                ?: System.currentTimeMillis(),
                prefs.appLanguage)
    }

    private fun showTime() {
        timeField.text = TimeUtil.getTime(stateViewModel.time.value ?: System.currentTimeMillis(),
                prefs.is24HourFormat, prefs.appLanguage)
    }

    private fun moveTask(listId: String) {
        val item = mItem
        if (item != null) {
            val initListId = item.listId
            if (!listId.matches(initListId.toRegex())) {
                item.listId = listId
                viewModel.moveGoogleTask(item, initListId)
            } else {
                Toast.makeText(this, getString(R.string.this_is_same_list), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectList(move: Boolean) {
        if (mIsLoading) return
        var list = viewModel.googleTaskLists.value
        if (list == null) list = ArrayList()
        val names = ArrayList<String>()
        var position = 0
        for (i in list.indices) {
            val item = list[i]
            names.add(item.title)
            if (stateViewModel.listId != "" && item.listId != "" && item.listId.matches(stateViewModel.listId.toRegex())) {
                position = i
            }
        }
        val builder = dialogues.getDialog(this)
        builder.setTitle(R.string.choose_list)
        val finalList = list
        builder.setSingleChoiceItems(ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, names),
                position) { dialog, which ->
            dialog.dismiss()
            if (move) {
                moveTask(finalList[which].listId)
            } else {
                showTaskList(finalList[which])
            }
        }
        val alert = builder.create()
        alert.show()
    }

    private fun saveTask() {
        if (mIsLoading) return
        val taskName = editField.text.toString().trim()
        if (taskName.matches("".toRegex())) {
            editField.error = getString(R.string.must_be_not_empty)
            return
        }
        val note = detailsField.text.toString().trim()
        var due: Long = 0
        if (isDate()) due = stateViewModel.date.value ?: 0
        var reminder: Reminder? = null
        if (isReminder()) reminder = createReminder(taskName)
        var item = mItem
        if (stateViewModel.action.matches(TasksConstants.EDIT.toRegex()) && item != null) {
            val initListId = item.listId
            item.listId = stateViewModel.listId
            item.status = GTasks.TASKS_NEED_ACTION
            item.title = taskName
            item.notes = note
            if (reminder != null) {
                item.uuId = reminder.uuId
            }
            item.dueDate = due
            if (stateViewModel.listId != "") {
                viewModel.updateAndMoveGoogleTask(item, initListId, reminder)
            } else {
                viewModel.updateGoogleTask(item, reminder)
            }
        } else {
            item = GoogleTask()
            item.listId = stateViewModel.listId
            item.status = GTasks.TASKS_NEED_ACTION
            item.title = taskName
            item.notes = note
            item.dueDate = due
            if (reminder != null) {
                item.uuId = reminder.uuId
            }
            viewModel.newGoogleTask(item, reminder)
        }
    }

    private fun dateTime(): Long {
        val result = Calendar.getInstance()
        result.timeInMillis = System.currentTimeMillis()

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = stateViewModel.date.value ?: System.currentTimeMillis()

        result.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
        result.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
        result.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))

        calendar.timeInMillis = stateViewModel.time.value ?: System.currentTimeMillis()

        result.set(Calendar.HOUR, calendar.get(Calendar.HOUR))
        result.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
        result.set(Calendar.SECOND, 0)
        result.set(Calendar.MILLISECOND, 0)

        return result.timeInMillis
    }

    private fun createReminder(task: String): Reminder? {
        val group = viewModel.defaultReminderGroup.value ?: return null
        val due = dateTime()
        val reminder = Reminder()
        reminder.type = Reminder.BY_DATE
        reminder.summary = SuperUtil.normalizeSummary(task)
        reminder.groupUuId = group.groupUuId
        reminder.startTime = TimeUtil.getGmtFromDateTime(due)
        reminder.eventTime = TimeUtil.getGmtFromDateTime(due)
        return reminder
    }

    private fun deleteDialog() {
        if (mIsLoading) return
        val builder = dialogues.getDialog(this)
        builder.setMessage(getString(R.string.delete_this_task))
        builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            dialog.dismiss()
            deleteTask()
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteTask() {
        if (mIsLoading) return
        mItem?.let { viewModel.deleteGoogleTask(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_task, menu)
        if (mItem != null) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, R.string.delete_task)
            menu.add(Menu.NONE, MENU_ITEM_MOVE, 100, R.string.move_to_another_list)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            MENU_ITEM_DELETE -> {
                deleteDialog()
                return true
            }
            MENU_ITEM_MOVE -> {
                selectList(true)
                return true
            }
            R.id.action_add -> {
                saveTask()
                return true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun dateDialog() {
        val c = Calendar.getInstance()
        c.timeInMillis = stateViewModel.date.value ?: System.currentTimeMillis()
        TimeUtil.showDatePicker(this, themeUtil.dialogStyle, prefs, c.get(Calendar.YEAR),
                c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), mDateCallBack)
    }

    private fun timeDialog() {
        val c = Calendar.getInstance()
        c.timeInMillis = stateViewModel.time.value ?: System.currentTimeMillis()
        TimeUtil.showTimePicker(this, themeUtil.dialogStyle, prefs.is24HourFormat,
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), mTimeCallBack)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(stateViewModel)
        lifecycle.removeObserver(viewModel)
        hideKeyboard()
        UpdatesHelper.updateTasksWidget(this)
    }

    override fun onBackPressed() {
        if (mIsLoading) return
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PinLoginActivity.REQ_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                finish()
            } else {
                stateViewModel.isLogged = true
            }
        }
    }

    companion object {
        private const val MENU_ITEM_DELETE = 12
        private const val MENU_ITEM_MOVE = 14
        private const val ARG_LOGGED = "arg_logged"
        private const val ARG_LIST = "arg_list"
        private const val ARG_LOADING = "arg_loading"

        fun openLogged(context: Context, intent: Intent? = null) {
            if (intent == null) {
                context.startActivity(Intent(context, TaskActivity::class.java)
                        .putExtra(ARG_LOGGED, true))
            } else {
                intent.putExtra(ARG_LOGGED, true)
                context.startActivity(intent)
            }
        }
    }
}
