package com.github.naz013.localbackup.crypto

import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object BackupCipher {
  const val IV_SIZE_BYTES = 12
  const val TAG_LENGTH_BITS = 128

  private const val TRANSFORMATION = "AES/GCM/NoPadding"

  fun generateIv(): ByteArray {
    val iv = ByteArray(IV_SIZE_BYTES)
    SecureRandom().nextBytes(iv)
    return iv
  }

  fun encryptingStream(
    output: OutputStream,
    key: SecretKey,
    iv: ByteArray
  ): CipherOutputStream {
    val cipher = Cipher.getInstance(TRANSFORMATION)
    cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BITS, iv))
    return CipherOutputStream(output, cipher)
  }

  /**
   * GCM only verifies its auth tag once every byte has been read, so a wrong passphrase or a
   * corrupted/tampered file surfaces from the final read()/close() call as an [java.io.IOException]
   * wrapping an [javax.crypto.AEADBadTagException] ([CipherInputStream]'s documented behavior) -
   * callers must fully drain the stream before trusting its contents.
   */
  fun decryptingStream(
    input: InputStream,
    key: SecretKey,
    iv: ByteArray
  ): CipherInputStream {
    val cipher = Cipher.getInstance(TRANSFORMATION)
    cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BITS, iv))
    return CipherInputStream(input, cipher)
  }
}
