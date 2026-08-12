package com.github.naz013.appwidgets.notes

import androidx.datastore.core.DataStore
import com.github.naz013.appwidgets.notes.data.NotesAppWidgetState
import com.github.naz013.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class NotesAppWidgetStateDataStore(
  private val notesAppWidgetViewModel: NotesAppWidgetViewModel
) : DataStore<NotesAppWidgetState> {
  override val data: Flow<NotesAppWidgetState>
    get() {
      return flow { emit(notesAppWidgetViewModel.getState()) }
    }

  override suspend fun updateData(
    transform: suspend (t: NotesAppWidgetState) -> NotesAppWidgetState
  ): NotesAppWidgetState {
    Logger.d(TAG, "Update data")
    TODO("Not yet implemented")
  }

  companion object {
    private const val TAG = "NotesAppWidgetStateDataStore"
  }
}
