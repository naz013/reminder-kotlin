package com.elementary.tasks.calendar.dayview

import com.elementary.tasks.calendar.dayview.weekheader.WeekDay
import com.elementary.tasks.calendar.dayview.weekheader.WeekHeaderController
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.arch.OneWayLiveData
import com.elementary.tasks.core.utils.DispatcherProvider
import org.threeten.bp.LocalDate

class WeekViewModel(
  dispatcherProvider: DispatcherProvider,
  private val weekHeaderController: WeekHeaderController
) : BaseProgressViewModel(dispatcherProvider) {

  val week = OneWayLiveData<List<WeekDay>>()

  fun onDateSelected(date: LocalDate) {
    week.viewModelPost(weekHeaderController.calculateWeek(date))
  }
}
