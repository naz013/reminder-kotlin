package com.elementary.tasks.calendar.dayview

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.calendar.dayview.weekheader.WeekHeaderController
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.feature.common.capitalizeFirstLetter
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate

class WeekViewModel(
  startDate: LocalDate,
  dispatcherProvider: DispatcherProvider,
  private val weekHeaderController: WeekHeaderController,
  private val dateTimeManager: DateTimeManager,
) : BaseProgressViewModel(dispatcherProvider) {

  private val _state = mutableLiveDataOf<DayViewState>()
  val state = _state.toLiveData()

  private val _moveToDate = mutableLiveEventOf<LocalDate>()
  val moveToDate = _moveToDate.toLiveData()

  val initDate = startDate
  var lastSelectedDate: LocalDate = startDate
    private set
  var lastPosition: Int = InfiniteDayViewPagerAdapter.CENTER_POSITION
    private set

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      Logger.i(TAG, "Initializing week view model with date $lastSelectedDate")

      val state = stateForDate(lastSelectedDate)

      withContext(dispatcherProvider.main()) {
        _state.value = state
        _moveToDate.value = Event(lastSelectedDate)
      }
    }
  }

  fun updateLastPosition(position: Int) {
    lastPosition = position
  }

  fun selectDate(date: LocalDate) {
    viewModelScope.launch(dispatcherProvider.default()) {
      Logger.i(TAG, "Select date called with date: $date")

      lastSelectedDate = date
      val state = stateForDate(date)

      withContext(dispatcherProvider.main()) {
        _state.value = state
        _moveToDate.value = Event(date)
      }
    }
  }

  fun onDateSelected(date: LocalDate) {
    viewModelScope.launch(dispatcherProvider.default()) {
      Logger.i(TAG, "On date selected: $date")

      lastSelectedDate = date
      val state = stateForDate(date)

      withContext(dispatcherProvider.main()) {
        _state.value = state
      }
    }
  }

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    Logger.d(TAG, "On resume, restoring last selected date $lastSelectedDate")
    onDateSelected(lastSelectedDate)
  }

  override fun onPause(owner: LifecycleOwner) {
    super.onPause(owner)
    Logger.d(TAG, "On pause.")
  }

  private suspend fun stateForDate(date: LocalDate): DayViewState {
    return DayViewState(
      title = dateTimeManager.formatCalendarDate(date).capitalizeFirstLetter(),
      days = weekHeaderController.calculateWeek(date)
    )
  }

  companion object {
    private const val TAG = "WeekViewModel"
  }
}
