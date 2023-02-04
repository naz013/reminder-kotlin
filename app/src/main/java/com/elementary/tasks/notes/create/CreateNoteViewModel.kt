package com.elementary.tasks.notes.create

import android.content.ClipData
import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.AnalyticsEventSender
import com.elementary.tasks.core.analytics.Feature
import com.elementary.tasks.core.analytics.FeatureUsedEvent
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.cloud.FileConfig
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
import com.elementary.tasks.core.data.ui.note.UiNoteEdit
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.elementary.tasks.core.data.ui.note.UiNoteImageState
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.notes.create.images.ImageDecoder
import com.elementary.tasks.notes.work.DeleteNoteBackupWorker
import com.elementary.tasks.notes.work.NoteSingleBackupWorker
import com.elementary.tasks.reminder.work.ReminderDeleteBackupWorker
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Random
import java.util.UUID

class CreateNoteViewModel(
  private val id: String,
  private val imageDecoder: ImageDecoder,
  dispatcherProvider: DispatcherProvider,
  private val googleCalendarUtils: GoogleCalendarUtils,
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
  private val uiNoteEditAdapter: UiNoteEditAdapter
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
  var fontStyle: MutableLiveData<Int> = MutableLiveData()
  var palette: MutableLiveData<Int> = MutableLiveData()
  var isReminderAttached: MutableLiveData<Boolean> = MutableLiveData()
  var images: MutableLiveData<List<UiNoteImage>> = MutableLiveData()

  private var localNote: NoteWithImages? = null
  private var localReminder: Reminder? = null

  var hasSameInDb: Boolean = false
    private set

  var date: LocalDate = LocalDate.now()
    private set
  var time: LocalTime = LocalTime.now()
    private set

  var isLogged = false
  var isNoteEdited = false
    private set
  private var isReminderEdited = false
  var isFromFile: Boolean = false
    private set

  init {
    load()
  }

  fun loadFromFile(uri: Uri) {
    viewModelScope.launch(dispatcherProvider.default()) {
      runCatching {
        if (ContentResolver.SCHEME_CONTENT != uri.scheme) {
          val any = MemoryUtil.readFromUri(contextProvider.context, uri, FileConfig.FILE_NAME_NOTE)
          if (any != null && any is OldNote) {
            BackupTool.oldNoteToNew(any)?.also {
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
    setDateTime(null)
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
        images.postValue(uiNoteEdit.images)
//        colorOpacity.postValue(Pair(uiNoteEdit.colorPosition, uiNoteEdit.opacity))
      }
      isNoteEdited = true
      localReminder = reminderDao.getByNoteKey(if (id == "") "1" else id)?.also { reminder ->
        if (!isReminderEdited) {
          setDateTime(reminder.eventTime)
          isReminderAttached.postValue(true)
          isReminderEdited = true
        }
      }
    }
  }

  private fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val note = notesDao.getById(id)
      hasSameInDb = note?.note != null
    }
  }

  private fun setDateTime(eventTime: String?) {
    val dateTime = dateTimeManager.fromGmtToLocal(eventTime) ?: LocalDateTime.now()
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
        data = null,
        id = 0
      )
      var list = images.value ?: listOf()
      var mutable = list.toMutableList()
      val position = mutable.size
      mutable.add(imageFile)
      withUIContext {
        images.postValue(mutable)
      }

      val outputStream = ByteArrayOutputStream()
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
      imageFile = imageFile.copy(
        data = outputStream.toByteArray(),
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
      workerLauncher.startWork(DeleteNoteBackupWorker::class.java, Constants.INTENT_ID, note.key)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  fun deleteReminder() {
    val reminder = localReminder ?: return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      eventControlFactory.getController(reminder).stop()
      reminderDao.delete(reminder)
      googleCalendarUtils.deleteEvents(reminder.uuId)
      workerLauncher.startWork(
        ReminderDeleteBackupWorker::class.java,
        Constants.INTENT_ID,
        reminder.uuId
      )
      postInProgress(false)
      postCommand(Commands.UPDATED)
    }
  }

  fun parseDrop(clipData: ClipData, text: String) {
    Timber.d("parseDrop: ${clipData.itemCount}, ${clipData.description}")
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
    if (!hasReminder && localReminder != null) deleteReminder()
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
    analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_NOTE))
    saveNote(noteWithImages, reminder)
  }

  private fun saveNote(note: NoteWithImages, reminder: Reminder?) {
    val v = note.note ?: return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      v.updatedAt = DateTimeManager.gmtDateTime
      Timber.d("saveNote: %s", note)
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
    var reminder = localReminder
    if (reminder == null) {
      reminder = Reminder()
    }
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

    var noteWithImages = localNote
    var note = noteWithImages?.note
    if (note == null) {
      note = Note()
    }
    note.summary = text
    note.date = dateTimeManager.getNowGmtDateTime()
    note.color = pair.first
    note.style = fontStyle.value ?: 0
    note.palette = palette.value ?: 0
    note.opacity = pair.second

    if (noteWithImages == null) {
      noteWithImages = NoteWithImages()
    }

    noteWithImages.images = images.map {
      ImageFile(image = it.data, id = it.id)
    }
    noteWithImages.note = note
    return noteWithImages
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
        eventControlFactory.getController(reminder).start()
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          Constants.INTENT_ID, reminder.uuId
        )
      }
    }
  }

  private fun saveImages(list: List<ImageFile>, id: String) {
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
