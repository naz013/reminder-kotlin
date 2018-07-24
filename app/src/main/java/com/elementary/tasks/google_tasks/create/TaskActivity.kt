package com.elementary.tasks.google_tasks.create

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.googleTasks.GoogleTaskViewModel
import kotlinx.android.synthetic.main.activity_create_google_task.*
import java.util.*
import javax.inject.Inject

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
open class TaskActivity : ThemedActivity() {

    private lateinit var viewModel: GoogleTaskViewModel

    private var mHour = 0
    private var mMinute = 0
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 1
    private var listId: String = ""
    private var action: String = ""
    private var isReminder = false
    private var isDate = false

    private var mItem: GoogleTask? = null
    private var mDialog: ProgressDialog? = null
    private var myDateCallBack: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        mYear = year
        mMonth = monthOfYear
        mDay = dayOfMonth
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(year, monthOfYear, dayOfMonth)
        dateField.text = TimeUtil.getDate(calendar.time)
    }

    private var myCallBack: TimePickerDialog.OnTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        mHour = hourOfDay
        mMinute = minute
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
        c.set(Calendar.MINUTE, minute)
        timeField.text = TimeUtil.getTime(c.time, prefs.is24HourFormatEnabled)
    }

    @Inject
    lateinit var updatesHelper: UpdatesHelper

    init {
        ReminderApp.appComponent.inject(this)
    }

    private fun hideDialog() {
        if (mDialog != null && mDialog!!.isShowing) {
            try {
                mDialog?.dismiss()
            } catch (e: IllegalArgumentException) {
                LogUtil.d(TAG, "hideDialog: " + e.localizedMessage)
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_google_task)
        initToolbar()
        initFields()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        mHour = calendar.get(Calendar.HOUR_OF_DAY)
        mMinute = calendar.get(Calendar.MINUTE)
        mYear = calendar.get(Calendar.YEAR)
        mMonth = calendar.get(Calendar.MONTH)
        mDay = calendar.get(Calendar.DAY_OF_MONTH)
        val intent = intent
        val tmp = intent.getStringExtra(Constants.INTENT_ID)
        action = intent.getStringExtra(TasksConstants.INTENT_ACTION)
        if (action == "") action = TasksConstants.CREATE

        if (action.matches(TasksConstants.CREATE.toRegex())) {
            initViewModel("", tmp)
        } else {
            initViewModel(tmp, "")
        }
        switchDate()
    }

    private fun initViewModel(taskId: String, listId: String) {
        this.listId = listId
        viewModel = ViewModelProviders.of(this, GoogleTaskViewModel.Factory(application, taskId)).get(GoogleTaskViewModel::class.java)
        viewModel.isInProgress.observe(this, Observer{ aBoolean ->
            if (aBoolean != null) {
                if (aBoolean) showProgressDialog()
                else hideDialog()
            }
        })
        viewModel.result.observe(this, Observer{ commands ->
            if (commands != null) {
                when (commands) {
                    Commands.SAVED, Commands.DELETED -> finish()
                }
            }
        })
        viewModel.googleTask.observe(this, Observer { googleTask ->
            if (googleTask != null) {
                editTask(googleTask)
            }
        })
        viewModel.googleTaskLists.observe(this, Observer{ googleTaskLists ->
            if (googleTaskLists != null && listId != "") {
                selectCurrent(googleTaskLists)
            }
        })
        viewModel.defaultTaskList.observe(this, Observer{ googleTaskList ->
            if (googleTaskList != null && listId == "") {
                showTaskList(googleTaskList)
            }
        })
        viewModel.reminder.observe(this, Observer{ reminder ->
            if (reminder != null) {
                showReminder(reminder)
            }
        })
    }

    private fun showReminder(reminder: Reminder) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
        timeField.text = TimeUtil.getTime(calendar.time, prefs.is24HourFormatEnabled)
        isReminder = true
    }

    private fun showTaskList(googleTaskList: GoogleTaskList) {
        this.listId = googleTaskList.listId
        toolbar.setTitle(R.string.new_task)
        listText.text = googleTaskList.title
        setColor(googleTaskList.color)
    }

    private fun selectCurrent(googleTaskLists: List<GoogleTaskList>) {
        toolbar.setTitle(R.string.new_task)
        for (googleTaskList in googleTaskLists) {
            if (googleTaskList.listId == listId) {
                showTaskList(googleTaskList)
                break
            }
        }
    }

    private fun editTask(googleTask: GoogleTask) {
        this.mItem = googleTask
        this.listId = googleTask.listId
        toolbar.setTitle(R.string.edit_task)
        editField.setText(googleTask.title)

        val note = googleTask.notes
        if (note != "") {
            noteField.setText(note)
            noteField.setSelection(noteField.text.toString().trim { it <= ' ' }.length)
        }
        val time = googleTask.dueDate
        if (time != 0L) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = time
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            isDate = true
            dateField.text = TimeUtil.getDate(calendar.time)
        }
        if (viewModel.googleTaskLists.value != null) {
            for (googleTaskList in viewModel.googleTaskLists.value!!) {
                if (googleTaskList.listId == googleTask.listId) {
                    showTaskList(googleTaskList)
                    break
                }
            }
        }
        viewModel.loadReminder(googleTask.uuId)
    }

    private fun initFields() {
        listText.setOnClickListener { selectList(false) }
        dateField.setOnClickListener { selectDateAction(1) }
        timeField.setOnClickListener { selectDateAction(2) }
    }

    private fun initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }
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
            if (isDate) selection = 1
        }
        if (type == 2) {
            if (isReminder) selection = 1
        }
        builder.setSingleChoiceItems(adapter, selection) { dialog, which ->
            if (which != -1) {
                dialog.dismiss()
                if (type == 1) {
                    when (which) {
                        0 -> {
                            isDate = false
                            switchDate()
                        }
                        1 -> {
                            isDate = true
                            dateDialog()
                        }
                    }
                }
                if (type == 2) {
                    when (which) {
                        0 -> {
                            isReminder = false
                            switchDate()
                        }
                        1 -> {
                            isReminder = true
                            timeDialog()
                        }
                    }
                }
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun switchDate() {
        if (!isDate) dateField.text = getString(R.string.no_date)
        if (!isReminder) timeField.text = getString(R.string.no_reminder)
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

    private fun showProgressDialog() {
        mDialog = ProgressDialog.show(this, null, getString(R.string.please_wait), true, false)
    }

    private fun selectList(move: Boolean) {
        var list = viewModel.googleTaskLists.value
        if (list == null) list = ArrayList()
        val names = ArrayList<String>()
        var position = 0
        for (i in list.indices) {
            val item = list[i]
            names.add(item.title)
            if (listId != "" && item.listId != "" && item.listId.matches(listId.toRegex())) {
                position = i
            }
        }
        val builder = dialogues.getDialog(this)
        builder.setTitle(R.string.choose_list)
        val finalList = list
        builder.setSingleChoiceItems(ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, names),
                position) { dialog, which ->
            dialog.dismiss()
            if (move)
                moveTask(finalList[which].listId)
            else
                showTaskList(finalList[which])
        }
        val alert = builder.create()
        alert.show()
    }

    override fun onStop() {
        super.onStop()
        if (mItem != null && prefs.isAutoSaveEnabled) {
            saveTask()
        }
    }

    private fun saveTask() {
        val taskName = editField.text.toString().trim { it <= ' ' }
        if (taskName.matches("".toRegex())) {
            editField.error = getString(R.string.must_be_not_empty)
            return
        }
        val note = noteField.text.toString().trim { it <= ' ' }
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(mYear, mMonth, mDay, 12, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        var due: Long = 0
        if (isDate) due = calendar.timeInMillis
        var reminder: Reminder? = null
        if (isReminder) reminder = createReminder(taskName)
        var item = mItem
        if (action.matches(TasksConstants.EDIT.toRegex()) && item != null) {
            val initListId = item.listId
            item.listId = listId
            item.status = Google.TASKS_NEED_ACTION
            item.title = taskName
            item.notes = note
            if (reminder != null) {
                item.uuId = reminder.uuId
            }
            item.dueDate = due
            if (listId != "") {
                viewModel.updateAndMoveGoogleTask(item, initListId, reminder)
            } else {
                viewModel.updateGoogleTask(item, reminder)
            }
        } else {
            item = GoogleTask()
            item.listId = listId
            item.status = Google.TASKS_NEED_ACTION
            item.title = taskName
            item.notes = note
            item.dueDate = due
            if (reminder != null) {
                item.uuId = reminder.uuId
            }
            viewModel.newGoogleTask(item, reminder)
        }
    }

    private fun createReminder(task: String): Reminder? {
        val group = viewModel.defaultGroup.value ?: return null
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(mYear, mMonth, mDay, mHour, mMinute)
        val due = calendar.timeInMillis
        val reminder = Reminder()
        reminder.type = Reminder.BY_DATE
        reminder.summary = task
        reminder.groupUuId = group.uuId
        reminder.startTime = TimeUtil.getGmtFromDateTime(due)
        reminder.eventTime = TimeUtil.getGmtFromDateTime(due)
        return reminder
    }

    private fun deleteDialog() {
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
        if (mItem != null) {
            viewModel.deleteGoogleTask(mItem!!)
        }
    }

    private fun setColor(i: Int) {
        appBar.setBackgroundColor(themeUtil.getNoteColor(i))
        if (Module.isLollipop) {
            window.statusBarColor = themeUtil.getNoteDarkColor(i)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_create_task, menu)
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
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun dateDialog() {
        TimeUtil.showDatePicker(this, prefs, myDateCallBack, mYear, mMonth, mDay)
    }

    private fun timeDialog() {
        TimeUtil.showTimePicker(this, prefs.is24HourFormatEnabled, myCallBack, mHour, mMinute)
    }

    override fun onDestroy() {
        super.onDestroy()
        updatesHelper.updateTasksWidget()
    }

    companion object {

        private const val TAG = "TaskActivity"

        private const val MENU_ITEM_DELETE = 12
        private const val MENU_ITEM_MOVE = 14
    }
}
