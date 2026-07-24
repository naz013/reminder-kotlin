package com.elementary.tasks.telephony

import com.elementary.tasks.share.FileIntentSender
import com.elementary.tasks.share.FileIntentSenderImpl
import org.koin.dsl.module

val intentModule = module {
  factory { PhoneCallerImpl(get()) as PhoneCaller }
  factory { SmsSenderImpl(get()) as SmsSender }
  factory { FileIntentSenderImpl(get()) as FileIntentSender }
  factory { ApplicationLauncherImpl(get()) as ApplicationLauncher }
  factory { UrlLauncherImpl(get()) as UrlLauncher }
}
