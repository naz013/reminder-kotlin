package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.reminder.lists.adapter.ShopListRecyclerAdapter
import kotlinx.android.synthetic.main.fragment_reminder_shop.*
import timber.log.Timber

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
class ShopFragment : RepeatableTypeFragment() {

    private val mAdapter = ShopListRecyclerAdapter()
    private val mActionListener = object : ShopListRecyclerAdapter.ActionListener {
        override fun onItemCheck(position: Int, isChecked: Boolean) {
            val item = mAdapter.getItem(position)
            item.isChecked = !item.isChecked
            mAdapter.updateData()
        }

        override fun onItemDelete(position: Int) {
            mAdapter.delete(position)
        }
    }

    override fun prepare(): Reminder? {
        if (mAdapter.itemCount == 0) {
            reminderInterface.showSnackbar(getString(R.string.shopping_list_is_empty))
            return null
        }
        val reminder = reminderInterface.reminder
        reminder.shoppings = mAdapter.data
        reminder.target = ""
        reminder.type = Reminder.BY_DATE_SHOP
        reminder.repeatInterval = 0
        reminder.exportToCalendar = false
        reminder.exportToTasks = false
        reminder.hasReminder = attackDelay.isChecked
        if (attackDelay.isChecked) {
            val startTime = dateView.dateTime
            val time = TimeUtil.getGmtFromDateTime(startTime)
            Timber.d("EVENT_TIME %s", TimeUtil.getFullDateTime(startTime, true))
            if (!TimeCount.isCurrent(time)) {
                Toast.makeText(context, R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show()
                return null
            }
            reminder.startTime = time
            reminder.eventTime = time
        } else {
            reminder.eventTime = ""
            reminder.startTime = ""
        }
        return reminder
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reminder_shop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(scrollView) {
            reminderInterface.updateScroll(it)
        }
        moreLayout?.isNestedScrollingEnabled = false

        if (Module.isPro) {
            ledView.visibility = View.VISIBLE
        } else {
            ledView.visibility = View.GONE
        }

        tuneExtraView.dialogues = dialogues
        tuneExtraView.hasAutoExtra = false

        todoList.layoutManager = LinearLayoutManager(context)
        mAdapter.listener = mActionListener
        todoList.adapter = mAdapter
        shopEdit.setOnEditorActionListener { _, actionId, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_NEXT) {
                addNewItem()
                return@setOnEditorActionListener true
            }
            false
        }
        addButton.setOnClickListener { addNewItem() }
        attackDelay.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                delayLayout.visibility = View.VISIBLE
            else
                delayLayout.visibility = View.GONE
        }

        delayLayout.visibility = View.GONE

        melodyView.onFileSelectListener = {
            reminderInterface.selectMelody()
        }
        attachmentView.onFileSelectListener = {
            reminderInterface.attachFile()
        }
        groupView.onGroupSelectListener = {
            reminderInterface.selectGroup()
        }

        initPropertyFields()
        editReminder()
    }

    private fun initPropertyFields() {
        taskSummary.bindProperty(reminderInterface.reminder.summary) {
            reminderInterface.reminder.summary = it.trim()
        }
        dateView.bindProperty(reminderInterface.reminder.eventTime) {
            reminderInterface.reminder.eventTime = it
        }
        priorityView.bindProperty(reminderInterface.reminder.priority) {
            reminderInterface.reminder.priority = it
            updateHeader()
        }
        melodyView.bindProperty(reminderInterface.reminder.melodyPath) {
            reminderInterface.reminder.melodyPath = it
        }
        attachmentView.bindProperty(reminderInterface.reminder.attachmentFile) {
            reminderInterface.reminder.attachmentFile = it
        }
        loudnessView.bindProperty(reminderInterface.reminder.volume) {
            reminderInterface.reminder.volume = it
        }
        windowTypeView.bindProperty(reminderInterface.reminder.windowType) {
            reminderInterface.reminder.windowType = it
        }
        tuneExtraView.bindProperty(reminderInterface.reminder) {
            reminderInterface.reminder.copyExtra(it)
        }
        if (Module.isPro) {
            ledView.bindProperty(reminderInterface.reminder.color) {
                reminderInterface.reminder.color = it
            }
        }
    }

    private fun updateHeader() {
        cardSummary?.text = getSummary()
    }

    private fun addNewItem() {
        val task = shopEdit.text.toString().trim()
        if (task == "") {
            shopLayout.error = getString(R.string.must_be_not_empty)
            shopLayout.isErrorEnabled = true
            return
        }
        mAdapter.addItem(ShopItem(task.replace("\n".toRegex(), " ")))
        shopEdit.setText("")
    }

    private fun editReminder() {
        val reminder = reminderInterface.reminder
        showGroup(groupView, reminder)
        mAdapter.data = reminder.shoppings
        attackDelay.isChecked = reminder.hasReminder && !TextUtils.isEmpty(reminder.eventTime)
        updateHeader()
    }

    override fun onGroupUpdate(reminderGroup: ReminderGroup) {
        super.onGroupUpdate(reminderGroup)
        groupView?.reminderGroup = reminderGroup
        updateHeader()
    }

    override fun onMelodySelect(path: String) {
        super.onMelodySelect(path)
        melodyView.file = path
    }

    override fun onAttachmentSelect(path: String) {
        super.onAttachmentSelect(path)
        attachmentView.file = path
    }
}
