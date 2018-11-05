package com.elementary.tasks.navigation.settings.theme

import androidx.annotation.ColorInt

data class Theme(
        val id: Int,
        var isSelected: Boolean = false,
        val isDark: Boolean = false,
        val name: String = "",
        @ColorInt val statusColor: Int,
        @ColorInt val barColor: Int,
        @ColorInt val bgColor: Int
)