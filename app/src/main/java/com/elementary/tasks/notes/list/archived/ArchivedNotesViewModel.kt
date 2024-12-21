package com.elementary.tasks.notes.list.archived

import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.note.UiNoteListAdapter
import com.elementary.tasks.core.data.repository.NoteImageRepository
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.notes.list.NoteSortProcessor
import com.elementary.tasks.notes.list.SearchableNotesData
import com.elementary.tasks.notes.work.DeleteNoteBackupWorker
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.repository.NoteRepository
import kotlinx.coroutines.launch
import java.io.File

class ArchivedNotesViewModel(
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val textProvider: TextProvider,
  private val uiNoteListAdapter: UiNoteListAdapter,
  private val prefs: Prefs,
  private val noteRepository: NoteRepository,
  private val noteImageRepository: NoteImageRepository
) : BaseProgressViewModel(dispatcherProvider) {

  private val _sharedFile = mutableLiveDataOf<Pair<NoteWithImages, File>>()
  val sharedFile = _sharedFile.toLiveData()

  private val noteSortProcessor = NoteSortProcessor()
  private val notesData = SearchableNotesData(
    dispatcherProvider = dispatcherProvider,
    parentScope = viewModelScope,
    noteRepository = noteRepository,
    isArchived = true
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

  fun unArchive(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = noteRepository.getById(id)
      if (noteWithImages == null) {
        postInProgress(false)
        postError(textProvider.getText(R.string.notes_failed_to_update))
        return@launch
      }

      val note = noteWithImages.note
      if (note == null) {
        postInProgress(false)
        postError(textProvider.getText(R.string.notes_failed_to_update))
        return@launch
      }

      note.archived = false
      noteRepository.save(note)

      workerLauncher.startWork(DeleteNoteBackupWorker::class.java, Constants.INTENT_ID, note.key)

      notesData.refresh()

      postInProgress(false)
      postCommand(Commands.UPDATED)
    }
  }

  fun deleteNote(id: String) {
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
      noteRepository.delete(note.key)
      noteRepository.deleteImageForNote(note.key)
      noteImageRepository.clearFolder(note.key)
      workerLauncher.startWork(DeleteNoteBackupWorker::class.java, Constants.INTENT_ID, note.key)

      notesData.refresh()

      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
