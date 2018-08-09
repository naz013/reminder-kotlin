package com.elementary.tasks.reminder.preview

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.reminders.ReminderViewModel
import com.elementary.tasks.reminder.createEdit.CreateReminderActivity
import com.elementary.tasks.reminder.lists.ShopListRecyclerAdapter
import kotlinx.android.synthetic.main.activity_shopping_preview.*
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
class ShoppingPreviewActivity : ThemedActivity() {

    private lateinit var viewModel: ReminderViewModel

    private var shoppingAdapter = ShopListRecyclerAdapter()
    private var mReminder: Reminder? = null
    private val mUiHandler = Handler(Looper.getMainLooper())

    @Inject
    lateinit var reminderUtils: ReminderUtils

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""
        setContentView(R.layout.activity_shopping_preview)
        initActionBar()
        initViews()
        initViewModel(id)
    }

    private fun initViewModel(id: String) {
        val factory = ReminderViewModel.Factory(application, id)
        viewModel = ViewModelProviders.of(this, factory).get(ReminderViewModel::class.java)
        viewModel.reminder.observe(this, Observer{ reminder ->
            if (reminder != null) {
                showInfo(reminder)
            }
        })
        viewModel.result.observe(this, Observer{ commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED -> closeWindow()
                }
            }
        })
    }

    private fun showInfo(reminder: Reminder) {
        this.mReminder = reminder
        statusSwitch.isChecked = reminder.isActive
        if (!reminder.isActive) {
            statusText.setText(R.string.disabled)
        } else {
            statusText.setText(R.string.enabled4)
        }
        if (TextUtils.isEmpty(reminder.eventTime)) {
            switchWrapper.visibility = View.GONE
        } else {
            switchWrapper.visibility = View.VISIBLE
        }
        window_type_view.text = getWindowType(reminder.windowType)
        taskText.text = reminder.summary
        type.text = reminderUtils.getTypeString(reminder.type)
        itemPhoto.setImageResource(themeUtil.getReminderIllustration(reminder.type))
        var catColor = 0
        if (reminder.group != null) {
            catColor = reminder.group!!.color
        }
        val mColor = themeUtil.getColor(themeUtil.getCategoryColor(catColor))
        appBar.setBackgroundColor(mColor)
        if (Module.isLollipop) {
            window.statusBarColor = themeUtil.getNoteDarkColor(catColor)
        }
        loadData()
    }

    private fun getWindowType(reminderWType: Int): String {
        var windowType = prefs.reminderType
        val ignore = prefs.isIgnoreWindowType
        if (!ignore) {
            windowType = reminderWType
        }
        return if (windowType == 0) getString(R.string.full_screen) else getString(R.string.simple)
    }

    private fun loadData() {
        val reminder = mReminder ?: return
        shoppingAdapter.listener = object : ShopListRecyclerAdapter.ActionListener {
            override fun onItemCheck(position: Int, isChecked: Boolean) {
                val item = shoppingAdapter.getItem(position)
                item.isChecked = !item.isChecked
                shoppingAdapter.updateData()
                reminder.shoppings = shoppingAdapter.data
                viewModel.saveReminder(reminder)
            }

            override fun onItemDelete(position: Int) {
                shoppingAdapter.delete(position)
                reminder.shoppings = shoppingAdapter.data
                viewModel.saveReminder(reminder)
            }
        }
        shoppingAdapter.data = reminder.shoppings
        todoList.layoutManager = LinearLayoutManager(this)
        todoList.adapter = shoppingAdapter
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
            viewModel.moveToTrash(mReminder!!)
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
        switchWrapper.setOnClickListener { switchClick() }
    }

    private fun switchClick() {
        if (mReminder != null) {
            viewModel.toggleReminder(mReminder!!)
        }
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onBackPressed() {
        closeWindow()
    }
}
