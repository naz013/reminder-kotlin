package com.elementary.tasks.settings.other

import androidx.lifecycle.ViewModel
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.BuildParams
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.common.TextProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class OtherSettingsViewModel(
  private val packageManagerWrapper: PackageManagerWrapper,
  private val textProvider: TextProvider,
  private val analyticsEventSender: AnalyticsEventSender,
) : ViewModel() {

  val state: StateFlow<OtherSettingsState> field = MutableStateFlow(OtherSettingsState())

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.OTHER_SETTINGS))
  }

  fun onAboutClick() {
    val appName = if (BuildParams.isPro) {
      textProvider.getString(R.string.app_name_pro)
    } else {
      textProvider.getString(R.string.app_name)
    }
    state.update {
      it.copy(
        aboutDialog = AboutDialogState(
          appName = appName.uppercase(),
          version = packageManagerWrapper.getVersionName(),
          translators = textProvider.getStringArray(R.array.app_translators).joinToString("\n"),
        ),
      )
    }
  }

  fun onAboutDialogDismiss() {
    state.update { it.copy(aboutDialog = null) }
  }
}
