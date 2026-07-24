package com.elementary.tasks.notes.create

import android.content.ClipData
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.util.Patterns
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.request.ImageRequest
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.converters.NoteToOldNoteConverter
import com.elementary.tasks.core.data.adapter.note.UiNoteEditAdapter
import com.elementary.tasks.core.data.repository.NoteImageRepository
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.elementary.tasks.core.data.ui.note.UiNoteImageState
import com.elementary.tasks.core.utils.ImageLoader
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.notes.NoteColorEngine
import com.elementary.tasks.notes.SharedNote
import com.elementary.tasks.notes.create.drop.DroppedContentParser
import com.elementary.tasks.notes.create.images.ImageDecoder
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.elementary.tasks.notes.usecase.CreateSharedNoteFileUseCase
import com.elementary.tasks.notes.usecase.DeleteNoteUseCase
import com.elementary.tasks.notes.usecase.SaveNoteUseCase
import com.elementary.tasks.reminder.scheduling.usecase.ActivateReminderUseCase
import com.elementary.tasks.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.common.system.SystemInfo
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.font.FontParams
import com.github.naz013.domain.note.ImageFile
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

class NoteEditViewModel(
  private val id: String?,
  private val sharedText: String?,
  private val sharedImageUris: List<String>?,
  private val fromIntentData: Boolean,
  private val imageDecoder: ImageDecoder,
  private val dispatcherProvider: DispatcherProvider,
  private val noteRepository: NoteRepository,
  private val groupV2Repository: GroupV2Repository,
  private val reminderRepository: ReminderRepository,
  private val deleteReminderUseCase: DeleteReminderUseCase,
  private val prefs: Prefs,
  private val dateTimeManager: DateTimeManager,
  private val textProvider: TextProvider,
  private val contextProvider: ContextProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  private val uiNoteEditAdapter: UiNoteEditAdapter,
  private val noteImageRepository: NoteImageRepository,
  private val noteToOldNoteConverter: NoteToOldNoteConverter,
  private val intentDataReader: IntentDataReader,
  private val deleteNoteUseCase: DeleteNoteUseCase,
  private val saveNoteUseCase: SaveNoteUseCase,
  private val createSharedNoteFileUseCase: CreateSharedNoteFileUseCase,
  private val activateReminderUseCase: ActivateReminderUseCase,
  private val droppedContentParser: DroppedContentParser,
  private val imagesSingleton: ImagesSingleton,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val systemInfo: SystemInfo,
  private val imageLoader: ImageLoader,
  private val noteColorEngine: NoteColorEngine,
) : ViewModel() {

  val is24HourFormat: Boolean = prefs.is24HourFormat

  private val _state = MutableStateFlow(NoteEditState())
  val state: StateFlow<NoteEditState> = _state.asStateFlow()

  private val _textUpdate = mutableLiveDataOf<Event<TextUpdate>>()
  val textUpdate = _textUpdate.toLiveData()

  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  init {
    val colorCode = noteColorEngine.getColorCode(
      noteColorEngine.getLastPalette(),
      noteColorEngine.getLastColorCode(),
    )
    val opacity = noteColorEngine.getLasterOpacity()

    _state.update {
      it.copy(
        colorIndex = colorCode,
        opacity = opacity,
        fontSize =
          if (prefs.isNoteFontSizeRememberingEnabled) {
            prefs.lastNoteFontSize
          } else {
            FontParams.DEFAULT_FONT_SIZE
          },
        fontStyle =
          if (prefs.isNoteFontStyleRememberingEnabled) {
            prefs.lastNoteFontStyle
          } else {
            FontParams.DEFAULT_FONT_STYLE
          },
        titleFontSize =
          if (prefs.isNoteFontSizeRememberingEnabled) {
            prefs.lastNoteTitleFontSize
          } else {
            FontParams.DEFAULT_TITLE_FONT_SIZE
          },
        titleFontStyle =
          if (prefs.isNoteFontStyleRememberingEnabled) {
            prefs.lastNoteTitleFontStyle
          } else {
            FontParams.DEFAULT_FONT_STYLE
          },
        hasCamera = systemInfo.hasCamera,
        sliderColors = noteColorEngine.allColors(),
        noteColors = noteColorEngine.colorsFor(colorCode, opacity),
      )
    }
    onNewTime(LocalTime.now())
    onNewDate(LocalDate.now())

    load()
  }

  fun onDateClicked() {
    event.emit(
      ViewModelEvent.ShowDatePicker(
        date = _state.value.date,
        title = textProvider.getString(R.string.select_date),
      )
    )
  }

  fun onTimeClicked() {
    event.emit(
      ViewModelEvent.ShowTimePicker(
        time = _state.value.time,
        title = textProvider.getString(R.string.select_time),
      )
    )
  }

  fun onColorSelected(index: Int) {
    if (prefs.isNoteColorRememberingEnabled) {
      prefs.lastNoteColor = index
    }
    _state.update { it.copy(colorIndex = index, noteColors = noteColorEngine.colorsFor(index, it.opacity)) }
  }

  fun onOpacityChanged(value: Int) {
    prefs.noteColorOpacity = value
    _state.update { it.copy(opacity = value, noteColors = noteColorEngine.colorsFor(it.colorIndex, value)) }
  }

  fun onFontSizeChanged(value: Int) {
    if (_state.value.focusedField == NoteTextField.TITLE) {
      prefs.lastNoteTitleFontSize = value
      _state.update { it.copy(titleFontSize = value) }
    } else {
      prefs.lastNoteFontSize = value
      _state.update { it.copy(fontSize = value) }
    }
  }

  fun onFontStyleChanged(value: Int) {
    if (_state.value.focusedField == NoteTextField.TITLE) {
      prefs.lastNoteTitleFontStyle = value
      _state.update { it.copy(titleFontStyle = value) }
    } else {
      prefs.lastNoteFontStyle = value
      _state.update { it.copy(fontStyle = value) }
    }
  }

  fun onFieldFocused(field: NoteTextField) {
    _state.update { it.copy(focusedField = field) }
  }

  fun onReminderAttachedChanged(value: Boolean) {
    _state.update { it.copy(isReminderAttached = value) }
  }

  fun onTabClicked(tab: EditTab) {
    _state.update { it.copy(expandedTab = if (it.expandedTab == tab) null else tab) }
  }

  fun onTextFieldValueChange(value: TextFieldValue) {
    _state.update { it.copy(textFieldValue = value, boldRange = null) }
  }

  fun onTitleFieldValueChange(value: TextFieldValue) {
    _state.update { it.copy(titleFieldValue = value) }
  }

  fun onSpeechStarted() {
    _state.update { it.copy(speechState = SpeechUiState.STARTED) }
  }

  fun onSpeechStopped() {
    _state.update { it.copy(speechState = SpeechUiState.IDLE) }
  }

  fun onSpeechSpeaking() {
    _state.update { it.copy(speechState = SpeechUiState.SPEAKING) }
  }

  fun onSpeechError() {
    _state.update { it.copy(speechState = SpeechUiState.IDLE) }
  }

  fun onSpeechResult(
    text: String,
    boldRange: IntRange?,
  ) {
    _state.update {
      it.copy(
        speechState = SpeechUiState.STOPPED,
        textFieldValue = TextFieldValue(text = text, selection = TextRange(text.length)),
        boldRange = boldRange,
      )
    }
  }

  fun onImageOpen(position: Int) {
    val s = _state.value
    imagesSingleton.setCurrent(
      images = s.images,
      backgroundColor = s.noteColors.background,
    )
    event.value = Event(ViewModelEvent.OpenImagePreview(position))
  }

  fun onDeleteRequested() {
    _state.update { it.copy(activeDialog = NoteEditDialog.DELETE) }
  }

  fun onDialogDismissed() {
    _state.update { it.copy(activeDialog = null) }
  }

  fun onSaveClicked() {
    if (shouldConfirmBeforeSaving()) {
      _state.update { it.copy(activeDialog = NoteEditDialog.SAME_NOTE) }
    } else {
      saveNote()
    }
  }

  fun onDeleteConfirmed() {
    _state.update { it.copy(activeDialog = null) }
    deleteNote()
  }

  /** True when saving should first confirm overwrite-vs-keep, because this came from an
   *  imported file that already has a matching note in the database. */
  fun shouldConfirmBeforeSaving(): Boolean = _state.value.isFromFile && _state.value.hasSameInDb

  /** Collapses the currently expanded tab, if any. Returns true if a tab was collapsed. */
  fun collapseExpandedTab(): Boolean {
    val wasExpanded = _state.value.expandedTab != null
    if (wasExpanded) {
      _state.update { it.copy(expandedTab = null) }
    }
    return wasExpanded
  }

  fun loadFromFile(uri: Uri) {
    viewModelScope.launch(dispatcherProvider.default()) {
      runCatching {
        if (ContentResolver.SCHEME_CONTENT != uri.scheme) {
          val any = MemoryUtil.readFromUri(contextProvider.context, uri, SharedNote.FILE_EXTENSION)
          if (any != null && any is SharedNote) {
            noteToOldNoteConverter.toNote(any)?.also {
              _state.update { s -> s.copy(isFromFile = true) }
              onNoteLoaded(it)
              findSame(it.getKey())
            }
          }
        }
      }
    }
  }

  fun onShareClick() {
    viewModelScope.launch(dispatcherProvider.io()) {
      val note = createObject()
      val file = createSharedNoteFileUseCase(note)
      Logger.i(TAG, "Share note file path: ${file?.absolutePath}")
      withContext(dispatcherProvider.main()) {
        if (file != null) {
          if (file.exists() && file.canRead()) {
            event.emit(
              ViewModelEvent.ShareNote(
                text = _state.value.textFieldValue.text.trim(),
                file = file,
              )
            )
          } else {
            event.emit(ViewModelEvent.Error(textProvider.getText(R.string.error_sending)))
          }

        } else {
          event.emit(ViewModelEvent.Error(textProvider.getText(R.string.error_sending)))
        }
      }
    }
  }

  fun onNewTime(localTime: LocalTime) {
    _state.update {
      it.copy(
        time = localTime,
        reminderTimeFormatted = dateTimeManager.getTime(localTime),
      )
    }
  }

  fun onNewDate(localDate: LocalDate) {
    _state.update {
      it.copy(
        date = localDate,
        reminderDateFormatted = dateTimeManager.getDate(localDate),
      )
    }
  }

  private fun replaceText(text: String) {
    _state.update {
      it.copy(
        textFieldValue = TextFieldValue(text = text, selection = TextRange(text.length)),
        boldRange = null,
      )
    }
    _textUpdate.postValue(Event(TextUpdate(text = text)))
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.main()) {
      when {
        sharedText != null -> replaceText(sharedText)

        !sharedImageUris.isNullOrEmpty() -> {
          addMultiple(sharedImageUris.map { Uri.parse(it) })
        }

        fromIntentData -> {
          intentDataReader.get(IntentKeys.INTENT_ITEM, NoteWithImages::class.java)?.run {
            onNoteLoaded(this)
            findSame(this.getKey())
          }
        }

        else -> {
          val noteWithImages =
            id?.let {
              withContext(dispatcherProvider.io()) {
                noteRepository.getById(id)
              }
            }
          noteWithImages?.also { onNoteLoaded(it) }
        }
      }
    }
  }

  private fun onNoteLoaded(noteWithImages: NoteWithImages) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val uiNoteEdit = uiNoteEditAdapter.convert(noteWithImages)
      val colorCode = noteColorEngine.getColorCode(uiNoteEdit.colorPalette, uiNoteEdit.colorPosition)
      _state.update {
        it.copy(
          canDelete = true,
          colorIndex = colorCode,
          opacity = uiNoteEdit.opacity,
          noteColors = noteColorEngine.colorsFor(colorCode, uiNoteEdit.opacity),
          fontStyle = uiNoteEdit.typeface,
          fontSize = uiNoteEdit.fontSize,
          titleFontStyle = uiNoteEdit.titleTypeface,
          titleFontSize = uiNoteEdit.titleFontSize,
          images = uiNoteEdit.images,
          textFieldValue =
            TextFieldValue(
              text = uiNoteEdit.text,
              selection = TextRange(uiNoteEdit.text.length),
            ),
          titleFieldValue =
            TextFieldValue(
              text = uiNoteEdit.title,
              selection = TextRange(uiNoteEdit.title.length),
            ),
          boldRange = null,
          noteId = noteWithImages.getKey(),
        )
      }
      _textUpdate.postValue(Event(TextUpdate(text = uiNoteEdit.text)))

      noteWithImages.getKey().also { loadLinkedReminder(it) }
    }
  }

  private suspend fun loadLinkedReminder(noteKey: String) {
    val reminder =
      reminderRepository
        .getByNoteKey(noteKey)
        .firstOrNull { it.isActive && !it.isRemoved }

    if (reminder != null) {
      dateTimeManager.fromGmtToLocal(reminder.eventTime)?.also { localDateTime ->
        onNewDate(localDateTime.toLocalDate())
        onNewTime(localDateTime.toLocalTime())
      }
      _state.update {
        it.copy(
          isReminderAttached = true,
          reminderId = reminder.uuId,
        )
      }
    }
  }

  private suspend fun findSame(id: String) {
    val hasSameInDb =
      withContext(dispatcherProvider.io()) {
        val noteWithImages = noteRepository.getById(id)
        noteWithImages != null
      }
    _state.update {
      it.copy(
        hasSameInDb = hasSameInDb,
        isFromFile = true,
      )
    }
  }

  fun removeImage(position: Int) {
    val list = _state.value.images.toMutableList()
    if (position < list.size) {
      list.removeAt(position)
      _state.update { it.copy(images = list) }
    }
  }

  fun addBitmap(bitmap: Bitmap) {
    viewModelScope.launch(dispatcherProvider.default()) {
      var imageFile =
        UiNoteImage(
          state = UiNoteImageState.LOADING,
          id = 0,
          fileName = UUID.randomUUID().toString(),
        )
      var mutable = _state.value.images.toMutableList()
      val position = mutable.size
      mutable.add(imageFile)
      withUIContext {
        _state.update { it.copy(images = mutable) }
      }

      val bos = ByteArrayOutputStream()
      bitmap.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
      val bitmapdata = bos.toByteArray()
      val bs = ByteArrayInputStream(bitmapdata)

      val filePath = noteImageRepository.saveTemporaryImage(imageFile.fileName, bs)

      Logger.i(TAG, "Add bitmap saved to: $filePath")
      imageFile =
        imageFile.copy(
          filePath = filePath,
          state = UiNoteImageState.READY,
        )

      mutable = _state.value.images.toMutableList()
      if (position < mutable.size) {
        mutable[position] = imageFile
      }
      withUIContext {
        _state.update { it.copy(images = mutable) }
      }
    }
  }

  /** Downloads an image from a pasted/typed URL and appends it, mirroring the download step of
   *  the previous Fragment-based `PhotoSelectionUtil.downloadUrl`. */
  fun downloadImageFromUrl(url: String) {
    if (!Patterns.WEB_URL.matcher(url).matches()) {
      event.value = Event(ViewModelEvent.Error(textProvider.getText(R.string.wrong_url)))
      return
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      val request = ImageRequest.Builder(contextProvider.context).data(url).build()
      val bitmap = runCatching { imageLoader.execute(request).drawable?.toBitmap() }.getOrNull()
      if (bitmap != null) {
        addBitmap(bitmap)
      } else {
        withContext(dispatcherProvider.main()) {
          event.value = Event(ViewModelEvent.Error(textProvider.getText(R.string.failed_to_download)))
        }
      }
    }
  }

  fun addMultiple(uris: List<Uri>) {
    val count = _state.value.images.size
    imageDecoder.startDecoding(viewModelScope, uris, count, {
      val mutable = _state.value.images.toMutableList()
      mutable.addAll(it)
      _state.update { s -> s.copy(images = mutable) }
    }, { i, imageFile ->
      setImage(imageFile, i)
    })
  }

  private fun setImage(
    imageFile: UiNoteImage,
    position: Int,
  ) {
    val list = _state.value.images.toMutableList()
    if (position < list.size) {
      if (imageFile.state == UiNoteImageState.ERROR) {
        list.removeAt(position)
      } else {
        list[position] = imageFile
      }
      _state.update { it.copy(images = list) }
    }
  }

  private fun deleteNote() {
    val id =
      id ?: run {
        Logger.w(TAG, "Note id is null")
        return
      }
    viewModelScope.launch(dispatcherProvider.io()) {
      noteRepository.getById(id) ?: run {
        withContext(dispatcherProvider.main()) {
          event.value = Event(ViewModelEvent.Error(textProvider.getText(R.string.default_error_msg)))
        }
        return@launch
      }

      deleteNoteUseCase(id)

      withContext(dispatcherProvider.main()) {
        appWidgetUpdater.updateNotesWidget()
        appWidgetUpdater.updateAllWidgets()
        event.value = Event(ViewModelEvent.MoveBack)
      }
    }
  }

  /**
   * Processes a [ClipData] payload received from a drag-and-drop drop event.
   *
   * Each item in the clip data is classified by [DroppedContentParser]:
   * - Inline text, `.txt`  `.md`  other `text*` files, and PDF documents are extracted
   *   as text and appended (after the current body text) to the note.
   * - Image URIs are forwarded to [addMultiple] for the image pipeline.
   * - Unsupported types result in an error toast.
   *
   * @param clipData the payload from [android.view.DragEvent.ACTION_DROP].
   */
  fun parseDrop(clipData: ClipData) {
    Logger.i(TAG, "Parse drop called with ${clipData.itemCount} items.")
    viewModelScope.launch(dispatcherProvider.default()) {
      val result = droppedContentParser.parse(clipData)
      Logger.i(
        TAG,
        "Drop parsed: ${result.textContent.size} text items, " +
          "${result.imageUris.size} images, ${result.unsupportedCount} unsupported",
      )

      val currentText = _state.value.textFieldValue.text
      val allTextParts =
        buildList {
          if (currentText.isNotEmpty()) add(currentText)
          addAll(result.textContent)
        }
      if (allTextParts.isNotEmpty()) {
        val combined = allTextParts.joinToString("\n")
        withContext(dispatcherProvider.main()) {
          replaceText(combined)
        }
      }

      if (result.imageUris.isNotEmpty()) {
        addMultiple(result.imageUris)
      }

      if (result.unsupportedCount > 0) {
        withContext(dispatcherProvider.main()) {
          event.value = Event(ViewModelEvent.Error(textProvider.getText(R.string.unsupported_file_format)))
        }
      }
    }
  }

  fun saveNote(newId: Boolean = false) {
    _state.update { it.copy(activeDialog = null) }

    viewModelScope.launch(dispatcherProvider.main()) {
      val noteWithImages = createObject()
      val hasReminder = _state.value.isReminderAttached
      var reminder: Reminder? = null
      var reminderToDelete: Reminder? = null
      val note = noteWithImages.note
      if (hasReminder && note != null) {
        // Reuse the existing linked reminder's identity so saving updates it in place instead
        // of creating a duplicate — unless we're splitting off a new note copy (newId), in which
        // case the original reminder must stay with the original note.
        reminder = createReminder(note, reuseExisting = !newId) ?: run {
          Logger.e(TAG, "Failed to create reminder")
          return@launch
        }
      } else if (!newId) {
        // The switch was turned off for a note that had a reminder attached — remove it.
        reminderToDelete = getLinkedReminder(_state.value.reminderId)
      }

      if (newId) {
        noteWithImages.note?.key = UUID.randomUUID().toString()
        reminder?.noteId = noteWithImages.getKey()
      }
      noteWithImages.note?.archived = false
      analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_NOTE))
      Logger.logEvent("Note saved")
      saveNote(noteWithImages, reminder, reminderToDelete)
    }
  }

  private suspend fun saveNote(
    note: NoteWithImages,
    reminder: Reminder?,
    reminderToDelete: Reminder?,
  ) {
    val v = note.note ?: return
    withContext(dispatcherProvider.default()) {
      v.updatedAt = DateTimeManager.gmtDateTime
      saveNoteUseCase(note)
      Logger.i(TAG, "Note saved with id: ${v.key}")
      if (reminder != null) {
        saveReminder(reminder)
      } else if (reminderToDelete != null) {
        deleteReminderUseCase(reminderToDelete)
      }

      withContext(dispatcherProvider.main()) {
        appWidgetUpdater.updateNotesWidget()
        appWidgetUpdater.updateAllWidgets()
        event.value = Event(ViewModelEvent.MoveBack)
      }
    }
  }

  private suspend fun getLinkedReminder(reminderId: String?): Reminder? {
    reminderId ?: return null
    return withContext(dispatcherProvider.io()) {
      reminderRepository.getById(reminderId)
    }
  }

  private suspend fun createReminder(
    note: Note,
    reuseExisting: Boolean,
  ): Reminder? {
    val reminderId = _state.value.reminderId
    val existing =
      if (reuseExisting && reminderId != null) {
        getLinkedReminder(reminderId)
      } else {
        null
      }
    val reminder = existing?.copy() ?: Reminder()
    if (existing == null) {
      reminder.delay = 0
      reminder.eventCount = 0
      reminder.useGlobal = true
    }
    reminder.type = Reminder.BY_DATE
    reminder.noteId = note.key
    reminder.isActive = true
    reminder.isRemoved = false
    reminder.summary = SuperUtil.normalizeSummary(note.title.ifBlank { note.summary })

    val startTime = LocalDateTime.of(_state.value.date, _state.value.time)
    if (!dateTimeManager.isCurrent(startTime)) {
      event.value = Event(ViewModelEvent.Error(textProvider.getText(R.string.reminder_is_outdated)))
      return null
    }
    reminder.startTime = dateTimeManager.getGmtFromDateTime(startTime)
    reminder.eventTime = reminder.startTime
    return reminder
  }

  private suspend fun createObject(): NoteWithImages {
    val s = _state.value
    val images = s.images
    val oldNote =
      withContext(dispatcherProvider.io()) {
        noteRepository.getById(s.noteId)
      }

    var note = oldNote?.note
    if (note == null) {
      note = Note(syncState = SyncState.WaitingForUpload)
    }
    note.summary = s.textFieldValue.text.trim()
    note.title = s.titleFieldValue.text.trim()
    note.date = dateTimeManager.getNowGmtDateTime()
    note.color = noteColorEngine.getLegacyColorCode(s.colorIndex)
    note.style = s.fontStyle
    note.fontSize = s.fontSize
    note.titleFontStyle = s.titleFontStyle
    note.titleFontSize = s.titleFontSize
    note.palette = noteColorEngine.getLegacyPalette(s.colorIndex)
    note.opacity = s.opacity
    note.syncState = SyncState.WaitingForUpload

    return (oldNote ?: NoteWithImages()).copy(
      images =
        images.map {
          ImageFile(
            id = it.id,
            fileName = it.fileName,
            filePath = it.filePath,
          )
        },
      note = note,
    )
  }

  private fun saveReminder(reminder: Reminder) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val group = groupV2Repository.defaultGroup()
      if (group != null) {
        reminder.groupColor = group.color
        reminder.groupTitle = group.title
        reminder.groupUuId = group.uuId
        activateReminderUseCase(reminder)
      }
    }
  }

  sealed interface ViewModelEvent {
    data class OpenImagePreview(
      val position: Int,
    ) : ViewModelEvent

    data class Error(
      val message: String,
    ) : ViewModelEvent

    data object MoveBack : ViewModelEvent

    data class ShareNote(
      val text: String,
      val file: File,
    ) : ViewModelEvent

    data class ShowDatePicker(
      val date: LocalDate,
      val title: String,
    ) : ViewModelEvent

    data class ShowTimePicker(
      val time: LocalTime,
      val title: String,
    ) : ViewModelEvent
  }

  companion object {
    private const val TAG = "CreateNoteViewModel"
  }
}
