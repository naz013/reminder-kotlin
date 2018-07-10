package com.elementary.tasks.core.drawing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint

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

class Image(bitmap: Bitmap) : Drawing {

    private var bitmap: Bitmap? = null
    override var x = 0f
    override var y = 0f
    override var opacity = 255
    internal var percentage = 100
        private set

    override var strokeWidth: Float
        get() = 0f
        set(width) {

        }

    init {
        this.bitmap = bitmap
    }

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    override fun draw(canvas: Canvas, scale: Boolean) {
        val paint = Paint()
        paint.alpha = this.opacity
        canvas.drawBitmap(getBitmap(scale)!!, getBitmapX(scale), getBitmapY(scale), paint)
    }

    private fun getBitmapY(scale: Boolean): Float {
        return if (scale) {
            this.y / 5
        } else this.y
    }

    private fun getBitmapX(scale: Boolean): Float {
        return if (scale) {
            this.x / 5
        } else this.x
    }

    private fun getBitmap(scale: Boolean): Bitmap? {
        if (this.percentage >= 100 && !scale) {
            return this.bitmap
        } else {
            val scalar = if (scale) 7 else 1
            val dstWidth = this.bitmap!!.width * (this.percentage / scalar) / 100
            val dstHeight = this.bitmap!!.height * (this.percentage / scalar) / 100
            return Bitmap.createScaledBitmap(this.bitmap!!, dstWidth, dstHeight, true)
        }
    }

    internal fun setScalePercentage(percentage: Int) {
        this.percentage = percentage
    }
}
