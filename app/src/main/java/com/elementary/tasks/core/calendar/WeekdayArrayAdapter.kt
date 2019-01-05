package com.elementary.tasks.core.calendar

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.elementary.tasks.R

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

class WeekdayArrayAdapter(context: Context, textViewResourceId: Int,
                          objects: List<String>, private val isDark: Boolean) : ArrayAdapter<String>(context, textViewResourceId, objects) {

    override fun areAllItemsEnabled(): Boolean {
        return false
    }

    override fun isEnabled(position: Int): Boolean {
        return false
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val textView = super.getView(position, convertView, parent) as TextView
        val item = getItem(position)
        if (item != null) {
            if (item.length <= 2) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            } else {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            }
        }
        val textColor: Int = if (isDark) {
            ContextCompat.getColor(context, R.color.pureWhite)
        } else {
            ContextCompat.getColor(context, R.color.pureBlack)
        }
        textView.setTextColor(textColor)
        textView.gravity = Gravity.CENTER
        return textView
    }
}
