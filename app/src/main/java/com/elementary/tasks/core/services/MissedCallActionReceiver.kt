package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import com.elementary.tasks.Actions
import com.elementary.tasks.core.services.action.birthday.BirthdayActionProcessor
import com.elementary.tasks.core.services.action.missedcall.MissedCallActionProcessor
import com.elementary.tasks.core.utils.Constants
import org.koin.core.component.inject
import timber.log.Timber

@Deprecated("After S")
class MissedCallActionReceiver : BaseBroadcast() {

  private val missedCallActionProcessor by inject<MissedCallActionProcessor>()

  override fun onReceive(context: Context, intent: Intent?) {
    if (intent != null) {
      val action = intent.action
      val phoneNumber = intent.getStringExtra(Constants.INTENT_ID) ?: ""
      Timber.d("onReceive: $action, phoneNumber=$phoneNumber")
      if (action != null && phoneNumber.isNotEmpty()) {
        when {
          action.matches(ACTION_CALL.toRegex()) -> missedCallActionProcessor.makeCall(phoneNumber)
          action.matches(ACTION_SMS.toRegex()) -> missedCallActionProcessor.sendSms(phoneNumber)
          action.matches(ACTION_HIDE.toRegex()) -> missedCallActionProcessor.cancel(phoneNumber)
        }
      }
    }
  }

  companion object {
    const val ACTION_HIDE = Actions.MissedCall.ACTION_HIDE_SIMPLE
    const val ACTION_CALL = Actions.MissedCall.ACTION_CALL
    const val ACTION_SMS = Actions.MissedCall.ACTION_SMS
  }
}
