package com.github.naz013.localbackup.transfer

/**
 * Shared constants between the sending side ([TransferSender] / `SendDataViewModel`, built into
 * both flavors) and the receiving side ([TransferReceiverActivity], also built into both flavors,
 * declared exported in this module's manifest). [RECEIVER_ACTIVITY_CLASS_NAME] resolves correctly
 * across the free/PRO package boundary because this class lives in a module compiled unchanged
 * into both APKs - only the applicationId differs, never this fully-qualified name.
 */
internal object TransferContract {
  const val ACTION_RECEIVE_TRANSFER = "com.github.naz013.reminder.action.RECEIVE_TRANSFER"
  const val EXTRA_RESULT_COUNT = "com.github.naz013.reminder.extra.RESULT_COUNT"
  const val RECEIVER_ACTIVITY_CLASS_NAME = "com.github.naz013.localbackup.transfer.TransferReceiverActivity"
  const val MIME_TYPE = "application/zip"
}
