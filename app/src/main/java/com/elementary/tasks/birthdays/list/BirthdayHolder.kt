package com.elementary.tasks.birthdays.list

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeUtil
import kotlinx.android.synthetic.main.list_item_birthday.view.*
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
        RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_birthday, parent, false)) {

    @Inject
    lateinit var prefs: Prefs

    init {
        ReminderApp.appComponent.inject(this)
        if (showMore) {
            itemView.button_more.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.MORE) }
            itemView.button_more.visibility = View.VISIBLE
        } else {
            itemView.button_more.visibility = View.GONE
        }
        itemView.itemCard.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.OPEN) }
    }

    fun setData(item: Birthday) {
        itemView.eventText.text = item.name
        if (TextUtils.isEmpty(item.number)) {
            itemView.eventNumber.visibility = View.GONE
        } else {
            itemView.eventNumber.visibility = View.VISIBLE
            itemView.eventNumber.text = item.number
        }
        loadBirthday(itemView.eventDate, item.date)
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
