package com.elementary.tasks.notes.create

import android.content.ClipData
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.cloud.converters.NoteToOldNoteConverter
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.note.UiNoteEditAdapter
import com.elementary.tasks.core.data.repository.NoteImageRepository
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.elementary.tasks.core.data.ui.note.UiNoteImageState
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.notes.SharedNote
import com.elementary.tasks.notes.create.drop.DroppedContentParser
import com.elementary.tasks.notes.create.images.ImageDecoder
import com.elementary.tasks.notes.usecase.CreateSharedNoteFileUseCase
import com.elementary.tasks.notes.usecase.DeleteNoteUseCase
import com.elementary.tasks.notes.usecase.SaveNoteUseCase
import com.elementary.tasks.reminder.scheduling.usecase.ActivateReminderUseCase
import com.elementary.tasks.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.font.FontParams
import com.github.naz013.domain.note.ImageFile
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.ui.common.isAlmostTransparent
import com.github.naz013.ui.common.isColorDark
import com.github.naz013.ui.common.theme.ThemeProvider
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
import java.util.Random
import java.util.UUID

class CreateNoteViewModel(
  private val id: String,
  private val imageDecoder: ImageDecoder,
  dispatcherProvider: DispatcherProvider,
  private val noteRepository: NoteRepository,
  private val reminderGroupRepository: ReminderGroupRepository,
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
  private val themeProvider: ThemeProvider,
) : BaseProgressViewModel(dispatcherProvider) {
  private val _state = MutableStateFlow(NoteEditState())
  val state: StateFlow<NoteEditState> = _state.asStateFlow()

  private val _textUpdate = mutableLiveDataOf<Event<TextUpdate>>()
  val textUpdate = _textUpdate.toLiveData()

  private val _titleUpdate = mutableLiveDataOf<Event<String>>()
  val titleUpdate = _titleUpdate.toLiveData()

  private val _noteToShare = mutableLiveDataOf<Event<Pair<String, File>>>()
  val noteToShare = _noteToShare.toLiveData()

  private var localNote: NoteWithImages? = null
  private var linkedReminder: Reminder? = null

  var hasSameInDb: Boolean = false
    private set

  var date: LocalDate = LocalDate.now()
    private set
  var time: LocalTime = LocalTime.now()
    private set

  init {
    setDateTime()
    if (id.isEmpty()) {
      applyNewNoteDefaults()
    } else {
      load()
    }
  }

  private fun applyNewNoteDefaults() {
    val color =
      if (prefs.isNoteColorRememberingEnabled) {
        prefs.lastNoteColor
      } else {
        Random().nextInt(ThemeProvider.NOTE_COLORS)
      }
    val fontSize =
      if (prefs.isNoteFontSizeRememberingEnabled) {
        prefs.lastNoteFontSize
      } else {
        FontParams.DEFAULT_FONT_SIZE
      }
    val fontStyle =
      if (prefs.isNoteFontStyleRememberingEnabled) {
        prefs.lastNoteFontStyle
      } else {
        FontParams.DEFAULT_FONT_STYLE
      }
    val titleFontSize =
      if (prefs.isNoteFontSizeRememberingEnabled) {
        prefs.lastNoteTitleFontSize
      } else {
        FontParams.DEFAULT_TITLE_FONT_SIZE
      }
    val titleFontStyle =
      if (prefs.isNoteFontStyleRememberingEnabled) {
        prefs.lastNoteTitleFontStyle
      } else {
        FontParams.DEFAULT_FONT_STYLE
      }
    // A remembered opacity of 0 would make every new note's background fully invisible —
    // never a useful "remembered" default, so treat it the same as unset.
    val opacity = prefs.noteColorOpacity.takeIf { it > 0 } ?: 100
    _state.update {
      it.copy(
        colorIndex = color,
        opacity = opacity,
        palette = prefs.notePalette,
        fontSize = fontSize,
        fontStyle = fontStyle,
        titleFontSize = titleFontSize,
        titleFontStyle = titleFontStyle,
      )
    }
  }

  fun onColorSelected(index: Int) {
    if (prefs.isNoteColorRememberingEnabled) {
      prefs.lastNoteColor = index
    }
    _state.update { it.copy(colorIndex = index) }
  }

  fun onOpacityChanged(value: Int) {
    prefs.noteColorOpacity = value
    _state.update { it.copy(opacity = value) }
  }

  fun onPaletteChanged(value: Int) {
    prefs.notePalette = value
    _state.update { it.copy(palette = value) }
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

  /**
   * Derives the note's background/status-bar/content colors from [state]'s color index, opacity
   * and palette. A pure function of [state] so the Activity/Compose layer never has to know about
   * [ThemeProvider] or the contrast math itself.
   */
  fun colorsFor(state: NoteEditState): NoteColors {
    val solidColor = themeProvider.getNoteLightColor(state.colorIndex, 100, state.palette)
    val isBgDark =
      if (state.opacity.isAlmostTransparent()) {
        themeProvider.isDark
      } else {
        solidColor.isColorDark()
      }
    val backgroundColor = themeProvider.getNoteLightColor(state.colorIndex, state.opacity, state.palette)
    val contentColor = if (isBgDark) PURE_WHITE else PURE_BLACK
    return NoteColors(
      background = backgroundColor,
      statusBarColor = solidColor,
      content = contentColor,
      sliderColors = themeProvider.noteColorsForSlider(state.palette),
    )
  }

  fun sliderColorsForPalette(palette: Int): IntArray = themeProvider.noteColorsForSlider(palette)

  /** True when saving should first confirm overwrite-vs-keep, because this came from an
   *  imported file that already has a matching note in the database. */
  fun shouldConfirmBeforeSaving(): Boolean = _state.value.isFromFile && hasSameInDb

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

  fun shareNote(
    text: String,
    title: String,
  ) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.io()) {
      val note = createObject(text, title)
      val file = createSharedNoteFileUseCase(note)
      Logger.i(TAG, "Share note file path: ${file?.absolutePath}")
      withContext(dispatcherProvider.main()) {
        postInProgress(false)
        if (file != null) {
          _noteToShare.postValue(Event(Pair(text, file)))
        } else {
          postError(textProvider.getText(R.string.error_sending))
        }
      }
    }
  }

  fun onNewTime(localTime: LocalTime) {
    time = localTime
    _state.update { it.copy(reminderTimeFormatted = dateTimeManager.getTime(time)) }
  }

  fun onNewDate(localDate: LocalDate) {
    date = localDate
    _state.update { it.copy(reminderDateFormatted = dateTimeManager.getDate(date)) }
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.default()) {
      localNote = noteRepository.getById(id)
      localNote?.also { noteWithImages -> onNoteLoaded(noteWithImages) }
    }
  }

  fun onNoteReceivedFromIntent() {
    intentDataReader.get(IntentKeys.INTENT_ITEM, NoteWithImages::class.java)?.run {
      onNoteLoaded(this)
    }
  }

  private fun onNoteLoaded(noteWithImages: NoteWithImages) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val uiNoteEdit = uiNoteEditAdapter.convert(noteWithImages)
      _state.update {
        it.copy(
          colorIndex = uiNoteEdit.colorPosition,
          opacity = uiNoteEdit.opacity,
          palette = uiNoteEdit.colorPalette,
          fontStyle = uiNoteEdit.typeface,
          fontSize = uiNoteEdit.fontSize,
          titleFontStyle = uiNoteEdit.titleTypeface,
          titleFontSize = uiNoteEdit.titleFontSize,
          images = uiNoteEdit.images,
          isNoteEdited = true,
        )
      }
      _textUpdate.postValue(Event(TextUpdate(text = uiNoteEdit.text)))
      _titleUpdate.postValue(Event(uiNoteEdit.title))

      val noteKey = noteWithImages.note?.key
      if (!noteKey.isNullOrEmpty()) {
        loadLinkedReminder(noteKey)
      }
    }
  }

  private suspend fun loadLinkedReminder(noteKey: String) {
    val reminder =
      reminderRepository
        .getByNoteKey(noteKey)
        .firstOrNull { it.isActive && !it.isRemoved }
    linkedReminder = reminder
    if (reminder != null) {
      dateTimeManager.fromGmtToLocal(reminder.eventTime)?.also { localDateTime ->
        onNewDate(localDateTime.toLocalDate())
        onNewTime(localDateTime.toLocalTime())
      }
      _state.update { it.copy(isReminderAttached = true) }
    }
  }

  private fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val note = noteRepository.getById(id)
      hasSameInDb = note?.note != null
    }
  }

  private fun setDateTime() {
    val dateTime = LocalDateTime.now()
    onNewDate(dateTime.toLocalDate())
    onNewTime(dateTime.toLocalTime())
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

  fun deleteNote() {
    val noteWithImages = localNote ?: return
    val note = noteWithImages.note ?: return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      deleteNoteUseCase(note.key)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  /**
   * Processes a [ClipData] payload received from a drag-and-drop drop event.
   *
   * Each item in the clip data is classified by [DroppedContentParser]:
   * - Inline text, `.txt`  `.md`  other `text*` files, and PDF documents are extracted
   *   as text and appended (after [text]) to the note.
   * - Image URIs are forwarded to [addMultiple] for the image pipeline.
   * - Unsupported types result in an error toast.
   *
   * @param clipData the payload from [android.view.DragEvent.ACTION_DROP].
   * @param text the current text already present in the note editor.
   */
  fun parseDrop(
    clipData: ClipData,
    text: String,
  ) {
    Logger.i(TAG, "Parse drop called with ${clipData.itemCount} items.")
    viewModelScope.launch(dispatcherProvider.default()) {
      val result = droppedContentParser.parse(clipData)
      Logger.i(
        TAG,
        "Drop parsed: ${result.textContent.size} text items, " +
          "${result.imageUris.size} images, ${result.unsupportedCount} unsupported",
      )

      val allTextParts =
        buildList {
          if (text.isNotEmpty()) add(text)
          addAll(result.textContent)
        }
      if (allTextParts.isNotEmpty()) {
        val combined = allTextParts.joinToString("\n")
        _textUpdate.postValue(Event(TextUpdate(text = combined)))
      }

      if (result.imageUris.isNotEmpty()) {
        addMultiple(result.imageUris)
      }

      if (result.unsupportedCount > 0) {
        withUIContext {
          postError(textProvider.getText(R.string.unsupported_file_format))
        }
      }
    }
  }

  fun saveNote(
    text: String,
    title: String,
    newId: Boolean = false,
  ) {
    val noteWithImages = createObject(text, title)
    val hasReminder = _state.value.isReminderAttached
    var reminder: Reminder? = null
    var reminderToDelete: Reminder? = null
    val note = noteWithImages.note
    if (hasReminder && note != null) {
      // Reuse the existing linked reminder's identity so saving updates it in place instead
      // of creating a duplicate — unless we're splitting off a new note copy (newId), in which
      // case the original reminder must stay with the original note.
      reminder = createReminder(note, reuseExisting = !newId) ?: return
    } else if (!newId) {
      // The switch was turned off for a note that had a reminder attached — remove it.
      reminderToDelete = linkedReminder
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

  private fun saveNote(
    note: NoteWithImages,
    reminder: Reminder?,
    reminderToDelete: Reminder?,
  ) {
    val v = note.note ?: return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      v.updatedAt = DateTimeManager.gmtDateTime
      saveNoteUseCase(note)
      Logger.i(TAG, "Note saved with id: ${v.key}")
      if (reminder != null) {
        saveReminder(reminder)
      } else if (reminderToDelete != null) {
        deleteReminderUseCase(reminderToDelete)
      }
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  private fun createReminder(
    note: Note,
    reuseExisting: Boolean,
  ): Reminder? {
    val existing = if (reuseExisting) linkedReminder else null
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

    val startTime = LocalDateTime.of(date, time)
    if (!dateTimeManager.isCurrent(startTime)) {
      postError(textProvider.getText(R.string.reminder_is_outdated))
      return null
    }
    reminder.startTime = dateTimeManager.getGmtFromDateTime(startTime)
    reminder.eventTime = reminder.startTime
    return reminder
  }

  private fun createObject(
    text: String,
    title: String,
  ): NoteWithImages {
    val s = _state.value
    val images = s.images

    val noteWithImages = localNote
    var note = noteWithImages?.note
    if (note == null) {
      note = Note(syncState = SyncState.WaitingForUpload)
    }
    note.summary = text
    note.title = title
    note.date = dateTimeManager.getNowGmtDateTime()
    note.color = s.colorIndex
    note.style = s.fontStyle
    note.fontSize = s.fontSize
    note.titleFontStyle = s.titleFontStyle
    note.titleFontSize = s.titleFontSize
    note.palette = s.palette
    note.opacity = s.opacity
    note.syncState = SyncState.WaitingForUpload

    return (noteWithImages ?: NoteWithImages()).copy(
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
      val group = reminderGroupRepository.defaultGroup()
      if (group != null) {
        reminder.groupColor = group.groupColor
        reminder.groupTitle = group.groupTitle
        reminder.groupUuId = group.groupUuId
        activateReminderUseCase(reminder)
      }
    }
  }

  companion object {
    private const val TAG = "CreateNoteViewModel"
    private const val PURE_WHITE = android.graphics.Color.WHITE
    private const val PURE_BLACK = android.graphics.Color.BLACK
  }
}
