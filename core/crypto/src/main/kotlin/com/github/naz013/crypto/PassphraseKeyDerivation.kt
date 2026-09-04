package com.github.naz013.crypto

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * PBKDF2WithHmacSHA256 at a high iteration count instead of Argon2 - this is the first crypto
 * code in the repo, and Argon2 would need a native/JNI dependency the codebase has never taken
 * on. See the Local Backup feature plan for this trade-off.
 */
object PassphraseKeyDerivation {
  const val SALT_SIZE_BYTES = 16
  const val KEY_SIZE_BITS = 256
  const val ITERATIONS = 600_000

  private const val ALGORITHM = "PBKDF2WithHmacSHA256"

  fun generateSalt(): ByteArray {
    val salt = ByteArray(SALT_SIZE_BYTES)
    SecureRandom().nextBytes(salt)
    return salt
  }

  fun deriveKey(
    passphrase: CharArray,
    salt: ByteArray,
    iterations: Int = ITERATIONS
  ): SecretKeySpec {
    val spec = PBEKeySpec(passphrase, salt, iterations, KEY_SIZE_BITS)
    try {
      val factory = SecretKeyFactory.getInstance(ALGORITHM)
      val derived = factory.generateSecret(spec)
      return SecretKeySpec(derived.encoded, "AES")
    } finally {
      spec.clearPassword()
    }
  }
}
