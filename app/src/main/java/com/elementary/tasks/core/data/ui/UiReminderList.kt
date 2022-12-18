package com.elementary.tasks.core.data.ui

import com.elementary.tasks.AdsProvider.Companion.REMINDER_BANNER_ID
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.data.ui.reminder.UiReminderDueData
import com.elementary.tasks.core.data.ui.reminder.UiReminderIllustration
import com.elementary.tasks.core.data.ui.reminder.UiReminderPlace
import com.elementary.tasks.core.data.ui.reminder.UiReminderStatus
import com.elementary.tasks.core.data.ui.reminder.UiReminderTarget
import com.elementary.tasks.core.data.ui.reminder.UiReminderType

sealed class UiReminderList {
  abstract val id: String
}

data class UiReminderListAds(
  override val id: String = REMINDER_BANNER_ID
) : UiReminderList()

sealed class UiReminderListData : UiReminderList() {
  abstract val group: UiGroup?
  abstract val isRepeating: Boolean
  abstract val status: UiReminderStatus
  abstract val due: UiReminderDueData?
  abstract val summary: String
}

data class UiReminderListActive(
  override val id: String,
  val type: UiReminderType,
  override val summary: String,
  val illustration: UiReminderIllustration,
  val priority: String,
  override val group: UiGroup?,
  override val due: UiReminderDueData,
  val isRunning: Boolean,
  override val status: UiReminderStatus,
  val actionTarget: UiReminderTarget?,
  override val isRepeating: Boolean
) : UiReminderListData()

data class UiReminderListActiveGps(
  override val id: String,
  val type: UiReminderType,
  override val summary: String,
  val illustration: UiReminderIllustration,
  val priority: String,
  override val group: UiGroup?,
  val isRunning: Boolean,
  override val status: UiReminderStatus,
  val actionTarget: UiReminderTarget?,
  val places: List<UiReminderPlace>
) : UiReminderListData() {
  override val isRepeating: Boolean = false
  override val due: UiReminderDueData? = null
}

data class UiReminderListActiveShop(
  override val id: String,
  val type: UiReminderType,
  override val summary: String,
  val illustration: UiReminderIllustration,
  val priority: String,
  override val group: UiGroup?,
  override val due: UiReminderDueData,
  val isRunning: Boolean,
  override val status: UiReminderStatus,
  val shopList: List<ShopItem>
) : UiReminderListData() {
  override val isRepeating: Boolean = false
}

data class UiReminderListRemoved(
  override val id: String,
  val type: UiReminderType,
  override val summary: String,
  val illustration: UiReminderIllustration,
  val priority: String,
  override val group: UiGroup?,
  val actionTarget: UiReminderTarget?,
  override val status: UiReminderStatus,
  override val due: UiReminderDueData,
  override val isRepeating: Boolean
) : UiReminderListData()

data class UiReminderListRemovedGps(
  override val id: String,
  val type: UiReminderType,
  override val summary: String,
  val illustration: UiReminderIllustration,
  val priority: String,
  override val group: UiGroup?,
  override val status: UiReminderStatus,
  val actionTarget: UiReminderTarget?,
  val places: List<UiReminderPlace>
) : UiReminderListData() {
  override val isRepeating: Boolean = false
  override val due: UiReminderDueData? = null
}

data class UiReminderListRemovedShop(
  override val id: String,
  val type: UiReminderType,
  override val summary: String,
  val illustration: UiReminderIllustration,
  val priority: String,
  override val group: UiGroup?,
  override val due: UiReminderDueData,
  override val status: UiReminderStatus,
  val shopList: List<ShopItem>
) : UiReminderListData() {
  override val isRepeating: Boolean = false
}
