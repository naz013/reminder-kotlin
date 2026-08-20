package com.github.naz013.feature.settings.export

internal data class CloudBackupSettingsState(
  val autoBackupStateName: String = "",
  val networkTypeName: String = "",
  val hasAnyCloudApi: Boolean = false,
  val isInProgress: Boolean = false,
  val dialog: CloudBackupDialog? = null,
)

internal sealed class CloudBackupDialog {
  data class AutoBackupInterval(
    val options: List<String>,
    val selectedIndex: Int,
  ) : CloudBackupDialog()

  data class NetworkType(
    val options: List<String>,
    val selectedIndex: Int,
  ) : CloudBackupDialog()

  data object EraseConfirm : CloudBackupDialog()
}
