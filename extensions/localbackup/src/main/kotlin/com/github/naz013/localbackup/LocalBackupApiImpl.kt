package com.github.naz013.localbackup

import com.github.naz013.crypto.BackupCipher
import com.github.naz013.crypto.PassphraseKeyDerivation
import com.github.naz013.localbackup.archive.BackupArchiveReader
import com.github.naz013.localbackup.archive.BackupArchiveWriter
import com.github.naz013.localbackup.archive.BackupEnvelope
import com.github.naz013.logging.Logger
import java.io.InputStream
import java.io.OutputStream
import java.util.Arrays
import javax.crypto.AEADBadTagException

internal class LocalBackupApiImpl(
  private val buildBackupEnvelopeUseCase: BuildBackupEnvelopeUseCase,
  private val applyBackupEnvelopeUseCase: ApplyBackupEnvelopeUseCase,
  private val archiveWriter: BackupArchiveWriter,
  private val archiveReader: BackupArchiveReader
) : LocalBackupApi {

  override suspend fun export(output: OutputStream, passphrase: CharArray): Result<Unit> {
    val result = runCatching {
      val envelope = buildBackupEnvelopeUseCase()

      val salt = PassphraseKeyDerivation.generateSalt()
      val iv = BackupCipher.generateIv()
      val key = PassphraseKeyDerivation.deriveKey(passphrase, salt)

      BackupFileHeader(salt, iv, PassphraseKeyDerivation.ITERATIONS).writeTo(output)
      BackupCipher.encryptingStream(output, key, iv).use { cipherOutput ->
        archiveWriter.write(cipherOutput, envelope)
      }
      Logger.i(TAG, "Exported local backup: ${envelope.summary()}")
    }
    Arrays.fill(passphrase, '0')
    return result.onFailure { Logger.e(TAG, "Failed to export local backup", it) }
  }

  override suspend fun import(input: InputStream, passphrase: CharArray): Result<ImportSummary> {
    val result = runCatching {
      val header = BackupFileHeader.readFrom(input)
      val key = PassphraseKeyDerivation.deriveKey(passphrase, header.salt, header.iterations)

      val envelope = BackupCipher.decryptingStream(input, key, header.iv).use { cipherInput ->
        archiveReader.read(cipherInput)
      }

      val summary = applyBackupEnvelopeUseCase(envelope)
      Logger.i(TAG, "Imported local backup: ${envelope.summary()}")
      summary
    }
    Arrays.fill(passphrase, '0')
    return result.fold(
      onSuccess = { Result.success(it) },
      onFailure = { e ->
        Logger.e(TAG, "Failed to import local backup", e)
        Result.failure(if (e.isWrongPassphrase()) WrongPassphraseException() else e)
      }
    )
  }

  private fun BackupEnvelope.summary(): String =
    "reminders=${reminders.size}, groups=${groups.size}, birthdays=${birthdays.size}, " +
      "places=${places.size}, presets=${presets.size}, tags=${tags.size}, " +
      "tagAssignments=${tagAssignments.size}, routines=${routines.size}, " +
      "routineExecutions=${routineExecutions.size}, workflowRules=${workflowRules.size}, " +
      "workflowTemplates=${workflowTemplates.size}"

  private fun Throwable.isWrongPassphrase(): Boolean {
    var current: Throwable? = this
    while (current != null) {
      if (current is AEADBadTagException) return true
      current = current.cause
    }
    return false
  }

  companion object {
    private const val TAG = "LocalBackupApi"
  }
}
