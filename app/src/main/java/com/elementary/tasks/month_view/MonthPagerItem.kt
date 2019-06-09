package com.elementary.tasks.month_view

import java.io.Serializable

data class MonthPagerItem(
        var month: Int,
        var year: Int
) : Serializable
