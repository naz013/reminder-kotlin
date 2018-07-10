package com.elementary.tasks.notes.list

import android.content.Context

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

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
class KeepLayoutManager(context: Context, spanCount: Int, private val mAdapter: RecyclerView.Adapter<*>) : GridLayoutManager(context, spanCount) {

    init {
        init()
    }

    private fun init() {
        spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val size = mAdapter.itemCount
                when (size % 3) {
                    1 -> return if (position == 0) {
                        6
                    } else {
                        2
                    }
                    2 -> return if (position < 2) {
                        3
                    } else {
                        2
                    }
                    else -> return 2
                }
            }
        }
    }
}
