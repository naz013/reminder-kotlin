package com.github.naz013.feature.reminder.build.logic

import com.github.naz013.testing.BaseTest
import com.github.naz013.feature.reminder.build.EmailBuilderItem
import com.github.naz013.feature.reminder.build.UiListBuilderItemState
import com.github.naz013.feature.reminder.build.adapter.BiErrorForUiAdapter
import com.github.naz013.feature.reminder.build.adapter.BiValueForUiAdapter
import com.github.naz013.domain.reminder.BiType
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UiBuilderItemsAdapterTest : BaseTest() {
  private val requiresAllConstraintCalculator = mockk<BuilderItemRequiresAllConstraintCalculator>()
  private val requiresAnyConstraintCalculator = mockk<BuilderItemRequiresAnyConstraintCalculator>()
  private val permissionConstraintCalculator = mockk<BuilderItemPermissionConstraintCalculator>()
  private val biValueForUiAdapter = mockk<BiValueForUiAdapter>(relaxed = true)
  private val biErrorForUiAdapter = mockk<BiErrorForUiAdapter>(relaxed = true)

  private lateinit var adapter: UiBuilderItemsAdapter

  @Before
  override fun setUp() {
    super.setUp()
    adapter =
      UiBuilderItemsAdapter(
        requiresAllConstraintCalculator,
        requiresAnyConstraintCalculator,
        permissionConstraintCalculator,
        biValueForUiAdapter,
        biErrorForUiAdapter,
      )
    every { requiresAllConstraintCalculator(any(), any()) } returns emptyList()
    every { requiresAnyConstraintCalculator(any(), any()) } returns emptyList()
    every { permissionConstraintCalculator(any()) } returns emptyList()
  }

  private fun emailItem() = EmailBuilderItem(title = "Email", description = null)

  @Test
  fun `a freshly added item with no value is EmptyState, not ErrorState`() {
    val item = emailItem()

    val result = adapter.calculateStates(listOf(item))

    assertEquals(UiListBuilderItemState.EmptyState, result.single().state)
    assertEquals("", result.single().errorText)
  }

  @Test
  fun `an item with an invalid value is ErrorState with a specific message`() {
    val item = emailItem()
    item.modifier.update("not-an-email")
    every { biErrorForUiAdapter.getInvalidValueMessage(BiType.EMAIL) } returns "Enter a valid email address"

    val result = adapter.calculateStates(listOf(item))

    assertTrue(result.single().state is UiListBuilderItemState.ErrorState)
    assertEquals("Enter a valid email address", result.single().errorText)
  }

  @Test
  fun `a valid value is DoneState`() {
    val item = emailItem()
    item.modifier.update("person@example.com")

    val result = adapter.calculateStates(listOf(item))

    assertEquals(UiListBuilderItemState.DoneState, result.single().state)
  }

  @Test
  fun `a constraint violation is ErrorState even without a value, using the constraint message`() {
    val item = emailItem()
    every { requiresAllConstraintCalculator(any(), any()) } returns listOf(BiType.DATE)
    every { biErrorForUiAdapter.getUiString(any()) } returns "Requires: Date"

    val result = adapter.calculateStates(listOf(item))

    assertTrue(result.single().state is UiListBuilderItemState.ErrorState)
    assertEquals("Requires: Date", result.single().errorText)
  }
}
