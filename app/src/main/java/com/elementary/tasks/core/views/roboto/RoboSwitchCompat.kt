package com.elementary.tasks.core.views.roboto

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.SwitchCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.AssetsUtil

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
class RoboSwitchCompat : SwitchCompat {

    private var mTypeface: Typeface? = null

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        if (isInEditMode) {
            return
        }
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.RoboSwitchCompat)
            val fontCode = a.getInt(R.styleable.RoboSwitchCompat_switch_font_style, -1)
            mTypeface = if (fontCode != -1) {
                AssetsUtil.getTypeface(context, fontCode)
            } else {
                AssetsUtil.getDefaultTypeface(context)
            }
            a.recycle()
        } else {
            mTypeface = AssetsUtil.getDefaultTypeface(context)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mTypeface != null) {
            typeface = mTypeface
        }
    }
}
