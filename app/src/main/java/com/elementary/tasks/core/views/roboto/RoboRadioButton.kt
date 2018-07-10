package com.elementary.tasks.core.views.roboto

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.AssetsUtil
import com.elementary.tasks.core.utils.MeasureUtils
import com.elementary.tasks.core.utils.Module

import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatRadioButton

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
class RoboRadioButton : AppCompatRadioButton {

    private var mTypeface: Typeface? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (isInEditMode) {
            return
        }
        if (attrs != null) {
            val a = getContext().obtainStyledAttributes(attrs, R.styleable.RoboRadioButton)
            val fontCode = a.getInt(R.styleable.RoboRadioButton_radio_font_style, -1)
            if (fontCode != -1) {
                mTypeface = AssetsUtil.getTypeface(getContext(), fontCode)
            } else {
                mTypeface = AssetsUtil.getDefaultTypeface(getContext())
            }
            var drawableLeft: Drawable? = null
            var drawableRight: Drawable? = null
            var drawableBottom: Drawable? = null
            var drawableTop: Drawable? = null
            if (Module.isLollipop) {
                drawableLeft = a.getDrawable(R.styleable.RoboRadioButton_drawableLeftCompat)
                drawableRight = a.getDrawable(R.styleable.RoboRadioButton_drawableRightCompat)
                drawableBottom = a.getDrawable(R.styleable.RoboRadioButton_drawableBottomCompat)
                drawableTop = a.getDrawable(R.styleable.RoboRadioButton_drawableTopCompat)
            } else {
                val drawableLeftId = a.getResourceId(R.styleable.RoboRadioButton_drawableLeftCompat, -1)
                val drawableRightId = a.getResourceId(R.styleable.RoboRadioButton_drawableRightCompat, -1)
                val drawableBottomId = a.getResourceId(R.styleable.RoboRadioButton_drawableBottomCompat, -1)
                val drawableTopId = a.getResourceId(R.styleable.RoboRadioButton_drawableTopCompat, -1)

                if (drawableLeftId != -1) {
                    drawableLeft = AppCompatResources.getDrawable(context, drawableLeftId)
                }
                if (drawableRightId != -1) {
                    drawableRight = AppCompatResources.getDrawable(context, drawableRightId)
                }
                if (drawableBottomId != -1) {
                    drawableBottom = AppCompatResources.getDrawable(context, drawableBottomId)
                }
                if (drawableTopId != -1) {
                    drawableTop = AppCompatResources.getDrawable(context, drawableTopId)
                }
            }
            compoundDrawablePadding = MeasureUtils.dp2px(context, 8)
            setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight, drawableBottom)
            a.recycle()
        } else {
            mTypeface = AssetsUtil.getDefaultTypeface(getContext())
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mTypeface != null) {
            typeface = mTypeface
        }
    }
}
