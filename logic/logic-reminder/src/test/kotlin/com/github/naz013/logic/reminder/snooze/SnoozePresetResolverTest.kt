package com.github.naz013.logic.reminder.snooze

import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class SnoozePresetResolverTest {
  private val resolver = SnoozePresetResolver()
  private val evening = LocalTime.of(18, 0)
  private val morning = LocalTime.of(8, 0)
  private val utc = ZoneId.of("UTC")
  private val newYork = ZoneId.of("America/New_York")

  private fun resolve(
    preset: SnoozePresetType,
    now: ZonedDateTime,
    eveningTime: LocalTime = evening,
    morningTime: LocalTime = morning,
  ) = resolver.resolve(preset, now, eveningTime, morningTime)

  @Test
  fun `adds minutes to now for relative presets`() {
    val now = ZonedDateTime.of(2026, 5, 10, 10, 0, 0, 0, utc)

    assertEquals(LocalDateTime.of(2026, 5, 10, 10, 15), resolve(SnoozePresetType.MINUTES_15, now))
    assertEquals(LocalDateTime.of(2026, 5, 10, 11, 0), resolve(SnoozePresetType.HOUR_1, now))
    assertEquals(LocalDateTime.of(2026, 5, 10, 13, 0), resolve(SnoozePresetType.HOURS_3, now))
  }

  @Test
  fun `resolves this evening to today when it is still ahead`() {
    val now = ZonedDateTime.of(2026, 5, 10, 10, 0, 0, 0, utc)

    assertEquals(LocalDateTime.of(2026, 5, 10, 18, 0), resolve(SnoozePresetType.THIS_EVENING, now))
  }

  @Test
  fun `rolls this evening to tomorrow when evening time has passed`() {
    val now = ZonedDateTime.of(2026, 5, 10, 19, 30, 0, 0, utc)

    assertEquals(LocalDateTime.of(2026, 5, 11, 18, 0), resolve(SnoozePresetType.THIS_EVENING, now))
  }

  @Test
  fun `rolls this evening to tomorrow when now is exactly evening time`() {
    val now = ZonedDateTime.of(2026, 5, 10, 18, 0, 0, 0, utc)

    assertEquals(LocalDateTime.of(2026, 5, 11, 18, 0), resolve(SnoozePresetType.THIS_EVENING, now))
  }

  @Test
  fun `resolves tomorrow morning to next day even before morning time`() {
    val now = ZonedDateTime.of(2026, 5, 10, 6, 0, 0, 0, utc)

    assertEquals(LocalDateTime.of(2026, 5, 11, 8, 0), resolve(SnoozePresetType.TOMORROW_MORNING, now))
  }

  @Test
  fun `rolls this evening over month end`() {
    val now = ZonedDateTime.of(2026, 5, 31, 23, 0, 0, 0, utc)

    assertEquals(LocalDateTime.of(2026, 6, 1, 18, 0), resolve(SnoozePresetType.THIS_EVENING, now))
  }

  @Test
  fun `skips the missing hour when relative preset crosses spring forward`() {
    val now = ZonedDateTime.of(2026, 3, 8, 1, 30, 0, 0, newYork)

    assertEquals(LocalDateTime.of(2026, 3, 8, 3, 30), resolve(SnoozePresetType.HOUR_1, now))
  }

  @Test
  fun `repeats the hour when relative preset crosses fall back`() {
    val now = ZonedDateTime.of(2026, 11, 1, 1, 30, 0, 0, newYork)

    assertEquals(LocalDateTime.of(2026, 11, 1, 1, 30), resolve(SnoozePresetType.HOUR_1, now))
  }

  @Test
  fun `moves evening time inside a spring forward gap to the next valid time`() {
    val now = ZonedDateTime.of(2026, 3, 8, 0, 30, 0, 0, newYork)

    val result = resolve(SnoozePresetType.THIS_EVENING, now, eveningTime = LocalTime.of(2, 30))

    assertEquals(LocalDateTime.of(2026, 3, 8, 3, 30), result)
  }

  @Test
  fun `keeps tomorrow morning wall clock time across fall back`() {
    val now = ZonedDateTime.of(2026, 10, 31, 22, 0, 0, 0, newYork)

    assertEquals(LocalDateTime.of(2026, 11, 1, 8, 0), resolve(SnoozePresetType.TOMORROW_MORNING, now))
  }

  @Test
  fun `parses and serializes preset lists`() {
    assertEquals(SnoozePresetType.DEFAULT_ENABLED, SnoozePresetType.parseList(null))
    assertEquals(emptyList<SnoozePresetType>(), SnoozePresetType.parseList(""))
    assertEquals(
      listOf(SnoozePresetType.HOUR_1, SnoozePresetType.THIS_EVENING),
      SnoozePresetType.parseList("HOUR_1,UNKNOWN,THIS_EVENING,HOUR_1"),
    )
    assertEquals(
      "MINUTES_15,HOURS_3",
      SnoozePresetType.serialize(listOf(SnoozePresetType.MINUTES_15, SnoozePresetType.HOURS_3)),
    )
  }
}
