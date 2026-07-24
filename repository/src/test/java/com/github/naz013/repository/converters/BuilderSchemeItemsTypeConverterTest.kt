package com.github.naz013.repository.converters

import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.BuilderSchemeItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class BuilderSchemeItemsTypeConverterTest {
  private lateinit var converter: BuilderSchemeItemsTypeConverter

  @Before
  fun setUp() {
    converter = BuilderSchemeItemsTypeConverter()
  }

  @Test
  fun `list round trips through json unchanged`() {
    val list = listOf(BuilderSchemeItem(BiType.DATE, 0), BuilderSchemeItem(BiType.TIME, 1))

    val json = converter.toJson(list)
    val result = converter.toList(json)

    assertEquals(list, result)
  }

  @Test
  fun `toJson returns null for a null list`() {
    assertNull(converter.toJson(null))
  }

  @Test
  fun `toJson returns null for an empty list`() {
    assertNull(converter.toJson(emptyList()))
  }

  @Test
  fun `toList returns null for null json`() {
    assertNull(converter.toList(null))
  }

  @Test
  fun `toList falls back to null for malformed json`() {
    assertNull(converter.toList("not-json"))
  }
}
