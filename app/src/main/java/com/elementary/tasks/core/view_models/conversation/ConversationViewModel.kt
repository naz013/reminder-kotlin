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
import com.backdoor.engine.misc.ContactOutput
import com.backdoor.engine.misc.ContactsInterface
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.core.SplashScreen
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.data.models.*
import com.elementary.tasks.core.dialogs.VoiceHelpActivity
import com.elementary.tasks.core.dialogs.VoiceResultDialog
import com.elementary.tasks.core.dialogs.VolumeDialog
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.reminders.BaseRemindersViewModel
import com.elementary.tasks.navigation.MainActivity
import com.elementary.tasks.navigation.settings.other.SendFeedbackActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.voice.Container
import com.elementary.tasks.voice.Reply
import org.koin.standalone.inject
import timber.log.Timber
import java.util.*

class ConversationViewModel : BaseRemindersViewModel() {

    private var _shoppingLists = MutableLiveData<List<Reminder>>()
    var shoppingLists: LiveData<List<Reminder>> = _shoppingLists

    private var _enabledReminders = MutableLiveData<List<Reminder>>()
    var enabledReminders: LiveData<List<Reminder>> = _enabledReminders

    private var _activeReminders = MutableLiveData<List<Reminder>>()
    var activeReminders: LiveData<List<Reminder>> = _activeReminders

    private var _notes = MutableLiveData<List<NoteWithImages>>()
    var notes: LiveData<List<NoteWithImages>> = _notes

    private var _birthdays = MutableLiveData<List<Birthday>>()
    var birthdays: LiveData<List<Birthday>> = _birthdays

    private var _replies = MutableLiveData<List<Reply>>()
    var replies: LiveData<List<Reply>> = _replies
    private val mReplies = mutableListOf<Reply>()
    private var hasPartial = false

    private val recognizer: Recognizer by inject()
    private val language: Language by inject()
    private val context: Context by inject()

    init {
        clearConversation()
    }

    fun addMoreItemsToList(position: Int) {
        val reply = mReplies[position]
        val container = reply.content as Container<*>
        Timber.d("addMoreItemsToList: $container")
        when {
            container.type is ReminderGroup -> {
                mReplies.removeAt(position)
                for (item in container.list) {
                    mReplies.add(0, Reply(Reply.GROUP, item))
                }
                _replies.postValue(mReplies)
            }
            container.type is NoteWithImages -> {
                mReplies.removeAt(position)
                for (item in container.list) {
                    mReplies.add(0, Reply(Reply.NOTE, item))
                }
                _replies.postValue(mReplies)
            }
            container.type is Reminder -> {
                mReplies.removeAt(position)
                addRemindersToList(container)
                _replies.postValue(mReplies)
            }
            container.type is Birthday -> {
                mReplies.removeAt(position)
                val reversed = ArrayList(container.list)
                reversed.reverse()
                for (item in reversed) {
                    mReplies.add(0, Reply(Reply.BIRTHDAY, item))
                }
                _replies.postValue(mReplies)
            }
        }
    }

    private fun addRemindersToList(container: Container<*>) {
        val reversed = ArrayList((container as Container<Reminder>).list)
        reversed.reverse()
        for (item in reversed) {
            if (item.viewType == Reminder.REMINDER) {
                mReplies.add(0, Reply(Reply.REMINDER, item))
            } else {
                mReplies.add(0, Reply(Reply.SHOPPING, item))
            }
        }
    }

    fun removeFirst() {
        if (mReplies[0].viewType == Reply.ASK) {
            mReplies.removeAt(0)
            _replies.postValue(mReplies)
        } else {
            removeAsk()
        }
    }

    fun removeAsk() {
        for (i in 1 until mReplies.size) {
            val reply = mReplies[i]
            if (reply.viewType == Reply.ASK) {
                mReplies.removeAt(i)
                _replies.postValue(mReplies)
                break
            }
        }
    }

    fun removePartial() {
        if (hasPartial) {
            mReplies.removeAt(0)
            _replies.postValue(mReplies)
        }
        hasPartial = false
    }

    fun addReply(reply: Reply?, isPartial: Boolean = false) {
        if (reply != null) {
            if (!isPartial) {
                hasPartial = false
                mReplies.add(0, reply)
                _replies.postValue(mReplies)
            } else {
                if (hasPartial) {
                    mReplies[0].content = reply.content
                    _replies.postValue(mReplies)
                } else {
                    hasPartial = true
                    mReplies.add(0, reply)
                    _replies.postValue(mReplies)
                }
            }
        }
    }

    fun getNotes() {
        postInProgress(true)
        launchDefault {
            val list = LinkedList(appDb.notesDao().all())
            postInProgress(false)
            _notes.postValue(list)
        }
    }

    fun getShoppingReminders() {
        postInProgress(true)
        launchDefault {
            val list = LinkedList(appDb.reminderDao().getAllTypes(true, false, intArrayOf(Reminder.BY_DATE_SHOP)))
            postInProgress(false)
            _shoppingLists.postValue(list)
        }
    }

    fun getEnabledReminders(dateTime: Long) {
        postInProgress(true)
        launchDefault {
            val list = LinkedList(appDb.reminderDao().getAllTypesInRange(
                    true,
                    false,
                    TimeUtil.getGmtFromDateTime(System.currentTimeMillis()),
                    TimeUtil.getGmtFromDateTime(dateTime)))
            postInProgress(false)
            _enabledReminders.postValue(list)
        }
    }

    fun getReminders(dateTime: Long) {
        postInProgress(true)
        launchDefault {
            val list = LinkedList(appDb.reminderDao().getActiveInRange(
                    false,
                    TimeUtil.getGmtFromDateTime(System.currentTimeMillis()),
                    TimeUtil.getGmtFromDateTime(dateTime)))
            postInProgress(false)
            _activeReminders.postValue(list)
        }
    }

    fun getBirthdays(dateTime: Long, time: Long) {
        postInProgress(true)
        launchDefault {
            val list = LinkedList(appDb.birthdaysDao().all())
            for (i in list.indices.reversed()) {
                val itemTime = list[i].getDateTime(time)
                if (itemTime < System.currentTimeMillis() || itemTime > dateTime) {
                    list.removeAt(i)
                }
            }
            postInProgress(false)
            _birthdays.postValue(list)
        }
    }

    fun findSuggestion(suggestion: String): Model? {
        recognizer.setContactHelper(ContactHelper())
        return recognizer.parse(suggestion)
    }

    fun findResults(matches: ArrayList<*>): Reminder? {
        recognizer.setContactHelper(ContactHelper())
        for (i in matches.indices) {
            val key = matches[i]
            val keyStr = key.toString()
            val model = recognizer.parse(keyStr)
            if (model != null) {
                Timber.d("findResults: $model")
                return createReminder(model)
            }
        }
        return null
    }

    fun parseResults(matches: ArrayList<*>, isWidget: Boolean, context: Context) {
        for (i in matches.indices) {
            val key = matches[i]
            val keyStr = key.toString()
            val model = findSuggestion(keyStr)
            if (model != null) {
                Timber.d("parseResults: $model")
                val types = model.type
                if (types == ActionType.ACTION && isWidget) {
                    when (model.action) {
                        Action.APP -> context.startActivity(Intent(context, SplashScreen::class.java))
                        Action.HELP -> context.startActivity(Intent(context, VoiceHelpActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT))
                        Action.BIRTHDAY -> AddBirthdayActivity.openLogged(context)
                        Action.REMINDER -> CreateReminderActivity.openLogged(context)
                        Action.VOLUME -> context.startActivity(Intent(context, VolumeDialog::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT))
                        Action.TRASH -> emptyTrash(true)
                        Action.DISABLE -> disableAllReminders(true)
                        Action.SETTINGS -> {
                            val startActivityIntent = Intent(context, MainActivity::class.java)
                            startActivityIntent.putExtra(Constants.INTENT_POSITION, R.id.nav_settings)
                            context.startActivity(startActivityIntent)
                        }
                        Action.REPORT -> {
                            context.startActivity(Intent(context, SendFeedbackActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT))
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
            context.startActivity(Intent(context, VoiceResultDialog::class.java)
                    .putExtra(Constants.INTENT_ID, reminder.uuId)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP))
        } else {
            Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show()
        }
    }

    fun disableAllReminders(showToast: Boolean) {
        postInProgress(true)
        launchDefault {
            for (reminder in appDb.reminderDao().getAll(active = true, removed = false)) {
                stopReminder(reminder)
            }
            postInProgress(false)
            postCommand(Commands.DELETED)
            withUIContext {
                if (showToast) {
                    Toast.makeText(context, R.string.all_reminders_were_disabled, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun emptyTrash(showToast: Boolean) {
        postInProgress(true)
        launchDefault {
            val archived = appDb.reminderDao().getAll(active = false, removed = true)
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
        val summary = model.summary ?: ""
        val repeat = model.repeatInterval
        val weekdays = model.weekdays
        val isCalendar = model.isHasCalendar
        val startTime = model.dateTime
        var eventTime = TimeUtil.getDateTimeFromGmt(startTime)
        var typeT = Reminder.BY_DATE
        if (action == Action.WEEK || action == Action.WEEK_CALL || action == Action.WEEK_SMS) {
            typeT = Reminder.BY_WEEK
            eventTime = TimeCount.getNextWeekdayTime(TimeUtil.getDateTimeFromGmt(startTime), weekdays, 0)
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
            appDb.notesDao().insert(note)
        }
        UpdatesHelper.updateNotesWidget(context)
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
        return ReminderGroup(model.summary, Random().nextInt(16))
    }

    fun saveGroup(model: ReminderGroup, showToast: Boolean) {
        launchDefault {
            appDb.reminderGroupDao().insert(model)
        }
        if (showToast) {
            Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show()
        }
    }

    fun clearConversation() {
        recognizer.updateLocale(language.getVoiceLanguage(prefs.voiceLocale))
        mReplies.clear()
        _replies.postValue(mReplies)
    }

    inner class ContactHelper : ContactsInterface {

        override fun findEmail(s: String): ContactOutput? {
            var input = s
            if (!Permissions.checkPermission(context, Permissions.READ_CONTACTS)) {
                return null
            }
            var number: String? = null
            val parts = input.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (part in parts) {
                var res = part
                while (part.length > 1) {
                    val selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + part + "%'"
                    val projection = arrayOf(ContactsContract.CommonDataKinds.Email.DATA)
                    val c = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            projection, selection, null, null)
                    if (c != null && c.moveToFirst()) {
                        number = c.getString(0)
                        c.close()
                    }
                    if (number != null)
                        break
                    res = part.substring(0, part.length - 2)
                }
                if (number != null) {
                    input = input.replace(res, "")
                    break
                }
            }
            return ContactOutput(input, number)
        }

        override fun findNumber(s: String): ContactOutput? {
            var input = s
            if (!Permissions.checkPermission(context, Permissions.READ_CONTACTS)) {
                return null
            }
            var number: String? = null
            val parts = input.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (part in parts) {
                var res = part
                while (part.length > 1) {
                    val selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + part + "%'"
                    val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val c = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            projection, selection, null, null)
                    if (c != null && c.moveToFirst()) {
                        number = c.getString(0)
                        c.close()
                    }
                    if (number != null) {
                        break
                    }
                    res = part.substring(0, part.length - 1)
                }
                if (number != null) {
                    input = input.replace(res, "")
                    break
                }
            }
            return ContactOutput(input.trim { it <= ' ' }, number)
        }
    }
}
