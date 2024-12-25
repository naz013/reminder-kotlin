package com.github.naz013.usecase.notes

import org.koin.dsl.module

val notesUseCaseModule = module {
  factory { GetAllNotesUseCase(get()) }
  factory { SearchNotesByTextUseCase(get()) }
  factory { GetNoteByIdUseCase(get()) }
}
