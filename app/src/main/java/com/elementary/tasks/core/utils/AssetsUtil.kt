package com.elementary.tasks.core.utils

import android.content.Context
import android.graphics.Typeface

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
object AssetsUtil {

    fun getTypeface(context: Context, code: Int): Typeface {
        return when (code) {
            0 -> Typeface.createFromAsset(context.assets, "fonts/roboto_black.ttf")
            1 -> Typeface.createFromAsset(context.assets, "fonts/roboto_black_italic.ttf")
            2 -> Typeface.createFromAsset(context.assets, "fonts/roboto_bold.ttf")
            3 -> Typeface.createFromAsset(context.assets, "fonts/roboto_bold_italic.ttf")
            4 -> Typeface.createFromAsset(context.assets, "fonts/roboto_italic.ttf")
            5 -> Typeface.createFromAsset(context.assets, "fonts/roboto_light.ttf")
            6 -> Typeface.createFromAsset(context.assets, "fonts/roboto_light_italic.ttf")
            7 -> Typeface.createFromAsset(context.assets, "fonts/roboto_medium.ttf")
            8 -> Typeface.createFromAsset(context.assets, "fonts/roboto_medium_italic.ttf")
            9 -> Typeface.createFromAsset(context.assets, "fonts/roboto_regular.ttf")
            10 -> Typeface.createFromAsset(context.assets, "fonts/roboto_thin.ttf")
            11 -> Typeface.createFromAsset(context.assets, "fonts/roboto_thin_italic.ttf")
            else -> Typeface.createFromAsset(context.assets, "fonts/roboto_regular.ttf")
        }
    }

    fun getDefaultTypeface(context: Context): Typeface {
        return getTypeface(context, 9)
    }
}
