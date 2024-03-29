package com.elementary.tasks.notes.list.archived

import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.note.UiNoteListAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteNotificationAdapter
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.repository.NoteImageRepository
import com.elementary.tasks.core.data.repository.NoteRepository
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.notes.list.NoteSortProcessor
import com.elementary.tasks.notes.list.SearchableNotesData
import com.elementary.tasks.notes.work.DeleteNoteBackupWorker
import com.elementary.tasks.notes.work.NoteSingleBackupWorker
import kotlinx.coroutines.launch
import java.io.File

class ArchivedNotesViewModel(
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val backupTool: BackupTool,
  private val notesDao: NotesDao,
  private val textProvider: TextProvider,
  private val uiNoteListAdapter: UiNoteListAdapter,
  private val prefs: Prefs,
  private val noteRepository: NoteRepository,
  private val noteImageRepository: NoteImageRepository,
  private val uiNoteNotificationAdapter: UiNoteNotificationAdapter,
  private val notifier: Notifier
) : BaseProgressViewModel(dispatcherProvider) {

  private val _sharedFile = mutableLiveDataOf<Pair<NoteWithImages, File>>()
  val sharedFile = _sharedFile.toLiveData()

  private val noteSortProcessor = NoteSortProcessor()
  private val notesData = SearchableNotesData(
    dispatcherProvider = dispatcherProvider,
    parentScope = viewModelScope,
    notesDao = notesDao,
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
      val noteWithImages = notesDao.getById(id)
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
      notesDao.insert(note)

      workerLauncher.startWork(DeleteNoteBackupWorker::class.java, Constants.INTENT_ID, note.key)

      notesData.refresh()

      postInProgress(false)
      postCommand(Commands.UPDATED)
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
      noteImageRepository.clearFolder(note.key)
      workerLauncher.startWork(DeleteNoteBackupWorker::class.java, Constants.INTENT_ID, note.key)

      notesData.refresh()

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

      notesData.refresh()

      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  fun showNoteInNotification(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val noteWithImages = notesDao.getById(id) ?: return@launch
      uiNoteNotificationAdapter.convert(noteWithImages).also {
        withUIContext { notifier.showNoteNotification(it) }
      }
    }
  }
}
