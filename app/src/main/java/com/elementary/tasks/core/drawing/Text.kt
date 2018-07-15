package com.elementary.tasks.core.drawing

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.annotation.ColorInt

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

class Text(text: String, fontSize: Float, textPaint: Paint) : Drawing {

    var text = ""
    private var fontSize = 32f
        get
        set
    private var textPaint = Paint()
    override var x = 0f
    override var y = 0f
    var fontFamily: Int = 0
        private set

    override var opacity: Int
        get() = this.textPaint.alpha
        set(opacity) {
            this.textPaint.alpha = opacity
        }

    override var strokeWidth: Float
        get() = this.textPaint.strokeWidth
        set(width) {
            this.textPaint.strokeWidth = width
        }

    init {
        this.text = text
        this.fontSize = fontSize
        this.textPaint = textPaint
    }

    fun setFontSize(fontSize: Float) {
        this.fontSize = fontSize
        this.textPaint.textSize = fontSize
    }

    fun setTextColor(@ColorInt color: Int) {
        this.textPaint.color = color
    }

    fun setFontFamily(id: Int, fontFamily: Typeface) {
        this.fontFamily = id
        this.textPaint.typeface = fontFamily
    }

    fun getFontSize(): Float {
        return fontSize
    }

    override fun draw(canvas: Canvas, scale: Boolean) {
        drawText(canvas, scale)
    }

    private fun drawText(canvas: Canvas, scale: Boolean) {
        if (this.text.isEmpty()) {
            return
        }
        val textX = this.getTextX(scale)
        val textY = this.getTextY(scale)
        val paintForMeasureText = Paint()
        val textLength = paintForMeasureText.measureText(this.text)
        val lengthOfChar = textLength / this.text.length.toFloat()
        val restWidth = canvas.width - textX  // text-align : right
        val numChars = if (lengthOfChar <= 0) 1 else Math.floor((restWidth / lengthOfChar).toDouble()).toInt()  // The number of characters at 1 line
        val modNumChars = if (numChars < 1) 1 else numChars
        var y = textY
        var i = 0
        val len = this.text.length
        while (i < len) {
            val substring: String
            if (i + modNumChars < len) {
                substring = this.text.substring(i, i + modNumChars)
            } else {
                substring = this.text.substring(i, len)
            }
            y += getFontScaled(scale)
            canvas.drawText(substring, textX, y, getTransformed(scale))
            i += modNumChars
        }
    }

    private fun getTransformed(scale: Boolean): Paint {
        if (scale) {
            val p = Paint(this.textPaint)
            p.textSize = getFontScaled(true)
            return p
        }
        return this.textPaint
    }

    private fun getFontScaled(scale: Boolean): Float {
        return if (scale) {
            this.fontSize / 4
        } else this.fontSize
    }

    private fun getTextY(scale: Boolean): Float {
        return if (scale) {
            this.y / 7
        } else this.y
    }

    private fun getTextX(scale: Boolean): Float {
        return if (scale) {
            this.x / 7
        } else this.x
    }
}
