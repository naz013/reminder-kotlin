package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.IdRes
import com.elementary.tasks.R
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.view_priority.view.*

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
class PriorityPickerView : LinearLayout {

    var onPriorityChaneListener: ((Int) -> Unit)? = null
    var priority: Int = 2
        set(value) {
            field = value
            chipGroup.check(chipIdFromPriority(value))
        }
        get() {
            return priorityFromChip(chipGroup.checkedChipId)
        }
    private var mLastIdRes: Int = R.id.chipNormal

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    private fun chipIdFromPriority(id: Int): Int {
        return when (id) {
            0 -> R.id.chipLowest
            1 -> R.id.chipLow
            2 -> R.id.chipNormal
            3 -> R.id.chipHigh
            4 -> R.id.chipHighest
            else -> R.id.chipNormal
        }
    }

    private fun priorityFromChip(id: Int): Int {
        mLastIdRes = id
        return when (id) {
            R.id.chipLowest -> 0
            R.id.chipLow -> 1
            R.id.chipNormal -> 2
            R.id.chipHigh -> 3
            R.id.chipHighest -> 4
            else -> 2
        }
    }

    private fun init(context: Context) {
        View.inflate(context, R.layout.view_priority, this)
        orientation = LinearLayout.VERTICAL

        hintIcon.setOnLongClickListener {
            Toast.makeText(context, context.getString(R.string.priority), Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }
        chipGroup.setOnCheckedChangeListener { _, id ->
            if (isAnyChecked()) {
                updateState(priorityFromChip(id))
            } else {
                chipView(mLastIdRes).isChecked = true
                updateState(priorityFromChip(mLastIdRes))
            }
        }
    }

    private fun chipView(@IdRes id: Int): Chip {
        return when (id) {
            R.id.chipLowest -> chipLowest
            R.id.chipLow -> chipLow
            R.id.chipNormal -> chipNormal
            R.id.chipHigh -> chipHigh
            R.id.chipHighest -> chipHighest
            else -> chipNormal
        }
    }

    private fun isAnyChecked(): Boolean {
        return chipLowest.isChecked || chipLow.isChecked || chipNormal.isChecked
                || chipHigh.isChecked || chipHighest.isChecked
    }

    private fun updateState(priority: Int) {
        onPriorityChaneListener?.invoke(priority)
    }
}
