package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import com.elementary.tasks.R
import kotlinx.android.synthetic.main.view_loudness.view.*
import java.util.*

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
class RadiusPickerView : LinearLayout {

    var onLevelUpdateListener: ((level: Int) -> Unit)? = null
    var radius: Int = 0
        get() {
            return field - 1
        }
        private set(value) {
            field = value
            if (value > 0) {
                labelView.text = String.format(Locale.getDefault(), context.getString(R.string.radius_x_meters), (value - 1).toString())
            } else {
                labelView.text = context.getString(R.string.default_string)
            }
        }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    fun setRadiusValue(level: Int) {
        sliderView.progress = level + 1
        this.radius = sliderView.progress
    }

    private fun init(context: Context) {
        View.inflate(context, R.layout.view_radius_picker, this)
        orientation = LinearLayout.HORIZONTAL

        hintIcon.setOnLongClickListener {
            Toast.makeText(context, context.getString(R.string.radius), Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }
        sliderView.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                radius = progress
                onLevelUpdateListener?.invoke(radius)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }
}
