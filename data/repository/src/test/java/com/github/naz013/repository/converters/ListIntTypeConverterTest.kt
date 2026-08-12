package com.github.naz013.repository.converters

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ListIntTypeConverterTest {
  private lateinit var converter: ListIntTypeConverter

  @Before
  fun setUp() {
    converter = ListIntTypeConverter()
  }

  @Test
  fun `list round trips through json unchanged`() {
    val list = listOf(1, 0, 1, 1, 0, 0, 1)

    val result = converter.toList(converter.toJson(list))

    assertEquals(list, result)
  }

  @Test
  fun `toList returns an empty list for null json`() {
    assertEquals(emptyList<Int>(), converter.toList(null))
  }

  @Test
  fun `toList falls back to an empty list for malformed json`() {
    assertEquals(emptyList<Int>(), converter.toList("not-json"))
  }
}
