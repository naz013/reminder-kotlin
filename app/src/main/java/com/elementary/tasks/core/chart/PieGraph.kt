package com.elementary.tasks.core.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

import java.util.ArrayList

/**
 * Created by Daniel Nadeau
 * daniel.nadeau01@gmail.com
 * danielnadeau.blogspot.com
 *
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

class PieGraph : View {

    private var slices: MutableList<PieSlice> = ArrayList()
    private val paint = Paint()
    private val path = Path()

    private var indexSelected = -1
    var thickness = 50
        set(thickness) {
            field = thickness
            postInvalidate()
        }
    private var listener: OnSliceClickedListener? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    public override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.TRANSPARENT)
        paint.reset()
        paint.isAntiAlias = true
        val midX: Float
        val midY: Float
        var radius: Float
        val innerRadius: Float
        path.reset()
        var currentAngle = 270f
        var currentSweep: Float
        var totalValue = 0
        val padding = 2f
        midX = (width / 2).toFloat()
        midY = (height / 2).toFloat()
        if (midX < midY) {
            radius = midX
        } else {
            radius = midY
        }
        radius -= padding
        innerRadius = radius - this.thickness
        for (slice in slices) {
            totalValue += slice.value.toInt()
        }
        var count = 0
        for (slice in slices) {
            val p = Path()
            paint.color = slice.color
            currentSweep = slice.value / totalValue * 360
            p.arcTo(RectF(midX - radius, midY - radius, midX + radius, midY + radius), currentAngle + padding, currentSweep - padding)
            p.arcTo(RectF(midX - innerRadius, midY - innerRadius, midX + innerRadius, midY + innerRadius), currentAngle + padding + (currentSweep - padding), -(currentSweep - padding))
            p.close()
            slice.path = p
            slice.region = Region((midX - radius).toInt(), (midY - radius).toInt(), (midX + radius).toInt(), (midY + radius).toInt())
            canvas.drawPath(p, paint)
            if (indexSelected == count && listener != null) {
                path.reset()
                paint.color = slice.color
                paint.color = Color.parseColor("#33B5E5")
                paint.alpha = 100
                if (slices.size > 1) {
                    path.arcTo(RectF(midX - radius - padding * 2, midY - radius - padding * 2, midX + radius + padding * 2, midY + radius + padding * 2), currentAngle, currentSweep + padding)
                    path.arcTo(RectF(midX - innerRadius + padding * 2, midY - innerRadius + padding * 2, midX + innerRadius - padding * 2, midY + innerRadius - padding * 2), currentAngle + currentSweep + padding, -(currentSweep + padding))
                    path.close()
                } else {
                    path.addCircle(midX, midY, radius + padding, Path.Direction.CW)
                }
                canvas.drawPath(path, paint)
                paint.alpha = 255
            }
            currentAngle = currentAngle + currentSweep
            count++
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val point = Point()
        point.x = event.x.toInt()
        point.y = event.y.toInt()
        var count = 0
        for (slice in slices) {
            val r = Region()
            r.setPath(slice.path, slice.region)
            if (r.contains(point.x, point.y) && event.action == MotionEvent.ACTION_DOWN) {
                indexSelected = count
            } else if (event.action == MotionEvent.ACTION_UP) {
                if (r.contains(point.x, point.y) && listener != null) {
                    if (indexSelected > -1) {
                        listener!!.onClick(indexSelected)
                    }
                    indexSelected = -1
                }
            }
            count++
        }
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) {
            postInvalidate()
        }
        return true
    }

    fun getSlices(): List<PieSlice> {
        return slices
    }

    fun setSlices(slices: MutableList<PieSlice>) {
        this.slices = slices
        postInvalidate()
    }

    fun getSlice(index: Int): PieSlice {
        return slices[index]
    }

    fun addSlice(slice: PieSlice) {
        this.slices.add(slice)
        postInvalidate()
    }

    fun setOnSliceClickedListener(listener: OnSliceClickedListener) {
        this.listener = listener
    }

    fun removeSlices() {
        for (i in slices.indices.reversed()) {
            slices.removeAt(i)
        }
        postInvalidate()
    }

    internal interface OnSliceClickedListener {
        fun onClick(index: Int)
    }
}
