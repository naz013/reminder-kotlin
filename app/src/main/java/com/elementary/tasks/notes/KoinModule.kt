package com.elementary.tasks.notes

import com.elementary.tasks.core.cloud.converters.NoteToOldNoteConverter
import com.elementary.tasks.core.data.repository.NoteImageRepository
import com.elementary.tasks.notes.create.CreateNoteViewModel
import com.elementary.tasks.notes.list.NotesViewModel
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.elementary.tasks.notes.preview.PreviewNoteViewModel
import com.elementary.tasks.notes.preview.reminders.ReminderToUiNoteAttachedReminder
import com.elementary.tasks.notes.usecase.ChangeNoteArchiveStateUseCase
import com.elementary.tasks.notes.usecase.CreateSharedNoteFileUseCase
import com.elementary.tasks.notes.usecase.DeleteNoteUseCase
import com.elementary.tasks.notes.usecase.SaveNoteUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val noteModule = module {
  factoryOf(::DeleteNoteUseCase)
  factoryOf(::SaveNoteUseCase)
  factoryOf(::ChangeNoteArchiveStateUseCase)

  factoryOf(::CreateSharedNoteFileUseCase)

  factoryOf(::ReminderToUiNoteAttachedReminder)

  factoryOf(::NoteToOldNoteConverter)

  singleOf(::ImagesSingleton)

  singleOf(::NoteImageRepository)

  viewModelOf(::CreateNoteViewModel)
  viewModel { (isArchived: Boolean) ->
    NotesViewModel(
      get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
      isArchived
    )
  }
  viewModel { (id: String) ->
    PreviewNoteViewModel(
      id,
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
    )
  }
}
