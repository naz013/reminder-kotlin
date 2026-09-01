package com.github.naz013.logic.note

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val logicNoteModule = module {
  factoryOf(::SaveNoteUseCase)
  factoryOf(::InsertDemoNotesUseCase)
}
