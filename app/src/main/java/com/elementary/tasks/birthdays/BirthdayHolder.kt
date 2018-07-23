package com.elementary.tasks.birthdays

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Birthday
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.views.roboto.RoboTextView
import com.mcxiaoke.koi.ext.onClick
import com.mcxiaoke.koi.ext.onLongClick
import kotlinx.android.synthetic.main.list_item_events.view.*
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
class BirthdayHolder(parent: ViewGroup, private val listener: ((View, Int, ListActions) -> Unit)?) :
        RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_events, parent, false)) {

    @Inject lateinit var prefs: Prefs

    init {
        ReminderApp.appComponent.inject(this)
        if (Module.isLollipop) {
            itemView.itemCard.cardElevation = Configs.CARD_ELEVATION
        }
        itemView.itemCard.onLongClick { view ->
            listener?.invoke(view, adapterPosition, ListActions.MORE)
            true
        }
        itemView.itemCard.onClick { listener?.invoke(it, adapterPosition, ListActions.OPEN) }
    }

    fun setColor(color: Int) {
        itemView.eventColor.setBackgroundColor(color)
    }

    fun setData(item: Birthday) {
        itemView.eventText.text = item.name
        itemView.eventNumber.text = item.number
        loadBirthday(itemView.eventDate, item.date)
    }

    private fun loadBirthday(textView: RoboTextView, fullDate: String) {
        val is24 = prefs.is24HourFormatEnabled
        val dateItem = TimeUtil.getFutureBirthdayDate(prefs, fullDate)
        if (dateItem != null) {
            textView.text = SuperUtil.appendString(TimeUtil.getFullDateTime(dateItem.calendar.timeInMillis, is24, false),
                    "\n", TimeUtil.getAgeFormatted(textView.context, dateItem.year))
        }
    }
}
