package com.github.naz013.localbackup.transfer

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.localbackup.transfer.compose.TransferReceiveScreen
import com.github.naz013.localbackup.transfer.compose.TransferReceiveState
import com.github.naz013.localbackup.transfer.compose.TransferReceiverViewModel
import com.github.naz013.localbackup.transfer.compose.TransferStatus
import com.github.naz013.logging.Logger
import com.github.naz013.platform.SystemInfo
import com.github.naz013.ui.common.compose.ComposeActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Cross-app receiving end of the "Transfer to PRO/Free" feature - see [TransferSender] for the
 * sending side and the trust-boundary reasoning. This Activity is declared `exported="true"` in
 * this module's manifest (both flavors ship the exact same class), since there's no shared signing
 * certificate to gate it with a signature permission instead. [callingPackage] is only ever
 * populated when the caller used `startActivityForResult`/`ActivityResultContracts`, so a caller
 * that doesn't match the expected sibling package (or a plain `am start`, or a relaunch from
 * Recents) is rejected before any file is even opened.
 */
internal class TransferReceiverActivity : ComposeActivity() {

  private val buildInfo: BuildInfo by inject()
  private var isCallerValid = false

  override fun onCreate(savedInstanceState: Bundle?) {
    setResult(RESULT_CANCELED)
    isCallerValid = isRequestValid()
    super.onCreate(savedInstanceState)
    if (!isCallerValid) {
      Logger.w(TAG, "Rejected transfer request from callingPackage=$callingPackage")
      finish()
    }
  }

  private fun isRequestValid(): Boolean {
    val expectedCaller = if (buildInfo.isPro) SystemInfo.FREE_PACKAGE_NAME else SystemInfo.PRO_PACKAGE_NAME
    return callingPackage == expectedCaller &&
      intent?.action == TransferContract.ACTION_RECEIVE_TRANSFER &&
      intent?.data != null
  }

  @Composable
  override fun ActivityContent() {
    if (!isCallerValid) return

    val uriString = intent.data.toString()
    val viewModel = koinViewModel<TransferReceiverViewModel> { parametersOf(uriString) }
    val state by viewModel.state.collectAsState(TransferReceiveState())

    TransferReceiveScreen(
      state = state,
      onDoneClick = {
        val successCount = (state.status as? TransferStatus.Success)?.totalCount
        if (successCount != null) {
          setResult(RESULT_OK, Intent().putExtra(TransferContract.EXTRA_RESULT_COUNT, successCount))
        }
        finish()
      }
    )
  }

  companion object {
    private const val TAG = "TransferReceiverActivity"
  }
}
