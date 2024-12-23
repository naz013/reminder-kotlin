package com.elementary.tasks.navigation

import com.github.naz013.navigation.Navigator
import org.koin.dsl.module

val navigationModule = module {
  single { NavigatorImpl() }
  single { get<NavigatorImpl>() as NavigationObservable }
  single { get<NavigatorImpl>() as Navigator }
}
