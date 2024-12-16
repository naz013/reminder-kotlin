package com.elementary.tasks.notes.create

import android.content.ClipData
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.AnalyticsEventSender
import com.elementary.tasks.core.analytics.Feature
import com.elementary.tasks.core.analytics.FeatureUsedEvent
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.converters.NoteToOldNoteConverter
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.note.UiNoteEditAdapter
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.OldNote
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.repository.NoteImageRepository
import com.elementary.tasks.core.data.ui.note.UiNoteEdit
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.elementary.tasks.core.data.ui.note.UiNoteImageState
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.ui.font.FontParams
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.notes.create.images.ImageDecoder
import com.elementary.tasks.notes.work.DeleteNoteBackupWorker
import com.elementary.tasks.notes.work.NoteSingleBackupWorker
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import com.github.naz013.logging.Logger
import kotlinx.coroutines.launch
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
  private val eventControlFactory: EventControlFactory,
  private val workerLauncher: WorkerLauncher,
  private val notesDao: NotesDao,
  private val reminderDao: ReminderDao,
  private val reminderGroupDao: ReminderGroupDao,
  private val prefs: Prefs,
  private val dateTimeManager: DateTimeManager,
  private val textProvider: TextProvider,
  private val backupTool: BackupTool,
  private val contextProvider: ContextProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  private val uiNoteEditAdapter: UiNoteEditAdapter,
  private val noteImageRepository: NoteImageRepository,
  private val noteToOldNoteConverter: NoteToOldNoteConverter
) : BaseProgressViewModel(dispatcherProvider) {

  private val _dateFormatted = mutableLiveDataOf<String>()
  val dateFormatted = _dateFormatted.toLiveData()

  private val _timeFormatted = mutableLiveDataOf<String>()
  val timeFormatted = _timeFormatted.toLiveData()

  private val _note = mutableLiveDataOf<UiNoteEdit>()
  val note = _note.toLiveData()

  private val _noteToShare = mutableLiveDataOf<Pair<String, File>>()
  val noteToShare = _noteToShare.toLiveData()

  private val _parsedText = mutableLiveDataOf<String>()
  val parsedText = _parsedText.toLiveData()

  var colorOpacity: MutableLiveData<Pair<Int, Int>> = MutableLiveData()
  var fontStyle: MutableLiveData<Int> = MutableLiveData(FontParams.DEFAULT_FONT_STYLE)
  var fontSize: MutableLiveData<Int> = MutableLiveData(FontParams.DEFAULT_FONT_SIZE)
  var palette: MutableLiveData<Int> = MutableLiveData()
  var isReminderAttached: MutableLiveData<Boolean> = MutableLiveData()
  var images: MutableLiveData<List<UiNoteImage>> = MutableLiveData()

  private var localNote: NoteWithImages? = null

  var hasSameInDb: Boolean = false
    private set

  var date: LocalDate = LocalDate.now()
    private set
  var time: LocalTime = LocalTime.now()
    private set

  var isLogged = false
  var isNoteEdited = false
    private set
  var isFromFile: Boolean = false
    private set

  init {
    load()
  }

  fun onFontSizeChanged(value: Int) {
    fontSize.postValue(value)
  }

  fun onFontStyleChanged(value: Int) {
    fontStyle.postValue(value)
  }

  fun loadFromFile(uri: Uri) {
    viewModelScope.launch(dispatcherProvider.default()) {
      runCatching {
        if (ContentResolver.SCHEME_CONTENT != uri.scheme) {
          val any = MemoryUtil.readFromUri(contextProvider.context, uri, FileConfig.FILE_NAME_NOTE)
          if (any != null && any is OldNote) {
            noteToOldNoteConverter.toNote(any)?.also {
              isFromFile = true
              onNoteLoaded(it)
              findSame(it.getKey())
            }
          }
        }
      }
    }
  }

  fun shareNote(text: String, opacity: Int) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val note = createObject(text, opacity)
      val file = backupTool.noteToFile(note)
      withUIContext {
        postInProgress(false)
        if (file != null) {
          _noteToShare.postValue(Pair(text, file))
        } else {
          postError(textProvider.getText(R.string.error_sending))
        }
      }
    }
  }

  fun onNewTime(localTime: LocalTime) {
    time = localTime
    _timeFormatted.postValue(dateTimeManager.getTime(time))
  }

  fun onNewDate(localDate: LocalDate) {
    date = localDate
    _dateFormatted.postValue(dateTimeManager.getDate(date))
  }

  fun load() {
    setDateTime()
    viewModelScope.launch(dispatcherProvider.default()) {
      localNote = notesDao.getById(id)
      localNote?.also { noteWithImages ->
        onNoteLoaded(noteWithImages)
      }
    }
  }

  fun onNoteReceivedFromIntent(noteWithImages: NoteWithImages?) {
    noteWithImages?.also { onNoteLoaded(it) }
  }

  private fun onNoteLoaded(noteWithImages: NoteWithImages) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val uiNoteEdit = uiNoteEditAdapter.convert(noteWithImages)
      _note.postValue(uiNoteEdit)
      if (!isNoteEdited) {
        palette.postValue(uiNoteEdit.colorPalette)
        fontStyle.postValue(uiNoteEdit.typeface)
        fontSize.postValue(uiNoteEdit.fontSize)
        images.postValue(uiNoteEdit.images)
      }
      isNoteEdited = true
    }
  }

  private fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val note = notesDao.getById(id)
      hasSameInDb = note?.note != null
    }
  }

  private fun setDateTime() {
    val dateTime = LocalDateTime.now()
    onNewDate(dateTime.toLocalDate())
    onNewTime(dateTime.toLocalTime())
  }

  fun removeImage(position: Int) {
    val list = (images.value ?: listOf()).toMutableList()
    if (position < list.size) {
      list.removeAt(position)
      images.postValue(list)
    }
  }

  fun addBitmap(bitmap: Bitmap) {
    viewModelScope.launch(dispatcherProvider.default()) {
      var imageFile = UiNoteImage(
        state = UiNoteImageState.LOADING,
        id = 0,
        fileName = UUID.randomUUID().toString()
      )
      var list = images.value ?: listOf()
      var mutable = list.toMutableList()
      val position = mutable.size
      mutable.add(imageFile)
      withUIContext {
        images.postValue(mutable)
      }

      val bos = ByteArrayOutputStream()
      bitmap.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
      val bitmapdata = bos.toByteArray()
      val bs = ByteArrayInputStream(bitmapdata)

      val filePath = noteImageRepository.saveTemporaryImage(imageFile.fileName, bs)

      Logger.d("addBitmap: size=${bos.size()}")
      imageFile = imageFile.copy(
        filePath = filePath,
        state = UiNoteImageState.READY
      )

      list = images.value ?: listOf()
      mutable = list.toMutableList()
      mutable[position] = imageFile
      withUIContext {
        images.postValue(mutable)
      }
    }
  }

  fun addMultiple(uris: List<Uri>) {
    val count = images.value?.size ?: 0
    imageDecoder.startDecoding(viewModelScope, uris, count, {
      val list = images.value ?: listOf()
      val mutable = list.toMutableList()
      mutable.addAll(it)
      images.postValue(mutable)
    }, { i, imageFile ->
      setImage(imageFile, i)
    })
  }

  private fun setImage(imageFile: UiNoteImage, position: Int) {
    val list = (images.value ?: listOf()).toMutableList()
    if (position < list.size) {
      if (imageFile.state == UiNoteImageState.ERROR) {
        list.removeAt(position)
      } else {
        list[position] = imageFile
      }
      images.postValue(list)
    }
  }

  fun deleteNote() {
    val noteWithImages = localNote ?: return
    val note = noteWithImages.note ?: return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      notesDao.delete(note)
      for (image in noteWithImages.images) {
        notesDao.delete(image)
      }
      noteImageRepository.clearFolder(note.key)
      workerLauncher.startWork(DeleteNoteBackupWorker::class.java, Constants.INTENT_ID, note.key)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  fun parseDrop(clipData: ClipData, text: String) {
    Logger.d("parseDrop: ${clipData.itemCount}, ${clipData.description}")
    viewModelScope.launch(dispatcherProvider.default()) {
      var parsedText = ""
      val uris = mutableListOf<Uri>()
      for (i in 0 until clipData.itemCount) {
        uris.add(clipData.getItemAt(i).uri)
        if (!clipData.getItemAt(i).text.isNullOrEmpty()) {
          parsedText = clipData.getItemAt(i).text.toString()
        }
      }

      if (parsedText.isNotEmpty()) {
        withUIContext {
          if (text.isEmpty()) {
            _parsedText.postValue(parsedText)
          } else {
            _parsedText.postValue("$text\n$parsedText")
          }
        }
      } else {
        addMultiple(uris)
      }
    }
  }

  fun saveNote(text: String, opacity: Int, newId: Boolean = false) {
    val noteWithImages = createObject(text, opacity)
    val hasReminder = isReminderAttached.value ?: false
    var reminder: Reminder? = null
    val note = noteWithImages.note
    if (hasReminder && note != null) {
      reminder = createReminder(note) ?: return
    }

    if (prefs.isNoteColorRememberingEnabled) {
      prefs.lastNoteColor = noteWithImages.getColor()
    }
    if (newId) {
      noteWithImages.note?.key = UUID.randomUUID().toString()
      reminder?.noteId = noteWithImages.getKey()
    }
    noteWithImages.note?.archived = false
    analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_NOTE))
    Logger.logEvent("Note saved")
    saveNote(noteWithImages, reminder)
  }

  private fun saveNote(note: NoteWithImages, reminder: Reminder?) {
    val v = note.note ?: return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      v.updatedAt = DateTimeManager.gmtDateTime
      Logger.d("saveNote: $note")
      saveImages(note.images, v.key)
      notesDao.insert(v)
      workerLauncher.startWork(NoteSingleBackupWorker::class.java, Constants.INTENT_ID, v.key)
      postInProgress(false)
      postCommand(Commands.SAVED)
      if (reminder != null) {
        saveReminder(reminder)
      }
    }
  }

  private fun createReminder(note: Note): Reminder? {
    val reminder = Reminder()
    reminder.type = Reminder.BY_DATE
    reminder.delay = 0
    reminder.eventCount = 0
    reminder.useGlobal = true
    reminder.noteId = note.key
    reminder.isActive = true
    reminder.isRemoved = false
    reminder.summary = SuperUtil.normalizeSummary(note.summary)

    val startTime = LocalDateTime.of(date, time)
    if (!dateTimeManager.isCurrent(startTime)) {
      postError(textProvider.getText(R.string.reminder_is_outdated))
      return null
    }
    reminder.startTime = dateTimeManager.getGmtFromDateTime(startTime)
    reminder.eventTime = reminder.startTime
    return reminder
  }

  private fun newColor(): Int = if (prefs.isNoteColorRememberingEnabled) {
    prefs.lastNoteColor
  } else {
    Random().nextInt(ThemeProvider.NOTE_COLORS)
  }

  private fun createObject(text: String, opacity: Int): NoteWithImages {
    val images = this.images.value ?: emptyList()

    val pair = colorOpacity.value ?: Pair(newColor(), opacity)

    val noteWithImages = localNote
    var note = noteWithImages?.note
    if (note == null) {
      note = Note()
    }
    note.summary = text
    note.date = dateTimeManager.getNowGmtDateTime()
    note.color = pair.first
    note.style = fontStyle.value ?: FontParams.DEFAULT_FONT_STYLE
    note.fontSize = fontSize.value ?: FontParams.DEFAULT_FONT_SIZE
    note.palette = palette.value ?: 0
    note.opacity = pair.second

    return (noteWithImages ?: NoteWithImages()).copy(
      images = images.map {
        ImageFile(
          id = it.id,
          fileName = it.fileName,
          filePath = it.filePath
        )
      },
      note = note
    )
  }

  private fun saveReminder(reminder: Reminder) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val group = reminderGroupDao.defaultGroup()
      if (group != null) {
        reminder.groupColor = group.groupColor
        reminder.groupTitle = group.groupTitle
        reminder.groupUuId = group.groupUuId
        reminderDao.insert(reminder)
      }
      if (reminder.groupUuId != "") {
        eventControlFactory.getController(reminder).enable()
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          Constants.INTENT_ID,
          reminder.uuId
        )
      }
    }
  }

  private fun saveImages(list: List<ImageFile>, id: String) {
    val oldList = notesDao.getImagesByNoteId(id)
    Logger.d("saveImages: ${oldList.size}")
    for (image in oldList) {
      Logger.d("saveImages: delete -> ${image.id}, ${image.noteId}")
      notesDao.delete(image)
    }
    noteImageRepository.moveImagesToFolder(list, id)
      .map { it.copy(noteId = id) }
      .takeIf { it.isNotEmpty() }
      ?.also {
        Logger.d("saveImages: new list -> $it")
        notesDao.insertAll(it)
      }
  }
}
