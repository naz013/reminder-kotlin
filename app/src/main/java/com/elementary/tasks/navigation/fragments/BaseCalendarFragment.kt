package com.elementary.tasks.navigation.fragments

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.CalendarEventsAdapter
import com.elementary.tasks.birthdays.DayViewProvider
import com.elementary.tasks.birthdays.EventsDataSingleton
import com.elementary.tasks.birthdays.EventsItem
import com.elementary.tasks.birthdays.createEdit.AddBirthdayActivity
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.reminder.createEdit.AddReminderActivity
import kotlinx.android.synthetic.main.dialog_action_picker.view.*
import java.util.*

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
        val builder = Dialogues.getDialog(context!!)
        val binding = LayoutInflater.from(context).inflate(R.layout.dialog_action_picker, null)
        binding.addBirth.setOnClickListener {
            mDialog!!.dismiss()
            addBirthday()
        }
        binding.addBirth.setOnLongClickListener {
            showMessage(getString(R.string.add_birthday))
            true
        }
        binding.addEvent.setOnClickListener {
            mDialog!!.dismiss()
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
        mDialog!!.show()
    }

    private fun showMessage(string: String) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }

    private fun loadEvents(binding: View) {
        val provider = EventsDataSingleton.getInstance().provider
        if (provider != null && provider.isReady) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = dateMills
            val mDay = calendar.get(Calendar.DAY_OF_MONTH)
            val mMonth = calendar.get(Calendar.MONTH)
            val mYear = calendar.get(Calendar.YEAR)
            provider.findMatches(mDay, mMonth, mYear, true, object : DayViewProvider.Callback {
                override fun apply(list: List<EventsItem>) {
                    if (context != null) {
                        val mAdapter = CalendarEventsAdapter()
                        mAdapter.setData(list)
                        binding.eventsList.adapter = mAdapter
                        binding.eventsList.visibility = View.VISIBLE
                        binding.loadingView.visibility = View.GONE
                    }
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        initProvider()
    }

    protected fun initProvider() {
        val time = prefs.birthdayTime
        val isFeature = prefs.isFutureEventEnabled
        val isRemindersEnabled = prefs.isRemindersInCalendarEnabled
        var provider = EventsDataSingleton.getInstance().provider
        if (provider == null) {
            provider = DayViewProvider(context!!)
            EventsDataSingleton.getInstance().provider = provider
        }
        if (!provider.isInProgress) {
            provider.setBirthdays(true)
            provider.setTime(TimeUtil.getBirthdayTime(time))
            provider.setReminders(isRemindersEnabled)
            provider.setFeature(isFeature)
            provider.fillArray()
        }
    }

    private fun addReminder() {
        activity!!.startActivityForResult(Intent(context, AddReminderActivity::class.java)
                .putExtra(Constants.INTENT_DATE, dateMills), REMINDER_CODE)
    }

    private fun addBirthday() {
        activity!!.startActivityForResult(Intent(context, AddBirthdayActivity::class.java)
                .putExtra(Constants.INTENT_DATE, dateMills), BD_CODE)
    }

    companion object {

        const val REMINDER_CODE = 1110
        const val BD_CODE = 1111
    }
}
