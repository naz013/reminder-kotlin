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

  @Test
  fun `pro trial promo clicked event has its own name`() {
    assertEquals("pro_trial_promo_clicked", ProTrialPromoClickedEvent.getName())
  }

  @Test
  fun `pro trial activated event has its own name`() {
    assertEquals("pro_trial_activated", ProTrialActivatedEvent.getName())
  }
}
