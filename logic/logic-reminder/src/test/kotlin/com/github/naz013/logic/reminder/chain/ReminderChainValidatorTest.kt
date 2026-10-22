package com.github.naz013.logic.reminder.chain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderChainValidatorTest {

  private val validator = ReminderChainValidator()

  @Test
  fun `detects a reminder waiting for itself`() {
    assertTrue(validator.wouldCreateCycle("a", "a") { null })
  }

  @Test
  fun `detects a direct cycle between two reminders`() {
    val links = mapOf("b" to "a")

    assertTrue(validator.wouldCreateCycle("a", "b") { links[it] })
  }

  @Test
  fun `detects a long cycle`() {
    val links = mapOf("b" to "c", "c" to "d", "d" to "e", "e" to "a")

    assertTrue(validator.wouldCreateCycle("a", "b") { links[it] })
  }

  @Test
  fun `accepts a valid chain`() {
    val links = mapOf("b" to "c", "c" to "d")

    assertFalse(validator.wouldCreateCycle("a", "b") { links[it] })
  }

  @Test
  fun `accepts a predecessor without any link`() {
    assertFalse(validator.wouldCreateCycle("a", "b") { null })
  }

  @Test
  fun `stops on an existing loop that does not include the reminder`() {
    val links = mapOf("b" to "c", "c" to "b")

    assertTrue(validator.wouldCreateCycle("a", "b") { links[it] })
  }
}
