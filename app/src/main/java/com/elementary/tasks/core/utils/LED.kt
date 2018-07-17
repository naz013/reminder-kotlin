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
    const val NUM_OF_LEDS = 17

    private const val WHITE = -0x1
    private const val RED = -0xbbcca
    private const val GREEN = -0xb350b0
    const val BLUE = -0xde690d
    private const val ORANGE = -0x6800
    private const val YELLOW = -0x14c5
    private const val AMBER = -0x3ef9
    private const val PINK = -0x16e19d
    private const val GREEN_LIGHT = -0x743cb6
    private const val BLUE_LIGHT = -0xfc560c
    private const val CYAN = -0xff432c
    private const val PURPLE = -0x63d850
    private const val LIME = -0x3223c7
    private const val INDIGO = -0xc0ae4b
    private const val DEEP_PURPLE = -0x98c549
    private const val DEEP_ORANGE = -0xa8de
    private const val TEAL = -0xff6978

    fun getLED(code: Int): Int {
        if (!Module.isPro) {
            return CYAN
        }
        val color: Int
        when (code) {
            0 -> color = WHITE
            1 -> color = RED
            2 -> color = GREEN
            3 -> color = BLUE
            4 -> color = ORANGE
            5 -> color = YELLOW
            6 -> color = PINK
            7 -> color = GREEN_LIGHT
            8 -> color = BLUE_LIGHT
            9 -> color = PURPLE
            10 -> color = AMBER
            11 -> color = CYAN
            12 -> color = LIME
            13 -> color = INDIGO
            14 -> color = DEEP_ORANGE
            15 -> color = DEEP_PURPLE
            16 -> color = TEAL
            else -> color = BLUE
        }
        return color
    }

    fun getAllNames(context: Context): Array<String> {
        val colors = arrayOf<String>()
        for (i in 0 until LED.NUM_OF_LEDS) {
            colors[i] = LED.getTitle(context, i)
        }
        return colors
    }

    fun getTitle(context: Context, code: Int): String {
        val color: String
        when (code) {
            0 -> color = context.getString(R.string.white)
            1 -> color = context.getString(R.string.red)
            2 -> color = context.getString(R.string.green)
            3 -> color = context.getString(R.string.blue)
            4 -> color = context.getString(R.string.orange)
            5 -> color = context.getString(R.string.yellow)
            6 -> color = context.getString(R.string.pink)
            7 -> color = context.getString(R.string.green_light)
            8 -> color = context.getString(R.string.blue_light)
            9 -> color = context.getString(R.string.purple)
            10 -> color = context.getString(R.string.amber)
            11 -> color = context.getString(R.string.cyan)
            12 -> color = context.getString(R.string.lime)
            13 -> color = context.getString(R.string.indigo)
            14 -> color = context.getString(R.string.dark_orange)
            15 -> color = context.getString(R.string.dark_purple)
            16 -> color = context.getString(R.string.teal)
            else -> color = context.getString(R.string.blue)
        }
        return color
    }
}
