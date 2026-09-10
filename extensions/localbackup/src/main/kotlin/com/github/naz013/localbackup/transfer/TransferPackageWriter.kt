package com.github.naz013.localbackup.transfer

import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.files.DataConverter
import com.github.naz013.localbackup.archive.BackupArchiveWriter
import com.github.naz013.localbackup.archive.BackupEnvelope
import com.github.naz013.logging.Logger
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Packages a [BackupEnvelope] (the same reminders/groups/.../workflow rows [BackupArchiveWriter]
 * already knows how to frame) plus notes-with-images into a single unencrypted zip for the
 * same-device cross-app transfer feature - encryption is skipped here on purpose (see the
 * `TransferSender`/`TransferReceiverActivity` doc): the Android URI-grant + caller-package check
 * around this file is the actual trust boundary, not a passphrase.
 *
 * ```
 * envelope.bin              - BackupArchiveWriter output, unchanged
 * notes/<noteKey>.json      - one NoteV4Json per note, via the existing DataConverter
 * images/<noteKey>/<file>   - raw image bytes, one entry per attachment
 * ```
 */
internal class TransferPackageWriter(
  private val dataConverter: DataConverter,
  private val archiveWriter: BackupArchiveWriter
) {
  suspend fun write(output: OutputStream, envelope: BackupEnvelope, notes: List<NoteWithImages>) {
    ZipOutputStream(output).use { zip ->
      writeEntry(zip, ENVELOPE_ENTRY) { archiveWriter.write(it, envelope) }

      for (noteWithImages in notes) {
        val note = noteWithImages.note ?: continue
        writeEntry(zip, "$NOTES_DIR/${note.key}.json") {
          dataConverter.toOutputStream(note.toTransferJson(noteWithImages.images), it)
        }
        for (image in noteWithImages.images) {
          val file = File(image.filePath)
          if (!file.exists()) continue
          writeEntry(zip, "$IMAGES_DIR/${note.key}/${image.fileName}") { file.inputStream().use { input -> input.copyTo(it) } }
        }
      }
    }
    Logger.i(TAG, "Wrote transfer package: ${notes.size} notes")
  }

  /** [ZipOutputStream] entries must not be closed individually, only the callback's own
   * [ByteArrayOutputStream] target - `close()` on that is a no-op, so it's safe even though
   * [DataConverter.toOutputStream] closes whatever stream it's given. */
  private suspend fun writeEntry(zip: ZipOutputStream, name: String, write: suspend (OutputStream) -> Unit) {
    val buffer = ByteArrayOutputStream()
    write(buffer)
    zip.putNextEntry(ZipEntry(name))
    zip.write(buffer.toByteArray())
    zip.closeEntry()
  }

  companion object {
    private const val TAG = "TransferPackageWriter"
    const val ENVELOPE_ENTRY = "envelope.bin"
    const val NOTES_DIR = "notes"
    const val IMAGES_DIR = "images"
  }
}
