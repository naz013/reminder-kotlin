package com.github.naz013.localbackup.transfer

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.localbackup.transfer.compose.SendDataEvent
import com.github.naz013.localbackup.transfer.compose.SendDataScreen
import com.github.naz013.localbackup.transfer.compose.SendDataState
import com.github.naz013.localbackup.transfer.compose.SendDataViewModel
import com.github.naz013.ui.common.livedata.ObserveEvent
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.transferEntries(backStack: MutableList<NavKey>) {
  entry<TransferNavKey> {
    val viewModel = koinViewModel<SendDataViewModel>()
    val state by viewModel.state.collectAsState(SendDataState())

    val launcher = rememberLauncherForActivityResult(
      ActivityResultContracts.StartActivityForResult()
    ) { result ->
      viewModel.onTransferResult(result.resultCode, result.data)
    }

    viewModel.event.ObserveEvent { event ->
      when (event) {
        is SendDataEvent.LaunchTransfer -> launcher.launch(event.intent)
      }
    }

    SendDataScreen(
      state = state,
      onDoneClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    )
  }
}
