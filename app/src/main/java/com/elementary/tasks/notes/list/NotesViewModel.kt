package com.elementary.tasks.notes.list

import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.note.UiNoteListAdapter
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.livedata.SearchableLiveData
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.notes.work.DeleteNoteBackupWorker
import com.elementary.tasks.notes.work.NoteSingleBackupWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import java.io.File

class NotesViewModel(
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val backupTool: BackupTool,
  private val notesDao: NotesDao,
  private val textProvider: TextProvider,
  private val uiNoteListAdapter: UiNoteListAdapter,
  private val prefs: Prefs
) : BaseProgressViewModel(dispatcherProvider) {

  private val _sharedFile = mutableLiveDataOf<Pair<NoteWithImages, File>>()
  val sharedFile = _sharedFile.toLiveData()

  private val noteSortProcessor = NoteSortProcessor()
  private val notesData = SearchableNotesData(dispatcherProvider, viewModelScope, notesDao)
  val notes = Transformations.map(notesData) { list ->
    noteSortProcessor.apply(list.map { uiNoteListAdapter.convert(it) }, prefs.noteOrder)
  }

  fun onSearchUpdate(query: String) {
    notesData.onNewQuery(query)
  }

  fun onOrderChanged() {
    notesData.refresh()
  }

  fun shareNote(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val note = notesDao.getById(id)
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
      val noteWithImages = notesDao.getById(id)
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
      notesDao.delete(note)
      for (image in noteWithImages.images) {
        notesDao.delete(image)
      }
      workerLauncher.startWork(DeleteNoteBackupWorker::class.java, Constants.INTENT_ID, note.key)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  fun saveNoteColor(id: String, color: Int) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = notesDao.getById(id)
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
      notesDao.insert(note)
      workerLauncher.startWork(NoteSingleBackupWorker::class.java, Constants.INTENT_ID, note.key)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  internal class SearchableNotesData(
    dispatcherProvider: DispatcherProvider,
    parentScope: CoroutineScope,
    private val notesDao: NotesDao
  ) : SearchableLiveData<List<NoteWithImages>>(parentScope + dispatcherProvider.default()) {

    override fun runQuery(query: String): List<NoteWithImages> {
      return if (query.isEmpty()) {
        notesDao.getAll()
      } else {
        notesDao.searchByText(query.lowercase())
      }
    }
  }
}
