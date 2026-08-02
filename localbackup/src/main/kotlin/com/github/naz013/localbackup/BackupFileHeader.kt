package com.github.naz013.localbackup

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Unencrypted header preceding the GCM-encrypted archive payload - salt/IV/iteration-count must be
 * readable before the passphrase can even be tried, and none of them need to be secret (GCM's
 * security doesn't depend on hiding the IV, and a salt is public by design).
 */
internal data class BackupFileHeader(
  val salt: ByteArray,
  val iv: ByteArray,
  val iterations: Int
) {
  fun writeTo(output: OutputStream) {
    val dataOutput = DataOutputStream(output)
    dataOutput.write(MAGIC)
    dataOutput.writeInt(salt.size)
    dataOutput.write(salt)
    dataOutput.writeInt(iv.size)
    dataOutput.write(iv)
    dataOutput.writeInt(iterations)
    dataOutput.flush()
  }

  companion object {
    private val MAGIC = "RMDRBKP1".toByteArray(Charsets.US_ASCII)

    fun readFrom(input: InputStream): BackupFileHeader {
      val dataInput = DataInputStream(input)
      val magic = ByteArray(MAGIC.size)
      dataInput.readFully(magic)
      if (!magic.contentEquals(MAGIC)) {
        throw InvalidBackupFileException()
      }
      val salt = ByteArray(dataInput.readInt()).also { dataInput.readFully(it) }
      val iv = ByteArray(dataInput.readInt()).also { dataInput.readFully(it) }
      val iterations = dataInput.readInt()
      return BackupFileHeader(salt, iv, iterations)
    }
  }
}
