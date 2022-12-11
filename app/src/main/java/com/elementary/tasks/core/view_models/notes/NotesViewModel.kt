package com.elementary.tasks.core.view_models.notes

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.view_models.DispatcherProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class NotesViewModel(
  appDb: AppDb,
  prefs: Prefs,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  private val backupTool: BackupTool,
  notesDao: NotesDao
) : BaseNotesViewModel(
  prefs,
  dispatcherProvider,
  workManagerProvider,
  notesDao
) {

  private val _sharedFile = mutableLiveDataOf<Pair<NoteWithImages, File>>()
  val sharedFile = _sharedFile.toLiveData()

  val notes = appDb.notesDao().loadAll()

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
