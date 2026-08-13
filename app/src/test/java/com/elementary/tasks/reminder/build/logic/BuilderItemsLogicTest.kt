package com.elementary.tasks.reminder.build.logic

import com.elementary.tasks.BaseTest
import com.elementary.tasks.reminder.build.DateBuilderItem
import com.elementary.tasks.reminder.build.EmailBuilderItem
import com.elementary.tasks.reminder.build.TimeBuilderItem
import com.elementary.tasks.reminder.build.formatter.datetime.DateFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.TimeFormatter
import com.github.naz013.domain.reminder.BiType
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BuilderItemsLogicTest : BaseTest() {
  private val builderItemsHolder = BuilderItemsHolder()
  private lateinit var logic: BuilderItemsLogic

  @Before
  override fun setUp() {
    super.setUp()
    logic = BuilderItemsLogic(builderItemsHolder)
  }

  private fun dateItem(formatter: DateFormatter = DateFormatter(mockk(relaxed = true))) =
    DateBuilderItem(title = "Date", description = null, dateFormatter = formatter)

  private fun timeItem(formatter: TimeFormatter = TimeFormatter(mockk(relaxed = true))) =
    TimeBuilderItem(title = "Time", description = null, timeFormatter = formatter)

  private fun emailItem() = EmailBuilderItem(title = "Email", description = null)

  @Test
  fun `an item is available when nothing has been used`() {
    logic.setAllAvailable(listOf(dateItem(), emailItem()))

    val available = logic.getAvailable()

    assertTrue(available.any { it.biType == BiType.DATE })
    assertTrue(available.any { it.biType == BiType.EMAIL })
  }

  @Test
  fun `a used item is excluded from available even when the pool instance is object-unequal`() {
    // Pool item and used item share biType == DATE but are built with distinct DateFormatter
    // instances (as happens in the app: the pool is built once in initBuilder(), while Quick
    // Start/preset flows build their own item via a separate BiFactory call). Data-class equals()
    // on DateBuilderItem compares the formatter by reference, so the two are never equal - this
    // used to leave "Date" visible in the "+" dialog after it was already added.
    val pool = dateItem()
    val used = dateItem()
    logic.setAllAvailable(listOf(pool, emailItem()))
    logic.setAll(listOf(used))

    val available = logic.getAvailable()

    assertFalse(available.any { it.biType == BiType.DATE })
    assertTrue(available.any { it.biType == BiType.EMAIL })
  }

  @Test
  fun `a used item added via addNew is excluded from available`() {
    val pool = timeItem()
    logic.setAllAvailable(listOf(pool, emailItem()))

    logic.addNew(timeItem())

    val available = logic.getAvailable()

    assertFalse(available.any { it.biType == BiType.TIME })
    assertTrue(available.any { it.biType == BiType.EMAIL })
  }

  @Test
  fun `canAdd is false once every pool item has been used`() {
    logic.setAllAvailable(listOf(dateItem()))

    logic.addNew(dateItem())

    assertFalse(logic.canAdd())
  }

  @Test
  fun `addNew ignores a second item with a biType already used`() {
    // Simulates a fast double-tap on the selector sheet firing addNew twice for the same biType
    // before the sheet recomposes - getUsed() must never contain two items with the same biType,
    // since BuildReminderScreen keys its LazyColumn rows by biType.
    logic.setAllAvailable(listOf(dateItem(), emailItem()))

    logic.addNew(dateItem())
    logic.addNew(dateItem())

    assertEquals(1, logic.getUsed().count { it.biType == BiType.DATE })
  }

  @Test
  fun `setAll dedupes items with the same biType`() {
    // Simulates decomposing a persisted reminder whose recurrence rule failed to parse and fell
    // back to a different recurrence type than the one its saved builderScheme was written for
    // (see ReminderToBiDecomposer) - the resulting list can contain a duplicate biType, which
    // must not reach getUsed() since BuildReminderScreen keys its LazyColumn rows by biType.
    logic.setAll(listOf(dateItem(), timeItem(), dateItem()))

    assertEquals(1, logic.getUsed().count { it.biType == BiType.DATE })
    assertEquals(2, logic.getUsed().size)
  }
}
