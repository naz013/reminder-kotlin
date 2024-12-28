package com.github.naz013.appwidgets.events

import androidx.datastore.core.DataStore
import com.github.naz013.appwidgets.events.data.EventsAppWidgetState
import com.github.naz013.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class EventsAppWidgetStateDataStore(
  private val eventsAppWidgetViewModel: EventsAppWidgetViewModel
) : DataStore<EventsAppWidgetState> {
  override val data: Flow<EventsAppWidgetState>
    get() {
      return flow { emit(eventsAppWidgetViewModel.getState()) }
    }

  override suspend fun updateData(
    transform: suspend (t: EventsAppWidgetState) -> EventsAppWidgetState
  ): EventsAppWidgetState {
    Logger.d(TAG, "Update data")
    TODO("Not yet implemented")
  }

  companion object {
    private const val TAG = "EventsAppWidgetStateDataStore"
  }
}
