package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
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
            iFace.state.shopItems = mAdapter.data
        }

        override fun onItemDelete(position: Int) {
            mAdapter.delete(position)
            iFace.state.shopItems = mAdapter.data
        }
    }

    override fun prepare(): Reminder? {
        if (mAdapter.itemCount == 0) {
            iFace.showSnackbar(getString(R.string.shopping_list_is_empty))
            return null
        }
        val reminder = iFace.state.reminder
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

    override fun layoutRes(): Int = R.layout.fragment_reminder_shop

    override fun provideViews() {
        setViews(
                scrollView = scrollView,
                expansionLayout = moreLayout,
                ledPickerView = ledView,
                extraView = tuneExtraView,
                melodyView = melodyView,
                attachmentView = attachmentView,
                groupView = groupView,
                summaryView = taskSummary,
                dateTimeView = dateView,
                loudnessPickerView = loudnessView,
                priorityPickerView = priorityView,
                windowTypeView = windowTypeView
        )
    }

    override fun onNewHeader(newHeader: String) {
        cardSummary?.text = newHeader
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            iFace.state.isDelayAdded = isChecked
            if (isChecked) {
                delayLayout.visibility = View.VISIBLE
            } else {
                delayLayout.visibility = View.GONE
            }
        }
        delayLayout.visibility = View.GONE
        attackDelay.isChecked = iFace.state.isDelayAdded

        editReminder()
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
        iFace.state.shopItems = mAdapter.data
    }

    private fun editReminder() {
        val reminder = iFace.state.reminder
        if (iFace.state.isShopItemsEdited) {
            mAdapter.data = reminder.shoppings
            iFace.state.shopItems = reminder.shoppings
            attackDelay.isChecked = reminder.hasReminder && !TextUtils.isEmpty(reminder.eventTime)
        } else {
            mAdapter.data = iFace.state.shopItems
        }
    }
}
