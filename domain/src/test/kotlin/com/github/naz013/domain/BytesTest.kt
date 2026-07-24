package com.github.naz013.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BytesTest {

  @Test
  fun `hex string round trips through toHexString`() {
    val bytes = Bytes("1a2b")

    assertEquals("1a2b", bytes.toHexString())
  }

  @Test
  fun `int constructor produces the equivalent hex representation`() {
    val bytes = Bytes(255)

    assertEquals("ff", bytes.toHexString())
  }

  @Test
  fun `byte array constructor produces the equivalent hex representation`() {
    val bytes = Bytes(byteArrayOf(0x10, 0x20))

    assertEquals("1020", bytes.toHexString())
  }

  @Test
  fun `setBit turns a bit on without affecting other bits`() {
    val bytes = Bytes(0)

    bytes.setBit(0, 3)

    assertTrue(bytes.isBitSet(0, 3))
    assertFalse(bytes.isBitSet(0, 2))
  }

  @Test
  fun `unSetBit turns a previously set bit off`() {
    val bytes = Bytes(0)
    bytes.setBit(0, 5)

    bytes.unSetBit(0, 5)

    assertFalse(bytes.isBitSet(0, 5))
  }

  @Test
  fun `ZERO constant represents a single zero byte`() {
    assertEquals("00", Bytes.ZERO.toHexString())
  }

  @Test
  fun `EMPTY constant represents no bytes`() {
    assertEquals("", Bytes.EMPTY.toHexString())
  }
}
