package com.github.naz013.domain.note

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ImageFileTest {

  @Test
  fun `equals is true for images with the same content and id regardless of file path`() {
    val a = ImageFile(image = byteArrayOf(1, 2, 3), id = 1, filePath = "path/a", fileName = "a")
    val b = ImageFile(image = byteArrayOf(1, 2, 3), id = 1, filePath = "path/b", fileName = "b")

    assertEquals(a, b)
    assertEquals(a.hashCode(), b.hashCode())
  }

  @Test
  fun `equals is false when the image bytes differ`() {
    val a = ImageFile(image = byteArrayOf(1, 2, 3), id = 1)
    val b = ImageFile(image = byteArrayOf(1, 2, 4), id = 1)

    assertNotEquals(a, b)
  }

  @Test
  fun `equals is false when the id differs`() {
    val a = ImageFile(image = byteArrayOf(1, 2, 3), id = 1)
    val b = ImageFile(image = byteArrayOf(1, 2, 3), id = 2)

    assertNotEquals(a, b)
  }
}
