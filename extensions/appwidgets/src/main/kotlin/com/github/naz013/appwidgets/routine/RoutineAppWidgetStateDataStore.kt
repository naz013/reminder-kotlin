package com.github.naz013.appwidgets.routine

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

internal class RoutineAppWidgetStateDataStore(
  private val routineAppWidgetViewModel: RoutineAppWidgetViewModel
) : DataStore<RoutineAppWidgetState> {

  override val data: Flow<RoutineAppWidgetState>
    get() = flow { emit(routineAppWidgetViewModel.getState()) }

  override suspend fun updateData(
    transform: suspend (t: RoutineAppWidgetState) -> RoutineAppWidgetState
  ): RoutineAppWidgetState {
    return transform(data.first())
  }
}
