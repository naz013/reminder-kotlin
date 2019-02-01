package com.elementary.tasks.birthdays.list

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.databinding.ListItemBirthdayBinding
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
class BirthdayHolder(parent: ViewGroup, showMore: Boolean = true, private val listener: ((View, Int, ListActions) -> Unit)? = null) :
        HolderBinding<ListItemBirthdayBinding>(parent, R.layout.list_item_birthday) {

    @Inject
    lateinit var prefs: Prefs

    init {
        ReminderApp.appComponent.inject(this)
        if (showMore) {
            binding.buttonMore.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.MORE) }
            binding.buttonMore.visibility = View.VISIBLE
        } else {
            binding.buttonMore.visibility = View.GONE
        }
        binding.itemCard.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.OPEN) }
    }

    fun setData(item: Birthday) {
        binding.eventText.text = item.name
        if (TextUtils.isEmpty(item.number)) {
            binding.eventNumber.visibility = View.GONE
        } else {
            binding.eventNumber.visibility = View.VISIBLE
            binding.eventNumber.text = item.number
        }
        loadBirthday(binding.eventDate, item.date)
    }

    private fun loadBirthday(textView: TextView, fullDate: String) {
        val is24 = prefs.is24HourFormat
        val dateItem = TimeUtil.getFutureBirthdayDate(TimeUtil.getBirthdayTime(prefs.birthdayTime), fullDate)
        if (dateItem != null) {
            textView.text = SuperUtil.appendString(TimeUtil.getFullDateTime(dateItem.calendar.timeInMillis, is24, prefs.appLanguage),
                    "\n", TimeUtil.getAgeFormatted(textView.context, dateItem.year, prefs.appLanguage))
        }
    }
}
