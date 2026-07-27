package com.elementary.tasks.settings.test

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.cloud.converters.NoteToOldNoteConverter
import com.github.naz013.common.ContextProvider
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.files.DataConverter
import com.github.naz013.files.FileConfig
import com.github.naz013.files.model.SharedNote
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.ReminderV2Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ObjectExportViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val contextProvider: ContextProvider,
  private val reminderV2Repository: ReminderV2Repository,
  private val noteRepository: NoteRepository,
  private val birthdayRepository: BirthdayRepository,
  private val placeRepository: PlaceRepository,
  private val groupV2Repository: GroupV2Repository,
  private val noteToOldNoteConverter: NoteToOldNoteConverter,
  private val dataConverter: DataConverter,
) : ViewModel() {
  val state: StateFlow<ObjectExportState> field = MutableStateFlow(ObjectExportState())
  val navigationEvent: LiveData<Event<ObjectExportEvent>> field = mutableLiveEventOf()

  init {
    loadItems()
  }

  fun onObjectTypeSelected(objectType: ObjectExportType) {
    state.update { it.copy(objectType = objectType) }
    loadItems()
  }

  fun onItemClick(item: ObjectExportItem) {
    val fileName = item.title + getFileExt(state.value.objectType)
    navigationEvent.value = Event(ObjectExportEvent.RequestSaveLocation(fileName, item.id))
  }

  fun onSaveLocationPicked(
    itemId: String,
    uri: Uri,
  ) {
    val objectType = state.value.objectType
    viewModelScope.launch(dispatcherProvider.io()) {
      val obj = getObject(objectType, itemId)
      if (obj == null) {
        Logger.d(TAG, "Object is NULL, for id = $itemId")
        return@launch
      }

      val outputStream = contextProvider.context.contentResolver.openOutputStream(uri)
      if (outputStream == null) {
        Logger.d(TAG, "OutputStream is NULL")
        return@launch
      }

      try {
        if (objectType == ObjectExportType.Note) {
          val oldNote = noteToOldNoteConverter.toSharedNote(obj as NoteWithImages)
          if (oldNote == null) {
            Logger.d(TAG, "OldNote is NULL")
            return@launch
          }
          dataConverter.toOutputStream(oldNote, outputStream)
        } else {
          dataConverter.toOutputStream(obj, outputStream)
        }
      } catch (e: Exception) {
        Logger.e(TAG, "Failed to save object to stream: $e")
        return@launch
      }

      withContext(dispatcherProvider.main()) {
        navigationEvent.value = Event(ObjectExportEvent.ObjectSaved)
      }
    }
  }

  private fun loadItems() {
    val objectType = state.value.objectType
    viewModelScope.launch(dispatcherProvider.io()) {
      val items = loadItems(objectType)
      withContext(dispatcherProvider.main()) {
        state.update { it.copy(items = items) }
      }
    }
  }

  private suspend fun loadItems(objectType: ObjectExportType): List<ObjectExportItem> =
    when (objectType) {
      ObjectExportType.Reminder ->
        reminderV2Repository.getAll().map {
          ObjectExportItem(it.uuId, it.summary + "\nID: " + it.uuId)
        }

      ObjectExportType.Note ->
        noteRepository.getAll().map {
          ObjectExportItem(it.getKey(), it.getSummary() + "\nID: " + it.getKey())
        }

      ObjectExportType.Birthday ->
        birthdayRepository.getAll().map {
          ObjectExportItem(it.uuId, it.name + "\nID: " + it.uuId)
        }

      ObjectExportType.Place ->
        placeRepository.getAll().map {
          ObjectExportItem(it.id, it.name + "\nID: " + it.id)
        }

      ObjectExportType.Group ->
        groupV2Repository.getAll().map {
          ObjectExportItem(it.uuId, it.title + "\nID: " + it.uuId)
        }
    }

  private suspend fun getObject(
    objectType: ObjectExportType,
    itemId: String,
  ): Any? =
    when (objectType) {
      ObjectExportType.Reminder -> reminderV2Repository.getById(itemId)
      ObjectExportType.Note -> noteRepository.getById(itemId)
      ObjectExportType.Birthday -> birthdayRepository.getById(itemId)
      ObjectExportType.Place -> placeRepository.getById(itemId)
      ObjectExportType.Group -> groupV2Repository.getById(itemId)
    }

  private fun getFileExt(objectType: ObjectExportType): String =
    when (objectType) {
      ObjectExportType.Reminder -> FileConfig.FILE_NAME_REMINDER_V2
      ObjectExportType.Note -> SharedNote.FILE_EXTENSION
      ObjectExportType.Birthday -> FileConfig.FILE_NAME_BIRTHDAY
      ObjectExportType.Place -> FileConfig.FILE_NAME_PLACE
      ObjectExportType.Group -> FileConfig.FILE_NAME_GROUP_V2
    }

  companion object {
    private const val TAG = "OETest"
  }
}
