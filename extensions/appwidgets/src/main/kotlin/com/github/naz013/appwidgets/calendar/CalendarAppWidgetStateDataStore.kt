package com.github.naz013.appwidgets.calendar

import androidx.datastore.core.DataStore
import com.github.naz013.appwidgets.calendar.data.CalendarAppWidgetState
import com.github.naz013.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class CalendarAppWidgetStateDataStore(
  private val calendarAppWidgetViewModel: CalendarAppWidgetViewModel
) : DataStore<CalendarAppWidgetState> {
  override val data: Flow<CalendarAppWidgetState>
    get() {
      return flow { emit(calendarAppWidgetViewModel.getState()) }
    }

  override suspend fun updateData(
    transform: suspend (t: CalendarAppWidgetState) -> CalendarAppWidgetState
  ): CalendarAppWidgetState {
    Logger.d(TAG, "Update data")
    TODO("Not yet implemented")
  }

  companion object {
    private const val TAG = "CalendarAppWidgetStateDataStore"
  }
}
