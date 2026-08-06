package com.github.naz013.files

import org.koin.dsl.module

val fileModule = module {
  factory { DataConverterImpl() as DataConverter }
  factory { AndroidDataConverterImpl(get(), get()) as AndroidDataConverter }
}
