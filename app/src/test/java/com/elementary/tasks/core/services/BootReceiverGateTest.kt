package com.elementary.tasks.core.services

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BootReceiverGateTest {
  @Test
  fun `activates on boot even when zone is unchanged`() {
    assertTrue(shouldActivateReminders(isBoot = true, zoneChanged = false))
  }

  @Test
  fun `activates on zone change without boot`() {
    assertTrue(shouldActivateReminders(isBoot = false, zoneChanged = true))
  }

  @Test
  fun `does not activate on plain time change`() {
    assertFalse(shouldActivateReminders(isBoot = false, zoneChanged = false))
  }
}
