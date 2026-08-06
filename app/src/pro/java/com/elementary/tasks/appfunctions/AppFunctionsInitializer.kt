package com.elementary.tasks.appfunctions

import com.github.naz013.appfunctions.appFunctionsModule
import org.koin.core.context.loadKoinModules

/** Registers the `:appfunctions` Koin bindings so the AppFunctionService (declared in that
 * module's manifest) can resolve its dependencies. Only compiled into the PRO flavor. */
object AppFunctionsInitializer {
  fun init() {
    loadKoinModules(appFunctionsModule)
  }
}
