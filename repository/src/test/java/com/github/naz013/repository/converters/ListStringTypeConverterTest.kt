package com.github.naz013.repository.converters

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ListStringTypeConverterTest {
  private lateinit var converter: ListStringTypeConverter

  @Before
  fun setUp() {
    converter = ListStringTypeConverter()
  }

  @Test
  fun `list round trips through json unchanged`() {
    val list = listOf("a", "b", "c")

    val result = converter.toList(converter.toJson(list))

    assertEquals(list, result)
  }

  @Test
  fun `toList returns an empty list for empty json`() {
    assertEquals(emptyList<String>(), converter.toList(""))
  }

  @Test
  fun `toList falls back to an empty list for malformed json`() {
    assertEquals(emptyList<String>(), converter.toList("not-json"))
  }
}
