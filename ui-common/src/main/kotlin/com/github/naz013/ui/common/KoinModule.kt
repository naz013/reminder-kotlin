package com.github.naz013.ui.common

import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import com.github.naz013.ui.common.locale.Language
import com.github.naz013.ui.common.login.LoginStateViewModel
import com.github.naz013.ui.common.theme.ColorProvider
import com.github.naz013.ui.common.theme.ThemeProvider
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val uiCommonModule = module {
  single { ThemeProvider(get(), get()) }
  single { Language(get(), get(), get()) }
  single { Dialogues(get()) }
  single { ModelDateTimeFormatter(get(), get(), get()) }

  factory { ColorProvider(get()) }
  factory { UnitsConverter(get()) }

  viewModel { LoginStateViewModel() }
}
