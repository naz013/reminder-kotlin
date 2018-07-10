package com.elementary.tasks.core.utils

import android.content.Context
import androidx.annotation.Px
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.view.WindowManager

import java.util.Dictionary
import java.util.Hashtable

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

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
object MeasureUtils {

    private val sRecyclerViewItemHeights = Hashtable<Int, Int>()

    @Px
    fun dp2px(context: Context, dp: Int): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        var display: Display? = null
        if (wm != null) {
            display = wm.defaultDisplay
        }

        val displaymetrics = DisplayMetrics()
        display?.getMetrics(displaymetrics)
        return (dp * displaymetrics.density + 0.5f).toInt()
    }

    fun getScrollY(rv: RecyclerView, columnCount: Int, mIsGrid: Boolean): Int {
        val c = rv.getChildAt(0) ?: return 0
        var firstVisiblePosition: Int
        if (mIsGrid) {
            try {
                val layoutManager = rv.layoutManager as StaggeredGridLayoutManager?
                val pos = IntArray(4)
                layoutManager!!.findFirstVisibleItemPositions(pos)
                firstVisiblePosition = pos[0]
            } catch (e: ClassCastException) {
                val layoutManager = rv.layoutManager as LinearLayoutManager?
                firstVisiblePosition = layoutManager!!.findFirstVisibleItemPosition()
            }

        } else {
            try {
                val layoutManager = rv.layoutManager as LinearLayoutManager?
                firstVisiblePosition = layoutManager!!.findFirstVisibleItemPosition()
            } catch (e: ClassCastException) {
                val layoutManager = rv.layoutManager as StaggeredGridLayoutManager?
                val pos = IntArray(4)
                layoutManager!!.findFirstVisibleItemPositions(pos)
                firstVisiblePosition = pos[0]
            }

        }
        var scrollY = -c.top
        if (columnCount > 1) {
            sRecyclerViewItemHeights[firstVisiblePosition] = c.height + MeasureUtils.dp2px(rv.context, 8) / columnCount
        } else {
            sRecyclerViewItemHeights[firstVisiblePosition] = c.height
        }
        if (scrollY < 0) {
            scrollY = 0
        }
        for (i in 0 until firstVisiblePosition) {
            if (sRecyclerViewItemHeights[i] != null) {
                scrollY += sRecyclerViewItemHeights[i]
            }
        }
        return scrollY
    }
}
