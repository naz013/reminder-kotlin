package com.github.naz013.logic.notificationaction

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForegroundStateTrackerTest {
  private val tracker = ForegroundStateTracker()

  @Test
  fun `is not foreground before any activity resumes`() {
    assertFalse(tracker.isForeground.value)
  }

  @Test
  fun `becomes foreground once an activity resumes`() {
    tracker.onResumed()

    assertTrue(tracker.isForeground.value)
  }

  @Test
  fun `stays foreground while another activity is still resumed`() {
    tracker.onResumed()
    tracker.onResumed()
    tracker.onPaused()

    assertTrue(tracker.isForeground.value)
  }

  @Test
  fun `becomes backgrounded once every resumed activity has paused`() {
    tracker.onResumed()
    tracker.onResumed()
    tracker.onPaused()
    tracker.onPaused()

    assertFalse(tracker.isForeground.value)
  }
}
