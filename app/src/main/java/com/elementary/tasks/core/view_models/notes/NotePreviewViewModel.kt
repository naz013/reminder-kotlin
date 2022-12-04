package com.elementary.tasks.core.view_models.notes

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.notes.work.DeleteNoteBackupWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class NotePreviewViewModel(
  key: String,
  appDb: AppDb,
  prefs: Prefs,
  calendarUtils: CalendarUtils,
  eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  private val backupTool: BackupTool
) : BaseNotesViewModel(
  appDb,
  prefs,
  calendarUtils,
  eventControlFactory,
  dispatcherProvider,
  workManagerProvider
) {

  private val _sharedFile = mutableLiveDataOf<Pair<NoteWithImages, File>>()
  val sharedFile = _sharedFile.toLiveData()

  val note = appDb.notesDao().loadById(key)
  val reminder = appDb.reminderDao().loadByNoteKey(if (key == "") "1" else key)

  var hasSameInDb: Boolean = false

  fun deleteNote() {
    val noteWithImages = note.value ?: return
    val note = noteWithImages.note ?: return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      runBlocking(dispatcherProvider.io()) {
        appDb.notesDao().delete(note)
        for (image in noteWithImages.images) {
          appDb.notesDao().delete(image)
        }
      }
      startWork(DeleteNoteBackupWorker::class.java, Constants.INTENT_ID, note.key)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  fun shareNote() {
    val note = note.value ?: return
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
