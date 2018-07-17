package com.elementary.tasks.google_tasks

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast

import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.viewModels.googleTasks.GoogleTaskViewModel
import com.elementary.tasks.databinding.ActivityCreateGoogleTaskBinding

import java.util.ArrayList
import java.util.Calendar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders

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

    private var binding: ActivityCreateGoogleTaskBinding? = null
    private var viewModel: GoogleTaskViewModel? = null

    private var mHour = 0
    private var mMinute = 0
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 1
    private var listId: String? = null
    private var action: String? = null
    private var isReminder = false
    private var isDate = false

    private var mItem: GoogleTask? = null
    private var mDialog: ProgressDialog? = null
    private val mSimpleCallback = object : TasksCallback {
        override fun onFailed() {
            hideDialog()
        }

        override fun onComplete() {
            hideDialog()
            finish()
        }
    }

    internal var myDateCallBack: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        mYear = year
        mMonth = monthOfYear
        mDay = dayOfMonth
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(year, monthOfYear, dayOfMonth)
        binding!!.dateField.text = TimeUtil.getDate(calendar.time)
    }

    internal var myCallBack: TimePickerDialog.OnTimeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
        mHour = hourOfDay
        mMinute = minute
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
        c.set(Calendar.MINUTE, minute)
        binding!!.timeField.text = TimeUtil.getTime(c.time, prefs!!.is24HourFormatEnabled)
    }

    private fun hideDialog() {
        if (mDialog != null && mDialog!!.isShowing) {
            try {
                mDialog!!.dismiss()
            } catch (e: IllegalArgumentException) {
                LogUtil.d(TAG, "hideDialog: " + e.localizedMessage)
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_google_task)
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
        if (action == null) action = TasksConstants.CREATE

        if (action!!.matches(TasksConstants.CREATE.toRegex())) {
            initViewModel(null, tmp)
        } else {
            initViewModel(tmp, null)
        }
        switchDate()
    }

    private fun initViewModel(taskId: String?, listId: String?) {
        this.listId = listId
        viewModel = ViewModelProviders.of(this, GoogleTaskViewModel.Factory(application, taskId)).get(GoogleTaskViewModel::class.java)
        viewModel!!.isInProgress.observe(this, { aBoolean ->
            if (aBoolean != null) {
                if (aBoolean!!)
                    showProgressDialog()
                else
                    hideDialog()
            }
        })
        viewModel!!.result.observe(this, { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.SAVED, Commands.DELETED -> finish()
                }
            }
        })
        viewModel!!.googleTask.observe(this, { googleTask ->
            if (googleTask != null) {
                editTask(googleTask!!)
            }
        })
        viewModel!!.googleTaskLists.observe(this, { googleTaskLists ->
            if (googleTaskLists != null && listId != null) {
                selectCurrent(googleTaskLists!!)
            }
        })
        viewModel!!.defaultTaskList.observe(this, { googleTaskList ->
            if (googleTaskList != null && listId == null) {
                showTaskList(googleTaskList!!)
            }
        })
        viewModel!!.reminder.observe(this, { reminder ->
            if (reminder != null) {
                showReminder(reminder!!)
            }
        })
    }

    private fun showReminder(reminder: Reminder) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
        binding!!.timeField.text = TimeUtil.getTime(calendar.time, prefs!!.is24HourFormatEnabled)
        isReminder = true
    }

    private fun showTaskList(googleTaskList: GoogleTaskList) {
        this.listId = googleTaskList.listId
        binding!!.toolbar.setTitle(R.string.new_task)
        binding!!.listText.text = googleTaskList.title
        setColor(googleTaskList.color)
    }

    private fun selectCurrent(googleTaskLists: List<GoogleTaskList>) {
        binding!!.toolbar.setTitle(R.string.new_task)
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
        binding!!.toolbar.setTitle(R.string.edit_task)
        binding!!.editField.setText(googleTask.title)

        val note = googleTask.notes
        if (note != null) {
            binding!!.noteField.setText(note)
            binding!!.noteField.setSelection(binding!!.noteField.text!!.toString().trim { it <= ' ' }.length)
        }
        val time = mItem!!.dueDate
        if (time != 0L) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = time
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            isDate = true
            binding!!.dateField.text = TimeUtil.getDate(calendar.time)
        }
        if (viewModel!!.googleTaskLists.value != null) {
            for (googleTaskList in viewModel!!.googleTaskLists.value!!) {
                if (googleTaskList.listId == googleTask.listId) {
                    showTaskList(googleTaskList)
                    break
                }
            }
        }
        viewModel!!.loadReminder(googleTask.uuId)
    }

    private fun initFields() {
        binding!!.listText.setOnClickListener { v -> selectList(false) }
        binding!!.dateField.setOnClickListener { v -> selectDateAction(1) }
        binding!!.timeField.setOnClickListener { v -> selectDateAction(2) }
    }

    private fun initToolbar() {
        binding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        setSupportActionBar(binding!!.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(false)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
    }

    private fun selectDateAction(type: Int) {
        val builder = Dialogues.getDialog(this)
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
        if (!isDate) binding!!.dateField.text = getString(R.string.no_date)
        if (!isReminder) binding!!.timeField.text = getString(R.string.no_reminder)
    }

    private fun moveTask(listId: String?) {
        if (mItem != null) {
            val initListId = mItem!!.listId
            if (!listId!!.matches(initListId.toRegex())) {
                mItem!!.listId = listId
                viewModel!!.moveGoogleTask(mItem!!, initListId!!)
            } else {
                Toast.makeText(this, getString(R.string.this_is_same_list), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showProgressDialog() {
        mDialog = ProgressDialog.show(this, null, getString(R.string.please_wait), true, false)
    }

    private fun selectList(move: Boolean) {
        var list = viewModel!!.googleTaskLists.value
        if (list == null) list = ArrayList()
        val names = ArrayList<String>()
        var position = 0
        for (i in list.indices) {
            val item = list[i]
            names.add(item.title)
            if (listId != null && item.listId != null && item.listId!!.matches(listId.toRegex())) {
                position = i
            }
        }
        val builder = Dialogues.getDialog(this)
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
        if (mItem != null && prefs!!.isAutoSaveEnabled) {
            saveTask()
        }
    }

    private fun saveTask() {
        val taskName = binding!!.editField.text!!.toString().trim { it <= ' ' }
        if (taskName.matches("".toRegex())) {
            binding!!.editField.error = getString(R.string.must_be_not_empty)
            return
        }
        val note = binding!!.noteField.text!!.toString().trim { it <= ' ' }
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(mYear, mMonth, mDay, 12, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        var due: Long = 0
        if (isDate) due = calendar.timeInMillis
        var reminder: Reminder? = null
        if (isReminder) reminder = createReminder(taskName)
        if (action!!.matches(TasksConstants.EDIT.toRegex()) && mItem != null) {
            val initListId = mItem!!.listId
            mItem!!.listId = listId
            mItem!!.status = Google.TASKS_NEED_ACTION
            mItem!!.title = taskName
            mItem!!.notes = note
            if (reminder != null) {
                mItem!!.uuId = reminder.uuId
            }
            mItem!!.dueDate = due
            if (listId != null) {
                viewModel!!.updateAndMoveGoogleTask(mItem!!, initListId!!, reminder)
            } else {
                viewModel!!.updateGoogleTask(mItem!!, reminder)
            }
        } else {
            mItem = GoogleTask()
            mItem!!.listId = listId
            mItem!!.status = Google.TASKS_NEED_ACTION
            mItem!!.title = taskName
            mItem!!.notes = note
            mItem!!.dueDate = due
            if (reminder != null) {
                mItem!!.uuId = reminder.uuId
            }
            viewModel!!.newGoogleTask(mItem!!, reminder)
        }
    }

    private fun createReminder(task: String): Reminder? {
        val group = viewModel!!.defaultGroup.value ?: return null
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
        val builder = Dialogues.getDialog(this)
        builder.setMessage(getString(R.string.delete_this_task))
        builder.setPositiveButton(getString(R.string.yes)) { dialog, which ->
            dialog.dismiss()
            deleteTask()
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteTask() {
        if (mItem != null) {
            viewModel!!.deleteGoogleTask(mItem!!)
        }
    }

    private fun setColor(i: Int) {
        binding!!.appBar.setBackgroundColor(themeUtil!!.getNoteColor(i))
        if (Module.isLollipop) {
            window.statusBarColor = themeUtil!!.getNoteDarkColor(i)
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

    protected fun dateDialog() {
        TimeUtil.showDatePicker(this, myDateCallBack, mYear, mMonth, mDay)
    }

    protected fun timeDialog() {
        TimeUtil.showTimePicker(this, myCallBack, mHour, mMinute)
    }

    override fun onDestroy() {
        UpdatesHelper.getInstance(this).updateTasksWidget()
        super.onDestroy()
    }

    companion object {

        private val TAG = "TaskActivity"

        private val MENU_ITEM_DELETE = 12
        private val MENU_ITEM_MOVE = 14
    }
}
