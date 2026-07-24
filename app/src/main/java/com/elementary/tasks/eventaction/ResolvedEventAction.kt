package com.elementary.tasks.eventaction

import java.io.File

sealed interface ResolvedEventAction {
  data class MakeCall(
    val phoneNumber: String,
  ) : ResolvedEventAction

  data class SendSms(
    val phoneNumber: String,
    val message: String,
  ) : ResolvedEventAction

  data class OpenApp(
    val packageName: String,
  ) : ResolvedEventAction

  data class OpenLink(
    val url: String,
  ) : ResolvedEventAction

  data class SendEmail(
    val email: String,
    val subject: String,
    val body: String,
    val attachmentFile: File?,
  ) : ResolvedEventAction
}
