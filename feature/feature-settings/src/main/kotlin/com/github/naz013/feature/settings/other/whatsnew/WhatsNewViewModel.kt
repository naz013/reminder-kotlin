package com.github.naz013.feature.settings.other.whatsnew

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.common.TextProvider
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.settings.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils

class WhatsNewViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val packageManagerWrapper: PackageManagerWrapper,
  private val textProvider: TextProvider,
  private val buildInfo: BuildInfo,
) : ViewModel() {
  private val _state = MutableStateFlow(WhatsNewState())
  val state =
    _state
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        WhatsNewState(),
      ).onStart { loadState() }

  private fun loadState() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val versionName: String = packageManagerWrapper.getVersionName()
      val buildDate: String = StringUtils.capitalize(buildInfo.buildDate)

      withContext(dispatcherProvider.main()) {
        _state.update {
          WhatsNewState(
            versionName = versionName,
            lastUpdated = buildDate,
            whatsNewText = textProvider.getString(R.string.whats_new_text),
          )
        }
      }
    }
  }
}
