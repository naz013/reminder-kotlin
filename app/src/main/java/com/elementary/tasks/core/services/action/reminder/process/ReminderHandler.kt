package com.elementary.tasks.core.services.action.reminder.process

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.reminder.dialog.ReminderDialogActivity

@Deprecated("After Q")
class ReminderHandler(
  private val contextProvider: ContextProvider,
  private val notifier: Notifier
) : ActionHandler<Reminder> {

  override fun handle(data: Reminder) {
    sendCloseBroadcast(contextProvider.context, data.uuId)

    val intent = ReminderDialogActivity.getLaunchIntent(contextProvider.context, data.uuId)
    intent.putExtra(Constants.INTENT_NOTIFICATION, true)
    contextProvider.context.startActivity(intent)

    notifier.cancel(data.uniqueId)
  }

  private fun sendCloseBroadcast(context: Context, id: String) {
    val intent = Intent(ReminderDialogActivity.ACTION_STOP_BG_ACTIVITY)
    intent.putExtra(Constants.INTENT_ID, id)
    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
  }
}
