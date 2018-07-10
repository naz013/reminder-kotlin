package com.elementary.tasks.reminder.preview

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View

import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.view_models.reminders.ReminderViewModel
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity
import com.elementary.tasks.databinding.ActivityShoppingPreviewBinding
import com.elementary.tasks.reminder.lists.ShopListRecyclerAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager

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
class ShoppingPreviewActivity : ThemedActivity() {

    private var binding: ActivityShoppingPreviewBinding? = null
    private var viewModel: ReminderViewModel? = null

    private var shoppingAdapter: ShopListRecyclerAdapter? = null
    private var mReminder: Reminder? = null
    private val mUiHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getIntExtra(Constants.INTENT_ID, 0)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shopping_preview)
        initActionBar()
        initViews()

        initViewModel(id)
    }

    private fun initViewModel(id: Int) {
        val factory = ReminderViewModel.Factory(application, id)
        viewModel = ViewModelProviders.of(this, factory).get(ReminderViewModel::class.java)
        viewModel!!.reminder.observe(this, { reminder ->
            if (reminder != null) {
                showInfo(reminder)
            }
        })
        viewModel!!.result.observe(this, { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED -> closeWindow()
                }
            }
        })
    }

    private fun showInfo(reminder: Reminder?) {
        this.mReminder = reminder
        if (reminder != null) {
            binding!!.statusSwitch.isChecked = reminder.isActive
            if (!reminder.isActive) {
                binding!!.statusText.setText(R.string.disabled)
            } else {
                binding!!.statusText.setText(R.string.enabled4)
            }
            if (TextUtils.isEmpty(reminder.eventTime)) {
                binding!!.switchWrapper.visibility = View.GONE
            } else {
                binding!!.switchWrapper.visibility = View.VISIBLE
            }
            binding!!.windowTypeView.text = getWindowType(reminder.windowType)
            binding!!.taskText.text = reminder.summary
            binding!!.type.text = ReminderUtils.getTypeString(this, reminder.type)
            binding!!.itemPhoto.setImageResource(themeUtil!!.getReminderIllustration(reminder.type))
            var catColor = 0
            if (reminder.group != null) {
                catColor = reminder.group!!.color
            }
            val mColor = themeUtil!!.getColor(themeUtil!!.getCategoryColor(catColor))
            binding!!.appBar.setBackgroundColor(mColor)
            if (Module.isLollipop) {
                window.statusBarColor = themeUtil!!.getNoteDarkColor(catColor)
            }
            loadData()
        }
    }

    private fun getWindowType(reminderWType: Int): String {
        var windowType = Prefs.getInstance(this).reminderType
        val ignore = Prefs.getInstance(this).isIgnoreWindowType
        if (!ignore) {
            windowType = reminderWType
        }
        return if (windowType == 0) getString(R.string.full_screen) else getString(R.string.simple)
    }

    private fun loadData() {
        if (mReminder != null) {
            shoppingAdapter = ShopListRecyclerAdapter(this, mReminder!!.shoppings,
                    object : ShopListRecyclerAdapter.ActionListener {
                        override fun onItemCheck(position: Int, isChecked: Boolean) {
                            if (shoppingAdapter != null) {
                                val item = shoppingAdapter!!.getItem(position)
                                item.isChecked = !item.isChecked
                                shoppingAdapter!!.updateData()
                                viewModel!!.saveReminder(mReminder!!.setShoppings(shoppingAdapter!!.data))
                            }
                        }

                        override fun onItemDelete(position: Int) {
                            if (shoppingAdapter != null) {
                                shoppingAdapter!!.delete(position)
                                viewModel!!.saveReminder(mReminder!!.setShoppings(shoppingAdapter!!.data))
                            }
                        }
                    })
            binding!!.todoList.layoutManager = LinearLayoutManager(this)
            binding!!.todoList.adapter = shoppingAdapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_reminder_preview, menu)
        menu.getItem(1).isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val ids = item.itemId
        when (ids) {
            R.id.action_delete -> {
                removeReminder()
                return true
            }
            android.R.id.home -> {
                closeWindow()
                return true
            }
            R.id.action_edit -> editReminder()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun editReminder() {
        if (mReminder != null) {
            startActivity(Intent(this, CreateReminderActivity::class.java)
                    .putExtra(Constants.INTENT_ID, mReminder!!.uniqueId))
        }
    }

    private fun removeReminder() {
        if (mReminder != null) {
            viewModel!!.moveToTrash(mReminder!!)
        }
    }

    private fun closeWindow() {
        if (Module.isLollipop) {
            mUiHandler.post { this.finishAfterTransition() }
        } else {
            finish()
        }
    }

    private fun initViews() {
        binding!!.switchWrapper.setOnClickListener { v -> switchClick() }
    }

    private fun switchClick() {
        if (mReminder != null) {
            viewModel!!.toggleReminder(mReminder!!)
        }
    }

    private fun initActionBar() {
        setSupportActionBar(binding!!.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    override fun onBackPressed() {
        closeWindow()
    }
}
