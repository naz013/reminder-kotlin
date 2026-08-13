package com.github.naz013.ui.common.selection

import org.junit.Assert.assertEquals
import org.junit.Test

private data class Item(
  override val id: String,
  override val isSelected: Boolean = false,
) : Selectable<Item> {
  override fun withSelected(selected: Boolean) = copy(isSelected = selected)
}

class SelectableTest {

  @Test
  fun `select marks only the matching item as selected`() {
    val items = listOf(Item("1"), Item("2"), Item("3"))

    val result = items.select("2")

    assertEquals(listOf(false, true, false), result.map { it.isSelected })
  }

  @Test
  fun `select is a no-op when the id is not found`() {
    val items = listOf(Item("1"), Item("2"))

    val result = items.select("missing")

    assertEquals(items, result)
  }

  @Test
  fun `select on an already-selected item keeps it selected`() {
    val items = listOf(Item("1", isSelected = true))

    val result = items.select("1")

    assertEquals(true, result.single().isSelected)
  }

  @Test
  fun `toggleSelection flips only the matching item`() {
    val items = listOf(Item("1", isSelected = true), Item("2"))

    val result = items.toggleSelection("1")

    assertEquals(listOf(false, false), result.map { it.isSelected })
  }

  @Test
  fun `toggleSelection selects an unselected item`() {
    val items = listOf(Item("1"), Item("2"))

    val result = items.toggleSelection("2")

    assertEquals(listOf(false, true), result.map { it.isSelected })
  }

  @Test
  fun `clearSelection deselects every item`() {
    val items = listOf(Item("1", isSelected = true), Item("2", isSelected = true), Item("3"))

    val result = items.clearSelection()

    assertEquals(listOf(false, false, false), result.map { it.isSelected })
  }

  @Test
  fun `selectedCount counts only selected items`() {
    val items = listOf(Item("1", isSelected = true), Item("2"), Item("3", isSelected = true))

    assertEquals(2, items.selectedCount())
  }

  @Test
  fun `selectedIds returns the ids of selected items only`() {
    val items = listOf(Item("1", isSelected = true), Item("2"), Item("3", isSelected = true))

    assertEquals(setOf("1", "3"), items.selectedIds())
  }

  @Test
  fun `selectedIds is empty when nothing is selected`() {
    val items = listOf(Item("1"), Item("2"))

    assertEquals(emptySet<String>(), items.selectedIds())
  }
}
