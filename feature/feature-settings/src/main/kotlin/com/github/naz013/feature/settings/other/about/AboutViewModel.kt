package com.github.naz013.feature.settings.other.about

import androidx.lifecycle.ViewModel
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.common.TextProvider
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.ui.common.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import java.util.Calendar

internal class AboutViewModel(
  private val packageManagerWrapper: PackageManagerWrapper,
  private val textProvider: TextProvider,
  private val buildInfo: BuildInfo,
  private val analyticsEventSender: AnalyticsEventSender,
) : ViewModel() {

  private val _state = MutableStateFlow(AboutState())
  val state = _state.stateInWhileSubscribed(AboutState())
    .onStart { loadState() }

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.ABOUT))
  }

  private fun loadState() {
    val appName = textProvider.getString(if (buildInfo.isPro) R.string.app_name_pro else R.string.app_name)
    val year = Calendar.getInstance().get(Calendar.YEAR)
    _state.update {
      it.copy(
        appName = appName,
        versionInfo = textProvider.getString(
          R.string.about_version_info,
          packageManagerWrapper.getVersionName(),
          packageManagerWrapper.getVersionCode(),
        ),
        description = textProvider.getString(R.string.about_description),
        translators = textProvider.getStringArray(R.array.app_translators).joinToString("\n"),
        copyright = textProvider.getString(R.string.about_copyright, year, appName),
      )
    }
  }
}
