package com.elementary.tasks.notes

import com.elementary.tasks.core.data.repository.NoteImageRepository
import com.elementary.tasks.notes.create.NoteEditViewModel
import com.elementary.tasks.notes.list.NotesViewModel
import com.elementary.tasks.notes.preview.ImagePreviewViewModel
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
import org.koin.dsl.module

val noteModule =
  module {
    factoryOf(::DeleteNoteUseCase)
    factoryOf(::SaveNoteUseCase)
    factoryOf(::ChangeNoteArchiveStateUseCase)

    factoryOf(::CreateSharedNoteFileUseCase)

    factoryOf(::ReminderToUiNoteAttachedReminder)

    factoryOf(::NoteColorEngine)

    singleOf(::ImagesSingleton)

    singleOf(::NoteImageRepository)

    viewModel { (id: String?, sharedText: String?, sharedImageUris: List<String>?, fromIntentData: Boolean) ->
      NoteEditViewModel(
        id,
        sharedText,
        sharedImageUris,
        fromIntentData,
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
        get(),
        get(),
        get(),
        get(),
      )
    }
    viewModel { (position: Int) -> ImagePreviewViewModel(position, get(), get()) }
    viewModel { (isArchived: Boolean) ->
      NotesViewModel(
        isArchived = isArchived,
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
        get(),
        get(),
      )
    }
  }
