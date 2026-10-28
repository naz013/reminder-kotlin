package com.github.naz013.datecalc

import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import java.util.TimeZone

class DateTimeManagerZoneTest {
  private val originalZone: TimeZone = TimeZone.getDefault()
  private val nowProvider = mockk<NowDateTimeProvider>()
  private val berlin = ZoneId.of("Europe/Berlin")
  private val newYork = ZoneId.of("America/New_York")
  private lateinit var manager: DateTimeManager

  @Before
  fun setUp() {
    TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
    manager = DateTimeManager(nowProvider, mockk(relaxed = true))
  }

  @After
  fun tearDown() {
    TimeZone.setDefault(originalZone)
  }

  @Test
  fun `converts utc to the given zone wall clock`() {
    val utc = LocalDateTime.of(2025, 1, 6, 8, 0)

    val result = manager.utcToLocal(utc, berlin)

    assertEquals(LocalDateTime.of(2025, 1, 6, 9, 0), result)
  }

  @Test
  fun `converts utc to the device zone when no zone is given`() {
    val utc = LocalDateTime.of(2025, 1, 6, 8, 0)

    val result = manager.utcToLocal(utc)

    assertEquals(LocalDateTime.of(2025, 1, 6, 3, 0), result)
  }

  @Test
  fun `null zone behaves exactly like the device zone overloads`() {
    val local = LocalDateTime.of(2025, 6, 15, 12, 30)
    val device = manager.effectiveZone(null)

    assertEquals(manager.localToUtc(local), manager.localToUtc(local, device))
    assertEquals(manager.utcToLocal(local), manager.utcToLocal(local, device))
    assertEquals(manager.toMillis(local), manager.toMillis(local, device))
  }

  @Test
  fun `effective zone is the fixed zone for a valid id`() {
    assertEquals(berlin, manager.effectiveZone("Europe/Berlin"))
  }

  @Test
  fun `effective zone falls back to the device zone for an unknown id`() {
    assertEquals(newYork, manager.effectiveZone("Not/AZone"))
  }

  @Test
  fun `fixed zone gap time resolves forward after the gap`() {
    val local = LocalDateTime.of(2025, 3, 30, 2, 30)

    val result = manager.localToUtc(local, berlin)

    assertEquals(LocalDateTime.of(2025, 3, 30, 1, 30), result)
  }

  @Test
  fun `fixed zone overlap time uses the earlier offset`() {
    val local = LocalDateTime.of(2025, 10, 26, 2, 30)

    val result = manager.localToUtc(local, berlin)

    assertEquals(LocalDateTime.of(2025, 10, 26, 0, 30), result)
  }

  @Test
  fun `floating gap time resolves forward after the gap in the device zone`() {
    val local = LocalDateTime.of(2025, 3, 9, 2, 30)

    val result = manager.localToUtc(local)

    assertEquals(LocalDateTime.of(2025, 3, 9, 7, 30), result)
  }

  @Test
  fun `floating overlap time uses the earlier offset in the device zone`() {
    val local = LocalDateTime.of(2025, 11, 2, 1, 30)

    val result = manager.localToUtc(local)

    assertEquals(LocalDateTime.of(2025, 11, 2, 5, 30), result)
  }

  @Test
  fun `millis for a wall clock time in a fixed zone is independent of the device zone`() {
    val local = LocalDateTime.of(2025, 1, 6, 9, 0)

    val result = manager.toMillis(local, berlin)

    assertEquals(LocalDateTime.of(2025, 1, 6, 8, 0).toEpochSecond(ZoneOffset.UTC) * 1000, result)
  }

  @Test
  fun `current date time in a zone is the device now shifted to that zone`() {
    every { nowProvider.nowDateTime() } returns LocalDateTime.of(2025, 1, 6, 10, 0)

    val result = manager.getCurrentDateTime(berlin)

    assertEquals(LocalDateTime.of(2025, 1, 6, 16, 0), result)
  }
}
