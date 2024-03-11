package com.elementary.tasks.reminder.build.logic

import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.UiSelectorItem
import com.elementary.tasks.reminder.build.UiSelectorItemState
import com.elementary.tasks.reminder.build.adapter.BiErrorForUiAdapter
import com.elementary.tasks.reminder.build.bi.BuilderItemError
import com.elementary.tasks.reminder.build.bi.BuilderItemConstraints
import com.elementary.tasks.reminder.build.bi.ProcessedBuilderItems

class UiSelectorItemsAdapter(
  private val blockedByConstraintCalculator: BuilderItemBlockedByConstraintCalculator,
  private val permissionConstraintCalculator: BuilderItemPermissionConstraintCalculator,
  private val biErrorForUiAdapter: BiErrorForUiAdapter
) {

  fun calculateStates(
    used: List<BuilderItem<*>>,
    available: List<BuilderItem<*>>
  ): List<UiSelectorItem> {
    val usedMap = used.associateBy { it.biType }
    val processedBuilderItems = ProcessedBuilderItems(used)
    return available.map {
      val builderItemConstraints = BuilderItemConstraints(it.constraints)
      val errors = getErrors(builderItemConstraints, processedBuilderItems)
      val state = when {
        errors.isNotEmpty() -> {
          UiSelectorItemState.UiSelectorUnavailable(
            biErrorForUiAdapter.getUiString(errors)
          )
        }
        else -> UiSelectorItemState.UiSelectorAvailable
      }
      it.toUi(state)
    }.sortedBy { it.state !is UiSelectorItemState.UiSelectorAvailable }
  }

  private fun getErrors(
    item: BuilderItemConstraints,
    usedItems: ProcessedBuilderItems
  ): List<BuilderItemError> {
    val blockedBy = blockedByConstraintCalculator(item, usedItems)
      .takeIf { it.isNotEmpty() }
      ?.let { BuilderItemError.BlockedByConstraintError(it) }
//    val permissions = permissionConstraintCalculator.getPermissions(item)
//      .takeIf { it.isNotEmpty() }
//      ?.let { BuilderItemError.PermissionConstraintError(it) }

    return listOfNotNull(blockedBy)
  }

  private fun <T> BuilderItem<T>.toUi(state: UiSelectorItemState): UiSelectorItem {
    return UiSelectorItem(this, state)
  }
}
