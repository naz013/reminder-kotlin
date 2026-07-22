package com.github.naz013.feature.common

import org.junit.Assert.assertEquals
import org.junit.Test

class CollectionExtensionsTest {

  @Test
  fun `append concatenates every string with no separator`() {
    val result = listOf("a", "b", "c").append()

    assertEquals("abc", result)
  }

  @Test
  fun `append returns an empty string for an empty list`() {
    assertEquals("", emptyList<String>().append())
  }

  @Test
  fun `listOfNotEmpty drops nulls and empty strings`() {
    val result = listOfNotEmpty("a", null, "", "b")

    assertEquals(listOf("a", "b"), result)
  }

  @Test
  fun `listOfNotEmpty keeps blank strings that are not empty`() {
    val result = listOfNotEmpty(" ", "a")

    assertEquals(listOf(" ", "a"), result)
  }

  @Test
  fun `filterNotEmpty drops nulls and empty strings from a list`() {
    val result = listOf("a", null, "", "b").filterNotEmpty()

    assertEquals(listOf("a", "b"), result)
  }
}
