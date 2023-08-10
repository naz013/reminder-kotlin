package com.elementary.tasks.core.data.platform

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BytesTest {

  @Test
  fun testIfBitSet() {
    val bytes = Bytes(0)
    println("Before: $bytes")

    assertEquals("00", bytes.toHexString())
    assertEquals(false, bytes.isBitSet(0, 3))

    bytes.setBit(0, 3)
    println("Bit set: $bytes")

    assertEquals(true, bytes.isBitSet(0, 3))
  }

  @Test
  fun testIfBitUnSet() {
    val bytes = Bytes(0)
    println("Before: $bytes")

    assertEquals("00", bytes.toHexString())
    assertEquals(false, bytes.isBitSet(0, 3))

    bytes.setBit(0, 3)
    println("Bit set: $bytes")

    assertEquals(true, bytes.isBitSet(0, 3))

    bytes.unSetBit(0, 3)
    println("Bit un set: $bytes")

    assertEquals(false, bytes.isBitSet(0, 3))
  }
}
