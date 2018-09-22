package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.LED
import kotlinx.android.synthetic.main.view_led_color.view.*

/**
 * Copyright 2018 Nazar Suhovich
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
class LedPickerView : LinearLayout {

    var onLedChangeListener: ((Int) -> Unit)? = null
    var led: Int = LED.BLUE
        set(value) {
            field = value
            ledGroup.check(chipIdFromLed(value))
        }
        get() {
            return ledFromChip(ledGroup.checkedRadioButtonId)
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

    private fun chipIdFromLed(id: Int): Int {
        return when (id) {
            0 -> R.id.ledRed
            1 -> R.id.ledGreen
            2 -> R.id.ledBlue
            3 -> R.id.ledYellow
            4 -> R.id.ledPink
            5 -> R.id.ledOrange
            6 -> R.id.ledTeal
            else -> R.id.ledBlue
        }
    }

    private fun ledFromChip(id: Int): Int {
        return when (id) {
            R.id.ledRed -> 0
            R.id.ledGreen -> 1
            R.id.ledBlue -> 2
            R.id.ledYellow -> 3
            R.id.ledPink -> 4
            R.id.ledOrange -> 5
            R.id.ledTeal -> 6
            else -> 2
        }
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        View.inflate(context, R.layout.view_led_color, this)
        orientation = LinearLayout.VERTICAL

        ledGroup.setOnCheckedChangeListener { _, checkedId ->
            updateState(ledFromChip(checkedId))
        }
    }

    private fun updateState(led: Int) {
        onLedChangeListener?.invoke(led)
    }
}
