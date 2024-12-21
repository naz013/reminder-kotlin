package com.elementary.tasks.calendar.dayview

import com.elementary.tasks.calendar.dayview.weekheader.WeekDay
import com.elementary.tasks.calendar.dayview.weekheader.WeekHeaderController
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import org.threeten.bp.LocalDate

class WeekViewModel(
  dispatcherProvider: DispatcherProvider,
  private val weekHeaderController: WeekHeaderController
) : BaseProgressViewModel(dispatcherProvider) {

  private val _week = mutableLiveDataOf<List<WeekDay>>()
  val week = _week.toLiveData()

  fun onDateSelected(date: LocalDate) {
    _week.postValue(weekHeaderController.calculateWeek(date))
  }
}
