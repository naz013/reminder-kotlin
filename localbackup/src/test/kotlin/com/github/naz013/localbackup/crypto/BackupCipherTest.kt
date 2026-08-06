package com.github.naz013.localbackup.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.crypto.AEADBadTagException

class BackupCipherTest {

  private val plaintext = "the quick brown fox jumps over the lazy dog".toByteArray()

  private fun key(passphrase: String, salt: ByteArray) =
    PassphraseKeyDerivation.deriveKey(passphrase.toCharArray(), salt)

  private fun encrypt(passphrase: String, salt: ByteArray, iv: ByteArray, data: ByteArray): ByteArray {
    val output = ByteArrayOutputStream()
    BackupCipher.encryptingStream(output, key(passphrase, salt), iv).use { it.write(data) }
    return output.toByteArray()
  }

  private fun decrypt(passphrase: String, salt: ByteArray, iv: ByteArray, ciphertext: ByteArray): ByteArray {
    val input = ByteArrayInputStream(ciphertext)
    return BackupCipher.decryptingStream(input, key(passphrase, salt), iv).use { it.readBytes() }
  }

  @Test
  fun `round trips plaintext through the correct passphrase`() {
    val salt = PassphraseKeyDerivation.generateSalt()
    val iv = BackupCipher.generateIv()

    val ciphertext = encrypt("correct passphrase", salt, iv, plaintext)
    val decrypted = decrypt("correct passphrase", salt, iv, ciphertext)

    assertArrayEquals(plaintext, decrypted)
  }

  @Test
  fun `ciphertext is not equal to plaintext`() {
    val salt = PassphraseKeyDerivation.generateSalt()
    val iv = BackupCipher.generateIv()

    val ciphertext = encrypt("passphrase", salt, iv, plaintext)

    assertThrows(AssertionError::class.java) { assertArrayEquals(plaintext, ciphertext) }
  }

  private fun assertBadTag(block: () -> Unit) {
    val error = assertThrows(IOException::class.java, block)
    assertTrue(error.cause is AEADBadTagException)
  }

  @Test
  fun `decrypting with the wrong passphrase fails with a bad tag error`() {
    val salt = PassphraseKeyDerivation.generateSalt()
    val iv = BackupCipher.generateIv()
    val ciphertext = encrypt("correct passphrase", salt, iv, plaintext)

    assertBadTag { decrypt("wrong passphrase", salt, iv, ciphertext) }
  }

  @Test
  fun `decrypting tampered ciphertext fails with a bad tag error`() {
    val salt = PassphraseKeyDerivation.generateSalt()
    val iv = BackupCipher.generateIv()
    val ciphertext = encrypt("passphrase", salt, iv, plaintext)
    ciphertext[0] = ciphertext[0].inc()

    assertBadTag { decrypt("passphrase", salt, iv, ciphertext) }
  }

  @Test
  fun `decrypting with the wrong iv fails with a bad tag error`() {
    val salt = PassphraseKeyDerivation.generateSalt()
    val ciphertext = encrypt("passphrase", salt, BackupCipher.generateIv(), plaintext)

    assertBadTag { decrypt("passphrase", salt, BackupCipher.generateIv(), ciphertext) }
  }

  @Test
  fun `handles empty plaintext`() {
    val salt = PassphraseKeyDerivation.generateSalt()
    val iv = BackupCipher.generateIv()

    val ciphertext = encrypt("passphrase", salt, iv, ByteArray(0))
    val decrypted = decrypt("passphrase", salt, iv, ciphertext)

    assertArrayEquals(ByteArray(0), decrypted)
  }

  @Test
  fun `handles large plaintext across multiple stream writes`() {
    val salt = PassphraseKeyDerivation.generateSalt()
    val iv = BackupCipher.generateIv()
    val large = ByteArray(1_000_000) { (it % 256).toByte() }

    val ciphertext = encrypt("passphrase", salt, iv, large)
    val decrypted = decrypt("passphrase", salt, iv, ciphertext)

    assertArrayEquals(large, decrypted)
  }
}
