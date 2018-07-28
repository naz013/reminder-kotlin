package com.elementary.tasks.core.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.MeasureUtils
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.views.roboto.RoboTextView
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

class TextViewWithIcon : RoboTextView {

    @Inject
    lateinit var themeUtil: ThemeUtil

    init {
        ReminderApp.appComponent.inject(this)
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    fun setIcon(@DrawableRes icon: Int) {
        if (icon == 0) {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            return
        }
        val drawableLeft: Drawable? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.getDrawable(icon)
        } else {
            AppCompatResources.getDrawable(context, icon)
        }
        compoundDrawablePadding = MeasureUtils.dp2px(context, 16)
        setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        compoundDrawablePadding = MeasureUtils.dp2px(getContext(), 16)
        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.TextViewWithIcon, 0, 0)
            try {
                var drawableLeft: Drawable? = null
                val isDark = themeUtil.isDark
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    drawableLeft = a.getDrawable(R.styleable.TextViewWithIcon_tv_icon)
                } else {
                    var drawableLeftId = a.getResourceId(R.styleable.TextViewWithIcon_tv_icon, -1)
                    if (drawableLeftId != -1) {
                        drawableLeft = AppCompatResources.getDrawable(context, drawableLeftId)
                    }
                }
                if (drawableLeft != null) {
                    if (isDark) {
                        DrawableCompat.setTint(drawableLeft, ContextCompat.getColor(context, R.color.whitePrimary))
                    } else {
                        DrawableCompat.setTint(drawableLeft, ContextCompat.getColor(context, R.color.blackPrimary))
                    }
                }
                setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null)
            } catch (e: Exception) {
                LogUtil.d(TAG, "There was an error loading attributes.")
            } finally {
                a.recycle()
            }
        }
    }

    companion object {

        private const val TAG = "TextViewWithIcon"
    }
}
