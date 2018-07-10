package com.elementary.tasks.reminder.create_edit.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.databinding.FragmentReminderShopBinding
import com.elementary.tasks.reminder.lists.ShopListRecyclerAdapter
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ShopItem

import java.util.ArrayList

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
class ShopFragment : TypeFragment() {

    private var binding: FragmentReminderShopBinding? = null
    private var mAdapter: ShopListRecyclerAdapter? = null
    private var isReminder = false
    private var mSelectedPosition: Int = 0
    private val mActionListener = object : ShopListRecyclerAdapter.ActionListener {
        override fun onItemCheck(position: Int, isChecked: Boolean) {
            val item = mAdapter!!.getItem(position)
            item.isChecked = !item.isChecked
            mAdapter!!.updateData()
        }

        override fun onItemDelete(position: Int) {
            mAdapter!!.delete(position)
        }
    }

    override fun prepare(): Reminder? {
        if (`interface` == null) return null
        if (mAdapter!!.itemCount == 0) {
            `interface`!!.showSnackbar(getString(R.string.shopping_list_is_empty))
            return null
        }
        var reminder: Reminder? = `interface`!!.reminder
        val type = Reminder.BY_DATE_SHOP
        if (reminder == null) {
            reminder = Reminder()
        }
        reminder.shoppings = mAdapter!!.data
        reminder.target = null
        reminder.type = type
        reminder.repeatInterval = 0
        reminder.setClear(`interface`)
        if (isReminder) {
            val startTime = binding!!.dateViewShopping.dateTime
            val time = TimeUtil.getGmtFromDateTime(startTime)
            LogUtil.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true, true))
            if (!TimeCount.isCurrent(time)) {
                Toast.makeText(context, R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show()
                return null
            }
            reminder.startTime = time
            reminder.eventTime = time
        } else {
            reminder.eventTime = null
            reminder.startTime = null
        }
        return reminder
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentReminderShopBinding.inflate(inflater, container, false)
        binding!!.dateViewShopping.setOnLongClickListener { view ->
            selectDateDialog()
            true
        }
        binding!!.todoList.layoutManager = LinearLayoutManager(context)
        mAdapter = ShopListRecyclerAdapter(context, ArrayList(), mActionListener)
        binding!!.todoList.adapter = mAdapter
        binding!!.shopEdit.setOnEditorActionListener { textView, actionId, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_NEXT) {
                addNewItem()
                return@binding.shopEdit.setOnEditorActionListener true
            }
            false
        }
        binding!!.addButton.setOnClickListener { v -> addNewItem() }
        switchDate()
        editReminder()
        return binding!!.root
    }

    private fun addNewItem() {
        val task = binding!!.shopEdit.text!!.toString().trim { it <= ' ' }
        if (task.matches("".toRegex())) {
            binding!!.shopEdit.error = getString(R.string.must_be_not_empty)
            return
        }
        mAdapter!!.addItem(ShopItem(task.replace("\n".toRegex(), " ")))
        binding!!.shopEdit.setText("")
    }

    private fun editReminder() {
        if (`interface`!!.reminder == null) return
        val reminder = `interface`!!.reminder
        binding!!.dateViewShopping.setDateTime(reminder.eventTime)
        mAdapter!!.data = reminder.shoppings
        if (!TextUtils.isEmpty(reminder.eventTime)) {
            isReminder = true
            binding!!.dateViewShopping.setDateTime(reminder.eventTime)
        } else {
            isReminder = false
        }
        switchDate()
    }

    private fun selectDateDialog() {
        val builder = Dialogues.getDialog(context!!)
        val types = arrayOf(getString(R.string.no_reminder), getString(R.string.select_time))
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_single_choice, types)
        var selection = 0
        if (isReminder) selection = 1
        builder.setSingleChoiceItems(adapter, selection) { dialog, which -> mSelectedPosition = which }
        builder.setPositiveButton(R.string.ok) { dialogInterface, i ->
            makeAction()
            dialogInterface.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { dialogInterface -> mSelectedPosition = 0 }
        dialog.setOnDismissListener { dialogInterface -> mSelectedPosition = 0 }
        dialog.show()
    }

    private fun makeAction() {
        when (mSelectedPosition) {
            0 -> isReminder = false
            1 -> isReminder = true
        }
        switchDate()
    }

    private fun switchDate() {
        if (isReminder) {
            binding!!.dateViewShopping.setSingleText(null)
        } else {
            binding!!.dateViewShopping.setSingleText(getString(R.string.no_reminder))
        }
        binding!!.dateViewShopping.setOnClickListener { v -> if (!isReminder) selectDateDialog() }
    }

    companion object {

        private val TAG = "ShopFragment"
    }
}
