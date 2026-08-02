package com.github.naz013.tags

import com.github.naz013.tags.db.entity.TagEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class TagMapperTest {

  @Test
  fun `toEntity preserves id, name and color`() {
    val tag = Tag(id = "tag-1", name = "Work", color = 0xFF0000)

    val entity = tag.toEntity()

    assertEquals("tag-1", entity.id)
    assertEquals("Work", entity.name)
    assertEquals(0xFF0000, entity.color)
  }

  @Test
  fun `toDomain preserves id, name and color`() {
    val entity = TagEntity(id = "tag-2", name = "Home", color = 0x00FF00)

    val tag = entity.toDomain()

    assertEquals("tag-2", tag.id)
    assertEquals("Home", tag.name)
    assertEquals(0x00FF00, tag.color)
  }
}
