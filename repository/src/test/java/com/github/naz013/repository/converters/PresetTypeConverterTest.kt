package com.github.naz013.repository.converters

import com.github.naz013.domain.PresetType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PresetTypeConverterTest {
  private lateinit var converter: PresetTypeConverter

  @Before
  fun setUp() {
    converter = PresetTypeConverter()
  }

  @Test
  fun `every enum value round trips through its ordinal`() {
    for (type in PresetType.entries) {
      assertEquals(type, converter.toEnum(converter.toInt(type)))
    }
  }

  @Test
  fun `toInt encodes RECUR as zero and BUILDER as one`() {
    assertEquals(0, converter.toInt(PresetType.RECUR))
    assertEquals(1, converter.toInt(PresetType.BUILDER))
  }
}
