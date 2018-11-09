package com.elementary.tasks.monthView

import androidx.annotation.ColorInt
import com.elementary.tasks.core.viewModels.monthView.MonthViewViewModel
import com.elementary.tasks.dayView.day.EventModel
import java.util.*

interface MonthCallback {
    fun find(monthPagerItem: MonthPagerItem, listener: ((MonthPagerItem, List<EventModel>) -> Unit)?)

    fun getViewModel(): MonthViewViewModel

    @ColorInt
    fun birthdayColor(): Int

    @ColorInt
    fun reminderColor(): Int

    fun onDateClick(date: Date)

    fun onDateLongClick(date: Date)
}