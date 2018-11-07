package com.elementary.tasks.navigation.fragments

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.createEdit.AddBirthdayActivity
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.reminder.createEdit.CreateReminderActivity
import kotlinx.android.synthetic.main.dialog_action_picker.view.*

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
abstract class BaseCalendarFragment : BaseNavigationFragment() {

    protected var dateMills: Long = 0
    private var mDialog: AlertDialog? = null

    protected fun showActionDialog(showEvents: Boolean) {
        val builder = dialogues.getDialog(context!!)
        val binding = LayoutInflater.from(context).inflate(R.layout.dialog_action_picker, null)
        binding.addBirth.setOnClickListener {
            mDialog?.dismiss()
            addBirthday()
        }
        binding.addBirth.setOnLongClickListener {
            showMessage(getString(R.string.add_birthday))
            true
        }
        binding.addEvent.setOnClickListener {
            mDialog?.dismiss()
            addReminder()
        }
        binding.addEvent.setOnLongClickListener {
            showMessage(getString(R.string.add_reminder_menu))
            true
        }
        if (showEvents && dateMills != 0L) {
            binding.loadingView.visibility = View.VISIBLE
            binding.eventsList.layoutManager = LinearLayoutManager(context)
            loadEvents(binding)
        } else {
            binding.loadingView.visibility = View.GONE
        }
        if (dateMills != 0L) {
            binding.dateLabel.text = TimeUtil.getDate(dateMills)
        }
        builder.setView(binding)
        mDialog = builder.create()
        mDialog?.show()
    }

    private fun showMessage(string: String) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }

    private fun loadEvents(binding: View) {

    }

    private fun addReminder() {
        if (isAdded && activity != null) {
            activity?.startActivityForResult(Intent(context, CreateReminderActivity::class.java)
                    .putExtra(Constants.INTENT_DATE, dateMills), REMINDER_CODE)
        }
    }

    private fun addBirthday() {
        if (isAdded && activity != null) {
            activity?.startActivityForResult(Intent(context, AddBirthdayActivity::class.java)
                    .putExtra(Constants.INTENT_DATE, dateMills), BD_CODE)
        }
    }

    companion object {

        const val REMINDER_CODE = 1110
        const val BD_CODE = 1111
    }
}
