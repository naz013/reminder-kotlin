package com.elementary.tasks.groups

import android.content.ContentResolver
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
class CreateGroupActivity : ThemedActivity() {

    private lateinit var viewModel: GroupViewModel

    private var mItem: ReminderGroup? = null

    @Inject
    lateinit var backupTool: BackupTool

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        initActionBar()
        colorSlider.setColors(themeUtil.colorsForSlider())
        loadGroup()
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
        toolbar.setTitle(R.string.create_group)
    }

    private fun showGroup(reminderGroup: ReminderGroup) {
        this.mItem = reminderGroup
        nameInput.setText(reminderGroup.groupTitle)
        colorSlider.setSelection(reminderGroup.groupColor)
        defaultCheck.isEnabled = !reminderGroup.isDefaultGroup
        defaultCheck.isChecked = reminderGroup.isDefaultGroup
        toolbar.setTitle(R.string.change_group)
        invalidateOptionsMenu()
    }

    private fun loadGroup() {
        val intent = intent
        val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""
        initViewModel(id)
        if (intent.data != null) {
            try {
                val name = intent.data ?: return
                val scheme = name.scheme
                val item = if (ContentResolver.SCHEME_CONTENT == scheme) {
                    val cr = contentResolver
                    backupTool.getGroup(cr, name)
                } else {
                    backupTool.getGroup(name.path, null)
                }
                if (item != null) {
                    showGroup(item)
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
                    else -> {
                    }
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
        val text = nameInput.text.toString().trim { it <= ' ' }
        if (text.isEmpty()) {
            nameLayout.error = getString(R.string.must_be_not_empty)
            nameLayout.isErrorEnabled = true
            return
        }
        var item = mItem
        if (item == null) {
            item = ReminderGroup(text, UUID.randomUUID().toString(), 0, TimeUtil.gmtDateTime)
        }
        val wasDefault = item.isDefaultGroup

        item.groupColor = colorSlider.selectedItem
        item.isDefaultGroup = defaultCheck.isChecked
        item.groupTitle = text
        item.groupDateTime = TimeUtil.gmtDateTime

        viewModel.saveGroup(item, wasDefault)
    }

    override fun onStop() {
        super.onStop()
        if (mItem != null && prefs.isAutoSaveEnabled) {
            saveGroup()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_simple_save_action, menu)
        val groups = viewModel.allGroups.value ?: listOf()
        if (mItem != null && groups.size > 1) {
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
        val item = mItem
        if (item != null) {
            viewModel.deleteGroup(item)
        }
    }

    companion object {

        private const val MENU_ITEM_DELETE = 12
    }
}
