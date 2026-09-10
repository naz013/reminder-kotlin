package com.github.naz013.localbackup.transfer.compose

/** Shared by [SendDataScreen] and [TransferReceiveScreen] - both sides of the cross-app transfer
 * go through the same prepare/wait -> success/error shape, just with different wording. */
internal sealed interface TransferStatus {
  data object InProgress : TransferStatus

  data class Success(val totalCount: Int) : TransferStatus

  data class Error(val messageRes: Int) : TransferStatus
}
