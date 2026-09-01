package com.github.naz013.digest

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class DigestLanguageTest {

  @After
  fun tearDown() {
    Locale.setDefault(Locale.US)
  }

  @Test
  fun `maps an English device locale to ENGLISH`() {
    Locale.setDefault(Locale.US)

    assertEquals(DigestLanguage.ENGLISH, currentDeviceDigestLanguage())
  }

  @Test
  fun `maps a Japanese device locale to JAPANESE`() {
    Locale.setDefault(Locale.JAPAN)

    assertEquals(DigestLanguage.JAPANESE, currentDeviceDigestLanguage())
  }

  @Test
  fun `maps a Korean device locale to KOREAN`() {
    Locale.setDefault(Locale.KOREA)

    assertEquals(DigestLanguage.KOREAN, currentDeviceDigestLanguage())
  }

  @Test
  fun `returns null for an unsupported device locale, e_g_ French`() {
    Locale.setDefault(Locale.FRANCE)

    assertNull(currentDeviceDigestLanguage())
  }
}
