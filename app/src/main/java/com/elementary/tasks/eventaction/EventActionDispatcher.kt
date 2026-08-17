package com.elementary.tasks.eventaction

import androidx.compose.runtime.Composable
import com.elementary.tasks.BuildConfig
import com.github.naz013.ui.common.permission.PermissionRequester
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import com.github.naz013.feature.settings.SendEmailResolver
import com.github.naz013.feature.settings.rememberSendEmailResolver
import com.elementary.tasks.telephony.ApplicationLauncher
import com.elementary.tasks.telephony.PhoneCaller
import com.elementary.tasks.telephony.SmsSender
import com.elementary.tasks.telephony.UrlLauncher
import com.elementary.tasks.telephony.rememberApplicationLauncher
import com.elementary.tasks.telephony.rememberPhoneCaller
import com.elementary.tasks.telephony.rememberSmsSender
import com.elementary.tasks.telephony.rememberUrlLauncher
import com.github.naz013.common.Permissions
import com.github.naz013.logging.Logger

interface EventActionDispatcher {
  fun dispatch(action: ResolvedEventAction)
}

private class EventActionDispatcherImpl(
  private val applicationLauncher: ApplicationLauncher,
  private val urlLauncher: UrlLauncher,
  private val emailResolver: SendEmailResolver,
  private val phoneCaller: PhoneCaller,
  private val smsSender: SmsSender,
  private val permissionRequester: PermissionRequester,
) : EventActionDispatcher {
  override fun dispatch(action: ResolvedEventAction) {
    Logger.i(TAG, "Dispatching event action: $action")
    when (action) {
      is ResolvedEventAction.OpenApp -> {
        applicationLauncher.launch(action.packageName)
      }
      is ResolvedEventAction.OpenLink -> {
        urlLauncher.launch(action.url)
      }
      is ResolvedEventAction.MakeCall -> {
        permissionRequester.request(
          Permissions.CALL_PHONE,
          onGranted = { phoneCaller.call(action.phoneNumber) }
        )
      }
      is ResolvedEventAction.SendSms -> {
        smsSender.send(action.phoneNumber, action.message)
      }
      is ResolvedEventAction.SendEmail -> {
        emailResolver.send(
          email = action.email,
          subject = action.subject,
          message = action.body,
          file = action.attachmentFile,
        )
      }
    }
  }

  companion object {
    private const val TAG = "EventActionDispatcher"
  }
}

@Composable
fun rememberEventActionDispatcher(): EventActionDispatcher {
  return EventActionDispatcherImpl(
    applicationLauncher = rememberApplicationLauncher(),
    urlLauncher = rememberUrlLauncher(),
    emailResolver = rememberSendEmailResolver(BuildConfig.APPLICATION_ID),
    phoneCaller = rememberPhoneCaller(),
    smsSender = rememberSmsSender(),
    permissionRequester = rememberPermissionRequesterRationale(),
  )
}
