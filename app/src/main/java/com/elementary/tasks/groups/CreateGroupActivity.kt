package com.elementary.tasks.groups

import android.content.ContentResolver
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.groups.GroupViewModel
import com.elementary.tasks.core.views.ColorPickerView
import kotlinx.android.synthetic.main.activity_create_group.*
import java.io.IOException
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
class CreateGroupActivity : ThemedActivity(), ColorPickerView.OnColorListener {

    private lateinit var viewModel: GroupViewModel

    private var color = 0
    private var mItem: ReminderGroup? = null

    @Inject
    lateinit var backupTool: BackupTool

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        pickerView.setListener(this)
        pickerView.setSelectedColor(color)
        setColor(color)

        loadGroup()
    }

    private fun showGroup(reminderGroup: ReminderGroup) {
        this.mItem = reminderGroup
        editField.setText(reminderGroup.title)
        color = reminderGroup.color
        pickerView.setSelectedColor(color)
        setColor(color)
        invalidateOptionsMenu()
    }

    private fun loadGroup() {
        val intent = intent
        val id = intent.getStringExtra(Constants.INTENT_ID)
        if (id != null) {
            initViewModel(id)
        } else if (intent.data != null) {
            try {
                val name = intent.data
                val scheme = name?.scheme
                mItem = if (ContentResolver.SCHEME_CONTENT == scheme) {
                    val cr = contentResolver
                    backupTool.getGroup(cr, name)
                } else {
                    backupTool.getGroup(name.path, null)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        }
    }

    private fun initViewModel(id: String) {
        viewModel = ViewModelProviders.of(this, GroupViewModel.Factory(application, id)).get(GroupViewModel::class.java)
        viewModel.reminderGroup.observe(this, Observer{ group ->
            if (group != null) {
                showGroup(group)
            }
        })
        viewModel.result.observe(this, Observer{ commands ->
            if (commands != null) {
                when (commands) {
                    Commands.SAVED, Commands.DELETED -> finish()
                }
            }
        })
        viewModel.allGroups.observe(this, Observer{ groups ->
            if (groups != null) {
                invalidateOptionsMenu()
            }
        })
    }

    private fun saveGroup() {
        val text = editField.text.toString().trim { it <= ' ' }
        if (text.isEmpty()) {
            editField.error = getString(R.string.must_be_not_empty)
            return
        }
        var item = mItem
        if (item == null) {
            item = ReminderGroup(text, UUID.randomUUID().toString(), color, TimeUtil.gmtDateTime)
        } else {
            item.color = color
            item.dateTime = TimeUtil.gmtDateTime
            item.title = text
        }
        viewModel.saveGroup(item)
    }

    override fun onStop() {
        super.onStop()
        if (mItem != null && prefs.isAutoSaveEnabled) {
            saveGroup()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_group_edit, menu)
        if (mItem != null && viewModel.allGroups.value != null && viewModel.allGroups.value!!.size > 1) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                saveGroup()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            MENU_ITEM_DELETE -> {
                deleteItem()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteItem() {
        if (mItem != null) {
            viewModel.deleteGroup(mItem!!)
        }
        finish()
    }

    private fun setColor(i: Int) {
        color = i
        appBar.setBackgroundColor(themeUtil.getColor(themeUtil.getCategoryColor(i)))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = themeUtil.getNoteDarkColor(i)
        }
    }

    override fun onColorSelect(code: Int) {
        setColor(code)
    }

    companion object {

        private const val MENU_ITEM_DELETE = 12
    }
}
