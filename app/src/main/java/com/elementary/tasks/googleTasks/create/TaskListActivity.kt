package com.elementary.tasks.googleTasks.create

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.googleTasks.GoogleTaskListViewModel
import kotlinx.android.synthetic.main.activity_create_task_list.*
import kotlinx.android.synthetic.main.view_progress.*
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
class TaskListActivity : ThemedActivity() {

    private lateinit var viewModel: GoogleTaskListViewModel
    private var mItem: GoogleTaskList? = null
    private var mIsLoading = false

    @Inject
    lateinit var updatesHelper: UpdatesHelper

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task_list)
        progressMessageView.text = getString(R.string.please_wait)
        updateProgress(false)

        initActionBar()
        colorSlider.setColors(themeUtil.colorsForSlider())

        initViewModel(intent.getStringExtra(Constants.INTENT_ID) ?: "")
    }

    private fun updateProgress(b: Boolean) {
        mIsLoading = b
        if (b) {
            progressView.visibility = View.VISIBLE
        } else {
            progressView.visibility = View.GONE
        }
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        if (isDark) {
            toolbar.setNavigationIcon(R.drawable.ic_twotone_arrow_white_24px)
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_twotone_arrow_back_24px)
        }
        toolbar.setTitle(R.string.new_tasks_list)
    }

    private fun initViewModel(id: String) {
        viewModel = ViewModelProviders.of(this, GoogleTaskListViewModel.Factory(application, id)).get(GoogleTaskListViewModel::class.java)
        viewModel.googleTaskList.observe(this, Observer { googleTaskList ->
            if (googleTaskList != null) {
                editTaskList(googleTaskList)
            }
        })
        viewModel.isInProgress.observe(this, Observer { aBoolean ->
            if (aBoolean != null) {
                updateProgress(aBoolean)
            }
        })
        viewModel.result.observe(this, Observer { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED, Commands.SAVED -> onBackPressed()
                }
            }
        })
    }

    private fun editTaskList(googleTaskList: GoogleTaskList) {
        this.mItem = googleTaskList
        toolbar.title = getString(R.string.edit_task_list)
        editField.setText(googleTaskList.title)
        if (googleTaskList.def == 1) {
            defaultCheck.isChecked = true
            defaultCheck.isEnabled = false
        }
        colorSlider.setSelection(googleTaskList.color)
    }

    private fun saveTaskList() {
        if (mIsLoading) return
        val listName = editField.text.toString().trim()
        if (listName == "") {
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
        item.color = colorSlider.selectedItem
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
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
        if (mIsLoading) return
        val builder = dialogues.getDialog(this)
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
        val item = mItem ?: return
        viewModel.deleteGoogleTaskList(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_simple_save_action, menu)
        val item = mItem
        if (item != null && item.systemDefault != 1) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, R.string.delete_list)
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        updatesHelper.updateTasksWidget()
    }

    override fun onBackPressed() {
        if (mIsLoading) return
        super.onBackPressed()
    }

    companion object {

        private const val MENU_ITEM_DELETE = 12
    }
}
