package com.elementary.tasks.home.eventsview

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.logging.Logger

class HomeEventsViewModel : ViewModel(), DefaultLifecycleObserver {

  private val _selectedTab = mutableLiveDataOf<SelectedTab>()
  val selectedTab = _selectedTab.toLiveData()

  init {
    _selectedTab.value = SelectedTab.Reminders
  }

  fun onTabSelected(tab: SelectedTab) {
    Logger.i(TAG, "Selected tab: $tab")
    _selectedTab.value = tab
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
