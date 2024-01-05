package com.elementary.tasks.reminder.build.logic

import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.UiListBuilderItem
import com.elementary.tasks.reminder.build.UiLitBuilderItemState
import com.elementary.tasks.reminder.build.adapter.BiErrorForUiAdapter
import com.elementary.tasks.reminder.build.adapter.BiValueForUiAdapter
import com.elementary.tasks.reminder.build.bi.BuilderItemConstraints
import com.elementary.tasks.reminder.build.bi.BuilderItemError
import com.elementary.tasks.reminder.build.bi.ProcessedBuilderItems

class UiBuilderItemsAdapter(
  private val requiresAllConstraintCalculator: BuilderItemRequiresAllConstraintCalculator,
  private val requiresAnyConstraintCalculator: BuilderItemRequiresAnyConstraintCalculator,
  private val permissionConstraintCalculator: BuilderItemPermissionConstraintCalculator,
  private val biValueForUiAdapter: BiValueForUiAdapter,
  private val biErrorForUiAdapter: BiErrorForUiAdapter
) {

  fun calculateStates(items: List<BuilderItem<*>>): List<UiListBuilderItem> {
    val typeValueMap = items.associateBy { it.biType }
    val processedBuilderItems = ProcessedBuilderItems(items)

    return items.map {
      val builderItemConstraints = BuilderItemConstraints(it.constraints)
      val errors = getErrors(builderItemConstraints, processedBuilderItems)
      val state = when {
        errors.isNotEmpty() || !it.modifier.isCorrect() -> UiLitBuilderItemState.ErrorState(errors)
        it.modifier.getValue() == null -> UiLitBuilderItemState.EmptyState
        it.modifier.isCorrect() -> UiLitBuilderItemState.DoneState
        else -> UiLitBuilderItemState.EmptyState
      }
      it.toUi(
        state = state,
        value = biValueForUiAdapter.getUiRepresentation(it),
        errorText = biErrorForUiAdapter.getUiString(errors)
      )
    }
  }

  private fun getErrors(
    item: BuilderItemConstraints,
    items: ProcessedBuilderItems
  ): List<BuilderItemError> {
    val requiresAll = requiresAllConstraintCalculator(item, items)
      .takeIf { it.isNotEmpty() }
      ?.let { BuilderItemError.RequiresAllConstraintError(it) }
    val requiresAny = requiresAnyConstraintCalculator(item, items)
      .takeIf { it.isNotEmpty() }
      ?.let { BuilderItemError.RequiresAnyConstraintError(it) }
    val permissions = permissionConstraintCalculator(item)
      .takeIf { it.isNotEmpty() }
      ?.let { BuilderItemError.PermissionConstraintError(it) }

    return listOfNotNull(
      requiresAll,
      requiresAny,
      permissions
    )
  }

  private fun <T> BuilderItem<T>.toUi(
    state: UiLitBuilderItemState,
    value: String,
    errorText: String
  ): UiListBuilderItem {
    return UiListBuilderItem(
      builderItem = this,
      state = state,
      value = value,
      errorText = errorText
    )
  }
}
