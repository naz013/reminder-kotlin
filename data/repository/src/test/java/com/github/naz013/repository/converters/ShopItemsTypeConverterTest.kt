package com.github.naz013.repository.converters

import com.github.naz013.domain.reminder.ShopItem
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ShopItemsTypeConverterTest {
  private lateinit var converter: ShopItemsTypeConverter

  @Before
  fun setUp() {
    converter = ShopItemsTypeConverter()
  }

  @Test
  fun `list round trips through json unchanged`() {
    val list = listOf(ShopItem(summary = "Milk", createTime = "2023-01-01"))

    val result = converter.toList(converter.toJson(list))

    assertEquals(list, result)
  }

  @Test
  fun `toList returns an empty list for empty json`() {
    assertEquals(emptyList<ShopItem>(), converter.toList(""))
  }

  @Test
  fun `toList falls back to an empty list for malformed json instead of throwing`() {
    assertEquals(emptyList<ShopItem>(), converter.toList("not-json"))
  }
}
