package com.github.naz013.feature.common

import org.junit.Assert.assertEquals
import org.junit.Test

class StringExtensionsTest {

  @Test
  fun `capitalizeFirstLetter uppercases only the first character`() {
    assertEquals("Hello world", "hello world".capitalizeFirstLetter())
  }

  @Test
  fun `capitalizeFirstLetter leaves an already-capitalized string unchanged`() {
    assertEquals("Hello", "Hello".capitalizeFirstLetter())
  }

  @Test
  fun `capitalizeFirstLetter returns an empty string unchanged`() {
    assertEquals("", "".capitalizeFirstLetter())
  }
}
