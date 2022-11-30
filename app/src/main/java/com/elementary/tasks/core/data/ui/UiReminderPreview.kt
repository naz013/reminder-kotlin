package com.elementary.tasks.core.data.ui

import com.elementary.tasks.core.data.models.ShopItem

data class UiReminderPreview(
  val id: String,
  val type: UiReminderType,
  val noteId: String,
  val group: UiGroup?,
  val actionTarget: UiReminderTarget?,
  val summary: String,
  val isRunning: Boolean,
  val attachmentFile: String?,
  val windowType: String,
  val status: UiReminderStatus,
  val illustration: UiReminderIllustration,
  val melodyName: String?,
  val due: UiReminderDueData,
  val shopList: List<ShopItem>,
  val places: List<UiReminderPlace>
)