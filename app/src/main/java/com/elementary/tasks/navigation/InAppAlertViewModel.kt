package com.elementary.tasks.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.navigation.nav3.AppNavBridge
import com.github.naz013.common.TextProvider
import com.github.naz013.feature.birthday.BirthdaysNavKey
import com.github.naz013.feature.reminder.preview.ReminderPreviewNavKey
import com.github.naz013.logic.notificationaction.InAppAlert
import com.github.naz013.logic.notificationaction.InAppAlertBus
import com.github.naz013.logic.notificationaction.InAppAlertDomain
import com.github.naz013.ui.common.compose.foundation.InAppAlertBannerAction
import com.github.naz013.ui.common.compose.foundation.InAppAlertBannerState
import com.github.naz013.ui.common.icon.DrawableCatalog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class InAppAlertViewModel(
  private val inAppAlertBus: InAppAlertBus,
  private val appNavBridge: AppNavBridge,
  private val textProvider: TextProvider,
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
      actions = listOf(detailsAction()) + actions.map { action ->
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

  /** Opens the reminder/birthday's own preview screen in the app's nav graph - the processors that
   *  build [InAppAlert] have no navigation access, so this is built here instead of baked in there
   *  like the other actions. Goes through [AppNavBridge] (a Koin singleton) rather than the widget-
   *  oriented [com.github.naz013.navigation.Navigator]/[NavigationDispatcher] path, since that one
   *  resolves to an Intent targeting [BottomNavActivity] and is a no-op while it's already resumed -
   *  which, since this banner only ever shows in the foreground, is always the case here. */
  private fun InAppAlert.detailsAction(): InAppAlertBannerAction =
    InAppAlertBannerAction(
      iconRes = DrawableCatalog.Fluent.Open,
      label = textProvider.getText(R.string.details),
      onClick = {
        when (domain) {
          InAppAlertDomain.REMINDER -> appNavBridge.navigate(ReminderPreviewNavKey.Preview(alertId))
          InAppAlertDomain.BIRTHDAY -> appNavBridge.navigate(BirthdaysNavKey.Preview(alertId))
        }
        inAppAlertBus.clear(alertId)
      },
    )
}
