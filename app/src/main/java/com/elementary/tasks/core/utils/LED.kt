package com.elementary.tasks.core.utils

import android.content.Context

import com.elementary.tasks.R

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
object LED {
    const val NUM_OF_LEDS = 7

    private const val RED = -0xbbcca
    private const val GREEN = -0xb350b0
    const val BLUE = -0xde690d
    private const val YELLOW = -0x14c5
    private const val PINK = -0x16e19d
    private const val DEEP_ORANGE = -0xa8de
    private const val TEAL = -0xff6978

    fun getLED(code: Int): Int {
        if (!Module.isPro) {
            return BLUE
        }
        return when (code) {
            0 -> RED
            1 -> GREEN
            2 -> BLUE
            3 -> YELLOW
            4 -> PINK
            5 -> DEEP_ORANGE
            6 -> TEAL
            else -> BLUE
        }
    }

    fun getAllNames(context: Context): Array<String> {
        val colors = arrayOf<String>()
        for (i in 0 until LED.NUM_OF_LEDS) {
            colors[i] = LED.getTitle(context, i)
        }
        return colors
    }

    fun getTitle(context: Context, code: Int): String {
        return when (code) {
            0 -> context.getString(R.string.red)
            1 -> context.getString(R.string.green)
            2 -> context.getString(R.string.blue)
            3 -> context.getString(R.string.yellow)
            4 -> context.getString(R.string.pink)
            5 -> context.getString(R.string.dark_orange)
            6 -> context.getString(R.string.teal)
            else -> context.getString(R.string.blue)
        }
    }
}
