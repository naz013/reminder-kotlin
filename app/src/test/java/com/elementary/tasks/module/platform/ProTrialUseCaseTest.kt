package com.elementary.tasks.module.platform

import com.elementary.tasks.core.utils.params.Prefs
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProTrialUseCaseTest {

  private val dayMillis = 24L * 60 * 60 * 1000
  private val now = 5_000_000L
  private val proTrial = ProTrial(TimeSource { now })

  @Test
  fun `activate stores expiry 24 hours from now in prefs`() {
    val prefs = mockk<Prefs>(relaxed = true)
    val stored = slot<Long>()
    every { prefs.proTrialExpiryMillis = capture(stored) } returns Unit
    val useCase = ProTrialUseCase(prefs, proTrial)

    useCase.activate()

    assertEquals(now + dayMillis, stored.captured)
    verify(exactly = 1) { prefs.proTrialExpiryMillis = now + dayMillis }
  }

  @Test
  fun `extend adds 24 hours to the stored expiry while the trial is active`() {
    val prefs = mockk<Prefs>(relaxed = true)
    every { prefs.proTrialExpiryMillis } returns now + dayMillis
    val useCase = ProTrialUseCase(prefs, proTrial)

    val result = useCase.extend()

    assertTrue(result)
    verify(exactly = 1) { prefs.proTrialExpiryMillis = now + 2 * dayMillis }
  }

  @Test
  fun `extend does nothing when the trial is not active`() {
    val prefs = mockk<Prefs>(relaxed = true)
    every { prefs.proTrialExpiryMillis } returns now - 1L
    val useCase = ProTrialUseCase(prefs, proTrial)

    val result = useCase.extend()

    assertFalse(result)
    verify(exactly = 0) { prefs.proTrialExpiryMillis = any() }
  }

  @Test
  fun `extend does nothing when the maximum remaining time is reached`() {
    val prefs = mockk<Prefs>(relaxed = true)
    every { prefs.proTrialExpiryMillis } returns now + ProTrial.MAX_REMAINING_MILLIS
    val useCase = ProTrialUseCase(prefs, proTrial)

    val result = useCase.extend()

    assertFalse(result)
    verify(exactly = 0) { prefs.proTrialExpiryMillis = any() }
  }

  @Test
  fun `observe can extend emits true while below the maximum`() = runTest {
    val prefs = mockk<Prefs>()
    every { prefs.proTrialExpiryMillis } returns now + dayMillis
    val useCase = ProTrialUseCase(prefs, proTrial)

    assertTrue(useCase.observeCanExtend().first())
  }

  @Test
  fun `observe can extend emits false when the trial is not active`() = runTest {
    val prefs = mockk<Prefs>()
    every { prefs.proTrialExpiryMillis } returns 0L
    val useCase = ProTrialUseCase(prefs, proTrial)

    assertFalse(useCase.observeCanExtend().first())
  }

  @Test
  fun `observe emits full remaining duration right after activation`() = runTest {
    val prefs = mockk<Prefs>()
    every { prefs.proTrialExpiryMillis } returns now + dayMillis
    val useCase = ProTrialUseCase(prefs, proTrial)

    val first = useCase.observeRemainingMillis().first()

    assertEquals(dayMillis, first)
  }

  @Test
  fun `observe emits zero when trial has already expired`() = runTest {
    val prefs = mockk<Prefs>()
    every { prefs.proTrialExpiryMillis } returns now - 1L
    val useCase = ProTrialUseCase(prefs, proTrial)

    val first = useCase.observeRemainingMillis().first()

    assertEquals(0L, first)
  }

  @Test
  fun `observe emits zero when trial was never activated`() = runTest {
    val prefs = mockk<Prefs>()
    every { prefs.proTrialExpiryMillis } returns 0L
    val useCase = ProTrialUseCase(prefs, proTrial)

    val first = useCase.observeRemainingMillis().first()

    assertEquals(0L, first)
  }
}
