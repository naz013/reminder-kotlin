package com.github.naz013.localbackup.transfer

import com.github.naz013.files.DataConverter
import com.github.naz013.files.model.NoteV4Json
import com.github.naz013.localbackup.archive.BackupArchiveReader
import com.github.naz013.localbackup.archive.BackupEnvelope
import com.github.naz013.logging.Logger
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

/** Reverses [TransferPackageWriter]'s zip layout. See that class for the entry-name scheme. */
internal class TransferPackageReader(
  private val dataConverter: DataConverter,
  private val archiveReader: BackupArchiveReader
) {
  suspend fun read(input: InputStream): TransferPackage {
    var envelope: BackupEnvelope? = null
    val noteJsons = mutableMapOf<String, NoteV4Json>()
    val images = mutableMapOf<String, MutableList<Pair<String, ByteArray>>>()

    ZipInputStream(input).use { zip ->
      var entry = zip.nextEntry
      while (entry != null) {
        val name = entry.name
        val bytes = zip.readBytes()
        when {
          name == TransferPackageWriter.ENVELOPE_ENTRY ->
            envelope = archiveReader.read(ByteArrayInputStream(bytes))

          name.startsWith("${TransferPackageWriter.NOTES_DIR}/") && name.endsWith(".json") -> {
            val noteKey = name.removePrefix("${TransferPackageWriter.NOTES_DIR}/").removeSuffix(".json")
            (dataConverter.toData(ByteArrayInputStream(bytes)) as? NoteV4Json)?.let { noteJsons[noteKey] = it }
          }

          name.startsWith("${TransferPackageWriter.IMAGES_DIR}/") -> {
            val parts = name.removePrefix("${TransferPackageWriter.IMAGES_DIR}/").split("/", limit = 2)
            if (parts.size == 2) {
              images.getOrPut(parts[0]) { mutableListOf() }.add(parts[1] to bytes)
            }
          }
        }
        zip.closeEntry()
        entry = zip.nextEntry
      }
    }

    val resolvedEnvelope = envelope ?: throw InvalidTransferPackageException()
    val notes = noteJsons.map { (noteKey, json) ->
      TransferNoteItem(note = json.toTransferNote(), images = images[noteKey].orEmpty())
    }
    Logger.i(TAG, "Read transfer package: ${notes.size} notes")
    return TransferPackage(resolvedEnvelope, notes)
  }

  companion object {
    private const val TAG = "TransferPackageReader"
  }
}
