package com.elementary.tasks.reminder.lists.adapter

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.databinding.ListItemReminderBinding
import java.util.*

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
class ReminderHolder(parent: ViewGroup, hasHeader: Boolean, editable: Boolean, showMore: Boolean = true,
                     private val listener: ((View, Int, ListActions) -> Unit)? = null) :
        BaseHolder<ListItemReminderBinding>(parent, R.layout.list_item_reminder) {

    val listHeader: TextView = binding.listHeader

    init {
        if (editable) {
            binding.itemCheck.visibility = View.VISIBLE
        } else {
            binding.itemCheck.visibility = View.GONE
        }
        if (!hasHeader) {
            binding.listHeader.visibility = View.GONE
        }
        binding.todoList.visibility = View.GONE
        binding.itemCard.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.OPEN) }
        binding.itemCheck.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.SWITCH) }

        if (showMore) {
            binding.buttonMore.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.MORE) }
            binding.buttonMore.visibility = View.VISIBLE
        } else {
            binding.buttonMore.visibility = View.GONE
        }
    }

    fun setData(reminder: Reminder) {
        binding.taskText.text = reminder.summary
        loadDate(reminder)
        loadCheck(reminder)
        loadContact(reminder)
        loadLeft(reminder)
        loadRepeat(reminder)
        loadContainer(reminder.type)
        loadType(reminder.type)
        loadPriority(reminder.priority)
        loadGroup(reminder)
    }

    private fun loadGroup(reminder: Reminder) {
        val colorStateList = ColorStateList.valueOf(themeUtil.getCategoryColor(reminder.groupColor))
        binding.chipPriority.chipStrokeColor = colorStateList
        binding.chipType.chipStrokeColor = colorStateList
        binding.chipGroup.chipStrokeColor = colorStateList
        binding.chipGroup.text = reminder.groupTitle
    }

    private fun loadPriority(type: Int) {
        binding.chipPriority.text = ReminderUtils.getPriorityTitle(itemView.context, type)
    }

    private fun loadType(type: Int) {
        binding.chipType.text = ReminderUtils.getTypeString(itemView.context, type)
    }

    private fun loadLeft(item: Reminder) {
        if (item.isActive && !item.isRemoved) {
            binding.remainingTime.text = TimeCount.getRemaining(itemView.context, item.eventTime, item.delay, prefs.appLanguage)
        } else {
            binding.remainingTime.text = ""
        }
    }

    private fun loadRepeat(model: Reminder) {
        val context = binding.repeatInterval.context
        when {
            Reminder.isBase(model.type, Reminder.BY_MONTH) -> binding.repeatInterval.text = String.format(binding.repeatInterval.context.getString(R.string.xM), model.repeatInterval.toString())
            Reminder.isBase(model.type, Reminder.BY_WEEK) -> binding.repeatInterval.text = ReminderUtils.getRepeatString(context, prefs, model.weekdays)
            Reminder.isBase(model.type, Reminder.BY_DAY_OF_YEAR) -> binding.repeatInterval.text = binding.repeatInterval.context.getString(R.string.yearly)
            else -> binding.repeatInterval.text = IntervalUtil.getInterval(context, model.repeatInterval)
        }
    }

    private fun loadContainer(type: Int) {
        if (Reminder.isBase(type, Reminder.BY_LOCATION) || Reminder.isBase(type, Reminder.BY_OUT) || Reminder.isBase(type, Reminder.BY_PLACES)) {
            binding.endContainer.visibility = View.GONE
        } else {
            binding.endContainer.visibility = View.VISIBLE
        }
    }

    private fun loadDate(model: Reminder) {
        val is24 = prefs.is24HourFormat
        if (Reminder.isGpsType(model.type)) {
            val place = model.places[0]
            binding.taskDate.text = String.format(Locale.getDefault(), "%.5f %.5f (%d)", place.latitude, place.longitude, model.places.size)
            return
        }
        binding.taskDate.text = TimeUtil.getRealDateTime(model.eventTime, model.delay, is24, prefs.appLanguage)
    }

    private fun loadCheck(item: Reminder?) {
        if (item == null || item.isRemoved) {
            binding.itemCheck.visibility = View.GONE
            return
        }
        binding.itemCheck.isChecked = item.isActive
    }

    private fun loadContact(model: Reminder) {
        val type = model.type
        val number = model.target
        if (Reminder.isBase(type, Reminder.BY_SKYPE)) {
            binding.reminderPhone.visibility = View.VISIBLE
            binding.reminderPhone.text = number
        } else if (Reminder.isKind(type, Reminder.Kind.CALL) || Reminder.isKind(type, Reminder.Kind.SMS)) {
            binding.reminderPhone.visibility = View.VISIBLE
            val name = Contacts.getNameFromNumber(number, binding.reminderPhone.context)
            if (name == null) {
                binding.reminderPhone.text = number
            } else {
                binding.reminderPhone.text = "$name($number)"
            }
        } else if (Reminder.isSame(type, Reminder.BY_DATE_APP)) {
            val packageManager = binding.reminderPhone.context.packageManager
            var applicationInfo: ApplicationInfo? = null
            try {
                applicationInfo = packageManager.getApplicationInfo(number, 0)
            } catch (ignored: PackageManager.NameNotFoundException) {
            }

            val name = (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "???") as String
            binding.reminderPhone.visibility = View.VISIBLE
            binding.reminderPhone.text = "$name/$number"
        } else if (Reminder.isSame(type, Reminder.BY_DATE_EMAIL)) {
            val name = Contacts.getNameFromMail(number, binding.reminderPhone.context)
            binding.reminderPhone.visibility = View.VISIBLE
            if (name == null) {
                binding.reminderPhone.text = number
            } else {
                binding.reminderPhone.text = "$name($number)"
            }
        } else if (Reminder.isSame(type, Reminder.BY_DATE_LINK)) {
            binding.reminderPhone.visibility = View.VISIBLE
            binding.reminderPhone.text = number
        } else {
            binding.reminderPhone.visibility = View.GONE
        }
    }
}
