package com.github.naz013.ui.common

import com.github.naz013.datecalc.NowDateTimeProvider
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
  singleOf(::NowDateTimeProvider)

  factoryOf(::ColorProvider)
  factoryOf(::UnitsConverter)

  factory<DarkModeState> { get<ThemeProvider>() }

  viewModelOf(::LoginStateViewModel)
  viewModelOf(::PinLoginViewModel)
}
