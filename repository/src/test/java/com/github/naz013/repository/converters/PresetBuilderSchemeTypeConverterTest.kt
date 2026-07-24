package com.github.naz013.repository.converters

import com.github.naz013.domain.PresetBuilderScheme
import com.github.naz013.domain.reminder.BiType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PresetBuilderSchemeTypeConverterTest {
  private lateinit var converter: PresetBuilderSchemeTypeConverter

  @Before
  fun setUp() {
    converter = PresetBuilderSchemeTypeConverter()
  }

  @Test
  fun `list round trips through json unchanged`() {
    val list = listOf(PresetBuilderScheme(BiType.DATE, 0, "value"))

    val result = converter.toList(converter.toJson(list))

    assertEquals(list, result)
  }

  @Test
  fun `toJson returns empty string for an empty list`() {
    assertEquals("", converter.toJson(emptyList()))
  }

  @Test
  fun `toList returns an empty list for empty json`() {
    assertEquals(emptyList<PresetBuilderScheme>(), converter.toList(""))
  }

  @Test
  fun `toList falls back to an empty list for malformed json`() {
    assertEquals(emptyList<PresetBuilderScheme>(), converter.toList("not-json"))
  }
}
