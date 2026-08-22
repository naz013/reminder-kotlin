package com.elementary.tasks.core.services.action.inapp

import androidx.annotation.DrawableRes

/**
 * In-app mirror of a just-posted system notification (reminder or birthday), shown as a banner
 * while the app is foregrounded. [alertId] is the reminder/birthday `uuId` - used both to guard
 * against a dismiss/action racing a newer alert that has already superseded this one in
 * [InAppAlertBus], and (together with [domain]) by `InAppAlertViewModel` to build the "Details"
 * action's in-app navigation, which the processors that build this data class have no access to.
 */
data class InAppAlert(
  val alertId: String,
  val domain: InAppAlertDomain,
  val title: String,
  val text: String?,
  @DrawableRes val iconRes: Int,
  val actions: List<InAppAlertAction>,
)

enum class InAppAlertDomain { REMINDER, BIRTHDAY }

data class InAppAlertAction(
  @DrawableRes val iconRes: Int,
  val label: String,
  val onClick: () -> Unit,
)
