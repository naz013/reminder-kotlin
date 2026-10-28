package com.elementary.tasks.module.platform

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProTrialTest {

  private val dayMillis = 24L * 60 * 60 * 1000
  private var now = 1_000_000L
  private val proTrial = ProTrial(TimeSource { now })

  @Test
  fun `trial duration is exactly 24 hours`() {
    assertEquals(dayMillis, ProTrial.TRIAL_DURATION_MILLIS)
  }

  @Test
  fun `activation expiry is 24 hours after current time`() {
    val expiry = proTrial.activationExpiry()

    assertEquals(now + dayMillis, expiry)
  }

  @Test
  fun `remaining millis equals full duration right after activation`() {
    val expiry = proTrial.activationExpiry()

    assertEquals(dayMillis, proTrial.remainingMillis(expiry))
  }

  @Test
  fun `remaining millis decreases as time passes`() {
    val expiry = proTrial.activationExpiry()
    now += 3_600_000L

    assertEquals(dayMillis - 3_600_000L, proTrial.remainingMillis(expiry))
  }

  @Test
  fun `remaining millis is zero when expiry has passed`() {
    val expiry = proTrial.activationExpiry()
    now += dayMillis + 5_000L

    assertEquals(0L, proTrial.remainingMillis(expiry))
  }

  @Test
  fun `remaining millis is zero when no trial was ever activated`() {
    assertEquals(0L, proTrial.remainingMillis(0L))
  }

  @Test
  fun `trial is not expired right after activation`() {
    val expiry = proTrial.activationExpiry()

    assertFalse(proTrial.isExpired(expiry))
  }

  @Test
  fun `trial is not expired one millisecond before expiry`() {
    val expiry = proTrial.activationExpiry()
    now = expiry - 1

    assertFalse(proTrial.isExpired(expiry))
  }

  @Test
  fun `trial is expired exactly at expiry instant`() {
    val expiry = proTrial.activationExpiry()
    now = expiry

    assertTrue(proTrial.isExpired(expiry))
  }

  @Test
  fun `trial is expired after 24 hours have passed`() {
    val expiry = proTrial.activationExpiry()
    now += dayMillis + 1L

    assertTrue(proTrial.isExpired(expiry))
  }

  @Test
  fun `never activated trial reads as expired`() {
    assertTrue(proTrial.isExpired(0L))
  }

  @Test
  fun `extended expiry adds 24 hours to the current expiry`() {
    val expiry = proTrial.activationExpiry()

    assertEquals(expiry + dayMillis, proTrial.extendedExpiry(expiry))
  }

  @Test
  fun `extended expiry counts from now when the trial already expired`() {
    val expiry = proTrial.activationExpiry()
    now = expiry + 5_000L

    assertEquals(now + dayMillis, proTrial.extendedExpiry(expiry))
  }

  @Test
  fun `can extend when the result stays exactly at the maximum`() {
    assertTrue(proTrial.canExtend(ProTrial.MAX_REMAINING_MILLIS - dayMillis))
  }

  @Test
  fun `cannot extend when the result would exceed the maximum by one millisecond`() {
    assertFalse(proTrial.canExtend(ProTrial.MAX_REMAINING_MILLIS - dayMillis + 1L))
  }

  @Test
  fun `cannot extend when no time remains`() {
    assertFalse(proTrial.canExtend(0L))
  }
}
