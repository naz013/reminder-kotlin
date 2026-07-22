package com.elementary.tasks.reminder.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.usecase.reminders.GetReminderByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FullScreenMapViewModel(
  private val id: String,
  private val dispatcherProvider: DispatcherProvider,
  private val getReminderByIdUseCase: GetReminderByIdUseCase,
) : ViewModel() {
  val reminder: StateFlow<Reminder?> field = MutableStateFlow(null)
  var placeIndex = 0

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      reminder.value = getReminderByIdUseCase(id)
    }
  }
}
