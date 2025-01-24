package com.elementary.tasks.reminder.build.logic

import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.NoteBuilderItem
import com.elementary.tasks.reminder.build.UiBuilderItem
import com.elementary.tasks.reminder.build.UiListBuilderItem
import com.elementary.tasks.reminder.build.UiListNoteBuilderItem
import com.elementary.tasks.reminder.build.UiListBuilderItemState
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

  fun calculateStates(items: List<BuilderItem<*>>): List<UiBuilderItem> {
    val processedBuilderItems = ProcessedBuilderItems(items)

    return items.map {
      val builderItemConstraints = BuilderItemConstraints(it.constraints)
      val errors = getErrors(builderItemConstraints, processedBuilderItems)
      val state = when {
        errors.isNotEmpty() || !it.modifier.isCorrect() -> UiListBuilderItemState.ErrorState(errors)
        it.modifier.getValue() == null -> UiListBuilderItemState.EmptyState
        it.modifier.isCorrect() -> UiListBuilderItemState.DoneState
        else -> UiListBuilderItemState.EmptyState
      }
      toUi(
        builderItem = it,
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

  private fun toUi(
    builderItem: BuilderItem<*>,
    state: UiListBuilderItemState,
    value: String,
    errorText: String
  ): UiBuilderItem {
    return when {
      builderItem is NoteBuilderItem -> {
        builderItem.modifier.getValue()?.let {
          UiListNoteBuilderItem(
            builderItem = builderItem,
            state = state,
            value = value,
            errorText = errorText,
            noteData = it
          )
        } ?: UiListBuilderItem(
          builderItem = builderItem,
          state = state,
          value = value,
          errorText = errorText
        )
      }

      else -> {
        UiListBuilderItem(
          builderItem = builderItem,
          state = state,
          value = value,
          errorText = errorText
        )
      }
    }
  }
}
