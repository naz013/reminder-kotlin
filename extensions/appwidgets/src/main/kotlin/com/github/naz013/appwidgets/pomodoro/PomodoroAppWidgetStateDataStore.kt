package com.github.naz013.appwidgets.pomodoro

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

internal class PomodoroAppWidgetStateDataStore(
  private val pomodoroAppWidgetViewModel: PomodoroAppWidgetViewModel
) : DataStore<PomodoroAppWidgetState> {

  override val data: Flow<PomodoroAppWidgetState>
    get() = flow { emit(pomodoroAppWidgetViewModel.getState()) }

  override suspend fun updateData(
    transform: suspend (t: PomodoroAppWidgetState) -> PomodoroAppWidgetState
  ): PomodoroAppWidgetState {
    return transform(data.first())
  }
}
