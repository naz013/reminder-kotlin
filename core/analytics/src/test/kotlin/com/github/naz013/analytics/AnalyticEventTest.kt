package com.github.naz013.analytics

import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticEventTest {

  @Test
  fun `widget interacted event name is distinct from widget used event name`() {
    val interacted = WidgetInteractedEvent(Widget.CALENDAR)
    val used = WidgetUsedEvent(Widget.CALENDAR)

    assertEquals("widget_interacted", interacted.getName())
    assertEquals("widget_used", used.getName())
  }

  @Test
  fun `widget interacted event keeps the widget it was created with`() {
    val event = WidgetInteractedEvent(Widget.EVENTS)

    assertEquals(Widget.EVENTS, event.widget)
  }
}
