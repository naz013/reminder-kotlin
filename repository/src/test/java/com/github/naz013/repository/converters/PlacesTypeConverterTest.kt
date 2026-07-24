package com.github.naz013.repository.converters

import com.github.naz013.repository.entity.PlaceEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PlacesTypeConverterTest {
  private lateinit var converter: PlacesTypeConverter

  @Before
  fun setUp() {
    converter = PlacesTypeConverter()
  }

  @Test
  fun `list round trips through json unchanged`() {
    val list = listOf(PlaceEntity(name = "Home"), PlaceEntity(name = "Work"))

    val result = converter.toList(converter.toJson(list))

    assertEquals(list, result)
  }

  @Test
  fun `toList returns null for empty json`() {
    assertNull(converter.toList(""))
  }

  @Test
  fun `toList falls back to null for malformed json`() {
    assertNull(converter.toList("not-json"))
  }
}
