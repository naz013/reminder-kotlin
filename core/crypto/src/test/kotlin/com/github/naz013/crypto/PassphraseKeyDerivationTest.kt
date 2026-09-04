package com.github.naz013.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PassphraseKeyDerivationTest {

  @Test
  fun `deriving twice with the same passphrase and salt yields the same key`() {
    val salt = PassphraseKeyDerivation.generateSalt()

    val key1 = PassphraseKeyDerivation.deriveKey("correct horse battery staple".toCharArray(), salt)
    val key2 = PassphraseKeyDerivation.deriveKey("correct horse battery staple".toCharArray(), salt)

    assertArrayEquals(key1.encoded, key2.encoded)
  }

  @Test
  fun `different passphrases derive different keys`() {
    val salt = PassphraseKeyDerivation.generateSalt()

    val key1 = PassphraseKeyDerivation.deriveKey("passphrase-one".toCharArray(), salt)
    val key2 = PassphraseKeyDerivation.deriveKey("passphrase-two".toCharArray(), salt)

    assertFalse(key1.encoded.contentEquals(key2.encoded))
  }

  @Test
  fun `different salts derive different keys from the same passphrase`() {
    val salt1 = PassphraseKeyDerivation.generateSalt()
    val salt2 = PassphraseKeyDerivation.generateSalt()

    val key1 = PassphraseKeyDerivation.deriveKey("same passphrase".toCharArray(), salt1)
    val key2 = PassphraseKeyDerivation.deriveKey("same passphrase".toCharArray(), salt2)

    assertFalse(key1.encoded.contentEquals(key2.encoded))
  }

  @Test
  fun `generateSalt produces salts of the expected size and does not repeat`() {
    val salt1 = PassphraseKeyDerivation.generateSalt()
    val salt2 = PassphraseKeyDerivation.generateSalt()

    assertEquals(PassphraseKeyDerivation.SALT_SIZE_BYTES, salt1.size)
    assertNotEquals(salt1.toList(), salt2.toList())
  }

  @Test
  fun `derived key is AES and has the expected bit length`() {
    val key = PassphraseKeyDerivation.deriveKey("passphrase".toCharArray(), PassphraseKeyDerivation.generateSalt())

    assertEquals("AES", key.algorithm)
    assertEquals(PassphraseKeyDerivation.KEY_SIZE_BITS / 8, key.encoded.size)
  }
}
