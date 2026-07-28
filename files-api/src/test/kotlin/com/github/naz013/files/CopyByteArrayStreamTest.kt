package com.github.naz013.files

import org.junit.Assert.assertEquals
import org.junit.Test

class CopyByteArrayStreamTest {

  @Test
  fun `toInputStream reflects only the bytes written, not the backing array capacity`() {
    val stream = CopyByteArrayStream()

    stream.write(byteArrayOf(1, 2, 3, 4, 5))

    val inputStream = stream.toInputStream()
    val readBytes = inputStream.readBytes()

    assertEquals(5, readBytes.size)
    assertEquals(listOf<Byte>(1, 2, 3, 4, 5), readBytes.toList())
  }

  @Test
  fun `toInputStream reflects writes made after construction`() {
    val stream = CopyByteArrayStream()
    stream.write(byteArrayOf(9, 9, 9, 9, 9, 9, 9, 9, 9, 9)) // force internal buffer growth
    stream.reset()
    stream.write(byteArrayOf(7, 8))

    val readBytes = stream.toInputStream().readBytes()

    assertEquals(listOf<Byte>(7, 8), readBytes.toList())
  }
}
