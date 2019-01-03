package com.elementary.tasks.core.viewModels.conversation

import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.text.TextUtils
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.backdoor.engine.*
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.birthdays.createEdit.AddBirthdayActivity
import com.elementary.tasks.core.SplashScreen
import com.elementary.tasks.core.data.models.*
import com.elementary.tasks.core.dialogs.VoiceHelpDialog
import com.elementary.tasks.core.dialogs.VoiceResultDialog
import com.elementary.tasks.core.dialogs.VolumeDialog
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.reminders.BaseRemindersViewModel
import com.elementary.tasks.navigation.MainActivity
import com.elementary.tasks.reminder.createEdit.CreateReminderActivity
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Copyright 2018 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class ConversationViewModel(application: Application) : BaseRemindersViewModel(application) {

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

    @Inject
    lateinit var prefs: Prefs
    @Inject
    lateinit var recognizer: Recognizer

    init {
        ReminderApp.appComponent.inject(this)
    }

    fun getNotes() {
        postInProgress(true)
        launchDefault{
            val list = LinkedList(appDb.notesDao().all())
            withUIContext {
                postInProgress(false)
                _notes.postValue(list)
            }
        }
    }

    fun getShoppingReminders() {
        postInProgress(true)
        launchDefault {
            val list = LinkedList(appDb.reminderDao().getAllTypes(true, false, intArrayOf(Reminder.BY_DATE_SHOP)))
            withUIContext {
                postInProgress(false)
                _shoppingLists.postValue(list)
            }
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
            withUIContext {
                postInProgress(false)
                _enabledReminders.postValue(list)
            }
        }
    }

    fun getReminders(dateTime: Long) {
        postInProgress(true)
        launchDefault {
            val list = LinkedList(appDb.reminderDao().getActiveInRange(
                    false,
                    TimeUtil.getGmtFromDateTime(System.currentTimeMillis()),
                    TimeUtil.getGmtFromDateTime(dateTime)))
            withUIContext {
                postInProgress(false)
                _activeReminders.postValue(list)
            }
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
            withUIContext {
                postInProgress(false)
                _birthdays.postValue(list)
            }
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
                    val action = model.action
                    when (action) {
                        Action.APP -> context.startActivity(Intent(context, SplashScreen::class.java))
                        Action.HELP -> context.startActivity(Intent(context, VoiceHelpDialog::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT))
                        Action.BIRTHDAY -> context.startActivity(Intent(context, AddBirthdayActivity::class.java))
                        Action.REMINDER -> context.startActivity(Intent(context, CreateReminderActivity::class.java))
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
                            val startActivityIntent = Intent(context, MainActivity::class.java)
                            startActivityIntent.putExtra(Constants.INTENT_POSITION, R.id.nav_feedback)
                            context.startActivity(startActivityIntent)
                        }
                    }
                } else if (types == ActionType.NOTE) {
                    saveNote(createNote(model.summary), true, true)
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
            getApplication<ReminderApp>().startActivity(Intent(getApplication(), VoiceResultDialog::class.java)
                    .putExtra(Constants.INTENT_ID, reminder.uuId)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP))
        } else {
            Toast.makeText(getApplication(), R.string.saved, Toast.LENGTH_SHORT).show()
        }
    }

    fun disableAllReminders(showToast: Boolean) {
        postInProgress(true)
        launchDefault {
            for (reminder in appDb.reminderDao().getAll(true, false)) {
                stopReminder(reminder)
            }
            withUIContext {
                postInProgress(false)
                postCommand(Commands.DELETED)
                if (showToast) {
                    Toast.makeText(getApplication(), R.string.all_reminders_were_disabled, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun emptyTrash(showToast: Boolean) {
        postInProgress(true)
        launchDefault {
            val archived = appDb.reminderDao().getAll(false, true)
            for (reminder in archived) {
                deleteReminder(reminder, false)
                calendarUtils.deleteEvents(reminder.uniqueId)
            }
            withUIContext {
                postInProgress(false)
                postCommand(Commands.TRASH_CLEARED)
                if (showToast) {
                    Toast.makeText(getApplication(), R.string.trash_cleared, Toast.LENGTH_SHORT).show()
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
        updatesHelper.updateNotesWidget()
        if (showToast) {
            Toast.makeText(getApplication(), R.string.saved, Toast.LENGTH_SHORT).show()
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
                Toast.makeText(getApplication(), R.string.saved, Toast.LENGTH_SHORT).show()
            }
    }

    inner class ContactHelper : ContactsInterface {

        override fun findEmail(s: String): ContactOutput? {
            var input = s
            if (!Permissions.checkPermission(getApplication(), Permissions.READ_CONTACTS)) {
                return null
            }
            var number: String? = null
            val parts = input.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (part in parts) {
                var res = part
                while (part.length > 1) {
                    val selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + part + "%'"
                    val projection = arrayOf(ContactsContract.CommonDataKinds.Email.DATA)
                    val c = getApplication<ReminderApp>().contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
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
            if (!Permissions.checkPermission(getApplication(), Permissions.READ_CONTACTS)) {
                return null
            }
            var number: String? = null
            val parts = input.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (part in parts) {
                var res = part
                while (part.length > 1) {
                    val selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + part + "%'"
                    val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val c = getApplication<ReminderApp>().contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
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
