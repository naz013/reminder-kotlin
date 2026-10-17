package com.elementary.tasks.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.elementary.tasks.R
import com.github.naz013.localbackup.transfer.PreparedTransfer
import com.github.naz013.localbackup.transfer.TransferContract
import com.github.naz013.localbackup.transfer.TransferSender
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Builds the "Transfer to PRO/Free" intent and launches the sibling app, then reports the result
 * as a toast. Lives in `app` (not `feature-settings`) because it needs `extensions:localbackup`'s
 * [TransferSender], and `feature-settings` deliberately stays free of that dependency - same
 * reasoning as `onOpenLocalBackupExport`/`onOpenLocalBackupImport` next to this function's call
 * site in `AppNavGraph.kt`.
 */
@Composable
fun rememberTransferClickHandler(): () -> Unit {
  val transferSender = koinInject<TransferSender>()
  val toastDispatcher = rememberToastDispatcher()
  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current

  var pendingTransfer by remember { mutableStateOf<PreparedTransfer?>(null) }

  val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    pendingTransfer?.cleanUp(context)
    pendingTransfer = null
    if (result.resultCode == Activity.RESULT_OK) {
      val count = result.data?.getIntExtra(TransferContract.EXTRA_RESULT_COUNT, -1) ?: -1
      if (count >= 0) {
        toastDispatcher.showToast(message = context.getString(R.string.transfer_sent_success, count))
      }
      // A negative count means the receiving app returned RESULT_OK without the extra, which
      // shouldn't happen - stay silent rather than show a misleading message.
    }
    // RESULT_CANCELED covers both "user backed out on the other side" and "the receiving app
    // rejected the request" - neither warrants an error toast on the sending side.
  }

  return {
    coroutineScope.launch {
      val prepared = transferSender.prepareTransfer()
      if (prepared != null) {
        pendingTransfer = prepared
        launcher.launch(prepared.intent)
      } else {
        toastDispatcher.showToast(messageRes = R.string.transfer_send_failed)
      }
    }
  }
}
