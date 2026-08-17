package com.github.naz013.files

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val fileModule = module {
  factory { DataConverterImpl() as DataConverter }
  factory { AndroidDataConverterImpl(get(), get()) as AndroidDataConverter }
  singleOf(::BackupTool)
}
