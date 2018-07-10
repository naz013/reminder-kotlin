package com.elementary.tasks.core.drawing

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path

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

internal class Figure(val path: Path, private val paint: Paint) : Drawing {

    override var x: Float
        get() = 0f
        set(x) {

        }

    override var y: Float
        get() = 0f
        set(y) {

        }

    override var opacity: Int
        get() = this.paint.alpha
        set(opacity) {
            this.paint.alpha = opacity
        }

    override var strokeWidth: Float
        get() = this.paint.strokeWidth
        set(width) {
            this.paint.strokeWidth = width
        }

    override fun draw(canvas: Canvas, scale: Boolean) {
        if (scale) {
            val scaleMatrix = Matrix()
            val path = Path()
            scaleMatrix.setScale(0.15f, 0.15f)
            path.addPath(this.path, scaleMatrix)
            canvas.drawPath(path, this.paint)
        }
        canvas.drawPath(this.path, this.paint)
    }
}
