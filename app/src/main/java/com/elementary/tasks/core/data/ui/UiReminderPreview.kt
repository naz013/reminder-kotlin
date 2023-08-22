package com.elementary.tasks.core.data.ui

import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.core.data.ui.reminder.UiReminderDueData
import com.elementary.tasks.core.data.ui.reminder.UiReminderIllustration
import com.elementary.tasks.core.data.ui.reminder.UiReminderPlace
import com.elementary.tasks.core.data.ui.reminder.UiReminderStatus
import com.elementary.tasks.core.data.ui.reminder.UiReminderTarget
import com.elementary.tasks.core.data.ui.reminder.UiReminderType

data class UiReminderPreview(
  val id: String,
  val type: UiReminderType,
  val noteId: String,
  val group: UiGroupList?,
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
  val places: List<UiReminderPlace>,
  val allDay: Boolean
)
