package com.elementary.tasks.home.eventsview

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.feature.common.flow.viewModelStateIn
import com.github.naz013.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart

class HomeEventsViewModel : ViewModel(), DefaultLifecycleObserver {

  private val _selectedTab = MutableStateFlow<SelectedTab>(SelectedTab.Reminders)
  val selectedTab = _selectedTab.viewModelStateIn(viewModelScope, SelectedTab.Reminders)
    .onStart { initialStateLoad() }

  init {
    _selectedTab.value = SelectedTab.Reminders
  }

  fun onTabSelected(tab: SelectedTab) {
    Logger.i(TAG, "Selected tab: $tab")
    _selectedTab.tryEmit(tab)
  }

  private fun initialStateLoad() {
    Logger.i(TAG, "Initial state load")
    _selectedTab.tryEmit(SelectedTab.Reminders)
  }

  enum class SelectedTab {
    Reminders,
    Todo,
    Birthdays
  }

  companion object {
    private const val TAG = "HomeEventsViewModel"
  }
}
