package com.github.naz013.localbackup.transfer.compose

import android.content.Intent

internal data class SendDataState(
  val status: TransferStatus = TransferStatus.InProgress
)

internal sealed interface SendDataEvent {
  data class LaunchTransfer(val intent: Intent) : SendDataEvent
}
