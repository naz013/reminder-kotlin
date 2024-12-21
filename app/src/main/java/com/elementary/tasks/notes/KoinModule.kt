package com.elementary.tasks.notes

import com.elementary.tasks.core.cloud.converters.NoteToOldNoteConverter
import com.elementary.tasks.core.data.repository.NoteImageRepository
import com.elementary.tasks.notes.create.CreateNoteViewModel
import com.elementary.tasks.notes.list.NotesViewModel
import com.elementary.tasks.notes.list.archived.ArchivedNotesViewModel
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.elementary.tasks.notes.preview.NotePreviewViewModel
import com.elementary.tasks.notes.preview.reminders.ReminderToUiNoteAttachedReminder
import com.elementary.tasks.notes.work.DeleteNoteBackupWorker
import com.elementary.tasks.notes.work.NoteSingleBackupWorker
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val noteModule = module {
  factory { ReminderToUiNoteAttachedReminder(get()) }

  factory { NoteToOldNoteConverter(get()) }

  single { ImagesSingleton(get()) }

  factory { NoteImageRepository(get()) }

  worker { DeleteNoteBackupWorker(get(), get(), get(), get()) }
  worker { NoteSingleBackupWorker(get(), get(), get(), get()) }

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
      get()
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
      get()
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
      get()
    )
  }
  viewModel { (id: String) ->
    NotePreviewViewModel(
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
      get()
    )
  }
}
