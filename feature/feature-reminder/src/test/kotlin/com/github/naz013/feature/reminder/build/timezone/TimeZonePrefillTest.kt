package com.github.naz013.feature.reminder.build.timezone

import com.github.naz013.feature.reminder.build.DateBuilderItem
import com.github.naz013.feature.reminder.build.ICalStartDateBuilderItem
import com.github.naz013.feature.reminder.build.SummaryBuilderItem
import com.github.naz013.feature.reminder.build.TimeZoneBuilderItem
import com.github.naz013.logic.reminder.ReminderPreferences
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.ZoneId

class TimeZonePrefillTest {
  private val preferences = mockk<ReminderPreferences>()
  private val prefill = TimePrefillFactory.create(preferences)

  @Test
  fun `does nothing when new reminders are floating`() {
    every { preferences.useFixedZoneForNewReminders } returns false

    assertNull(prefill.zoneIdToPrefill(listOf(dateItem())))
  }

  @Test
  fun `pre-fills the device zone once a date item is present`() {
    every { preferences.useFixedZoneForNewReminders } returns true

    assertEquals("Europe/Berlin", prefill.zoneIdToPrefill(listOf(dateItem())))
  }

  @Test
  fun `pre-fills for an iCal reminder`() {
    every { preferences.useFixedZoneForNewReminders } returns true
    val item = ICalStartDateBuilderItem(title = "s", description = null, formatter = mockk(relaxed = true))

    assertEquals("Europe/Berlin", prefill.zoneIdToPrefill(listOf(item)))
  }

  @Test
  fun `does not pre-fill when no zone capable item is present`() {
    every { preferences.useFixedZoneForNewReminders } returns true
    val summary = SummaryBuilderItem(title = "s", description = null)

    assertNull(prefill.zoneIdToPrefill(listOf(summary)))
  }

  @Test
  fun `does not pre-fill when a zone item already exists`() {
    every { preferences.useFixedZoneForNewReminders } returns true
    val zoneItem = TimeZoneBuilderItem(title = "z", description = null, catalog = TimeZoneCatalog("Device"))

    assertNull(prefill.zoneIdToPrefill(listOf(dateItem(), zoneItem)))
  }

  private fun dateItem() = DateBuilderItem(title = "d", description = null, dateFormatter = mockk(relaxed = true))

  private object TimePrefillFactory {
    fun create(preferences: ReminderPreferences) = TimeZonePrefill(preferences) { ZoneId.of("Europe/Berlin") }
  }
}
