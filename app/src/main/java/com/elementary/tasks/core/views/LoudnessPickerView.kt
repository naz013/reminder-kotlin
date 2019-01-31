package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.views.LoudnessViewBinding

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
class LoudnessPickerView : LinearLayout {

    private lateinit var binding: LoudnessViewBinding
    var onLevelUpdateListener: ((level: Int) -> Unit)? = null
    var level: Int = 0
        get() {
            return field - 1
        }
        private set(value) {
            field = value
            if (value > 0) {
                binding.labelView.text = "${value - 1}"
            } else {
                binding.labelView.text = context.getString(R.string.default_string)
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

    fun setVolume(level: Int) {
        binding.sliderView.progress = level + 1
        this.level = binding.sliderView.progress
    }

    private fun init(context: Context) {
        View.inflate(context, R.layout.view_loudness, this)
        orientation = LinearLayout.HORIZONTAL
        binding = LoudnessViewBinding(this)

        binding.hintIcon.setOnLongClickListener {
            Toast.makeText(context, context.getString(R.string.loudness), Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }
        TooltipCompat.setTooltipText(binding.hintIcon, context.getString(R.string.loudness))
        binding.sliderView.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
