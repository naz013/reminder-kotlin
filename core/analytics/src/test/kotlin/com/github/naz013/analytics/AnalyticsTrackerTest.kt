package com.github.naz013.analytics

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AnalyticsTrackerTest {
  private lateinit var tracker: AnalyticsTracker

  @Before
  fun setUp() {
    tracker = AnalyticsTracker()
  }

  @Test
  fun `getTimeInSeconds returns zero for an event that was never tracked`() {
    assertEquals(0L, tracker.getTimeInSeconds(Event.FEATURE_USED))
  }

  @Test
  fun `getTimeInSeconds returns close to zero right after tracking`() {
    tracker.trackEvent(Event.WIDGET_USED)

    assertEquals(0L, tracker.getTimeInSeconds(Event.WIDGET_USED))
  }

  @Test
  fun `tracking one event does not affect the elapsed time of another`() {
    tracker.trackEvent(Event.FEATURE_USED)

    assertEquals(0L, tracker.getTimeInSeconds(Event.SCREEN_OPENED))
  }
}
