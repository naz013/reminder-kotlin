package com.github.naz013.crypto

import java.util.Base64

/**
 * Reversible Base64 encoding - NOT encryption. Anyone with access to the stored value can recover
 * the original string. Only use this for values that aren't secret but shouldn't sit as obvious
 * plaintext in shared prefs (e.g. an account name); never for passwords, PINs, or tokens - those
 * need one-way hashing (see [PinHasher]) or real symmetric encryption (see [BackupCipher]).
 */
object Base64Obfuscator {
  fun encode(value: String): String {
    if (value.isEmpty()) return ""
    return Base64.getEncoder().encodeToString(value.toByteArray(Charsets.UTF_8))
  }

  fun decode(value: String): String {
    return try {
      String(Base64.getDecoder().decode(value), Charsets.UTF_8)
    } catch (_: IllegalArgumentException) {
      ""
    }
  }
}
