package com.elementary.tasks.reminder.preview

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.usecase.reminders.GetReminderByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FullScreenMapViewModel(
  private val id: String,
  dispatcherProvider: DispatcherProvider,
  private val getReminderByIdUseCase: GetReminderByIdUseCase,
) : BaseProgressViewModel(dispatcherProvider) {
  val reminder: StateFlow<Reminder?> field = MutableStateFlow(null)
  var placeIndex = 0

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    viewModelScope.launch(dispatcherProvider.default()) {
      reminder.value = getReminderByIdUseCase(id)
    }
  }
}
