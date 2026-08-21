package com.elementary.tasks.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.services.action.inapp.InAppAlert
import com.elementary.tasks.core.services.action.inapp.InAppAlertBus
import com.github.naz013.ui.common.compose.foundation.InAppAlertBannerAction
import com.github.naz013.ui.common.compose.foundation.InAppAlertBannerState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class InAppAlertViewModel(
  private val inAppAlertBus: InAppAlertBus,
) : ViewModel() {
  val state: StateFlow<InAppAlertBannerState?> =
    inAppAlertBus.current
      .map { alert -> alert?.toBannerState() }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  private fun InAppAlert.toBannerState(): InAppAlertBannerState =
    InAppAlertBannerState(
      id = alertId,
      title = title,
      text = text,
      iconRes = iconRes,
      actions =
      actions.map { action ->
        InAppAlertBannerAction(
          iconRes = action.iconRes,
          label = action.label,
          onClick = {
            action.onClick()
            inAppAlertBus.clear(alertId)
          },
        )
      },
    )
}
