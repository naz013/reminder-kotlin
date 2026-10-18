package com.github.naz013.appwidgets.todo

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

internal class TodoAppWidgetStateDataStore(
  private val todoAppWidgetViewModel: TodoAppWidgetViewModel
) : DataStore<TodoAppWidgetState> {

  override val data: Flow<TodoAppWidgetState>
    get() = flow { emit(todoAppWidgetViewModel.getState()) }

  override suspend fun updateData(
    transform: suspend (t: TodoAppWidgetState) -> TodoAppWidgetState
  ): TodoAppWidgetState {
    return transform(data.first())
  }
}
