package com.github.naz013.appwidgets.singlenote

import androidx.datastore.core.DataStore
import com.github.naz013.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class SingleNoteAppWidgetStateDataStore(
  private val singleNoteAppWidgetViewModel: SingleNoteAppWidgetViewModel
) : DataStore<SingleNoteAppWidgetState> {
  override val data: Flow<SingleNoteAppWidgetState>
    get() {
      return flow { emit(singleNoteAppWidgetViewModel.getState()) }
    }

  override suspend fun updateData(
    transform: suspend (t: SingleNoteAppWidgetState) -> SingleNoteAppWidgetState
  ): SingleNoteAppWidgetState {
    Logger.d(TAG, "Update data")
    TODO("Not yet implemented")
  }

  companion object {
    private const val TAG = "SingleNoteAppWidgetStateDataStore"
  }
}
