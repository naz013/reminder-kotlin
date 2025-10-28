package com.elementary.tasks.notes.list.archived

import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.note.UiNoteListAdapter
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.notes.list.NoteSortProcessor
import com.elementary.tasks.notes.list.SearchableNotesData
import com.elementary.tasks.notes.usecase.ChangeNoteArchiveStateUseCase
import com.elementary.tasks.notes.usecase.DeleteNoteUseCase
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.repository.NoteRepository
import kotlinx.coroutines.launch
import java.io.File

class ArchivedNotesViewModel(
  dispatcherProvider: DispatcherProvider,
  private val uiNoteListAdapter: UiNoteListAdapter,
  private val prefs: Prefs,
  private val noteRepository: NoteRepository,
  private val deleteNoteUseCase: DeleteNoteUseCase,
  private val changeNoteArchiveStateUseCase: ChangeNoteArchiveStateUseCase
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
      changeNoteArchiveStateUseCase(id, false)

      notesData.refresh()

      postInProgress(false)
      postCommand(Commands.UPDATED)
    }
  }

  fun deleteNote(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      deleteNoteUseCase(id)

      notesData.refresh()

      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
