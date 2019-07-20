package com.elementary.tasks.core.app_widgets.events

import android.content.Context
import android.content.Intent
import com.elementary.tasks.Actions
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.core.services.BaseBroadcast
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.reminder.create.CreateReminderActivity
import timber.log.Timber

class EventActionReceiver : BaseBroadcast() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            Timber.d("onReceive: $action")
            if (action != null && action.matches(Actions.Reminder.ACTION_EDIT_EVENT.toRegex())) {
                val id = intent.getIntExtra(Constants.INTENT_ID, 0)
                val isReminder = intent.getBooleanExtra(TYPE, true)
                Timber.d("onHandleIntent: $id, isReminder $isReminder")
                if (id != 0) {
                    if (isReminder) {
                        CreateReminderActivity.openLogged(context,
                                Intent(context, CreateReminderActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        .putExtra(Constants.INTENT_ID, id))
                    } else {
                        AddBirthdayActivity.openLogged(context,
                                Intent(context, AddBirthdayActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        .putExtra(Constants.INTENT_ID, id))
                    }
                }
            }
        }
    }

    companion object {
        const val TYPE = "type"
    }
}
