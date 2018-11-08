package com.elementary.tasks.monthView

import com.elementary.tasks.core.viewModels.monthView.MonthViewViewModel
import com.elementary.tasks.dayView.day.EventModel

interface MonthCallback {
    fun find(monthPagerItem: MonthPagerItem, listener: ((MonthPagerItem, List<EventModel>) -> Unit)?)

    fun getViewModel(): MonthViewViewModel
}