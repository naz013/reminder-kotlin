package com.elementary.tasks.reminder.lists.adapter

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.*
import kotlinx.android.synthetic.main.list_item_reminder.view.*
import java.util.*
import javax.inject.Inject

/**
 * Copyright 2017 Nazar Suhovich
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
class ReminderHolder(parent: ViewGroup, private val listener: ((View, Int, ListActions) -> Unit)?, val editable: Boolean) :
        BaseHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_reminder, parent, false)) {

    @Inject
    lateinit var timeCount: TimeCount
    @Inject
    lateinit var reminderUtils: ReminderUtils

    val listHeader: TextView = itemView.listHeader

    init {
        ReminderApp.appComponent.inject(this)
        if (editable) {
            itemView.itemCheck.visibility = View.VISIBLE
        } else {
            itemView.itemCheck.visibility = View.GONE
        }
        itemView.itemCard.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.OPEN) }
        itemView.button_more.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.MORE) }
        itemView.itemCheck.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.SWITCH) }
    }

    fun setData(reminder: Reminder) {
        itemView.taskText.text = reminder.summary
        loadCard(reminder.reminderGroup)
        loadDate(reminder)
        loadCheck(reminder)
        loadContact(reminder)
        loadLeft(reminder)
        loadRepeat(reminder)
        loadContainer(reminder.type)
        loadType(reminder.type)
    }

    private fun loadType(type: Int) {
        itemView.chipType.text = reminderUtils.getTypeString(type)
    }

    private fun loadLeft(item: Reminder) {
        if (item.isActive && !item.isRemoved) {
            itemView.remainingTime.text = timeCount.getRemaining(item.eventTime, item.delay)
        } else {
            itemView.remainingTime.text = ""
        }
    }

    private fun loadRepeat(model: Reminder) {
        when {
            Reminder.isBase(model.type, Reminder.BY_MONTH) -> itemView.repeatInterval.text = String.format(itemView.repeatInterval.context.getString(R.string.xM), 1.toString())
            Reminder.isBase(model.type, Reminder.BY_WEEK) -> itemView.repeatInterval.text = reminderUtils.getRepeatString(model.weekdays)
            Reminder.isBase(model.type, Reminder.BY_DAY_OF_YEAR) -> itemView.repeatInterval.text = itemView.repeatInterval.context.getString(R.string.yearly)
            else -> itemView.repeatInterval.text = IntervalUtil.getInterval(itemView.repeatInterval.context, model.repeatInterval)
        }
    }

    private fun loadContainer(type: Int) {
        if (Reminder.isBase(type, Reminder.BY_LOCATION) || Reminder.isBase(type, Reminder.BY_OUT) || Reminder.isBase(type, Reminder.BY_PLACES)) {
            itemView.endContainer.visibility = View.GONE
        } else {
            itemView.endContainer.visibility = View.VISIBLE
        }
    }

    private fun loadCard(reminderGroup: ReminderGroup?) {

    }

    private fun loadDate(model: Reminder) {
        val is24 = prefs.is24HourFormatEnabled
        if (Reminder.isGpsType(model.type)) {
            val place = model.places[0]
            itemView.taskDate.text = String.format(Locale.getDefault(), "%.5f %.5f (%d)", place.latitude, place.longitude, model.places.size)
            return
        }
        itemView.taskDate.text = TimeUtil.getRealDateTime(model.eventTime, model.delay, is24)
    }

    private fun loadCheck(item: Reminder?) {
        if (item == null || item.isRemoved) {
            itemView.itemCheck.visibility = View.GONE
            return
        }
        itemView.itemCheck.isChecked = item.isActive
    }

    private fun loadContact(model: Reminder) {
        val type = model.type
        val number = model.target
        if (Reminder.isBase(type, Reminder.BY_SKYPE)) {
            itemView.reminder_phone.visibility = View.VISIBLE
            itemView.reminder_phone.text = number
        } else if (Reminder.isKind(type, Reminder.Kind.CALL) || Reminder.isKind(type, Reminder.Kind.SMS)) {
            itemView.reminder_phone.visibility = View.VISIBLE
            val name = Contacts.getNameFromNumber(number, itemView.reminder_phone.context)
            if (name == null) {
                itemView.reminder_phone.text = number
            } else {
                itemView.reminder_phone.text = "$name($number)"
            }
        } else if (Reminder.isSame(type, Reminder.BY_DATE_APP)) {
            val packageManager = itemView.reminder_phone.context.packageManager
            var applicationInfo: ApplicationInfo? = null
            try {
                applicationInfo = packageManager.getApplicationInfo(number, 0)
            } catch (ignored: PackageManager.NameNotFoundException) {
            }

            val name = (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "???") as String
            itemView.reminder_phone.visibility = View.VISIBLE
            itemView.reminder_phone.text = "$name/$number"
        } else if (Reminder.isSame(type, Reminder.BY_DATE_EMAIL)) {
            val name = Contacts.getNameFromMail(number, itemView.reminder_phone.context)
            itemView.reminder_phone.visibility = View.VISIBLE
            if (name == null) {
                itemView.reminder_phone.text = number
            } else {
                itemView.reminder_phone.text = "$name($number)"
            }
        } else if (Reminder.isSame(type, Reminder.BY_DATE_LINK)) {
            itemView.reminder_phone.visibility = View.VISIBLE
            itemView.reminder_phone.text = number
        } else {
            itemView.reminder_phone.visibility = View.GONE
        }
    }
}
