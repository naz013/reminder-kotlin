package com.elementary.tasks.core.services.action.birthday.process

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.elementary.tasks.birthdays.preview.ShowBirthdayActivity
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.services.PermanentBirthdayReceiver
import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Notifier

@Deprecated("After Q")
class BirthdayHandler(
  private val contextProvider: ContextProvider,
  private val notifier: Notifier
) : ActionHandler<Birthday> {

  override fun handle(data: Birthday) {
    sendCloseBroadcast(contextProvider.context, data.uuId)

    val notificationIntent = ShowBirthdayActivity.getLaunchIntent(contextProvider.context, data.uuId)
    notificationIntent.putExtra(Constants.INTENT_NOTIFICATION, true)
    contextProvider.context.startActivity(notificationIntent)
    notifier.cancel(PermanentBirthdayReceiver.BIRTHDAY_PERM_ID)
  }

  private fun sendCloseBroadcast(context: Context, id: String) {
    val intent = Intent(ShowBirthdayActivity.ACTION_STOP_BG_ACTIVITY)
    intent.putExtra(Constants.INTENT_ID, id)
    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
  }
}
