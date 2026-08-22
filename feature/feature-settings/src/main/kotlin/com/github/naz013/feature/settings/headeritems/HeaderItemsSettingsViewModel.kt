package com.github.naz013.feature.settings.headeritems

import androidx.lifecycle.ViewModel
import com.github.naz013.domain.home.HeaderNavigationSection
import com.github.naz013.logic.routine.RoutineConfig
import com.github.naz013.logic.workflow.WorkflowConfig
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.icon.DrawableCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal class HeaderItemsSettingsViewModel(
  private val preferences: HeaderItemsPreferences,
  private val routineConfig: RoutineConfig,
) : ViewModel() {
  val state: StateFlow<HeaderItemsSettingsState> field = MutableStateFlow(buildState())

  fun onToggle(
    section: HeaderNavigationSection,
    enabled: Boolean,
  ) {
    val disabled = preferences.disabledSections.toMutableSet()
    if (enabled) disabled.remove(section) else disabled.add(section)
    preferences.disabledSections = disabled
    state.update { buildState() }
  }

  fun onReorder(
    fromIndex: Int,
    toIndex: Int,
  ) {
    val current = state.value.configurableItems.toMutableList()
    if (fromIndex !in current.indices || toIndex !in current.indices) return
    val moved = current.removeAt(fromIndex)
    current.add(toIndex, moved)

    val newVisibleOrder = current.map { it.section }
    preferences.order = newVisibleOrder + preferences.order.filterNot { it in newVisibleOrder }
    state.update { it.copy(configurableItems = current) }
  }

  private fun buildState(): HeaderItemsSettingsState {
    val disabled = preferences.disabledSections
    val visibleConfigurableSections = preferences.order.filter { isAvailable(it) }
    return HeaderItemsSettingsState(
      pinnedItems = HeaderNavigationSection.pinned.map { section -> rowFor(section, isEnabled = true) },
      configurableItems =
        visibleConfigurableSections.map { section ->
          rowFor(section, isEnabled = section !in disabled)
        },
    )
  }

  private fun isAvailable(section: HeaderNavigationSection): Boolean = when (section) {
    HeaderNavigationSection.ROUTINES -> routineConfig.isEnabled
    HeaderNavigationSection.WORKFLOW -> WorkflowConfig.isEnabled
    else -> true
  }

  private fun rowFor(
    section: HeaderNavigationSection,
    isEnabled: Boolean,
  ): HeaderItemRow = when (section) {
    HeaderNavigationSection.CALENDAR ->
      HeaderItemRow(section, R.string.calendar, DrawableCatalog.Fluent.Calendar, isEnabled)
    HeaderNavigationSection.AGENDA ->
      HeaderItemRow(section, R.string.agenda, DrawableCatalog.Fluent.Timeline, isEnabled)
    HeaderNavigationSection.NOTES ->
      HeaderItemRow(section, R.string.notes, DrawableCatalog.Fluent.Note, isEnabled)
    HeaderNavigationSection.GOOGLE_TASKS ->
      HeaderItemRow(section, R.string.google_tasks, DrawableCatalog.Builder.GoogleTaskList, isEnabled)
    HeaderNavigationSection.GROUPS ->
      HeaderItemRow(section, R.string.groups, DrawableCatalog.Fluent.Group, isEnabled)
    HeaderNavigationSection.ROUTINES ->
      HeaderItemRow(section, R.string.routines, DrawableCatalog.Builder.Timer, isEnabled)
    HeaderNavigationSection.WORKFLOW ->
      HeaderItemRow(section, R.string.workflow_automations, DrawableCatalog.Fluent.ArrowRepeatAll, isEnabled)
  }
}
