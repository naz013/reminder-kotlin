package com.elementary.tasks.navigation.fragments

import android.content.Intent
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdayResolver
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.databinding.DialogActionPickerBinding
import com.elementary.tasks.day_view.DayViewFragment
import com.elementary.tasks.day_view.day.CalendarEventsAdapter
import com.elementary.tasks.day_view.day.EventModel
import com.elementary.tasks.reminder.ReminderResolver
import com.elementary.tasks.reminder.create.CreateReminderActivity
import kotlinx.coroutines.Job
import timber.log.Timber
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
abstract class BaseCalendarFragment<B : ViewDataBinding> : BaseNavigationFragment<B>() {

    protected var dateMills: Long = 0
    private var mDialog: AlertDialog? = null
    private var job: Job? = null
    private val birthdayResolver = BirthdayResolver(
            dialogAction = { dialogues },
            deleteAction = { }
    )
    private val reminderResolver = ReminderResolver(
            dialogAction = { dialogues },
            saveAction = { },
            toggleAction = { },
            deleteAction = { },
            allGroups = { listOf() }
    )

    protected fun showActionDialog(showEvents: Boolean, list: List<EventModel> = listOf()) {
        withContext {
            val builder = dialogues.getMaterialDialog(it)
            val binding = DialogActionPickerBinding.inflate(LayoutInflater.from(it))
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
            if (showEvents && list.isNotEmpty()) {
                binding.loadingView.visibility = View.VISIBLE
                binding.eventsList.layoutManager = LinearLayoutManager(it)
                loadEvents(binding, list)
            } else {
                binding.loadingView.visibility = View.GONE
            }
            if (dateMills != 0L) {
                val monthTitle = DateUtils.formatDateTime(activity, dateMills, DayViewFragment.MONTH_YEAR_FLAG).toString()
                binding.dateLabel.text = monthTitle
            }
            builder.setView(binding.root)
            builder.setOnDismissListener {
                job?.cancel()
            }
            mDialog = builder.create()
            mDialog?.show()
        }
    }

    private fun showMessage(string: String) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }

    private fun loadEvents(binding: DialogActionPickerBinding, list: List<EventModel>) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateMills
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val bTime = TimeUtil.getBirthdayTime(prefs.birthdayTime)

        this.job?.cancel()
        this.job = launchDefault {
            val res = ArrayList<EventModel>()
            for (item in list) {
                val mDay = item.day
                val mMonth = item.month
                val mYear = item.year
                val type = item.viewType
                if (type == EventModel.BIRTHDAY && mDay == day && mMonth == month) {
                    res.add(item)
                } else {
                    if (mDay == day && mMonth == month && mYear == year) {
                        res.add(item)
                    }
                }
            }
            Timber.d("Search events: found -> %d", res.size)
            val sorted = try {
                res.asSequence().sortedBy { it.getMillis(bTime) }.toList()
            } catch (e: IllegalArgumentException) {
                res
            }
            withUIContext { showList(binding, sorted) }
        }
    }

    private fun showList(binding: DialogActionPickerBinding, res: List<EventModel>) {
        val adapter = CalendarEventsAdapter()
        adapter.setEventListener(object : ActionsListener<EventModel> {
            override fun onAction(view: View, position: Int, t: EventModel?, actions: ListActions) {
                if (t != null) {
                    val model = t.model
                    if (model is Birthday) {
                        birthdayResolver.resolveAction(view, model, actions)
                    } else if (model is Reminder) {
                        reminderResolver.resolveAction(view, model, actions)
                    }
                }
            }
        })
        adapter.showMore = false
        adapter.setData(res)
        binding.eventsList.adapter = adapter
        binding.eventsList.visibility = View.VISIBLE
        binding.loadingView.visibility = View.GONE
    }

    private fun addReminder() {
        if (isAdded) {
            withActivity {
                CreateReminderActivity.openLogged(it, Intent(it, CreateReminderActivity::class.java)
                        .putExtra(Constants.INTENT_DATE, dateMills))
            }
        }
    }

    private fun addBirthday() {
        if (isAdded) {
            withActivity {
                AddBirthdayActivity.openLogged(it, Intent(it, AddBirthdayActivity::class.java)
                        .putExtra(Constants.INTENT_DATE, dateMills))
            }
        }
    }
}
