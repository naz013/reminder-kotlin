package com.github.naz013.feature.note

import com.github.naz013.feature.note.create.ImageLoader
import com.github.naz013.feature.note.create.NoteEditViewModel
import com.github.naz013.feature.note.create.drop.DroppedContentParser
import com.github.naz013.feature.note.create.images.ImageDecoder
import com.github.naz013.feature.note.image.NoteImageMigration
import com.github.naz013.feature.note.image.NoteImageRepository
import com.github.naz013.feature.note.list.NotesViewModel
import com.github.naz013.feature.note.preview.ImagePreviewViewModel
import com.github.naz013.feature.note.preview.ImagesSingleton
import com.github.naz013.feature.note.preview.PreviewNoteViewModel
import com.github.naz013.feature.note.preview.reminders.ReminderToUiNoteAttachedReminder
import com.github.naz013.feature.note.usecase.ChangeNoteArchiveStateUseCase
import com.github.naz013.feature.note.usecase.CreateSharedNoteFileUseCase
import com.github.naz013.feature.note.usecase.DeleteNoteUseCase
import com.github.naz013.feature.note.usecase.MergeNotesUseCase
import com.github.naz013.feature.note.usecase.SaveNoteUseCase
import com.github.naz013.feature.note.usecase.TogglePinnedNoteUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureNoteModule = module {
  factoryOf(::DeleteNoteUseCase)
  factoryOf(::MergeNotesUseCase)
  factoryOf(::SaveNoteUseCase)
  factoryOf(::ChangeNoteArchiveStateUseCase)
  factoryOf(::TogglePinnedNoteUseCase)

  factoryOf(::CreateSharedNoteFileUseCase)

  factoryOf(::ReminderToUiNoteAttachedReminder)

  factoryOf(::UiNoteEditAdapter)
  factoryOf(::UiNotePreviewAdapter)
  factoryOf(::UiNoteNotificationAdapter)

  singleOf(::ImagesSingleton)
  factoryOf(::ImageLoader)

  singleOf(::NoteImageRepository)
  factoryOf(::NoteImageMigration)
  factoryOf(::ImageDecoder)
  factoryOf(::DroppedContentParser)

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
      get(),
      get(),
      get(),
    )
  }
}
