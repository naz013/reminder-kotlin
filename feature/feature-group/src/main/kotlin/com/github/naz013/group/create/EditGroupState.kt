package com.github.naz013.group.create

import androidx.compose.ui.graphics.Color
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.feature.workflow.WorkflowConfig

internal data class EditGroupState(
  val id: String? = null,
  val title: String = "",
  val titleError: Boolean = false,
  val colorPosition: Int = 0,
  val sliderColors: List<Color> = emptyList(),
  val isDefault: Boolean = false,
  val defaultCheckEnabled: Boolean = true,
  val canDelete: Boolean = false,
  val isLoading: Boolean = false,
  val dialog: EditGroupDialog? = null,
  val isEdited: Boolean = false,
  val isFromFile: Boolean = false,
  val hasSameInDb: Boolean = false,
  val workflowsVisible: Boolean = WorkflowConfig.isEnabled,
  val notification: NotificationSettingsOverride = NotificationSettingsOverride(),
  val vibrateSubtitle: String = "",
  val repeatNotificationSubtitle: String = "",
  val bypassDndSubtitle: String = "",
  val wakeScreenSubtitle: String = "",
  val prioritySubtitle: String = "",
  val categorySubtitle: String = "",
  val lockScreenVisibilitySubtitle: String = "",
  val vibrationPatternSubtitle: String = "",
  val delayMinutesSubtitle: String = "",
  val hapticFeedbackEnabled: Boolean = true,
) {
  val hasId: Boolean
    get() = id.isNullOrEmpty().not()
}

internal enum class GroupNotificationDialogKind {
  VIBRATE, REPEAT_NOTIFICATION, BYPASS_DND, WAKE_SCREEN, PRIORITY, CATEGORY, LOCK_SCREEN_VISIBILITY, VIBRATION_PATTERN
}

internal sealed interface EditGroupDialog {
  data object CopyConflict : EditGroupDialog

  data object DeleteConfirm : EditGroupDialog

  data class NotificationChoice(
    val kind: GroupNotificationDialogKind,
    val title: String,
    val options: List<String>,
    val selectedIndex: Int,
  ) : EditGroupDialog

  data class DelayMinutes(
    val isOverridden: Boolean,
    val previewValue: Int,
  ) : EditGroupDialog
}
