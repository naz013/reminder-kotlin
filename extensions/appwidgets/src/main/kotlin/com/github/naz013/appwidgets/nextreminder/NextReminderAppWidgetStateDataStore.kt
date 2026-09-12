package com.github.naz013.appwidgets.nextreminder

import androidx.datastore.core.DataStore
import com.github.naz013.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class NextReminderAppWidgetStateDataStore(
  private val nextReminderAppWidgetViewModel: NextReminderAppWidgetViewModel
) : DataStore<NextReminderAppWidgetState> {
  override val data: Flow<NextReminderAppWidgetState>
    get() {
      return flow { emit(nextReminderAppWidgetViewModel.getState()) }
    }

  override suspend fun updateData(
    transform: suspend (t: NextReminderAppWidgetState) -> NextReminderAppWidgetState
  ): NextReminderAppWidgetState {
    Logger.d(TAG, "Update data")
    TODO("Not yet implemented")
  }

  companion object {
    private const val TAG = "NextReminderAppWidgetStateDataStore"
  }
}
