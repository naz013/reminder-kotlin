package com.github.naz013.common

import com.github.naz013.common.contacts.ContactsReader
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.datetime.NowDateTimeProvider
import org.koin.dsl.module

val platformCommonModule = module {
  single { TextProvider(get()) }
  single { ContextProvider(get()) }
  factory { PackageManagerWrapper(get()) }
  factory { ContactsReader(get()) }
  factory { DateTimeManager(NowDateTimeProvider(), get()) }
}
