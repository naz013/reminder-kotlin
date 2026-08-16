package com.github.naz013.feature.reminder.build.logic

import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.NoteBuilderItem
import com.github.naz013.feature.reminder.build.UiBuilderItem
import com.github.naz013.feature.reminder.build.UiListBuilderItem
import com.github.naz013.feature.reminder.build.UiListBuilderItemState
import com.github.naz013.feature.reminder.build.UiListNoteBuilderItem
import com.github.naz013.feature.reminder.build.adapter.BiErrorForUiAdapter
import com.github.naz013.feature.reminder.build.adapter.BiValueForUiAdapter
import com.github.naz013.feature.reminder.build.bi.BuilderItemConstraints
import com.github.naz013.feature.reminder.build.bi.BuilderItemError
import com.github.naz013.feature.reminder.build.bi.ProcessedBuilderItems

class UiBuilderItemsAdapter(
  private val requiresAllConstraintCalculator: BuilderItemRequiresAllConstraintCalculator,
  private val requiresAnyConstraintCalculator: BuilderItemRequiresAnyConstraintCalculator,
  private val permissionConstraintCalculator: BuilderItemPermissionConstraintCalculator,
  private val biValueForUiAdapter: BiValueForUiAdapter,
  private val biErrorForUiAdapter: BiErrorForUiAdapter,
) {
  fun calculateStates(items: List<BuilderItem<*>>): List<UiBuilderItem> {
    val processedBuilderItems = ProcessedBuilderItems(items)

    return items.map {
      val builderItemConstraints = BuilderItemConstraints(it.constraints)
      val errors = getErrors(builderItemConstraints, processedBuilderItems)
      // A missing value (getValue() == null) takes priority over isCorrect(): most modifiers
      // define isCorrect() as "has a value", so without this a freshly-added, untouched item
      // (Date, Email, Place, ...) would render as an ERROR row instead of the neutral EMPTY one
      // meant for exactly that case. Once a value is present, isCorrect() can still fail on its
      // own merits (e.g. a malformed email, or an empty shopping list) - that's a real error, and
      // needs its own message since no constraint was violated to supply one.
      val state =
        when {
          errors.isNotEmpty() -> UiListBuilderItemState.ErrorState(errors)
          it.modifier.getValue() == null -> UiListBuilderItemState.EmptyState
          !it.modifier.isCorrect() -> UiListBuilderItemState.ErrorState(errors)
          else -> UiListBuilderItemState.DoneState
        }
      val errorText =
        when {
          errors.isNotEmpty() -> biErrorForUiAdapter.getUiString(errors)
          state is UiListBuilderItemState.ErrorState -> biErrorForUiAdapter.getInvalidValueMessage(it.biType)
          else -> ""
        }
      toUi(
        builderItem = it,
        state = state,
        value = biValueForUiAdapter.getUiRepresentation(it),
        errorText = errorText,
      )
    }
  }

  private fun getErrors(
    item: BuilderItemConstraints,
    items: ProcessedBuilderItems,
  ): List<BuilderItemError> {
    val requiresAll =
      requiresAllConstraintCalculator(item, items)
        .takeIf { it.isNotEmpty() }
        ?.let { BuilderItemError.RequiresAllConstraintError(it) }
    val requiresAny =
      requiresAnyConstraintCalculator(item, items)
        .takeIf { it.isNotEmpty() }
        ?.let { BuilderItemError.RequiresAnyConstraintError(it) }
    val permissions =
      permissionConstraintCalculator(item)
        .takeIf { it.isNotEmpty() }
        ?.let { BuilderItemError.PermissionConstraintError(it) }

    return listOfNotNull(
      requiresAll,
      requiresAny,
      permissions,
    )
  }

  private fun toUi(
    builderItem: BuilderItem<*>,
    state: UiListBuilderItemState,
    value: String,
    errorText: String,
  ): UiBuilderItem =
    when {
      builderItem is NoteBuilderItem -> {
        builderItem.modifier.getValue()?.let {
          UiListNoteBuilderItem(
            builderItem = builderItem,
            state = state,
            value = value,
            errorText = errorText,
            noteData = it,
          )
        } ?: UiListBuilderItem(
          builderItem = builderItem,
          state = state,
          value = value,
          errorText = errorText,
        )
      }

      else -> {
        UiListBuilderItem(
          builderItem = builderItem,
          state = state,
          value = value,
          errorText = errorText,
        )
      }
    }
}
