package com.github.naz013.localbackup.transfer.compose

internal data class TransferReceiveState(
  val status: TransferStatus = TransferStatus.InProgress
)
