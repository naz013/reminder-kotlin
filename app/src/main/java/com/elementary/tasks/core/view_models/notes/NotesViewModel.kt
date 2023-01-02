package com.elementary.tasks.core.view_models.notes

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.DispatcherProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class NotesViewModel(
  dispatcherProvider: DispatcherProvider,
  workerLauncher: WorkerLauncher,
  private val backupTool: BackupTool,
  notesDao: NotesDao
) : BaseNotesViewModel(dispatcherProvider, workerLauncher, notesDao) {

  private val _sharedFile = mutableLiveDataOf<Pair<NoteWithImages, File>>()
  val sharedFile = _sharedFile.toLiveData()

  val notes = notesDao.loadAll()

  fun shareNote(note: NoteWithImages) {
    viewModelScope.launch(dispatcherProvider.default()) {
      postInProgress(true)
      val file = runBlocking {
        backupTool.noteToFile(note)
      }
      postInProgress(false)
      if (file != null) {
        _sharedFile.postValue(Pair(note, file))
      } else {
        postError("Failed to send Note")
      }
    }
  }
}
