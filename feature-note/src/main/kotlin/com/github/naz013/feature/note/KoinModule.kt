package com.github.naz013.feature.note

import org.koin.dsl.module

val featureNoteModule = module {
  factory { UiNoteImagesAdapter() }
  factory { UiNoteListItemAdapter(get(), get()) }
}
