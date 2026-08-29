package com.github.naz013.feature.settings.headeritems

import com.github.naz013.domain.home.HeaderNavigationSection
import com.github.naz013.logic.routine.RoutineConfig
import com.github.naz013.logic.workflow.WorkflowConfig
import com.github.naz013.testing.BaseTest
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HeaderItemsSettingsViewModelTest : BaseTest() {
  private val preferences = mockk<HeaderItemsPreferences>()
  private val routineConfig = mockk<RoutineConfig>()
  private val workflowConfig = mockk<WorkflowConfig>()

  private var order: List<HeaderNavigationSection> = HeaderNavigationSection.configurable
  private var disabledSections: Set<HeaderNavigationSection> = emptySet()
  private var isRoutineEnabled = true
  private var isWorkflowEnabled = false

  private lateinit var viewModel: HeaderItemsSettingsViewModel

  @Before
  override fun setUp() {
    super.setUp()

    every { preferences.order } answers { order }
    every { preferences.order = any() } answers { order = firstArg() }
    every { preferences.disabledSections } answers { disabledSections }
    every { preferences.disabledSections = any() } answers { disabledSections = firstArg() }
    every { routineConfig.isEnabled } answers { isRoutineEnabled }
    every { workflowConfig.isEnabled } answers { isWorkflowEnabled }

    viewModel = HeaderItemsSettingsViewModel(preferences, routineConfig, workflowConfig)
  }

  @Test
  fun `pinned items are calendar and agenda, always enabled`() {
    val state = viewModel.state.value

    assertEquals(
      listOf(HeaderNavigationSection.CALENDAR, HeaderNavigationSection.AGENDA),
      state.pinnedItems.map { it.section },
    )
    assertEquals(true, state.pinnedItems.all { it.isEnabled })
  }

  @Test
  fun `configurable items exclude workflow when its config is disabled`() {
    val state = viewModel.state.value

    assertEquals(
      listOf(
        HeaderNavigationSection.NOTES,
        HeaderNavigationSection.BIRTHDAYS,
        HeaderNavigationSection.GOOGLE_TASKS,
        HeaderNavigationSection.GROUPS,
        HeaderNavigationSection.TAG,
        HeaderNavigationSection.ROUTINES,
      ),
      state.configurableItems.map { it.section },
    )
  }

  @Test
  fun `configurable items exclude routines when its config is disabled`() {
    isRoutineEnabled = false
    viewModel = HeaderItemsSettingsViewModel(preferences, routineConfig, workflowConfig)

    val state = viewModel.state.value

    assertEquals(
      listOf(
        HeaderNavigationSection.NOTES,
        HeaderNavigationSection.BIRTHDAYS,
        HeaderNavigationSection.GOOGLE_TASKS,
        HeaderNavigationSection.GROUPS,
        HeaderNavigationSection.TAG,
      ),
      state.configurableItems.map { it.section },
    )
  }

  @Test
  fun `configurable items include workflow when its config is enabled`() {
    isWorkflowEnabled = true
    viewModel = HeaderItemsSettingsViewModel(preferences, routineConfig, workflowConfig)

    val state = viewModel.state.value

    assertEquals(
      listOf(
        HeaderNavigationSection.NOTES,
        HeaderNavigationSection.BIRTHDAYS,
        HeaderNavigationSection.GOOGLE_TASKS,
        HeaderNavigationSection.GROUPS,
        HeaderNavigationSection.TAG,
        HeaderNavigationSection.ROUTINES,
        HeaderNavigationSection.WORKFLOW,
      ),
      state.configurableItems.map { it.section },
    )
  }

  @Test
  fun `onToggle disabling a section updates state and persists it`() {
    viewModel.onToggle(HeaderNavigationSection.GOOGLE_TASKS, enabled = false)

    assertEquals(setOf(HeaderNavigationSection.GOOGLE_TASKS), disabledSections)
    val row = viewModel.state.value.configurableItems.first { it.section == HeaderNavigationSection.GOOGLE_TASKS }
    assertEquals(false, row.isEnabled)
  }

  @Test
  fun `onToggle re-enabling a section removes it from the disabled set`() {
    disabledSections = setOf(HeaderNavigationSection.GOOGLE_TASKS)
    viewModel = HeaderItemsSettingsViewModel(preferences, routineConfig, workflowConfig)

    viewModel.onToggle(HeaderNavigationSection.GOOGLE_TASKS, enabled = true)

    assertEquals(emptySet<HeaderNavigationSection>(), disabledSections)
  }

  @Test
  fun `onReorder moves an item and persists the new order`() {
    // Notes, Birthdays, Google Tasks, Groups, Tag, Routines -> move Routines (index 5) to the front
    viewModel.onReorder(fromIndex = 5, toIndex = 0)

    assertEquals(
      listOf(
        HeaderNavigationSection.ROUTINES,
        HeaderNavigationSection.NOTES,
        HeaderNavigationSection.BIRTHDAYS,
        HeaderNavigationSection.GOOGLE_TASKS,
        HeaderNavigationSection.GROUPS,
        HeaderNavigationSection.TAG,
      ),
      viewModel.state.value.configurableItems.map { it.section },
    )
    // Workflow isn't currently visible (its config is disabled), so it keeps its old relative
    // position tacked onto the end of the persisted order instead of being dropped.
    assertEquals(
      listOf(
        HeaderNavigationSection.ROUTINES,
        HeaderNavigationSection.NOTES,
        HeaderNavigationSection.BIRTHDAYS,
        HeaderNavigationSection.GOOGLE_TASKS,
        HeaderNavigationSection.GROUPS,
        HeaderNavigationSection.TAG,
        HeaderNavigationSection.WORKFLOW,
      ),
      order,
    )
  }

  @Test
  fun `onReorder with an out-of-range index is a no-op`() {
    val before = viewModel.state.value.configurableItems

    viewModel.onReorder(fromIndex = -1, toIndex = 0)

    assertEquals(before, viewModel.state.value.configurableItems)
  }
}
