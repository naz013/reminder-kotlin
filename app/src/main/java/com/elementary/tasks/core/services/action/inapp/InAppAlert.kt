package com.elementary.tasks.core.services.action.inapp

import androidx.annotation.DrawableRes

/**
 * In-app mirror of a just-posted system notification (reminder or birthday), shown as a banner
 * while the app is foregrounded. [alertId] is the reminder/birthday `uuId` - used only to guard
 * against a dismiss/action racing a newer alert that has already superseded this one in
 * [InAppAlertBus].
 */
data class InAppAlert(
  val alertId: String,
  val title: String,
  val text: String?,
  @DrawableRes val iconRes: Int,
  val actions: List<InAppAlertAction>,
)

data class InAppAlertAction(
  @DrawableRes val iconRes: Int,
  val label: String,
  val onClick: () -> Unit,
)
