package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import com.elementary.tasks.Actions
import com.elementary.tasks.core.services.action.birthday.BirthdayActionProcessor
import com.elementary.tasks.core.utils.Constants
import org.koin.core.component.inject
import timber.log.Timber

class BirthdayActionReceiver : BaseBroadcast() {

  private val birthdayActionProcessor by inject<BirthdayActionProcessor>()

  override fun onReceive(context: Context, intent: Intent?) {
    if (intent != null) {
      val action = intent.action
      val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""
      Timber.d("onReceive: $action, id=$id")
      if (action != null && id.isNotEmpty()) {
        when {
          action.matches(ACTION_CALL.toRegex()) -> birthdayActionProcessor.makeCall(id)
          action.matches(ACTION_SMS.toRegex()) -> birthdayActionProcessor.sendSms(id)
          action.matches(ACTION_HIDE.toRegex()) -> birthdayActionProcessor.cancel(id)
        }
      }
    }
  }

  companion object {
    const val ACTION_HIDE = Actions.Birthday.ACTION_HIDE_SIMPLE
    const val ACTION_CALL = Actions.Birthday.ACTION_CALL
    const val ACTION_SMS = Actions.Birthday.ACTION_SMS
  }
}
