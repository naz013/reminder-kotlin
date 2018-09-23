package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import com.elementary.tasks.R
import kotlinx.android.synthetic.main.view_repeat_limit.view.*

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
class RepeatLimitView : LinearLayout {

    var onLevelUpdateListener: ((level: Int) -> Unit)? = null
    var level: Int = 0
        get() {
            return field - 1
        }
        private set(value) {
            field = value
            if (value > 0) {
                labelView.text = "${value - 1}"
            } else {
                labelView.text = context.getString(R.string.no_limits)
            }
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

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        sliderView.isEnabled = enabled
    }

    fun setLimit(level: Int) {
        sliderView.progress = level + 1
        this.level = sliderView.progress
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        View.inflate(context, R.layout.view_repeat_limit, this)
        orientation = LinearLayout.HORIZONTAL

        sliderView.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                level = progress
                onLevelUpdateListener?.invoke(level)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }
}
