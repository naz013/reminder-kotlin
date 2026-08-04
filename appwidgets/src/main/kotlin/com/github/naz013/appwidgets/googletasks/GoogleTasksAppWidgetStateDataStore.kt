package com.github.naz013.appwidgets.googletasks

import androidx.datastore.core.DataStore
import com.github.naz013.appwidgets.googletasks.data.GoogleTasksAppWidgetState
import com.github.naz013.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class GoogleTasksAppWidgetStateDataStore(
  private val googleTasksAppWidgetViewModel: GoogleTasksAppWidgetViewModel
) : DataStore<GoogleTasksAppWidgetState> {
  override val data: Flow<GoogleTasksAppWidgetState>
    get() {
      return flow { emit(googleTasksAppWidgetViewModel.getState()) }
    }

  override suspend fun updateData(
    transform: suspend (t: GoogleTasksAppWidgetState) -> GoogleTasksAppWidgetState
  ): GoogleTasksAppWidgetState {
    Logger.d(TAG, "Update data")
    TODO("Not yet implemented")
  }

  companion object {
    private const val TAG = "GoogleTasksAppWidgetStateDataStore"
  }
}
