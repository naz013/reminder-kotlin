package com.github.naz013.common

import com.github.naz013.common.contacts.ContactsReader
import com.github.naz013.common.intent.IntentFactory
import com.github.naz013.common.system.SystemInfoImpl
import com.github.naz013.platform.SystemInfo
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val platformCommonModule = module {
  singleOf(::TextProvider)
  singleOf(::ContextProvider)
  factoryOf(::PackageManagerWrapper)
  factoryOf(::ContactsReader)
  factory { SystemInfoImpl(get(), get()) as SystemInfo }
  factoryOf(::IntentFactory)
}
