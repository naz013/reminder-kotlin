package com.github.naz013.localbackup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.localbackup.compose.LocalBackupMode
import com.github.naz013.localbackup.compose.LocalBackupScreen
import com.github.naz013.localbackup.compose.LocalBackupState
import com.github.naz013.localbackup.compose.LocalBackupViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.localBackupEntries(backStack: MutableList<NavKey>) {
  entry<LocalBackupNavKey.Export> { key -> LocalBackupEntry(key.uriString, LocalBackupMode.EXPORT, backStack) }
  entry<LocalBackupNavKey.Import> { key -> LocalBackupEntry(key.uriString, LocalBackupMode.IMPORT, backStack) }
}

@Composable
private fun LocalBackupEntry(
  uriString: String,
  mode: LocalBackupMode,
  backStack: MutableList<NavKey>
) {
  val viewModel = koinViewModel<LocalBackupViewModel> { parametersOf(uriString, mode) }

  val state by viewModel.state.collectAsState(LocalBackupState(mode = mode))
  LocalBackupScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onPassphraseChange = viewModel::onPassphraseChange,
    onConfirmPassphraseChange = viewModel::onConfirmPassphraseChange,
    onActionClick = viewModel::onActionClick
  )
}
