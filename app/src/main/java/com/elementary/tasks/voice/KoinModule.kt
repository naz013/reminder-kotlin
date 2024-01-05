package com.elementary.tasks.voice

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val voiceModule = module {
  factory { VoiceCommandProcessor(get(), get(), get(), get(), get()) }
  factory { ContactsHelper(get()) }

  viewModel { (id: String) -> VoiceResultDialogViewModel(id, get(), get(), get()) }
  viewModel {
    ConversationViewModel(
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get()
    )
  }
}
