package com.elementary.tasks.voice

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.backdoor.engine.Model
import com.backdoor.engine.Recognizer
import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.ActionType
import com.backdoor.engine.misc.ContactsInterface
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.core.analytics.Status
import com.elementary.tasks.core.analytics.VoiceAnalyticsTracker
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.UiReminderListAdapter
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayListAdapter
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.adapter.note.UiNoteListAdapter
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.dao.PlacesDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.data.repository.NoteRepository
import com.elementary.tasks.core.data.ui.UiReminderList
import com.elementary.tasks.core.data.ui.UiReminderListActiveShop
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.data.ui.UiReminderListRemovedShop
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.dialogs.VoiceHelpActivity
import com.elementary.tasks.core.dialogs.VoiceResultDialog
import com.elementary.tasks.core.dialogs.VolumeDialog
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.os.contacts.ContactsReader
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.IdProvider
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.params.PrefsConstants
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.home.BottomNavActivity
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.reminder.work.ReminderDeleteBackupWorker
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import com.elementary.tasks.settings.other.SendFeedbackActivity
import com.elementary.tasks.splash.SplashScreenActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.LinkedList
import java.util.Random

class ConversationViewModel(
  dispatcherProvider: DispatcherProvider,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val eventControlFactory: EventControlFactory,
  private val recognizer: Recognizer,
  private val workerLauncher: WorkerLauncher,
  private val updatesHelper: UpdatesHelper,
  private val dateTimeManager: DateTimeManager,
  private val idProvider: IdProvider,
  private val notesDao: NotesDao,
  private val birthdaysDao: BirthdaysDao,
  private val reminderDao: ReminderDao,
  private val reminderGroupDao: ReminderGroupDao,
  private val placesDao: PlacesDao,
  private val uiBirthdayListAdapter: UiBirthdayListAdapter,
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val uiGroupListAdapter: UiGroupListAdapter,
  private val uiNoteListAdapter: UiNoteListAdapter,
  private val prefs: Prefs,
  private val language: Language,
  private val contextProvider: ContextProvider,
  private val contactsReader: ContactsReader,
  private val voiceAnalyticsTracker: VoiceAnalyticsTracker,
  private val noteRepository: NoteRepository
) : BaseProgressViewModel(dispatcherProvider) {

  private var _shoppingLists = MutableLiveData<List<UiReminderList>>()
  var shoppingLists: LiveData<List<UiReminderList>> = _shoppingLists

  private var _enabledReminders = MutableLiveData<List<UiReminderList>>()
  var enabledReminders: LiveData<List<UiReminderList>> = _enabledReminders

  private var _activeReminders = MutableLiveData<List<UiReminderList>>()
  var activeReminders: LiveData<List<UiReminderList>> = _activeReminders

  private var _notes = mutableLiveDataOf<List<UiNoteList>>()
  var notes = _notes.toLiveData()

  private var _birthdays = MutableLiveData<List<UiBirthdayList>>()
  var birthdays: LiveData<List<UiBirthdayList>> = _birthdays

  private var _replies = MutableLiveData<List<Reply>>()
  var replies: LiveData<List<Reply>> = _replies
  private val repliesList = mutableListOf<Reply>()
  private var hasPartial = false

  var autoMicClick: Boolean = true
    private set
  var tellAboutEvent: Boolean = false
    private set
  val groups = mutableListOf<UiGroupList>()
  var defaultGroup: UiGroupList? = null

  init {
    voiceAnalyticsTracker.screenOpened()
    clearConversation()
    viewModelScope.launch(dispatcherProvider.default()) {
      autoMicClick = prefs.isAutoMicClick
      tellAboutEvent = prefs.isTellAboutEvent
      reminderGroupDao.defaultGroup(true)?.also {
        defaultGroup = uiGroupListAdapter.convert(it)
      }
    }
    reminderGroupDao.loadAll().observeForever { list ->
      if (list != null) {
        groups.clear()
        groups.addAll(list.map { uiGroupListAdapter.convert(it) })
      }
    }
  }

  fun getDateTimeText(gmtDateTime: String?): String {
    return dateTimeManager.getVoiceDateTime(gmtDateTime) ?: ""
  }

  fun toggleTellAboutEvent() {
    tellAboutEvent = !tellAboutEvent
    prefs.isTellAboutEvent = tellAboutEvent
  }

  fun toggleAutoMic() {
    autoMicClick = !autoMicClick
    prefs.isAutoMicClick = autoMicClick
  }

  fun addMoreItemsToList(position: Int) {
    val reply = repliesList[position]
    val container = reply.content as Container<*>
    Timber.d("addMoreItemsToList: $container")
    when (container.type) {
      is UiGroupList -> {
        repliesList.removeAt(position)
        for (item in container.list) {
          repliesList.add(0, Reply(Reply.GROUP, item))
        }
        _replies.postValue(repliesList)
      }

      is UiNoteList -> {
        repliesList.removeAt(position)
        for (item in container.list) {
          repliesList.add(0, Reply(Reply.NOTE, item))
        }
        _replies.postValue(repliesList)
      }

      is UiReminderList -> {
        repliesList.removeAt(position)
        addRemindersToList(container)
        _replies.postValue(repliesList)
      }

      is UiBirthdayList -> {
        repliesList.removeAt(position)
        val reversed = ArrayList(container.list)
        reversed.reverse()
        for (item in reversed) {
          repliesList.add(0, Reply(Reply.BIRTHDAY, item))
        }
        _replies.postValue(repliesList)
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  private fun addRemindersToList(container: Container<*>) {
    val reversed = ArrayList((container as Container<UiReminderList>).list).reversed()
    for (item in reversed) {
      when (item) {
        is UiReminderListActiveShop, is UiReminderListRemovedShop -> {
          repliesList.add(0, Reply(Reply.SHOPPING, item))
        }

        else -> {
          repliesList.add(0, Reply(Reply.REMINDER, item))
        }
      }
    }
  }

  fun removeFirst() {
    if (repliesList[0].viewType == Reply.ASK) {
      repliesList.removeAt(0)
      _replies.postValue(repliesList)
    } else {
      removeAsk()
    }
  }

  fun removeAsk() {
    for (i in 1 until repliesList.size) {
      val reply = repliesList[i]
      if (reply.viewType == Reply.ASK) {
        repliesList.removeAt(i)
        _replies.postValue(repliesList)
        break
      }
    }
  }

  fun removePartial() {
    if (hasPartial) {
      repliesList.removeAt(0)
      _replies.postValue(repliesList)
    }
    hasPartial = false
  }

  fun addReply(reply: Reply?, isPartial: Boolean = false) {
    if (reply != null) {
      if (!isPartial) {
        hasPartial = false
        repliesList.add(0, reply)
        _replies.postValue(repliesList)
      } else {
        if (hasPartial) {
          repliesList[0].content = reply.content
          _replies.postValue(repliesList)
        } else {
          hasPartial = true
          repliesList.add(0, reply)
          _replies.postValue(repliesList)
        }
      }
    }
  }

  fun getNotes() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val list = LinkedList(noteRepository.getAll()).map { uiNoteListAdapter.convert(it) }
      postInProgress(false)
      _notes.postValue(list)
    }
  }

  fun getShoppingReminders() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val list = reminderDao.getAllTypes(
        true,
        removed = false,
        types = intArrayOf(Reminder.BY_DATE_SHOP)
      ).map { uiReminderListAdapter.create(it) }
      postInProgress(false)
      _shoppingLists.postValue(list)
    }
  }

  fun getEnabledReminders(gmtDateTime: String?) {
    postInProgress(true)
    Timber.d("getEnabledReminders: gmt $gmtDateTime")
    viewModelScope.launch(dispatcherProvider.default()) {
      val list = reminderDao.getAllTypesInRange(
        active = true,
        removed = false,
        fromTime = dateTimeManager.getGmtDateTimeFromMillis(System.currentTimeMillis()),
        toTime = dateTimeManager.getGmtDateTimeFromMillis(
          dateTimeManager.getMillisFromGmtVoiceEngine(
            gmtDateTime
          )
        )
      ).map { uiReminderListAdapter.create(it) }
      postInProgress(false)
      _enabledReminders.postValue(list)
    }
  }

  fun getReminders(gmtDateTime: String?) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val list = reminderDao.getActiveInRange(
        false,
        dateTimeManager.getGmtDateTimeFromMillis(System.currentTimeMillis()),
        dateTimeManager.getGmtDateTimeFromMillis(
          dateTimeManager.getMillisFromGmtVoiceEngine(
            gmtDateTime
          )
        )
      ).map { uiReminderListAdapter.create(it) }
      postInProgress(false)
      _activeReminders.postValue(list)
    }
  }

  fun getBirthdays(gmtDateTime: String?) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val list = birthdaysDao.getAll()
        .map { uiBirthdayListAdapter.convert(it) }
        .filter {
          it.nextBirthdayDate >= System.currentTimeMillis() &&
            it.nextBirthdayDate < dateTimeManager.getMillisFromGmtVoiceEngine(gmtDateTime)
        }
      postInProgress(false)
      _birthdays.postValue(list)
    }
  }

  fun findSuggestion(suggestion: String): Model? {
    return runCatching {
      recognizer.setContactHelper(ContactHelper())
      val model = try {
        recognizer.recognize(suggestion)
      } catch (throwable: Throwable) {
        null
      }
      if (model == null) {
        voiceAnalyticsTracker.sendEvent(prefs.voiceLocale, Status.FAIL)
      } else {
        voiceAnalyticsTracker.sendEvent(prefs.voiceLocale, Status.SUCCESS, model)
      }
      model
    }.getOrNull()
  }

  fun findResults(matches: List<*>): Reminder? {
    recognizer.setContactHelper(ContactHelper())
    for (i in matches.indices) {
      val key = matches[i]
      val keyStr = key.toString()
      val model = runCatching { recognizer.recognize(keyStr) }.getOrNull()
      if (model != null) {
        Timber.d("findResults: $model")
        voiceAnalyticsTracker.sendEvent(prefs.voiceLocale, Status.SUCCESS, model)
        return createReminder(model)
      }
    }
    return null
  }

  fun parseResults(matches: List<*>, isWidget: Boolean, context: Context) {
    for (i in matches.indices) {
      val key = matches[i]
      val keyStr = key.toString()
      val model = findSuggestion(keyStr)
      if (model != null) {
        Timber.d("parseResults: $model")
        val types = model.type
        if (types == ActionType.ACTION && isWidget) {
          when (model.action) {
            Action.APP -> context.startActivity(
              Intent(
                context,
                SplashScreenActivity::class.java
              )
            )

            Action.HELP -> context.startActivity(
              Intent(context, VoiceHelpActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
            )

            Action.BIRTHDAY -> PinLoginActivity.openLogged(context, AddBirthdayActivity::class.java)
            Action.REMINDER -> PinLoginActivity.openLogged(
              context,
              CreateReminderActivity::class.java
            )

            Action.VOLUME -> context.startActivity(
              Intent(context, VolumeDialog::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
            )

            Action.TRASH -> emptyTrash(true)
            Action.DISABLE -> disableAllReminders(true)
            Action.SETTINGS -> {
              val activityIntent = Intent(context, BottomNavActivity::class.java)
              activityIntent.action = Intent.ACTION_VIEW
              activityIntent.putExtra(
                BottomNavActivity.ARG_DEST,
                BottomNavActivity.Companion.Dest.SETTINGS
              )
              context.startActivity(activityIntent)
            }

            Action.REPORT -> {
              context.startActivity(
                Intent(context, SendFeedbackActivity::class.java)
                  .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
              )
            }

            else -> {
            }
          }
        } else if (types == ActionType.NOTE) {
          saveNote(createNote(model.summary), showToast = true, addQuickNote = true)
        } else if (types == ActionType.REMINDER) {
          saveReminder(model, isWidget)
        } else if (types == ActionType.GROUP) {
          saveGroup(createGroup(model), true)
        }
        break
      }
    }
  }

  private fun saveReminder(model: Model, widget: Boolean) {
    val reminder = createReminder(model) ?: return
    saveAndStartReminder(reminder)
    if (widget) {
      contextProvider.context.startActivity(
        Intent(contextProvider.context, VoiceResultDialog::class.java)
          .putExtra(Constants.INTENT_ID, reminder.uuId)
          .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
      )
    } else {
      Toast.makeText(contextProvider.context, R.string.saved, Toast.LENGTH_SHORT).show()
    }
  }

  fun disableAllReminders(showToast: Boolean) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      for (reminder in reminderDao.getAll(active = true, removed = false)) {
        stopReminder(reminder)
      }
      postInProgress(false)
      postCommand(Commands.DELETED)
      withUIContext {
        if (showToast) {
          Toast.makeText(
            contextProvider.context,
            R.string.all_reminders_were_disabled,
            Toast.LENGTH_SHORT
          ).show()
        }
      }
    }
  }

  fun emptyTrash(showToast: Boolean) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val archived = reminderDao.getAll(
        active = false,
        removed = true
      )
      for (reminder in archived) {
        eventControlFactory.getController(reminder).stop()
        reminderDao.delete(reminder)
        googleCalendarUtils.deleteEvents(reminder.uuId)
        workerLauncher.startWork(
          ReminderDeleteBackupWorker::class.java,
          Constants.INTENT_ID,
          reminder.uuId
        )
        googleCalendarUtils.deleteEvents(reminder.uuId)
      }
      postInProgress(false)
      postCommand(Commands.TRASH_CLEARED)
      withUIContext {
        if (showToast) {
          Toast.makeText(contextProvider.context, R.string.trash_cleared, Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  fun toReminderListItem(reminder: Reminder): UiReminderListData {
    return uiReminderListAdapter.create(reminder)
  }

  fun createReminder(model: Model): Reminder? {
    val action = model.action
    val weekdays = model.weekdays
    var eventTime = dateTimeManager.getFromGmtVoiceEngine(model.dateTime) ?: return null
    var typeT = Reminder.BY_DATE
    if (action == Action.WEEK || action == Action.WEEK_CALL || action == Action.WEEK_SMS) {
      typeT = Reminder.BY_WEEK
      eventTime = dateTimeManager.getNextWeekdayTime(eventTime, weekdays, 0)
      if (model.target.isNullOrEmpty()) {
        typeT = if (action == Action.WEEK_CALL) {
          Reminder.BY_WEEK_CALL
        } else {
          Reminder.BY_WEEK_SMS
        }
      }
    } else if (action == Action.CALL) {
      typeT = Reminder.BY_DATE_CALL
    } else if (action == Action.MESSAGE) {
      typeT = Reminder.BY_DATE_SMS
    } else if (action == Action.MAIL) {
      typeT = Reminder.BY_DATE_EMAIL
    }

    val isCal = prefs.getBoolean(PrefsConstants.EXPORT_TO_CALENDAR)
    val isStock = prefs.getBoolean(PrefsConstants.EXPORT_TO_STOCK)

    val reminder = Reminder()
    val group = defaultGroup
    if (group != null) {
      reminder.groupColor = group.colorPosition
      reminder.groupTitle = group.title
      reminder.groupUuId = group.id
    }
    reminder.type = typeT
    reminder.summary = model.summary
    reminder.weekdays = weekdays
    reminder.repeatInterval = model.repeatInterval
    reminder.after = model.afterMillis
    reminder.target = model.target ?: ""
    reminder.eventTime = dateTimeManager.getGmtFromDateTime(eventTime)
    reminder.startTime = dateTimeManager.getGmtFromDateTime(eventTime)
    reminder.exportToCalendar = model.hasCalendar && (isCal || isStock)
    Timber.d("createReminder: $reminder")
    return reminder
  }

  fun saveAndStartReminder(reminder: Reminder, isEdit: Boolean = true) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      runBlocking {
        Timber.d("saveAndStartReminder: save START")
        if (reminder.groupUuId == "") {
          val group = defaultGroup
          if (group != null) {
            reminder.groupColor = group.colorPosition
            reminder.groupTitle = group.title
            reminder.groupUuId = group.id
          }
        }
        reminderDao.insert(reminder)
        if (!isEdit) {
          if (Reminder.isGpsType(reminder.type)) {
            val places = reminder.places
            if (places.isNotEmpty()) {
              placesDao.insert(places[0])
            }
          }
        }
        eventControlFactory.getController(reminder).start()
        Timber.d("saveAndStartReminder: save DONE")
      }
      workerLauncher.startWork(
        ReminderSingleBackupWorker::class.java,
        Constants.INTENT_ID,
        reminder.uuId
      )
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  fun createNote(note: String): Note {
    val color = Random().nextInt(15)
    val item = Note()
    item.color = color
    item.summary = note
    item.date = dateTimeManager.getNowGmtDateTime()
    return item
  }

  fun saveNote(note: Note, showToast: Boolean, addQuickNote: Boolean) {
    if (addQuickNote && prefs.getBoolean(PrefsConstants.QUICK_NOTE_REMINDER)) {
      saveQuickReminder(note.key, note.summary)
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      note.updatedAt = dateTimeManager.getNowGmtDateTime()
      notesDao.insert(note)
    }
    updatesHelper.updateNotesWidget()
    if (showToast) {
      Toast.makeText(contextProvider.context, R.string.saved, Toast.LENGTH_SHORT).show()
    }
  }

  fun saveQuickReminder(key: String, summary: String): Reminder {
    val after = (prefs.getInt(PrefsConstants.QUICK_NOTE_REMINDER_TIME) * 1000 * 60).toLong()
    val due = System.currentTimeMillis() + after
    val mReminder = Reminder()
    mReminder.type = Reminder.BY_DATE
    mReminder.delay = 0
    mReminder.eventCount = 0
    mReminder.useGlobal = true
    mReminder.noteId = key
    mReminder.summary = summary
    val group = defaultGroup
    if (group != null) {
      mReminder.groupColor = group.colorPosition
      mReminder.groupTitle = group.title
      mReminder.groupUuId = group.id
    }
    mReminder.startTime = dateTimeManager.getGmtDateTimeFromMillis(due)
    mReminder.eventTime = dateTimeManager.getGmtDateTimeFromMillis(due)
    saveAndStartReminder(mReminder)
    return mReminder
  }

  fun createGroup(model: Model): ReminderGroup {
    return ReminderGroup(
      groupTitle = model.summary,
      groupColor = Random().nextInt(16),
      groupDateTime = dateTimeManager.getNowGmtDateTime(),
      groupUuId = idProvider.generateUuid(),
      isDefaultGroup = false
    )
  }

  fun saveGroup(model: ReminderGroup, showToast: Boolean) {
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderGroupDao.insert(model)
    }
    if (showToast) {
      Toast.makeText(contextProvider.context, R.string.saved, Toast.LENGTH_SHORT).show()
    }
  }

  fun clearConversation() {
    recognizer.updateLocale(language.getVoiceLanguage(prefs.voiceLocale))
    repliesList.clear()
    _replies.postValue(repliesList)
  }

  private fun stopReminder(reminder: Reminder) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      eventControlFactory.getController(reminder).stop()
      postInProgress(false)
    }
  }

  inner class ContactHelper : ContactsInterface {

    override fun findEmail(input: String?): String? {
      return contactsReader.findEmail(input)
    }

    override fun findNumber(input: String?): String? {
      return contactsReader.findNumber(input)
    }
  }
}
