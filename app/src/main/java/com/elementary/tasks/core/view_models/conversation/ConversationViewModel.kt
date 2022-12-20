package com.elementary.tasks.core.view_models.conversation

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.text.TextUtils
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.backdoor.engine.Model
import com.backdoor.engine.Recognizer
import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.ActionType
import com.backdoor.engine.misc.ContactsInterface
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.birthdays.list.BirthdayListItem
import com.elementary.tasks.birthdays.list.BirthdayModelAdapter
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.dialogs.VoiceHelpActivity
import com.elementary.tasks.core.dialogs.VoiceResultDialog
import com.elementary.tasks.core.dialogs.VolumeDialog
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.PrefsConstants
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.toGmt
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.core.view_models.reminders.BaseRemindersViewModel
import com.elementary.tasks.home.BottomNavActivity
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.settings.other.SendFeedbackActivity
import com.elementary.tasks.splash.SplashScreenActivity
import com.elementary.tasks.voice.Container
import com.elementary.tasks.voice.Reply
import timber.log.Timber
import java.util.LinkedList
import java.util.Random

class ConversationViewModel(
    appDb: AppDb,
    currentStateHolder: CurrentStateHolder,
    calendarUtils: CalendarUtils,
    eventControlFactory: EventControlFactory,
    private val recognizer: Recognizer,
    private val birthdayModelAdapter: BirthdayModelAdapter,
    dispatcherProvider: DispatcherProvider,
    workManagerProvider: WorkManagerProvider,
    updatesHelper: UpdatesHelper,
    private val notesDao: NotesDao,
    private val birthdaysDao: BirthdaysDao
) : BaseRemindersViewModel(
    currentStateHolder.preferences,
    calendarUtils,
    eventControlFactory,
    dispatcherProvider,
    workManagerProvider,
    updatesHelper,
    appDb.reminderDao(),
    appDb.reminderGroupDao(),
    appDb.placesDao()
) {

    private var _shoppingLists = MutableLiveData<List<Reminder>>()
    var shoppingLists: LiveData<List<Reminder>> = _shoppingLists

    private var _enabledReminders = MutableLiveData<List<Reminder>>()
    var enabledReminders: LiveData<List<Reminder>> = _enabledReminders

    private var _activeReminders = MutableLiveData<List<Reminder>>()
    var activeReminders: LiveData<List<Reminder>> = _activeReminders

    private var _notes = MutableLiveData<List<NoteWithImages>>()
    var notes: LiveData<List<NoteWithImages>> = _notes

    private var _birthdays = MutableLiveData<List<BirthdayListItem>>()
    var birthdays: LiveData<List<BirthdayListItem>> = _birthdays

    private var _replies = MutableLiveData<List<Reply>>()
    var replies: LiveData<List<Reply>> = _replies
    private val repliesList = mutableListOf<Reply>()
    private val context = currentStateHolder.context
    private val language = currentStateHolder.language
    private var hasPartial = false

    init {
        clearConversation()
    }

    fun addMoreItemsToList(position: Int) {
        val reply = repliesList[position]
        val container = reply.content as Container<*>
        Timber.d("addMoreItemsToList: $container")
        when (container.type) {
            is ReminderGroup -> {
                repliesList.removeAt(position)
                for (item in container.list) {
                    repliesList.add(0, Reply(Reply.GROUP, item))
                }
                _replies.postValue(repliesList)
            }

            is NoteWithImages -> {
                repliesList.removeAt(position)
                for (item in container.list) {
                    repliesList.add(0, Reply(Reply.NOTE, item))
                }
                _replies.postValue(repliesList)
            }

            is Reminder -> {
                repliesList.removeAt(position)
                addRemindersToList(container)
                _replies.postValue(repliesList)
            }

            is BirthdayListItem -> {
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

    private fun addRemindersToList(container: Container<*>) {
        val reversed = ArrayList((container as Container<Reminder>).list).reversed()
        for (item in reversed) {
            if (item.viewType == Reminder.REMINDER) {
                repliesList.add(0, Reply(Reply.REMINDER, item))
            } else {
                repliesList.add(0, Reply(Reply.SHOPPING, item))
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
        launchDefault {
            val list = LinkedList(notesDao.all())
            postInProgress(false)
            _notes.postValue(list)
        }
    }

    fun getShoppingReminders() {
        postInProgress(true)
        launchDefault {
            val list = reminderDao.getAllTypes(
                true,
                removed = false,
                types = intArrayOf(Reminder.BY_DATE_SHOP)
            )
            postInProgress(false)
            _shoppingLists.postValue(list)
        }
    }

    fun getEnabledReminders(dateTime: Long) {
        postInProgress(true)
        Timber.d("getEnabledReminders: ${dateTime.toGmt()}")
        launchDefault {
            val list = reminderDao.getAllTypesInRange(
                active = true,
                removed = false,
                fromTime = TimeUtil.getGmtFromDateTime(System.currentTimeMillis()),
                toTime = TimeUtil.getGmtFromDateTime(dateTime)
            )
            postInProgress(false)
            _enabledReminders.postValue(list)
        }
    }

    fun getReminders(dateTime: Long) {
        postInProgress(true)
        launchDefault {
            val list = reminderDao.getActiveInRange(
                false,
                TimeUtil.getGmtFromDateTime(System.currentTimeMillis()),
                TimeUtil.getGmtFromDateTime(dateTime)
            )
            postInProgress(false)
            _activeReminders.postValue(list)
        }
    }

    fun getBirthdays(dateTime: Long) {
        postInProgress(true)
        launchDefault {
            val list = birthdaysDao.all()
                .map { birthdayModelAdapter.convert(it) }
                .filter {
                    it.nextBirthdayDate >= System.currentTimeMillis() && it.nextBirthdayDate < dateTime
                }
            postInProgress(false)
            _birthdays.postValue(list)
        }
    }

    fun findSuggestion(suggestion: String): Model? {
        recognizer.setContactHelper(ContactHelper())
        return recognizer.recognize(suggestion)
    }

    fun findResults(matches: List<*>): Reminder? {
        recognizer.setContactHelper(ContactHelper())
        for (i in matches.indices) {
            val key = matches[i]
            val keyStr = key.toString()
            val model = recognizer.recognize(keyStr)
            if (model != null) {
                Timber.d("findResults: $model")
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
                        Action.REMINDER -> PinLoginActivity.openLogged(context, CreateReminderActivity::class.java)
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
        val reminder = createReminder(model)
        saveAndStartReminder(reminder)
        if (widget) {
            context.startActivity(
                Intent(context, VoiceResultDialog::class.java)
                    .putExtra(Constants.INTENT_ID, reminder.uuId)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        } else {
            Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show()
        }
    }

    fun disableAllReminders(showToast: Boolean) {
        postInProgress(true)
        launchDefault {
            for (reminder in reminderDao.getAll(active = true, removed = false)) {
                stopReminder(reminder)
            }
            postInProgress(false)
            postCommand(Commands.DELETED)
            withUIContext {
                if (showToast) {
                    Toast.makeText(
                        context,
                        R.string.all_reminders_were_disabled,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun emptyTrash(showToast: Boolean) {
        postInProgress(true)
        launchDefault {
            val archived = reminderDao.getAll(
                active = false,
                removed = true
            )
            for (reminder in archived) {
                deleteReminder(reminder, false)
                calendarUtils.deleteEvents(reminder.uuId)
            }
            postInProgress(false)
            postCommand(Commands.TRASH_CLEARED)
            withUIContext {
                if (showToast) {
                    Toast.makeText(context, R.string.trash_cleared, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun createReminder(model: Model): Reminder {
        val action = model.action
        val number = model.target ?: ""
        val summary = model.summary
        val repeat = model.repeatInterval
        val weekdays = model.weekdays
        val isCalendar = model.hasCalendar
        val startTime = model.dateTime
        var eventTime = TimeUtil.getMillisFromGmt(startTime)
        var typeT = Reminder.BY_DATE
        if (action == Action.WEEK || action == Action.WEEK_CALL || action == Action.WEEK_SMS) {
            typeT = Reminder.BY_WEEK
            eventTime =
                TimeCount.getNextWeekdayTime(TimeUtil.getDateTimeFromGmt(startTime), weekdays, 0)
            if (!TextUtils.isEmpty(number)) {
                typeT = if (action == Action.WEEK_CALL)
                    Reminder.BY_WEEK_CALL
                else
                    Reminder.BY_WEEK_SMS
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
            reminder.groupColor = group.groupColor
            reminder.groupTitle = group.groupTitle
            reminder.groupUuId = group.groupUuId
        }
        reminder.type = typeT
        reminder.summary = summary
        reminder.weekdays = weekdays
        reminder.repeatInterval = repeat
        reminder.target = number
        reminder.eventTime = TimeUtil.getGmtFromDateTime(eventTime)
        reminder.startTime = TimeUtil.getGmtFromDateTime(eventTime)
        reminder.exportToCalendar = isCalendar && (isCal || isStock)
        Timber.d("createReminder: $reminder")
        return reminder
    }

    fun createNote(note: String): Note {
        val color = Random().nextInt(15)
        val item = Note()
        item.color = color
        item.summary = note
        item.date = TimeUtil.gmtDateTime
        return item
    }

    fun saveNote(note: Note, showToast: Boolean, addQuickNote: Boolean) {
        if (addQuickNote && prefs.getBoolean(PrefsConstants.QUICK_NOTE_REMINDER)) {
            saveQuickReminder(note.key, note.summary)
        }
        launchDefault {
            note.updatedAt = TimeUtil.gmtDateTime
            notesDao.insert(note)
        }
        updatesHelper.updateNotesWidget()
        if (showToast) {
            Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show()
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
            mReminder.groupColor = group.groupColor
            mReminder.groupTitle = group.groupTitle
            mReminder.groupUuId = group.groupUuId
        }
        mReminder.startTime = TimeUtil.getGmtFromDateTime(due)
        mReminder.eventTime = TimeUtil.getGmtFromDateTime(due)
        saveAndStartReminder(mReminder)
        return mReminder
    }

    fun createGroup(model: Model): ReminderGroup {
        return ReminderGroup(
            groupTitle = model.summary,
            groupColor = Random().nextInt(16)
        )
    }

    fun saveGroup(model: ReminderGroup, showToast: Boolean) {
        launchDefault {
            reminderGroupDao.insert(model)
        }
        if (showToast) {
            Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show()
        }
    }

    fun clearConversation() {
        recognizer.updateLocale(language.getVoiceLanguage(prefs.voiceLocale))
        repliesList.clear()
        _replies.postValue(repliesList)
    }

    inner class ContactHelper : ContactsInterface {

        override fun findEmail(input: String?): String? {
            if (!Permissions.checkPermission(context, Permissions.READ_CONTACTS) || input == null) {
                return null
            }
            var part: String = input
            var number: String? = null
            while (part.length > 1) {
                val selection =
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + part + "%'"
                val projection = arrayOf(ContactsContract.CommonDataKinds.Email.DATA)
                val c = context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection, selection, null, null
                )
                if (c != null && c.moveToFirst()) {
                    number = c.getString(0)
                    c.close()
                }
                if (number != null)
                    break
                part = part.substring(0, part.length - 2)
            }
            return number
        }

        override fun findNumber(input: String?): String? {
            if (!Permissions.checkPermission(context, Permissions.READ_CONTACTS) || input == null) {
                return null
            }
            var part: String = input
            var number: String? = null
            while (part.length > 1) {
                val selection =
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + part + "%'"
                val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val c = context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection, selection, null, null
                )
                if (c != null && c.moveToFirst()) {
                    number = c.getString(0)
                    c.close()
                }
                if (number != null) {
                    break
                }
                part = part.substring(0, part.length - 1)
            }
            return number
        }
    }
}
