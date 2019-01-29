package com.elementary.tasks.month_view

import androidx.annotation.ColorInt
import com.elementary.tasks.core.view_models.month_view.MonthViewViewModel
import com.elementary.tasks.day_view.day.EventModel
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