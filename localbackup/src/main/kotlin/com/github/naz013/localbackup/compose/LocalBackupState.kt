package com.github.naz013.localbackup.compose

enum class LocalBackupMode {
  EXPORT,
  IMPORT
}

data class LocalBackupState(
  val mode: LocalBackupMode = LocalBackupMode.EXPORT,
  val passphrase: String = "",
  val confirmPassphrase: String = "",
  val passphraseError: Boolean = false,
  val status: LocalBackupStatus = LocalBackupStatus.Idle
)

sealed interface LocalBackupStatus {
  data object Idle : LocalBackupStatus

  data object InProgress : LocalBackupStatus

  data class Success(
    val summary: String
  ) : LocalBackupStatus

  data class Error(
    val messageRes: Int
  ) : LocalBackupStatus
}
