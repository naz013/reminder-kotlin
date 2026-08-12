package com.github.naz013.appwidgets.birthdays

import androidx.datastore.core.DataStore
import com.github.naz013.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class BirthdaysAppWidgetStateDataStore(
  private val birthdaysAppWidgetViewModel: BirthdaysAppWidgetViewModel
) : DataStore<BirthdaysAppWidgetState> {
  override val data: Flow<BirthdaysAppWidgetState>
    get() {
      return flow { emit(birthdaysAppWidgetViewModel.getState()) }
    }

  override suspend fun updateData(
    transform: suspend (t: BirthdaysAppWidgetState) -> BirthdaysAppWidgetState
  ): BirthdaysAppWidgetState {
    Logger.d(TAG, "Update data")
    TODO("Not yet implemented")
  }

  companion object {
    private const val TAG = "BirthdaysAppWidgetStateDataStore"
  }
}
