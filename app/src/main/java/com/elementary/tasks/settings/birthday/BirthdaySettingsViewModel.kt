package com.elementary.tasks.settings.birthday

import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.github.naz013.feature.common.coroutine.DispatcherProvider

class BirthdaySettingsViewModel(
  dispatcherProvider: DispatcherProvider,
) : BaseProgressViewModel(dispatcherProvider) {


  companion object {
    private const val TAG = "BirthdaySettingsViewModel"
  }
}
