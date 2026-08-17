package com.github.naz013.feature.settings.calendar.country

import org.junit.Assert.assertEquals
import org.junit.Test

class CountryFlagEmojiTest {

  @Test
  fun `composes the two regional indicator symbols for a valid alpha-2 code`() {
    assertEquals("🇺🇸", countryCodeToFlagEmoji("US"))
  }

  @Test
  fun `is case-insensitive`() {
    assertEquals(countryCodeToFlagEmoji("US"), countryCodeToFlagEmoji("us"))
  }

  @Test
  fun `returns the input unchanged when it is not a 2-letter code`() {
    assertEquals("USA", countryCodeToFlagEmoji("USA"))
    assertEquals("U", countryCodeToFlagEmoji("U"))
    assertEquals("", countryCodeToFlagEmoji(""))
  }

  @Test
  fun `returns the input unchanged when it contains non-letter characters`() {
    assertEquals("U1", countryCodeToFlagEmoji("U1"))
  }
}
