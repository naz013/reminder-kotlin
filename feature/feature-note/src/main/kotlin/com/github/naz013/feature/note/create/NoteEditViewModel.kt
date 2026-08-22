package com.github.naz013.feature.note.create

import android.content.ClipData
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
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Tag
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.font.FontParams
import com.github.naz013.domain.note.ImageFile
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.note.R
import com.github.naz013.feature.note.UiNoteEditAdapter
import com.github.naz013.feature.note.create.drop.DroppedContentParser
import com.github.naz013.feature.note.create.images.ImageDecoder
import com.github.naz013.feature.note.image.NoteImageRepository
import com.github.naz013.feature.note.preview.ImagesSingleton
import com.github.naz013.feature.note.usecase.CreateSharedNoteFileUseCase
import com.github.naz013.feature.note.usecase.DeleteNoteUseCase
import com.github.naz013.feature.note.usecase.SaveNoteUseCase
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
import com.github.naz013.logic.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.logic.tag.ToggleTagAssignmentUseCase
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.platform.SystemInfo
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.ui.note.NoteColorEngine
import com.github.naz013.ui.note.NotePreferences
import com.github.naz013.ui.note.UiNoteImage
import com.github.naz013.ui.note.UiNoteImageState
import com.github.naz013.ui.tag.TagChipState
import com.github.naz013.ui.tag.TagChipStateAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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

@OptIn(ExperimentalCoroutinesApi::class)
internal class NoteEditViewModel(
  private val id: String?,
  private val sharedText: String?,
  private val sharedImageUris: List<String>?,
  private val fromIntentData: Boolean,
  private val imageDecoder: ImageDecoder,
  private val dispatcherProvider: DispatcherProvider,
  private val noteRepository: NoteRepository,
  private val reminderV2Repository: ReminderV2Repository,
  private val deleteReminderUseCase: DeleteReminderUseCase,
  private val notePreferences: NotePreferences,
  private val dateTimeManager: DateTimeManager,
  private val textProvider: TextProvider,
  private val contextProvider: ContextProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  private val uiNoteEditAdapter: UiNoteEditAdapter,
  private val noteImageRepository: NoteImageRepository,
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
  private val tagRepository: TagRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val toggleTagAssignmentUseCase: ToggleTagAssignmentUseCase,
  private val tagChipStateAdapter: TagChipStateAdapter,
) : ViewModel() {

  val is24HourFormat: Boolean = notePreferences.is24HourFormat

  private val _state = MutableStateFlow(NoteEditState())
  val state: StateFlow<NoteEditState> = _state.asStateFlow()

  private val _textUpdate = mutableLiveDataOf<Event<TextUpdate>>()
  val textUpdate = _textUpdate.toLiveData()

  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  init {
    // Existing notes get their color from onNoteLoaded() once load() finishes; seeding the
    // last-used-color default here too would make it flash before the real color appears.
    val isNewNote = id == null && !fromIntentData
    val colorCode = if (isNewNote) {
      noteColorEngine.getColorCode(
        noteColorEngine.getLastPalette(),
        noteColorEngine.getLastColorCode(),
      )
    } else {
      null
    }
    val opacity = if (isNewNote) noteColorEngine.getLasterOpacity() else null

    _state.update {
      it.copy(
        colorIndex = colorCode ?: it.colorIndex,
        opacity = opacity ?: it.opacity,
        noteColors = if (colorCode != null && opacity != null) {
          noteColorEngine.colorsFor(colorCode, opacity)
        } else {
          it.noteColors
        },
        fontSize =
        if (notePreferences.isNoteFontSizeRememberingEnabled) {
          notePreferences.lastNoteFontSize
        } else {
          FontParams.DEFAULT_FONT_SIZE
        },
        fontStyle =
        if (notePreferences.isNoteFontStyleRememberingEnabled) {
          notePreferences.lastNoteFontStyle
        } else {
          FontParams.DEFAULT_FONT_STYLE
        },
        titleFontSize =
        if (notePreferences.isNoteFontSizeRememberingEnabled) {
          notePreferences.lastNoteTitleFontSize
        } else {
          FontParams.DEFAULT_TITLE_FONT_SIZE
        },
        titleFontStyle =
        if (notePreferences.isNoteFontStyleRememberingEnabled) {
          notePreferences.lastNoteTitleFontStyle
        } else {
          FontParams.DEFAULT_FONT_STYLE
        },
        hasCamera = systemInfo.hasCamera,
        sliderColors = noteColorEngine.allColors(),
        hapticFeedbackEnabled = notePreferences.hapticsEnabled,
      )
    }
    onNewTime(LocalTime.now())
    onNewDate(LocalDate.now())

    load()
    observeTags()
  }

  private fun observeTags() {
    viewModelScope.launch(dispatcherProvider.default()) {
      tagRepository.observeAll()
        .map { tags ->
          tags.map { tagChipStateAdapter(it) }
        }
        .collect { tags ->
          _state.update { it.copy(allTags = tags) }
        }
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      _state.map { it.noteId }
        .distinctUntilChanged()
        .flatMapLatest { noteId -> tagAssignmentRepository.observeTagsForItem(noteId, TaggedItemType.NOTE) }
        .collect { tags ->
          _state.update { it.copy(selectedTagIds = tags.map(Tag::id).toSet()) }
        }
    }
  }

  fun onTagToggle(tag: TagChipState) {
    val noteId = _state.value.noteId
    val isSelected = tag.id in _state.value.selectedTagIds
    viewModelScope.launch(dispatcherProvider.io()) {
      toggleTagAssignmentUseCase(
        id = noteId,
        taggedItemType = TaggedItemType.NOTE,
        tagId = tag.id,
        isSelected = isSelected
      )
    }
  }

  fun onManageTagsClick() {
    event.emit(ViewModelEvent.OpenManageTags)
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
    if (notePreferences.isNoteColorRememberingEnabled) {
      notePreferences.lastNoteColor = index
    }
    _state.update { it.copy(colorIndex = index, noteColors = noteColorEngine.colorsFor(index, it.opacity)) }
  }

  fun onOpacityChanged(value: Int) {
    notePreferences.noteColorOpacity = value
    _state.update { it.copy(opacity = value, noteColors = noteColorEngine.colorsFor(it.colorIndex, value)) }
  }

  fun onFontSizeChanged(value: Int) {
    if (_state.value.focusedField == NoteTextField.TITLE) {
      notePreferences.lastNoteTitleFontSize = value
      _state.update { it.copy(titleFontSize = value) }
    } else {
      notePreferences.lastNoteFontSize = value
      _state.update { it.copy(fontSize = value) }
    }
  }

  fun onFontStyleChanged(value: Int) {
    if (_state.value.focusedField == NoteTextField.TITLE) {
      notePreferences.lastNoteTitleFontStyle = value
      _state.update { it.copy(titleFontStyle = value) }
    } else {
      notePreferences.lastNoteFontStyle = value
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
      reminderV2Repository
        .getByNoteId(noteKey)
        .firstOrNull { it.isActive && !it.isRemoved }

    if (reminder != null) {
      reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) }?.also { localDateTime ->
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
      withContext(dispatcherProvider.main()) {
        _state.update { it.copy(images = mutable) }
      }

      val bos = ByteArrayOutputStream()
      // Quality (2nd param) is ignored for PNG.
      bitmap.compress(CompressFormat.PNG, 0, bos)
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
      withContext(dispatcherProvider.main()) {
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
      var reminder: ReminderV2? = null
      var reminderToDelete: ReminderV2? = null
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
        reminder = reminder?.copy(noteId = noteWithImages.getKey())
      }
      noteWithImages.note?.archived = false
      analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_NOTE))
      Logger.logEvent("Note saved")
      saveNote(noteWithImages, reminder, reminderToDelete)
    }
  }

  private suspend fun saveNote(
    note: NoteWithImages,
    reminder: ReminderV2?,
    reminderToDelete: ReminderV2?,
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

  private suspend fun getLinkedReminder(reminderId: String?): ReminderV2? {
    reminderId ?: return null
    return withContext(dispatcherProvider.io()) {
      reminderV2Repository.getById(reminderId)
    }
  }

  private suspend fun createReminder(
    note: Note,
    reuseExisting: Boolean,
  ): ReminderV2? {
    val reminderId = _state.value.reminderId
    val existing =
      if (reuseExisting && reminderId != null) {
        getLinkedReminder(reminderId)
      } else {
        null
      }

    val startTime = LocalDateTime.of(_state.value.date, _state.value.time)
    if (!dateTimeManager.isCurrent(startTime)) {
      event.value = Event(ViewModelEvent.Error(textProvider.getText(R.string.reminder_is_outdated)))
      return null
    }
    val eventDateTime = dateTimeManager.localToUtc(startTime)
    val summary = normalizeReminderSummary(note.title.ifBlank { note.summary })

    return (existing ?: ReminderV2(schedule = ReminderSchedule(startDateTime = eventDateTime))).copy(
      recurrence = RecurrenceRule.Once,
      noteId = note.key,
      isActive = true,
      isRemoved = false,
      summary = summary,
      schedule = ReminderSchedule(startDateTime = eventDateTime, eventDateTime = eventDateTime),
    )
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

  private fun saveReminder(reminder: ReminderV2) {
    viewModelScope.launch(dispatcherProvider.default()) {
      activateReminderUseCase(reminder)
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

    data object OpenManageTags : ViewModelEvent
  }

  companion object {
    private const val TAG = "CreateNoteViewModel"
  }
}

private const val MAX_REMINDER_SUMMARY_LENGTH = 500

private fun normalizeReminderSummary(summary: String): String =
  if (summary.length > MAX_REMINDER_SUMMARY_LENGTH) {
    summary.substring(0, MAX_REMINDER_SUMMARY_LENGTH)
  } else {
    summary
  }
