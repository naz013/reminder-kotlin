package com.elementary.tasks.google_tasks.create

import android.app.ProgressDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.googleTasks.GoogleTaskListViewModel
import com.elementary.tasks.core.views.ColorPickerView
import kotlinx.android.synthetic.main.activity_create_task_list.*

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

class TaskListActivity : ThemedActivity(), ColorPickerView.OnColorListener {

    private lateinit var viewModel: GoogleTaskListViewModel
    private var mItem: GoogleTaskList? = null
    private var color: Int = 0

    private var mDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task_list)

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        pickerView.setListener(this)

        initViewModel(intent.getStringExtra(Constants.INTENT_ID))
    }

    private fun hideDialog() {
        try {
            if (mDialog != null && mDialog!!.isShowing) {
                mDialog?.dismiss()
            }
        } catch (e: Exception) {
        }
    }

    private fun initViewModel(id: String) {
        viewModel = ViewModelProviders.of(this, GoogleTaskListViewModel.Factory(application, id)).get(GoogleTaskListViewModel::class.java)
        viewModel.googleTaskList.observe(this, Observer{ googleTaskList ->
            if (googleTaskList != null) {
                editTaskList(googleTaskList)
            }
        })
        viewModel.isInProgress.observe(this, Observer{ aBoolean ->
            if (aBoolean != null) {
                if (aBoolean) showProgressDialog()
                else hideDialog()
            }
        })
        viewModel.result.observe(this, Observer{ commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED, Commands.SAVED -> finish()
                }
            }
        })
    }

    private fun editTaskList(googleTaskList: GoogleTaskList) {
        mItem = googleTaskList
        editField.setText(googleTaskList.title)
        if (googleTaskList.def == 1) {
            defaultCheck.isChecked = true
            defaultCheck.isEnabled = false
        }
        color = googleTaskList.color
        pickerView.setSelectedColor(color)
        setColor(color)
    }

    private fun saveTaskList() {
        val listName = editField.text.toString().trim()
        if (listName.matches("".toRegex())) {
            editField.error = getString(R.string.must_be_not_empty)
            return
        }
        var isNew = false
        var item = mItem
        if (item == null) {
            item = GoogleTaskList()
            isNew = true
        }
        item.title = listName
        item.color = color
        item.updated = System.currentTimeMillis()
        if (defaultCheck.isChecked) {
            item.def = 1
            val defList = viewModel.defaultTaskList.value
            if (defList != null) {
                defList.def = 0
                viewModel.saveLocalGoogleTaskList(defList)
            }
        }

        if (isNew) {
            viewModel.newGoogleTaskList(item)
        } else {
            viewModel.updateGoogleTaskList(item)
        }
    }

    override fun onStop() {
        super.onStop()
        if (mItem != null && prefs.isAutoSaveEnabled) {
            saveTaskList()
        }
    }

    private fun showProgressDialog() {
        mDialog = ProgressDialog.show(this, null, getString(R.string.please_wait), true, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            MENU_ITEM_DELETE -> {
                deleteDialog()
                true
            }
            R.id.action_add -> {
                saveTaskList()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteDialog() {
        val builder = Dialogues.getDialog(this)
        builder.setMessage(getString(R.string.delete_this_list))
        builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            dialog.dismiss()
            deleteList()
            finish()
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteList() {
        if (mItem != null) {
            viewModel.deleteGoogleTaskList(mItem!!)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_create_task_list, menu)
        if (mItem != null && mItem!!.systemDefault != 1) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, R.string.delete_list)
        }
        return true
    }

    private fun setColor(i: Int) {
        color = i
        appBar.setBackgroundColor(themeUtil.getNoteColor(i))
        if (Module.isLollipop) {
            window.statusBarColor = themeUtil.getNoteDarkColor(i)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        UpdatesHelper.getInstance(this).updateTasksWidget()
    }


    override fun onColorSelect(code: Int) {
        setColor(code)
    }

    companion object {

        private const val MENU_ITEM_DELETE = 12
    }
}
