package com.github.naz013.feature.calendar.timeline

import com.github.naz013.ui.agenda.AgendaCategory
import com.github.naz013.ui.agenda.UiAgendaItem
import com.github.naz013.ui.agenda.UiAgendaReminder
import com.github.naz013.ui.common.text.UiTextElement
import com.github.naz013.ui.common.text.UiTextFormat
import com.github.naz013.ui.reminder.UiReminderListActions
import com.github.naz013.ui.reminder.UiReminderListState
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

class TimelineLayoutTest {

  private fun eventAt(
    id: String,
    hour: Int,
    minute: Int = 0,
  ): UiAgendaItem =
    UiAgendaReminder(
      id = id,
      dateTime = LocalDateTime.of(2026, 7, 15, hour, minute),
      category = AgendaCategory.REMINDERS,
      mainText = UiTextElement(id, UiTextFormat(fontSize = 14f)),
      secondaryText = null,
      tertiaryText = null,
      tags = emptyList(),
      actions = UiReminderListActions(),
      state = UiReminderListState(),
    )

  @Test
  fun `empty input produces no positioned events`() {
    assertEquals(emptyList<PositionedTimelineEvent>(), layoutDayEvents(emptyList(), blockMinutes = 60))
  }

  @Test
  fun `single event occupies the only lane with correct start minutes`() {
    val result = layoutDayEvents(listOf(eventAt("a", hour = 9, minute = 30)), blockMinutes = 60)

    assertEquals(1, result.size)
    assertEquals(9 * 60 + 30, result.first().startMinutes)
    assertEquals(0, result.first().lane)
    assertEquals(1, result.first().laneCount)
  }

  @Test
  fun `events that do not overlap each keep a single lane`() {
    val result = layoutDayEvents(listOf(eventAt("a", 9), eventAt("b", 11)), blockMinutes = 60)

    assertEquals(2, result.size)
    result.forEach {
      assertEquals(0, it.lane)
      assertEquals(1, it.laneCount)
    }
  }

  @Test
  fun `two overlapping events split into two lanes`() {
    val result = layoutDayEvents(listOf(eventAt("a", 9, 0), eventAt("b", 9, 30)), blockMinutes = 60)

    assertEquals(listOf(0, 1), result.map { it.lane })
    assertEquals(listOf(2, 2), result.map { it.laneCount })
  }

  @Test
  fun `a freed lane is reused by a later non-overlapping event in the same cluster`() {
    // 09:00 (lane 0, ends 10:00), 09:30 (lane 1), 10:00 reuses lane 0 (free at 10:00) - the whole
    // chain stays one cluster, so all three report laneCount 2.
    val result = layoutDayEvents(listOf(eventAt("a", 9, 0), eventAt("b", 9, 30), eventAt("c", 10, 0)), blockMinutes = 60)

    assertEquals(listOf(0, 1, 0), result.map { it.lane })
    assertEquals(listOf(2, 2, 2), result.map { it.laneCount })
  }

  @Test
  fun `three mutually overlapping events use three lanes`() {
    val result = layoutDayEvents(listOf(eventAt("a", 9, 0), eventAt("b", 9, 15), eventAt("c", 9, 45)), blockMinutes = 60)

    assertEquals(listOf(0, 1, 2), result.map { it.lane })
    assertEquals(listOf(3, 3, 3), result.map { it.laneCount })
  }

  @Test
  fun `results are ordered by start time regardless of input order`() {
    val result = layoutDayEvents(listOf(eventAt("late", 15), eventAt("early", 8)), blockMinutes = 60)

    assertEquals(listOf(8 * 60, 15 * 60), result.map { it.startMinutes })
  }
}
