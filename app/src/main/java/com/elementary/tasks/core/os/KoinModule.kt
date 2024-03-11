package com.elementary.tasks.core.os

import com.elementary.tasks.core.os.contacts.ContactsReader
import com.elementary.tasks.core.os.power.WakeupManager
import org.koin.dsl.module

val osModule = module {
  single { WakeupManager(get()) }

  single { ContactsReader(get()) }
  single { ContextProvider(get()) }
  single { SystemServiceProvider(get()) }
  single { InputMethodManagerWrapper(get()) }
  single { PackageManagerWrapper(get()) }

  single { IntentDataHolder() }

  single { UnitsConverter(get()) }

  single { ColorProvider(get()) }

  factory { ContextSwitcher(get()) }
}
