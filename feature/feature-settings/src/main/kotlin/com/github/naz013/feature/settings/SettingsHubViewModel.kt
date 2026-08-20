package com.github.naz013.feature.settings

import androidx.lifecycle.ViewModel
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.feature.settings.security.SecuritySettingsPreferences
import com.github.naz013.platform.SystemInfo
import com.github.naz013.ui.common.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

internal class SettingsHubViewModel(
  private val remoteMessages: SettingsHubRemoteMessages,
  private val securityPreferences: SecuritySettingsPreferences,
  private val doNotDisturbChecker: SettingsHubDoNotDisturbChecker,
  private val textProvider: TextProvider,
  private val buildInfo: BuildInfo,
  private val systemInfo: SystemInfo,
  analyticsEventSender: AnalyticsEventSender,
) : ViewModel(),
  SettingsHubRemoteMessages.SaleObserver,
  SettingsHubRemoteMessages.UpdateObserver,
  SettingsHubRemoteMessages.MessageObserver {

  private val _state = MutableStateFlow(SettingsHubState())
  val state = _state.stateInWhileSubscribed(SettingsHubState())
    .onStart { addObservers() }

  private val doNotDisturbObserver: () -> Unit = { checkDoNotDisturb() }

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.SETTINGS))
    _state.update {
      it.copy(
        isBuyProBadgeVisible = !buildInfo.isPro && !systemInfo.isProAppInstalled,
        isPlayServicesWarningVisible = !systemInfo.googlePlayServicesAvailable,
        hasPinCode = securityPreferences.hasPinCode,
        isDeveloperOptionVisible = buildInfo.isDebug,
      )
    }
  }

  override fun onCleared() {
    doNotDisturbChecker.removeChangeObserver(doNotDisturbObserver)
    if (!buildInfo.isPro) {
      remoteMessages.removeSaleObserver(this)
    }
    remoteMessages.removeUpdateObserver(this)
    remoteMessages.removeMessageObserver(this)
  }

  private fun addObservers() {
    doNotDisturbChecker.addChangeObserver(doNotDisturbObserver)
    remoteMessages.addUpdateObserver(this)
    remoteMessages.addMessageObserver(this)
    if (!buildInfo.isPro) {
      remoteMessages.addSaleObserver(this)
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
    _state.update { it.copy(isDoNotDisturbActive = doNotDisturbChecker.isActive()) }
  }
}
