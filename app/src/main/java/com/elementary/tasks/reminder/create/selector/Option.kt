package com.elementary.tasks.reminder.create.selector

import androidx.annotation.DrawableRes

data class Option(
        val key: String,
        @DrawableRes
        val icon: Int,
        val name: String,
        var isSelected: Boolean = false
)