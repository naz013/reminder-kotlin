package com.elementary.tasks.core.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import androidx.annotation.Px

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

    @Px
    fun dp2px(context: Context, dp: Int): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        var display: Display? = null
        if (wm != null) {
            display = wm.defaultDisplay
        }

        val displaymetrics = DisplayMetrics()
        display?.getMetrics(displaymetrics)
        return (dp * displaymetrics.density + 0.5f).toInt()
    }
}
