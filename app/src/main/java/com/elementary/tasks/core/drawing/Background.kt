package com.elementary.tasks.core.drawing

import android.graphics.Canvas
import android.graphics.Color
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

class Background internal constructor(@field:ColorInt
                                      var color: Int) : Drawing {
    override var opacity = 255

    override var x: Float
        get() = 0f
        set(x) {

        }

    override var y: Float
        get() = 0f
        set(y) {

        }

    override var strokeWidth: Float
        get() = 0f
        set(width) {

        }

    override fun draw(canvas: Canvas, scale: Boolean) {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        canvas.drawColor(Color.argb(opacity, red, green, blue))
    }
}
