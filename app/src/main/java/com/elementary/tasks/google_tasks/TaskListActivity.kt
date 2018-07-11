package com.elementary.tasks.google_tasks

import android.app.ProgressDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.viewModels.google_tasks.GoogleTaskListViewModel
import com.elementary.tasks.core.views.ColorPickerView
import com.elementary.tasks.databinding.ActivityCreateTaskListBinding
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

class TaskListActivity : ThemedActivity(), ColorPickerView.OnColorListener {

    private var binding: ActivityCreateTaskListBinding? = null
    private var viewModel: GoogleTaskListViewModel? = null
    private var mItem: GoogleTaskList? = null
    private var color: Int = 0

    private var mDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_task_list)

        binding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        setSupportActionBar(binding!!.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(false)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        binding!!.pickerView.setListener(this)

        initViewModel(intent.getStringExtra(Constants.INTENT_ID))
    }

    private fun hideDialog() {
        if (mDialog != null && mDialog!!.isShowing) {
            mDialog!!.dismiss()
        }
    }

    private fun initViewModel(id: String) {
        viewModel = ViewModelProviders.of(this, GoogleTaskListViewModel.Factory(application, id)).get(GoogleTaskListViewModel::class.java)
        viewModel!!.googleTaskList.observe(this, { googleTaskList ->
            if (googleTaskList != null) {
                editTaskList(googleTaskList!!)
            }
        })
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
                    Commands.DELETED, Commands.SAVED -> finish()
                }
            }
        })
    }

    private fun editTaskList(googleTaskList: GoogleTaskList) {
        mItem = googleTaskList
        binding!!.editField.setText(mItem!!.title)
        if (mItem!!.def == 1) {
            binding!!.defaultCheck.isChecked = true
            binding!!.defaultCheck.isEnabled = false
        }
        color = mItem!!.color
        binding!!.pickerView.setSelectedColor(color)
        setColor(color)
    }

    private fun saveTaskList() {
        val listName = binding!!.editField.text!!.toString()
        if (listName.matches("".toRegex())) {
            binding!!.editField.error = getString(R.string.must_be_not_empty)
            return
        }
        var isNew = false
        if (mItem == null) {
            mItem = GoogleTaskList()
            isNew = true
        }
        mItem!!.title = listName
        mItem!!.color = color
        mItem!!.updated = System.currentTimeMillis()
        if (binding!!.defaultCheck.isChecked) {
            mItem!!.def = 1
            val defList = viewModel!!.defaultTaskList.value
            if (defList != null) {
                defList.def = 0
                viewModel!!.saveLocalGoogleTaskList(defList)
            }
        }

        if (isNew) {
            viewModel!!.newGoogleTaskList(mItem!!)
        } else {
            viewModel!!.updateGoogleTaskList(mItem!!)
        }
    }

    override fun onStop() {
        super.onStop()
        if (mItem != null && prefs!!.isAutoSaveEnabled) {
            saveTaskList()
        }
    }

    private fun showProgressDialog() {
        mDialog = ProgressDialog.show(this, null, getString(R.string.please_wait), true, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            MENU_ITEM_DELETE -> {
                deleteDialog()
                return true
            }
            R.id.action_add -> {
                saveTaskList()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun deleteDialog() {
        val builder = Dialogues.getDialog(this)
        builder.setMessage(getString(R.string.delete_this_list))
        builder.setPositiveButton(getString(R.string.yes)) { dialog, which ->
            dialog.dismiss()
            deleteList()
            finish()
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteList() {
        if (mItem != null) {
            viewModel!!.deleteGoogleTaskList(mItem!!)
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
        binding!!.appBar.setBackgroundColor(themeUtil!!.getNoteColor(i))
        if (Module.isLollipop) {
            window.statusBarColor = themeUtil!!.getNoteDarkColor(i)
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

        private val MENU_ITEM_DELETE = 12
    }
}
