package com.github.naz013.feature.reminder.build.logic

import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.UiSelectorItem
import com.github.naz013.feature.reminder.build.UiSelectorItemState
import com.github.naz013.feature.reminder.build.adapter.BiErrorForUiAdapter
import com.github.naz013.feature.reminder.build.bi.BuilderItemConstraints
import com.github.naz013.feature.reminder.build.bi.BuilderItemError
import com.github.naz013.feature.reminder.build.bi.ProcessedBuilderItems
import com.github.naz013.domain.reminder.BiType

internal class UiSelectorItemsAdapter(
  private val blockedByConstraintCalculator: BuilderItemBlockedByConstraintCalculator,
  private val permissionConstraintCalculator: BuilderItemPermissionConstraintCalculator,
  private val mandatoryIfConstraintCalculator: BuilderItemMandatoryIfConstraintCalculator,
  private val biErrorForUiAdapter: BiErrorForUiAdapter,
) {
  fun calculateStates(
    used: List<BuilderItem<*>>,
    available: List<BuilderItem<*>>,
  ): List<UiSelectorItem> {
    val processedBuilderItems = ProcessedBuilderItems(used)
    val requiredByMap = calculateRequiredByMap(used, processedBuilderItems)

    return available
      .map {
        val builderItemConstraints = BuilderItemConstraints(it.constraints)
        val errors = getErrors(builderItemConstraints, processedBuilderItems)
        val state =
          when {
            errors.isNotEmpty() -> {
              UiSelectorItemState.UiSelectorUnavailable(
                biErrorForUiAdapter.getUiString(errors),
              )
            }
            else -> UiSelectorItemState.UiSelectorAvailable
          }
        val requiredMessage =
          requiredByMap[it.biType]?.let { requiredBy ->
            biErrorForUiAdapter.getUiString(listOf(BuilderItemError.MandatoryIfConstraintError(requiredBy)))
          }
        it.toUi(state, requiredMessage)
      }.sortedBy { it.state !is UiSelectorItemState.UiSelectorAvailable }
  }

  /**
   * For every used item that declares `mandatoryIf(x)`, and whose `x` isn't used yet, `x` is now
   * mandatory - e.g. adding Date makes Time mandatory (and vice versa, since the pairing is
   * declared on both sides). Returns a map of "not-yet-used, now-mandatory type" to the used
   * type(s) that triggered it, so the selector can show *why* an item became required.
   */
  private fun calculateRequiredByMap(
    used: List<BuilderItem<*>>,
    processedBuilderItems: ProcessedBuilderItems,
  ): Map<BiType, List<BiType>> {
    val result = mutableMapOf<BiType, MutableList<BiType>>()
    used.forEach { usedItem ->
      val constraints = BuilderItemConstraints(usedItem.constraints)
      mandatoryIfConstraintCalculator(constraints, processedBuilderItems).forEach { requiredType ->
        result.getOrPut(requiredType) { mutableListOf() }.add(usedItem.biType)
      }
    }
    return result
  }

  private fun getErrors(
    item: BuilderItemConstraints,
    usedItems: ProcessedBuilderItems,
  ): List<BuilderItemError> {
    val blockedBy =
      blockedByConstraintCalculator(item, usedItems)
        .takeIf { it.isNotEmpty() }
        ?.let { BuilderItemError.BlockedByConstraintError(it) }
//    val permissions = permissionConstraintCalculator.getPermissions(item)
//      .takeIf { it.isNotEmpty() }
//      ?.let { BuilderItemError.PermissionConstraintError(it) }

    return listOfNotNull(blockedBy)
  }

  private fun <T> BuilderItem<T>.toUi(
    state: UiSelectorItemState,
    requiredMessage: String?,
  ): UiSelectorItem = UiSelectorItem(this, state, requiredMessage)
}
