package com.github.naz013.localbackup.transfer

import com.github.naz013.domain.note.Note
import com.github.naz013.localbackup.archive.BackupEnvelope

/** One note plus its image bytes, read back out of a transfer zip - [images] pairs each
 * attachment's file name with its raw bytes, ready to be staged via `NoteImageRepository`. */
internal data class TransferNoteItem(
  val note: Note,
  val images: List<Pair<String, ByteArray>>
)

internal data class TransferPackage(
  val envelope: BackupEnvelope,
  val notes: List<TransferNoteItem>
)

/** Thrown when a transfer zip is missing its `envelope.bin` entry - either corrupted in transit or
 * not a transfer package produced by [TransferPackageWriter] at all. */
internal class InvalidTransferPackageException : Exception("This file is not a valid Reminder transfer package.")
