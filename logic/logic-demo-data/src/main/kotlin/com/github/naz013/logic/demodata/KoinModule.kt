package com.github.naz013.logic.demodata

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val logicDemoDataModule = module {
  factoryOf(::InsertDemoDataUseCase)
}
