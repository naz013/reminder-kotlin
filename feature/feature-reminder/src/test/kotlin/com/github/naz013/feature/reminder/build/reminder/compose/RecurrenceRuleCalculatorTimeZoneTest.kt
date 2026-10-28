package com.github.naz013.feature.reminder.build.reminder.compose

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.DateBuilderItem
import com.github.naz013.feature.reminder.build.TimeBuilderItem
import com.github.naz013.feature.reminder.build.TimeZoneBuilderItem
import com.github.naz013.feature.reminder.build.bi.ProcessedBuilderItems
import com.github.naz013.feature.reminder.build.reminder.ICalDateTimeCalculator
import com.github.naz013.feature.reminder.build.timezone.TimeZoneCatalog
import com.github.naz013.testing.BaseTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId

class RecurrenceRuleCalculatorTimeZoneTest : BaseTest() {
  private val dateTimeManager = mockk<DateTimeManager>()
  private val berlin = ZoneId.of("Europe/Berlin")
  private val tokyo = ZoneId.of("Asia/Tokyo")

  private lateinit var calculator: RecurrenceRuleCalculator

  @Before
  override fun setUp() {
    super.setUp()
    every { dateTimeManager.localToUtc(any()) } answers { firstArg() }
    every { dateTimeManager.localToUtc(any(), berlin) } answers { firstArg<LocalDateTime>().minusHours(2) }
    every { dateTimeManager.localToUtc(any(), tokyo) } answers { firstArg<LocalDateTime>().minusHours(9) }
    calculator =
      RecurrenceRuleCalculator(dateTimeManager, mockk<ICalDateTimeCalculator>(), mockk<RecurrenceCalculator>())
  }

  @Test
  fun `resolves the picked wall-clock time in the fixed zone`() {
    val result = calculator(itemsOf(dateItem(), timeItem(), zoneItem("Europe/Berlin")))

    assertEquals(WALL_CLOCK.minusHours(2), result?.schedule?.eventDateTime)
  }

  @Test
  fun `changing the zone keeps the wall-clock and moves the instant`() {
    val inBerlin = calculator(itemsOf(dateItem(), timeItem(), zoneItem("Europe/Berlin")))
    val inTokyo = calculator(itemsOf(dateItem(), timeItem(), zoneItem("Asia/Tokyo")))

    verify { dateTimeManager.localToUtc(WALL_CLOCK, berlin) }
    verify { dateTimeManager.localToUtc(WALL_CLOCK, tokyo) }
    assertEquals(WALL_CLOCK.minusHours(2), inBerlin?.schedule?.eventDateTime)
    assertEquals(WALL_CLOCK.minusHours(9), inTokyo?.schedule?.eventDateTime)
  }

  @Test
  fun `uses the device zone when the zone item has no value`() {
    val result = calculator(itemsOf(dateItem(), timeItem(), zoneItem(null)))

    assertEquals(WALL_CLOCK, result?.schedule?.eventDateTime)
    verify(exactly = 0) { dateTimeManager.localToUtc(any(), any()) }
  }

  @Test
  fun `uses the device zone when there is no zone item`() {
    val result = calculator(itemsOf(dateItem(), timeItem()))

    assertEquals(WALL_CLOCK, result?.schedule?.eventDateTime)
    verify(exactly = 0) { dateTimeManager.localToUtc(any(), any()) }
  }

  private fun itemsOf(vararg items: BuilderItem<*>) = ProcessedBuilderItems(items.toList())

  private fun dateItem() =
    DateBuilderItem(title = "d", description = null, dateFormatter = mockk(relaxed = true)).apply {
      modifier.update(WALL_CLOCK.toLocalDate())
    }

  private fun timeItem() =
    TimeBuilderItem(title = "t", description = null, timeFormatter = mockk(relaxed = true)).apply {
      modifier.update(WALL_CLOCK.toLocalTime())
    }

  private fun zoneItem(zoneId: String?) =
    TimeZoneBuilderItem(title = "z", description = null, catalog = TimeZoneCatalog("Device")).apply {
      modifier.update(zoneId)
    }

  private companion object {
    val WALL_CLOCK: LocalDateTime = LocalDateTime.of(LocalDate.of(2026, 7, 24), LocalTime.of(9, 0))
  }
}
