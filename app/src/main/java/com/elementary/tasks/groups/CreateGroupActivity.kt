package com.elementary.tasks.groups

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.view_models.groups.GroupViewModel
import com.elementary.tasks.core.views.ColorPickerView
import com.elementary.tasks.databinding.ActivityCreateGroupBinding

import java.io.IOException
import java.util.UUID
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
class CreateGroupActivity : ThemedActivity(), ColorPickerView.OnColorListener {

    private var binding: ActivityCreateGroupBinding? = null
    private var viewModel: GroupViewModel? = null

    private var color = 0
    private var mItem: Group? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_group)
        setSupportActionBar(binding!!.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        binding!!.pickerView.setListener(this)
        binding!!.pickerView.setSelectedColor(color)
        setColor(color)

        loadGroup()
    }

    private fun showGroup(group: Group) {
        this.mItem = group
        binding!!.editField.setText(group.title)
        color = group.color
        binding!!.pickerView.setSelectedColor(color)
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
                val scheme = name!!.scheme
                if (ContentResolver.SCHEME_CONTENT == scheme) {
                    val cr = contentResolver
                    mItem = BackupTool.getInstance().getGroup(cr, name)
                } else {
                    mItem = BackupTool.getInstance().getGroup(name.path, null)
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
        viewModel!!.group.observe(this, { group ->
            if (group != null) {
                showGroup(group!!)
            }
        })
        viewModel!!.result.observe(this, { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.SAVED, Commands.DELETED -> finish()
                }
            }
        })
        viewModel!!.allGroups.observe(this, { groups ->
            if (groups != null) {
                invalidateOptionsMenu()
            }
        })
    }

    private fun saveGroup() {
        val text = binding!!.editField.text!!.toString().trim { it <= ' ' }
        if (text.length == 0) {
            binding!!.editField.error = getString(R.string.must_be_not_empty)
            return
        }
        if (mItem == null) {
            mItem = Group(text, UUID.randomUUID().toString(), color, TimeUtil.gmtDateTime)
        } else {
            mItem!!.color = color
            mItem!!.dateTime = TimeUtil.gmtDateTime
            mItem!!.title = text
        }
        viewModel!!.saveGroup(mItem!!)
    }

    override fun onStop() {
        super.onStop()
        if (mItem != null && prefs!!.isAutoSaveEnabled) {
            saveGroup()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_group_edit, menu)
        if (mItem != null && viewModel!!.allGroups.value != null && viewModel!!.allGroups.value!!.size > 1) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                saveGroup()
                return true
            }
            android.R.id.home -> {
                finish()
                return true
            }
            MENU_ITEM_DELETE -> {
                deleteItem()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun deleteItem() {
        if (mItem != null) {
            viewModel!!.deleteGroup(mItem!!)
        }
        finish()
    }

    private fun setColor(i: Int) {
        color = i
        val cs = ThemeUtil.getInstance(this)
        binding!!.appBar.setBackgroundColor(cs.getColor(cs.getCategoryColor(i)))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = cs.getNoteDarkColor(i)
        }
    }

    override fun onColorSelect(code: Int) {
        setColor(code)
    }

    companion object {

        private val MENU_ITEM_DELETE = 12
    }
}
