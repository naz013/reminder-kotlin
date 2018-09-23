package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.IdRes
import com.elementary.tasks.R
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.view_window_type.view.*

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
class WindowTypeView : LinearLayout {

    var onTypeChaneListener: ((Int) -> Unit)? = null
    var windowType: Int = 2
        set(value) {
            field = value
            chipGroup.check(chipIdFromType(value))
        }
        get() {
            return typeFromChip(chipGroup.checkedChipId)
        }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    private fun chipIdFromType(id: Int): Int {
        return when (id) {
            0 -> R.id.chipFullscreen
            1 -> R.id.chipSimple
            else -> R.id.chipFullscreen
        }
    }

    private fun typeFromChip(id: Int): Int {
        return when (id) {
            R.id.chipFullscreen -> 0
            R.id.chipSimple -> 1
            else -> 0
        }
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        View.inflate(context, R.layout.view_window_type, this)
        orientation = LinearLayout.VERTICAL

        chipGroup.setOnCheckedChangeListener { _, id ->
            if (isAnyChecked()) {
                updateState(typeFromChip(id))
            } else {
                chipView(id).isChecked = true
                updateState(typeFromChip(id))
            }
        }
    }

    private fun chipView(@IdRes id: Int): Chip {
        return when (id) {
            R.id.chipFullscreen -> chipFullscreen
            R.id.chipSimple -> chipSimple
            else -> chipFullscreen
        }
    }

    private fun isAnyChecked(): Boolean {
        return chipFullscreen.isChecked || chipSimple.isChecked
    }

    private fun updateState(type: Int) {
        onTypeChaneListener?.invoke(type)
    }
}
