package com.github.naz013.feature.reminder.build.logic

import com.github.naz013.domain.reminder.BiType
import com.github.naz013.feature.reminder.build.DateBuilderItem
import com.github.naz013.feature.reminder.build.TimeZoneBuilderItem
import com.github.naz013.feature.reminder.build.TimerBuilderItem
import com.github.naz013.feature.reminder.build.bi.BuilderItemConstraints
import com.github.naz013.feature.reminder.build.bi.ProcessedBuilderItems
import com.github.naz013.feature.reminder.build.timezone.TimeZoneCatalog
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeZoneConstraintsTest {
  private val zoneItem = TimeZoneBuilderItem(title = "z", description = null, catalog = TimeZoneCatalog("Device"))
  private val constraints = BuilderItemConstraints(zoneItem.constraints)

  @Test
  fun `is unavailable next to a countdown timer`() {
    val timer = TimerBuilderItem(title = "t", description = null, timerFormatter = mockk(relaxed = true))

    val blockedBy = BuilderItemBlockedByConstraintCalculator()(constraints, ProcessedBuilderItems(listOf(timer)))

    assertEquals(listOf(BiType.COUNTDOWN_TIMER), blockedBy)
  }

  @Test
  fun `is available next to a date`() {
    val date = DateBuilderItem(title = "d", description = null, dateFormatter = mockk(relaxed = true))

    val blockedBy = BuilderItemBlockedByConstraintCalculator()(constraints, ProcessedBuilderItems(listOf(date)))

    assertTrue(blockedBy.isEmpty())
  }

  @Test
  fun `requires a date based or recurring item`() {
    val missing = BuilderItemRequiresAnyConstraintCalculator()(constraints, ProcessedBuilderItems(emptyList()))
    val date = DateBuilderItem(title = "d", description = null, dateFormatter = mockk(relaxed = true))
    val satisfied = BuilderItemRequiresAnyConstraintCalculator()(constraints, ProcessedBuilderItems(listOf(date)))

    assertTrue(missing.isNotEmpty())
    assertTrue(satisfied.isEmpty())
  }
}
