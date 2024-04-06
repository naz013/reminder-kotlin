package com.elementary.tasks.core.os

import com.elementary.tasks.core.os.contacts.ContactsReader
import org.koin.dsl.module

val osModule = module {
  factory { ContactsReader(get()) }
  single { ContextProvider(get()) }
  factory { SystemServiceProvider(get()) }
  factory { PackageManagerWrapper(get()) }

  single { IntentDataHolder() }

  factory { UnitsConverter(get()) }

  factory { ColorProvider(get()) }

  factory { ContextSwitcher(get()) }
}
