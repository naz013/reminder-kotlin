package com.elementary.tasks.navigation

import com.elementary.tasks.navigation.nav3.AppNavBridge
import com.elementary.tasks.navigation.nav3.AppNavGraphViewModel
import com.github.naz013.navigation.Navigator
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val navigationModule =
  module {
    single { NavigatorImpl() }
    single { get<NavigatorImpl>() as NavigationObservable }
    single { get<NavigatorImpl>() as Navigator }

    single { NavigationDispatcher(get(), get()) }

    single { AppNavBridge() }

    viewModelOf(::AppNavGraphViewModel)
  }
