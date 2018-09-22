package com.elementary.tasks.core.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import timber.log.Timber
import java.lang.Exception
import android.util.TypedValue




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
class DividerView : View {

    private var color = Color.GRAY

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val typedValue = TypedValue()
        val themeArray = context.theme.obtainStyledAttributes(typedValue.data, intArrayOf(android.R.attr.textColorPrimary))
        val color = try {
            themeArray.getColor(0, 0)
        } catch (e: Exception) {
            Color.BLACK
        } finally {
            themeArray.recycle()
        }
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        this.color = Color.argb(38, r, g, b)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(color)
    }
}
