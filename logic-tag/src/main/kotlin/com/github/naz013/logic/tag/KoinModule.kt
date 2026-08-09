package com.github.naz013.logic.tag

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val logicTagModule = module {
  factoryOf(::ToggleTagAssignmentUseCase)
}
