package com.elementary.tasks.reminder.build.valuedialog.controller.shopitems

import com.elementary.tasks.BaseTest
import com.elementary.tasks.TestableObserver
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.ShopItem
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SubTasksViewModelTest : BaseTest() {
  private val dateTimeManager = mockk<DateTimeManager>()

  private lateinit var viewModel: SubTasksViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { dateTimeManager.getNowGmtDateTime() } returns "2026-07-23T00:00"
    viewModel = SubTasksViewModel(dateTimeManager)
    // saveItems is a SingleLiveEvent (a MediatorLiveData over an internal source): it only starts
    // forwarding values from that source once it has at least one active observer, so an
    // observer must be attached up front for `.value` reads below to see anything.
    viewModel.saveItems.observeForever {}
  }

  private fun shopItem(
    summary: String,
    createTime: String = "t",
    isChecked: Boolean = false,
  ) = ShopItem(summary = summary, createTime = createTime, isChecked = isChecked)

  @Test
  fun `initWithData with an empty list creates a single blank item`() {
    viewModel.initWithData(emptyList())

    val items = viewModel.showItems.value
    assertEquals(1, items?.size)
    assertEquals("", items?.get(0)?.summary)
    assertEquals("2026-07-23T00:00", items?.get(0)?.createTime)
    assertEquals(items, viewModel.saveItems.value)
  }

  @Test
  fun `initWithData with items resets position, showInput and canRemove`() {
    val a = shopItem("A").apply { position = 9; showInput = true; canRemove = true }
    val b = shopItem("B").apply { position = 3; showInput = true; canRemove = true }

    viewModel.initWithData(listOf(a, b))

    val items = viewModel.showItems.value!!
    assertEquals(listOf(0, 1), items.map { it.position })
    assertTrue(items.none { it.showInput })
    assertTrue(items.none { it.canRemove })
  }

  @Test
  fun `getNonEmptyItems returns empty list when the only item is blank`() {
    viewModel.initWithData(listOf(shopItem("")))

    assertEquals(emptyList<ShopItem>(), viewModel.getNonEmptyItems())
  }

  @Test
  fun `getNonEmptyItems drops the trailing item when it is blank`() {
    viewModel.initWithData(listOf(shopItem("Milk"), shopItem("")))

    assertEquals(listOf("Milk"), viewModel.getNonEmptyItems().map { it.summary })
  }

  @Test
  fun `getNonEmptyItems returns all items when the last one is not blank`() {
    viewModel.initWithData(listOf(shopItem("Milk"), shopItem("Eggs")))

    assertEquals(listOf("Milk", "Eggs"), viewModel.getNonEmptyItems().map { it.summary })
  }

  @Test
  fun `onTextChanged updates the item summary and posts only to saveItems`() {
    val showObserver = TestableObserver<List<ShopItem>>()
    viewModel.showItems.observeForever(showObserver)
    viewModel.initWithData(listOf(shopItem("")))
    val emissionsAfterInit = showObserver.numberOfEmissions()

    viewModel.onTextChanged(0, "Buy milk")

    assertEquals("Buy milk", viewModel.saveItems.value?.get(0)?.summary)
    assertEquals(emissionsAfterInit, showObserver.numberOfEmissions())
  }

  @Test
  fun `onTextChanged does nothing when position is out of bounds`() {
    viewModel.initWithData(listOf(shopItem("Milk")))
    val before = viewModel.showItems.value

    viewModel.onTextChanged(5, "ignored")

    assertSame(before, viewModel.showItems.value)
  }

  @Test
  fun `onEnterPressed appends a new item when pressed on the last position`() {
    viewModel.initWithData(listOf(shopItem("Milk")))

    viewModel.onEnterPressed(0)

    val items = viewModel.showItems.value!!
    assertEquals(2, items.size)
    assertEquals("Milk", items[0].summary)
    assertEquals(0, items[0].position)
    assertEquals(false, items[0].showInput)
    assertEquals("", items[1].summary)
    assertEquals(1, items[1].position)
    assertEquals(true, items[1].showInput)
  }

  @Test
  fun `onEnterPressed inserts a new item after the given position and shifts the rest`() {
    viewModel.initWithData(listOf(shopItem("A"), shopItem("B"), shopItem("C")))

    viewModel.onEnterPressed(0)

    val items = viewModel.showItems.value!!
    assertEquals(listOf("A", "", "B", "C"), items.map { it.summary })
    assertEquals(listOf(0, 1, 2, 3), items.map { it.position })
    assertEquals(true, items[1].showInput)
    assertTrue(items.filterIndexed { index, _ -> index != 1 }.none { it.showInput })
  }

  @Test
  fun `onEnterPressed does nothing when position is out of bounds`() {
    viewModel.initWithData(listOf(shopItem("Milk")))
    val before = viewModel.showItems.value

    viewModel.onEnterPressed(5)

    assertSame(before, viewModel.showItems.value)
  }

  @Test
  fun `onDeletePressed removes the item and marks the previous item as active`() {
    viewModel.initWithData(listOf(shopItem("A"), shopItem("B"), shopItem("C")))

    viewModel.onDeletePressed(1)

    val items = viewModel.showItems.value!!
    assertEquals(listOf("A", "C"), items.map { it.summary })
    assertEquals(listOf(0, 1), items.map { it.position })
    assertEquals(true, items[0].showInput)
    assertEquals(true, items[0].canRemove)
  }

  @Test
  fun `onDeletePressed marks the remaining single item as active when deleting the first`() {
    viewModel.initWithData(listOf(shopItem("A"), shopItem("B")))

    viewModel.onDeletePressed(0)

    val items = viewModel.showItems.value!!
    assertEquals(listOf("B"), items.map { it.summary })
    assertEquals(true, items[0].showInput)
    assertEquals(true, items[0].canRemove)
  }

  @Test
  fun `onDeletePressed does nothing when it is the only item`() {
    viewModel.initWithData(listOf(shopItem("A")))
    val before = viewModel.showItems.value

    viewModel.onDeletePressed(0)

    assertSame(before, viewModel.showItems.value)
  }

  @Test
  fun `onDeletePressed does nothing when position is out of bounds`() {
    viewModel.initWithData(listOf(shopItem("A"), shopItem("B")))
    val before = viewModel.showItems.value

    viewModel.onDeletePressed(5)

    assertSame(before, viewModel.showItems.value)
  }

  @Test
  fun `onRemovePressed adds a blank item back when removing the only item`() {
    viewModel.initWithData(listOf(shopItem("A")))

    viewModel.onRemovePressed(0)

    val items = viewModel.showItems.value!!
    assertEquals(1, items.size)
    assertEquals("", items[0].summary)
    assertEquals(0, items[0].position)
  }

  @Test
  fun `onRemovePressed activates the new last item when the removed item had input focus`() {
    viewModel.initWithData(listOf(shopItem("A"), shopItem("B")))
    // Pressing enter on the last position (1) appends a new, focused (showInput = true) blank
    // item at the end: [A, B, new(focused)].
    viewModel.onEnterPressed(1)
    val itemsBeforeRemoval = viewModel.showItems.value!!
    assertEquals(3, itemsBeforeRemoval.size)
    assertEquals(true, itemsBeforeRemoval.last().showInput)

    // Removing that focused trailing item should hand focus to the new last item (B).
    viewModel.onRemovePressed(2)

    val items = viewModel.showItems.value!!
    assertEquals(listOf("A", "B"), items.map { it.summary })
    assertEquals(true, items[items.size - 1].showInput)
  }

  @Test
  fun `onRemovePressed does nothing when position is out of bounds`() {
    viewModel.initWithData(listOf(shopItem("A")))
    val before = viewModel.showItems.value

    viewModel.onRemovePressed(5)

    assertSame(before, viewModel.showItems.value)
  }

  @Test
  fun `onCheckPressed toggles checked state without reordering the list`() {
    viewModel.initWithData(listOf(shopItem("A", createTime = "t1"), shopItem("B", createTime = "t2")))

    viewModel.onCheckPressed(0)

    val items = viewModel.showItems.value!!
    assertEquals(listOf("A", "B"), items.map { it.summary })
    assertEquals(true, items[0].isChecked)
    assertEquals(listOf(0, 1), items.map { it.position })
  }

  @Test
  fun `onCheckPressed does nothing when position is out of bounds`() {
    viewModel.initWithData(listOf(shopItem("A")))
    val before = viewModel.showItems.value

    viewModel.onCheckPressed(5)

    assertSame(before, viewModel.showItems.value)
  }
}
