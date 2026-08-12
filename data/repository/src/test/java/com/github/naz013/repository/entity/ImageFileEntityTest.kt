package com.github.naz013.repository.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ImageFileEntityTest {

  @Test
  fun `equals is true for entities with the same image bytes and id regardless of file path`() {
    val a = ImageFileEntity(image = byteArrayOf(1, 2, 3), id = 1, filePath = "a", fileName = "a")
    val b = ImageFileEntity(image = byteArrayOf(1, 2, 3), id = 1, filePath = "b", fileName = "b")

    assertEquals(a, b)
    assertEquals(a.hashCode(), b.hashCode())
  }

  @Test
  fun `equals is false when the id differs`() {
    val a = ImageFileEntity(image = byteArrayOf(1, 2, 3), id = 1)
    val b = ImageFileEntity(image = byteArrayOf(1, 2, 3), id = 2)

    assertNotEquals(a, b)
  }

  @Test
  fun `round trip through domain preserves fields`() {
    val entity = ImageFileEntity(image = byteArrayOf(9), noteId = "note-1", id = 7, filePath = "path", fileName = "name")

    val domain = entity.toDomain()
    val restored = ImageFileEntity(domain)

    assertEquals(entity, restored)
    assertEquals(entity.noteId, restored.noteId)
    assertEquals(entity.filePath, restored.filePath)
    assertEquals(entity.fileName, restored.fileName)
  }
}
