package com.elementary.tasks.core.utils

import android.content.Context

import com.elementary.tasks.R

object LED {
    private const val NUM_OF_LEDS = 7

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

    fun getAllNames(context: Context): List<String> {
        val colors = mutableListOf<String>()
        for (i in 0 until NUM_OF_LEDS) {
            colors.add(getTitle(context, i))
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
