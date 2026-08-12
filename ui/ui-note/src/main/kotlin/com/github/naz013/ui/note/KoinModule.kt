package com.github.naz013.ui.note

import org.koin.dsl.module

val uiNoteModule = module {
  factory { UiNoteImagesAdapter() }
  factory { UiNoteListItemAdapter(get(), get()) }
  factory { NoteColorEngine(get(), get()) }
}
