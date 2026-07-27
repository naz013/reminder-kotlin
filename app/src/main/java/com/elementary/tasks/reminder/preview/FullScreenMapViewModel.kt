package com.elementary.tasks.reminder.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.usecase.reminders.GetReminderV2ByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FullScreenMapViewModel(
  private val id: String,
  private val dispatcherProvider: DispatcherProvider,
  private val getReminderV2ByIdUseCase: GetReminderV2ByIdUseCase,
) : ViewModel() {
  val reminder: StateFlow<ReminderV2?> field = MutableStateFlow(null)
  var placeIndex = 0

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      reminder.value = getReminderV2ByIdUseCase(id)
    }
  }
}
