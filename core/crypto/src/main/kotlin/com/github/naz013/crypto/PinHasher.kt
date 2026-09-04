package com.github.naz013.crypto

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * One-way salted hashing for short PINs. PBKDF2 is deliberately used instead of a reversible
 * encoding - a PIN only ever needs to be verified, never read back, so it must not be
 * recoverable from the stored value.
 */
object PinHasher {
  private const val ALGORITHM = "PBKDF2WithHmacSHA256"
  private const val ITERATIONS = 15_000
  private const val KEY_LENGTH_BITS = 256
  private const val SALT_LENGTH_BYTES = 16
  private const val SEPARATOR = ":"

  fun hash(pin: String): String {
    val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
    return encode(salt) + SEPARATOR + encode(deriveKey(pin, salt))
  }

  fun matches(pin: String, storedHash: String): Boolean {
    val parts = storedHash.split(SEPARATOR)
    if (parts.size != 2) return false
    val salt = decode(parts[0])
    return encode(deriveKey(pin, salt)) == parts[1]
  }

  private fun deriveKey(pin: String, salt: ByteArray): ByteArray {
    val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
    return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
  }

  private fun encode(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

  private fun decode(string: String): ByteArray = Base64.getDecoder().decode(string)
}
