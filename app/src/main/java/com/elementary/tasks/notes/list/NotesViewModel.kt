package com.elementary.tasks.notes.list

import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.note.UiNoteListAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteNotificationAdapter
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.notes.usecase.ChangeNoteArchiveStateUseCase
import com.elementary.tasks.notes.usecase.DeleteNoteUseCase
import com.elementary.tasks.notes.usecase.SaveNoteUseCase
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.repository.NoteRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class NotesViewModel(
  dispatcherProvider: DispatcherProvider,
  private val backupTool: BackupTool,
  private val textProvider: TextProvider,
  private val uiNoteListAdapter: UiNoteListAdapter,
  private val prefs: Prefs,
  private val noteRepository: NoteRepository,
  private val uiNoteNotificationAdapter: UiNoteNotificationAdapter,
  private val notifier: Notifier,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val deleteNoteUseCase: DeleteNoteUseCase,
  private val changeNoteArchiveStateUseCase: ChangeNoteArchiveStateUseCase,
  private val saveNoteUseCase: SaveNoteUseCase
) : BaseProgressViewModel(dispatcherProvider) {

  private val _sharedFile = mutableLiveDataOf<Pair<NoteWithImages, File>>()
  val sharedFile = _sharedFile.toLiveData()

  private val noteSortProcessor = NoteSortProcessor()
  private val notesData = SearchableNotesData(
    dispatcherProvider = dispatcherProvider,
    parentScope = viewModelScope,
    noteRepository = noteRepository,
    isArchived = false
  )
  val notes = notesData.map { list ->
    noteSortProcessor.apply(list.map { uiNoteListAdapter.convert(it) }, prefs.noteOrder)
  }

  fun onSearchUpdate(query: String) {
    notesData.onNewQuery(query)
  }

  fun onOrderChanged() {
    notesData.refresh()
  }

  fun moveToArchive(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      changeNoteArchiveStateUseCase(id, true)

      notesData.refresh()

      postInProgress(false)
      postCommand(Commands.UPDATED)

      withUIContext {
        appWidgetUpdater.updateNotesWidget()
      }
    }
  }

  fun shareNote(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val note = noteRepository.getById(id)
      if (note == null) {
        postInProgress(false)
        postError(textProvider.getText(R.string.failed_to_send_note))
        return@launch
      }
      val file = runBlocking {
        backupTool.noteToFile(note)
      }
      postInProgress(false)
      if (file != null) {
        _sharedFile.postValue(Pair(note, file))
      } else {
        postError(textProvider.getText(R.string.failed_to_send_note))
      }
    }
  }

  fun deleteNote(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      deleteNoteUseCase(id)

      notesData.refresh()

      postInProgress(false)
      postCommand(Commands.DELETED)

      withUIContext {
        appWidgetUpdater.updateNotesWidget()
      }
    }
  }

  fun saveNoteColor(id: String, color: Int) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = noteRepository.getById(id)
      if (noteWithImages == null) {
        postInProgress(false)
        postCommand(Commands.FAILED)
        return@launch
      }
      val note = noteWithImages.note
      if (note == null) {
        postInProgress(false)
        postCommand(Commands.FAILED)
        return@launch
      }
      note.color = color
      note.updatedAt = DateTimeManager.gmtDateTime
      saveNoteUseCase(noteWithImages)

      notesData.refresh()

      postInProgress(false)
      postCommand(Commands.SAVED)

      withUIContext {
        appWidgetUpdater.updateNotesWidget()
      }
    }
  }

  fun showNoteInNotification(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = noteRepository.getById(id) ?: return@launch
      uiNoteNotificationAdapter.convert(noteWithImages).also {
        withUIContext { notifier.showNoteNotification(it) }
      }
    }
  }
}
