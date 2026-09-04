package com.github.naz013.ui.common

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.datecalc.NowDateTimeProviderImpl
import com.github.naz013.ui.common.compose.foundation.component.SettingsHighlightController
import com.github.naz013.ui.common.compose.foundation.share.FileIntentSender
import com.github.naz013.ui.common.compose.foundation.share.FileIntentSenderImpl
import com.github.naz013.ui.common.compose.foundation.telephony.ApplicationLauncher
import com.github.naz013.ui.common.compose.foundation.telephony.ApplicationLauncherImpl
import com.github.naz013.ui.common.compose.foundation.telephony.PhoneCaller
import com.github.naz013.ui.common.compose.foundation.telephony.PhoneCallerImpl
import com.github.naz013.ui.common.compose.foundation.telephony.SmsSender
import com.github.naz013.ui.common.compose.foundation.telephony.SmsSenderImpl
import com.github.naz013.ui.common.compose.foundation.telephony.UrlLauncher
import com.github.naz013.ui.common.compose.foundation.telephony.UrlLauncherImpl
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import com.github.naz013.ui.common.locale.Language
import com.github.naz013.ui.common.login.LoginStateViewModel
import com.github.naz013.ui.common.login.PinLoginViewModel
import com.github.naz013.ui.common.theme.ColorProvider
import com.github.naz013.ui.common.theme.DarkModeState
import com.github.naz013.ui.common.theme.ThemeModeHolder
import com.github.naz013.ui.common.theme.ThemeProvider
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val uiCommonModule = module {
  singleOf(::ThemeProvider)
  singleOf(::ThemeModeHolder)
  singleOf(::Language)
  singleOf(::ModelDateTimeFormatter)
  singleOf<NowDateTimeProvider>(::NowDateTimeProviderImpl)
  singleOf(::SettingsHighlightController)

  factoryOf(::ColorProvider)
  factoryOf(::UnitsConverter)

  factory<DarkModeState> { get<ThemeProvider>() }

  factory { PhoneCallerImpl(get()) as PhoneCaller }
  factory { SmsSenderImpl(get()) as SmsSender }
  factory { FileIntentSenderImpl(get(), get()) as FileIntentSender }
  factory { ApplicationLauncherImpl(get()) as ApplicationLauncher }
  factory { UrlLauncherImpl(get()) as UrlLauncher }

  viewModelOf(::LoginStateViewModel)
  viewModelOf(::PinLoginViewModel)
}
