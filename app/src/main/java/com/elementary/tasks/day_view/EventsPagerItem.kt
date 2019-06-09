package com.elementary.tasks.day_view

import java.io.Serializable

data class EventsPagerItem(
        var day: Int,
        var month: Int,
        var year: Int,
        var isToday: Boolean = false
) : Serializable
