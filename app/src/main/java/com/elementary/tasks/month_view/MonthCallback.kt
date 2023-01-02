package com.elementary.tasks.month_view

import androidx.annotation.ColorInt
import com.elementary.tasks.day_view.day.EventModel
import org.threeten.bp.LocalDate

interface MonthCallback {
  fun find(monthPagerItem: MonthPagerItem, listener: ((MonthPagerItem, List<EventModel>) -> Unit)?)
  fun getViewModel(): MonthViewViewModel
  @ColorInt
  fun birthdayColor(): Int
  @ColorInt
  fun reminderColor(): Int
  fun onDateClick(date: LocalDate)
  fun onDateLongClick(date: LocalDate)
}