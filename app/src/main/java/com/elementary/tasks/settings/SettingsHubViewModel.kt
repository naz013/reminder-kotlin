package com.elementary.tasks.settings

import androidx.lifecycle.ViewModel
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.datetime.DoNotDisturbManager
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.params.PrefsConstants
import com.elementary.tasks.core.utils.params.RemotePrefs
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.common.system.SystemInfo
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

class SettingsHubViewModel(
  private val remotePrefs: RemotePrefs,
  private val prefs: Prefs,
  private val doNotDisturbManager: DoNotDisturbManager,
  private val textProvider: TextProvider,
  private val buildInfo: BuildInfo,
  private val systemInfo: SystemInfo,
  analyticsEventSender: AnalyticsEventSender,
) : ViewModel(),
  RemotePrefs.SaleObserver,
  RemotePrefs.UpdateObserver,
  RemotePrefs.MessageObserver {

  private val _state = MutableStateFlow(SettingsHubState())
  val state = _state.stateInWhileSubscribed(SettingsHubState())
    .onStart { addObservers() }

  private val prefsObserver: (String) -> Unit = { checkDoNotDisturb() }

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.SETTINGS))
    _state.update {
      it.copy(
        isBuyProBadgeVisible = !buildInfo.isPro && !systemInfo.isProAppInstalled,
        isPlayServicesWarningVisible = !systemInfo.googlePlayServicesAvailable,
        hasPinCode = prefs.hasPinCode,
        isInsightsVisible = buildInfo.isPro,
      )
    }
  }

  override fun onCleared() {
    prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_ENABLED, prefsObserver)
    prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_FROM, prefsObserver)
    prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_TO, prefsObserver)
    prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_IGNORE, prefsObserver)
    if (!BuildParams.isPro) {
      remotePrefs.removeSaleObserver(this)
    }
    remotePrefs.removeUpdateObserver(this)
    remotePrefs.removeMessageObserver(this)
  }

  private fun addObservers() {
    prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_ENABLED, prefsObserver)
    prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_FROM, prefsObserver)
    prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_TO, prefsObserver)
    prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_IGNORE, prefsObserver)
    remotePrefs.addUpdateObserver(this)
    remotePrefs.addMessageObserver(this)
    if (!BuildParams.isPro) {
      remotePrefs.addSaleObserver(this)
    }
    checkDoNotDisturb()
  }

  override fun onUpdateChanged(
    hasUpdate: Boolean,
    version: String,
  ) {
    _state.update {
      it.copy(
        updateMessage = if (hasUpdate) textProvider.getString(
          R.string.new_update_message,
          version
        ) else null
      )
    }
  }

  override fun onSaleChanged(
    showDiscount: Boolean,
    discount: String,
    until: String,
  ) {
    _state.update {
      it.copy(
        saleMessage =
          if (showDiscount) {
            textProvider.getString(R.string.new_sale_message, discount, until)
          } else {
            null
          },
      )
    }
  }

  override fun onMessageChanged(
    showMessage: Boolean,
    message: String,
  ) {
    _state.update { it.copy(internalMessage = if (showMessage) message else null) }
  }

  private fun checkDoNotDisturb() {
    _state.update { it.copy(isDoNotDisturbActive = doNotDisturbManager.applyDoNotDisturb(0)) }
  }
}
