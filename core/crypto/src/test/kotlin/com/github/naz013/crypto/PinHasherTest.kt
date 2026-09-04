package com.github.naz013.crypto

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHasherTest {

  @Test
  fun `matches returns true for the pin that produced the hash`() {
    val hash = PinHasher.hash("123456")

    assertTrue(PinHasher.matches("123456", hash))
  }

  @Test
  fun `matches returns false for a different pin`() {
    val hash = PinHasher.hash("123456")

    assertFalse(PinHasher.matches("654321", hash))
  }

  @Test
  fun `hashing the same pin twice produces different output`() {
    val first = PinHasher.hash("123456")
    val second = PinHasher.hash("123456")

    assertNotEquals(first, second)
    assertTrue(PinHasher.matches("123456", first))
    assertTrue(PinHasher.matches("123456", second))
  }

  @Test
  fun `matches returns false for a malformed stored value`() {
    assertFalse(PinHasher.matches("123456", "not-a-valid-hash"))
  }
}
