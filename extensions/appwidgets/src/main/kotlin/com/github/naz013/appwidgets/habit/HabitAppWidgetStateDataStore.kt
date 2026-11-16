package com.github.naz013.appwidgets.habit

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

internal class HabitAppWidgetStateDataStore(
  private val habitAppWidgetViewModel: HabitAppWidgetViewModel
) : DataStore<HabitAppWidgetState> {

  override val data: Flow<HabitAppWidgetState>
    get() = flow { emit(habitAppWidgetViewModel.getState()) }

  override suspend fun updateData(
    transform: suspend (t: HabitAppWidgetState) -> HabitAppWidgetState
  ): HabitAppWidgetState {
    return transform(data.first())
  }
}
