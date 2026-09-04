package com.github.naz013.crypto

import org.junit.Assert.assertEquals
import org.junit.Test

class Base64ObfuscatorTest {

  @Test
  fun `decode reverses encode`() {
    val encoded = Base64Obfuscator.encode("someone@example.com")

    assertEquals("someone@example.com", Base64Obfuscator.decode(encoded))
  }

  @Test
  fun `encode of an empty string returns an empty string`() {
    assertEquals("", Base64Obfuscator.encode(""))
  }

  @Test
  fun `decode of a malformed value returns an empty string instead of throwing`() {
    assertEquals("", Base64Obfuscator.decode("not valid base64!!"))
  }
}
