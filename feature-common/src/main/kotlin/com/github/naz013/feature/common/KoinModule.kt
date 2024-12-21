package com.github.naz013.feature.common

import com.github.naz013.feature.common.android.ContextProvider
import com.github.naz013.feature.common.android.SystemServiceProvider
import com.github.naz013.feature.common.android.TextProvider
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import org.koin.dsl.module

val featureCommonModule = module {
  factory { DispatcherProvider() }

  single { ContextProvider(get()) }
  single { TextProvider(get()) }

  factory { SystemServiceProvider(get()) }
}
