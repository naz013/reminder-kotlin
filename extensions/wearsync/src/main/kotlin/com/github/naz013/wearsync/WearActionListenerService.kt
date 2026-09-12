package com.github.naz013.wearsync

import com.github.naz013.logging.Logger
import com.github.naz013.logic.notificationaction.reminder.ReminderActionProcessor
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import org.koin.android.ext.android.inject

/** Receives "done"/"snooze" taps relayed from the Wear companion app and forwards them to the
 * exact same [ReminderActionProcessor] the phone notification's own action buttons call. */
internal class WearActionListenerService : WearableListenerService() {

  private val reminderActionProcessor: ReminderActionProcessor by inject()

  override fun onMessageReceived(messageEvent: MessageEvent) {
    val id = String(messageEvent.data, Charsets.UTF_8)
    when (messageEvent.path) {
      WearSyncProtocol.ACTION_COMPLETE_PATH -> {
        Logger.i(TAG, "Wear requested complete for reminder $id")
        reminderActionProcessor.complete(id)
      }
      WearSyncProtocol.ACTION_SNOOZE_PATH -> {
        Logger.i(TAG, "Wear requested snooze for reminder $id")
        reminderActionProcessor.snooze(id)
      }
    }
  }

  companion object {
    private const val TAG = "WearActionListener"
  }
}
