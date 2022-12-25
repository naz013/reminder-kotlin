package com.elementary.tasks.core.view_models.notes

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.view_models.BaseProgressViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.notes.work.DeleteNoteBackupWorker
import com.elementary.tasks.notes.work.NoteSingleBackupWorker
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class BaseNotesViewModel(
  dispatcherProvider: DispatcherProvider,
  protected val workerLauncher: WorkerLauncher,
  protected val notesDao: NotesDao
) : BaseProgressViewModel(dispatcherProvider) {

  fun deleteNote(noteWithImages: NoteWithImages) {
    val note = noteWithImages.note ?: return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      notesDao.delete(note)
      for (image in noteWithImages.images) {
        notesDao.delete(image)
      }
      workerLauncher.startWork(DeleteNoteBackupWorker::class.java, Constants.INTENT_ID, note.key)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  fun saveNoteColor(note: NoteWithImages, color: Int) {
    val v = note.note ?: return
    note.note?.color = color
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      v.updatedAt = DateTimeManager.gmtDateTime
      notesDao.insert(v)
      workerLauncher.startWork(NoteSingleBackupWorker::class.java, Constants.INTENT_ID, v.key)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  fun saveNote(note: NoteWithImages) {
    val v = note.note ?: return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      v.updatedAt = DateTimeManager.gmtDateTime
      saveImages(note.images, v.key)
      notesDao.insert(v)
      workerLauncher.startWork(NoteSingleBackupWorker::class.java, Constants.INTENT_ID, v.key)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  protected fun saveImages(list: List<ImageFile>, id: String) {
    val oldList = notesDao.getImages(id)
    Timber.d("saveImages: ${oldList.size}")
    for (image in oldList) {
      Timber.d("saveImages: delete -> ${image.id}, ${image.noteId}")
      notesDao.delete(image)
    }
    if (list.isNotEmpty()) {
      val newList = list.map {
        it.noteId = id
        it
      }
      Timber.d("saveImages: new list -> $newList")
      notesDao.insertAll(newList)
    }
  }
}
