package com.github.naz013.localbackup

import java.io.InputStream
import java.io.OutputStream

interface LocalBackupApi {
  suspend fun export(output: OutputStream, passphrase: CharArray): Result<Unit>

  suspend fun import(input: InputStream, passphrase: CharArray): Result<ImportSummary>
}

data class ImportSummary(
  val remindersImported: Int,
  val groupsImported: Int,
  val birthdaysImported: Int,
  val placesImported: Int,
  val presetsImported: Int,
  val tagsImported: Int,
  val tagAssignmentsImported: Int,
  val routinesImported: Int = 0,
  val routineExecutionsImported: Int = 0
)

/** Distinguishes "wrong passphrase or corrupted file" from other IO failures during import. */
class WrongPassphraseException : Exception("The passphrase is incorrect, or the backup file is corrupted.")

class InvalidBackupFileException : Exception("This file is not a valid Reminder backup.")
