package com.github.naz013.localbackup.compose

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.localbackup.InvalidBackupFileException
import com.github.naz013.localbackup.LocalBackupApi
import com.github.naz013.localbackup.R
import com.github.naz013.localbackup.WrongPassphraseException
import com.github.naz013.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class LocalBackupViewModel(
  private val uriString: String,
  mode: LocalBackupMode,
  private val dispatcherProvider: DispatcherProvider,
  private val context: Context,
  private val localBackupApi: LocalBackupApi
) : ViewModel() {

  private val _state = MutableStateFlow(LocalBackupState(mode = mode))
  val state = _state.asStateFlow()

  fun onPassphraseChange(text: String) {
    _state.update { it.copy(passphrase = text, passphraseError = false) }
  }

  fun onConfirmPassphraseChange(text: String) {
    _state.update { it.copy(confirmPassphrase = text, passphraseError = false) }
  }

  fun onActionClick() {
    val current = _state.value
    if (current.passphrase.isBlank()) {
      _state.update { it.copy(passphraseError = true) }
      return
    }
    if (current.mode == LocalBackupMode.EXPORT && current.passphrase != current.confirmPassphrase) {
      _state.update { it.copy(passphraseError = true) }
      return
    }

    _state.update { it.copy(status = LocalBackupStatus.InProgress) }
    viewModelScope.launch(dispatcherProvider.io()) {
      val status = if (current.mode == LocalBackupMode.EXPORT) performExport() else performImport()
      withContext(dispatcherProvider.main()) {
        _state.update { it.copy(status = status) }
      }
    }
  }

  private suspend fun performExport(): LocalBackupStatus {
    val uri = Uri.parse(uriString)
    val output = context.contentResolver.openOutputStream(uri)
      ?: return LocalBackupStatus.Error(R.string.backup_open_file_failed)
    val result = output.use { localBackupApi.export(it, _state.value.passphrase.toCharArray()) }
    return result.fold(
      onSuccess = { LocalBackupStatus.Success(context.getString(R.string.backup_export_success)) },
      onFailure = { e ->
        Logger.e(TAG, "Export failed", e)
        LocalBackupStatus.Error(R.string.backup_export_failed)
      }
    )
  }

  private suspend fun performImport(): LocalBackupStatus {
    val uri = Uri.parse(uriString)
    val input = context.contentResolver.openInputStream(uri)
      ?: return LocalBackupStatus.Error(R.string.backup_open_file_failed)
    val result = input.use { localBackupApi.import(it, _state.value.passphrase.toCharArray()) }
    return result.fold(
      onSuccess = { summary ->
        LocalBackupStatus.Success(
          context.getString(
            R.string.backup_import_success,
            summary.remindersImported + summary.groupsImported + summary.birthdaysImported +
              summary.placesImported + summary.presetsImported + summary.tagsImported +
              summary.tagAssignmentsImported
          )
        )
      },
      onFailure = { e ->
        Logger.e(TAG, "Import failed", e)
        when (e) {
          is WrongPassphraseException -> LocalBackupStatus.Error(R.string.backup_wrong_passphrase)
          is InvalidBackupFileException -> LocalBackupStatus.Error(R.string.backup_invalid_file)
          else -> LocalBackupStatus.Error(R.string.backup_import_failed)
        }
      }
    )
  }

  companion object {
    private const val TAG = "LocalBackupViewModel"
  }
}
