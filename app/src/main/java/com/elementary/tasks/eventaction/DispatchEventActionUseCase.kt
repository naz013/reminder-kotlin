package com.elementary.tasks.eventaction

import android.content.Context
import com.elementary.tasks.core.utils.TelephonyUtil
import com.github.naz013.logging.Logger

class DispatchEventActionUseCase {
  operator fun invoke(
    context: Context,
    action: ResolvedEventAction,
  ) {
    Logger.i(TAG, "Dispatching event action: $action")
    when (action) {
      is ResolvedEventAction.OpenApp -> {
        TelephonyUtil.openApp(action.packageName, context)
      }
      is ResolvedEventAction.OpenLink -> {
        TelephonyUtil.openLink(action.url, context)
      }
      is ResolvedEventAction.MakeCall -> {
        TelephonyUtil.makeCall(action.phoneNumber, context)
      }
      is ResolvedEventAction.SendSms -> {
        TelephonyUtil.sendSms(context, action.phoneNumber, action.message)
      }
      is ResolvedEventAction.SendEmail -> {
        TelephonyUtil.sendMail(
          context = context,
          email = action.email,
          subject = action.subject,
          message = action.body,
          filePath = action.attachmentPath,
        )
      }
    }
  }

  companion object {
    private const val TAG = "DispatchEventActionUseCase"
  }
}
