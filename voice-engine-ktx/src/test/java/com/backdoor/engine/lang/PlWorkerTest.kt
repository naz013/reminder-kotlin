package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.ZoneId

/**
 * Test commands
 *
 * sprawdź pocztę za 15 minut
 * dzwonić do serwisu co 2 dni
 * sprawdź pocztę jutro o 15:00
 * w każdy wtorek pobudka o 7 rano
 * w każdy wtorek pobudka o siódmej rano
 * jutro o 15:40 odwiedzić lekarzy i dodać wydarzenie do kalendarza
 */
class PlWorkerTest {

  private val worker = PlWorker(ZoneId.of("GMT"))

  @Test
  fun testReplaceNumbers() {
    assertEquals(
      "w każdy wtorek pobudka o 7 rano",
      worker.replaceNumbers("w każdy wtorek pobudka o 7 rano")
    )

    assertEquals(
      "w każdy wtorek pobudka o 7.0 rano",
      worker.replaceNumbers("w każdy wtorek pobudka o siódmej rano")
    )
  }

  @Test
  fun testHasCalendar() {
    val input = "jutro o 15:40 odwiedzić lekarzy i dodać wydarzenie do kalendarza"

    assertEquals(true, worker.hasCalendar(input))

    assertEquals("jutro o 15:40 odwiedzić lekarzy i dodać wydarzenie", worker.clearCalendar(input))
  }

  @Test
  fun testHasNoCalendar() {
    val input = "w każdy wtorek pobudka o 7 rano"

    assertEquals(false, worker.hasCalendar(input))

    assertEquals("w każdy wtorek pobudka o 7 rano", worker.clearCalendar(input))
  }

  @Test
  fun testShowAction() {
    val input = "pokaż aktywne przypomnienie"

    assertEquals(true, worker.hasShowAction(input))
    assertEquals(Action.ACTIVE_REMINDERS, worker.getShowAction(input))
  }

  @Test
  fun testNotAWeekday() {

  }
}