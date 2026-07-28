package com.elementary.tasks.groups.create

import androidx.compose.ui.graphics.Color
import com.elementary.tasks.workflow.WorkflowConfig

data class EditGroupState(
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
) {
  val hasId: Boolean
    get() = id.isNullOrEmpty().not()
}

sealed interface EditGroupDialog {
  data object CopyConflict : EditGroupDialog

  data object DeleteConfirm : EditGroupDialog
}
