package com.elementary.tasks.notes

import com.elementary.tasks.core.cloud.converters.NoteToOldNoteConverter
import com.elementary.tasks.core.data.repository.NoteImageRepository
import com.elementary.tasks.notes.create.CreateNoteViewModel
import com.elementary.tasks.notes.list.NotesViewModel
import com.elementary.tasks.notes.list.archived.ArchivedNotesViewModel
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.elementary.tasks.notes.preview.PreviewNoteViewModel
import com.elementary.tasks.notes.preview.reminders.ReminderToUiNoteAttachedReminder
import com.elementary.tasks.notes.usecase.ChangeNoteArchiveStateUseCase
import com.elementary.tasks.notes.usecase.CreateSharedNoteFileUseCase
import com.elementary.tasks.notes.usecase.DeleteNoteUseCase
import com.elementary.tasks.notes.usecase.SaveNoteUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val noteModule = module {
  factory { DeleteNoteUseCase(get(), get(), get()) }
  factory { SaveNoteUseCase(get(), get(), get()) }
  factory { ChangeNoteArchiveStateUseCase(get(), get()) }

  factory { CreateSharedNoteFileUseCase(get()) }

  factory { ReminderToUiNoteAttachedReminder(get()) }

  factory { NoteToOldNoteConverter() }

  single { ImagesSingleton(get()) }

  factory { NoteImageRepository(get()) }

  viewModel {
    CreateNoteViewModel(
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
    )
  }
  viewModel {
    NotesViewModel(
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
  viewModel {
    ArchivedNotesViewModel(
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
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
