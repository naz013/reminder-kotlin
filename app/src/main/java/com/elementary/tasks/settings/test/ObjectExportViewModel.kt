package com.elementary.tasks.settings.test

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.common.ContextProvider
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.note.OldNote
import com.github.naz013.domain.reminder.migration.toReminder
import com.github.naz013.domain.reminder.migration.toReminderGroup
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
        dataConverter.toOutputStream(obj, outputStream)
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
      ObjectExportType.ReminderV2, ObjectExportType.ReminderV1 ->
        reminderV2Repository.getAll().map {
          ObjectExportItem(it.uuId, it.summary + "\nID: " + it.uuId)
        }

      ObjectExportType.NoteV2, ObjectExportType.NoteV1 ->
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

      ObjectExportType.GroupV2, ObjectExportType.GroupV1 ->
        groupV2Repository.getAll().map {
          ObjectExportItem(it.uuId, it.title + "\nID: " + it.uuId)
        }
    }

  private suspend fun getObject(
    objectType: ObjectExportType,
    itemId: String,
  ): Any? =
    when (objectType) {
      ObjectExportType.ReminderV2 -> reminderV2Repository.getById(itemId)
      ObjectExportType.ReminderV1 -> reminderV2Repository.getById(itemId)?.toReminder()
      ObjectExportType.NoteV2 -> noteRepository.getById(itemId)?.toSharedNote()
      ObjectExportType.NoteV1 -> noteRepository.getById(itemId)?.toOldNote()
      ObjectExportType.Birthday -> birthdayRepository.getById(itemId)
      ObjectExportType.Place -> placeRepository.getById(itemId)
      ObjectExportType.GroupV2 -> groupV2Repository.getById(itemId)
      ObjectExportType.GroupV1 -> groupV2Repository.getById(itemId)?.toReminderGroup()
    }

  private fun getFileExt(objectType: ObjectExportType): String =
    when (objectType) {
      ObjectExportType.ReminderV2 -> FileConfig.FILE_NAME_REMINDER_V2
      ObjectExportType.ReminderV1 -> FileConfig.FILE_NAME_REMINDER
      ObjectExportType.NoteV2 -> SharedNote.FILE_EXTENSION
      ObjectExportType.NoteV1 -> FileConfig.FILE_NAME_NOTE
      ObjectExportType.Birthday -> FileConfig.FILE_NAME_BIRTHDAY
      ObjectExportType.Place -> FileConfig.FILE_NAME_PLACE
      ObjectExportType.GroupV2 -> FileConfig.FILE_NAME_GROUP_V2
      ObjectExportType.GroupV1 -> FileConfig.FILE_NAME_GROUP
    }

  private fun NoteWithImages.toSharedNote(): SharedNote {
    return SharedNote(
      text = this.note?.summary ?: "",
      title = this.note?.title ?: "",
      titleFontSize = this.note?.titleFontSize ?: -1,
      titleFontStyle = this.note?.titleFontStyle ?: -1,
      id = this.note?.key ?: "",
      date = this.note?.date ?: "",
      color = this.note?.color ?: 0,
      style = this.note?.style ?: 0,
      palette = this.note?.palette ?: 0,
      updatedAt = this.note?.updatedAt,
      opacity = this.note?.opacity ?: 100,
      fontSize = this.note?.fontSize ?: -1,
    )
  }

  private fun NoteWithImages.toOldNote(): OldNote {
    return OldNote(
      summary = this.getSummary(),
      key = this.getKey(),
      date = this.getGmtTime(),
      color = this.getColor(),
      palette = this.getPalette(),
      style = this.getStyle(),
      fontSize = this.getFontSize(),
      updatedAt = this.note?.updatedAt,
      images = emptyList(),
      uniqueId = this.note?.uniqueId ?: 0,
      archived = this.note?.archived ?: false,
      version = this.note?.version ?: 0,
    )
  }

  companion object {
    private const val TAG = "OETest"
  }
}
