package com.elementary.tasks.core.viewModels.conversation

import android.app.Application
import android.content.Intent
import android.provider.ContactsContract
import android.text.TextUtils
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.backdoor.engine.*
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.birthdays.createEdit.AddBirthdayActivity
import com.elementary.tasks.core.SplashScreen
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.dialogs.VoiceHelpDialog
import com.elementary.tasks.core.dialogs.VoiceResultDialog
import com.elementary.tasks.core.dialogs.VolumeDialog
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.temp.UI
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.reminders.BaseRemindersViewModel
import com.elementary.tasks.navigation.MainActivity
import com.elementary.tasks.reminder.createEdit.CreateReminderActivity
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
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

    var shoppingLists = MutableLiveData<List<Reminder>>()
    var enabledReminders = MutableLiveData<List<Reminder>>()
    var activeReminders = MutableLiveData<List<Reminder>>()
    var notes = MutableLiveData<List<Note>>()
    var birthdays = MutableLiveData<List<Birthday>>()

    @Inject
    lateinit var prefs: Prefs
    @Inject
    lateinit var timeCount: TimeCount
    @Inject
    lateinit var recognizer: Recognizer

    init {
        ReminderApp.appComponent.inject(this)
    }

    fun getNotes() {
        isInProgress.postValue(true)
        launch(CommonPool) {
            val list = LinkedList(appDb.notesDao().all())
            withContext(UI) {
                isInProgress.postValue(false)
                notes.postValue(list)
            }
        }
    }

    fun getShoppingReminders() {
        isInProgress.postValue(true)
        launch(CommonPool) {
            val list = LinkedList(appDb.reminderDao().getAllTypes(true, false, intArrayOf(Reminder.BY_DATE_SHOP)))
            withContext(UI) {
                isInProgress.postValue(false)
                shoppingLists.postValue(list)
            }
        }
    }

    fun getEnabledReminders(dateTime: Long) {
        isInProgress.postValue(true)
        launch(CommonPool) {
            val list = LinkedList(appDb.reminderDao().getAllTypesInRange(
                    true,
                    false,
                    TimeUtil.getGmtFromDateTime(System.currentTimeMillis()),
                    TimeUtil.getGmtFromDateTime(dateTime)))
            withContext(UI) {
                isInProgress.postValue(false)
                enabledReminders.postValue(list)
            }
        }
    }

    fun getReminders(dateTime: Long) {
        isInProgress.postValue(true)
        launch(CommonPool) {
            val list = LinkedList(appDb.reminderDao().getActiveInRange(
                    false,
                    TimeUtil.getGmtFromDateTime(System.currentTimeMillis()),
                    TimeUtil.getGmtFromDateTime(dateTime)))
            withContext(UI) {
                isInProgress.postValue(false)
                activeReminders.postValue(list)
            }
        }
    }

    fun getBirthdays(dateTime: Long, time: Long) {
        isInProgress.postValue(true)
        launch(CommonPool) {
            val list = LinkedList(appDb.birthdaysDao().all())
            for (i in list.indices.reversed()) {
                val itemTime = list[i].getDateTime(time)
                if (itemTime < System.currentTimeMillis() || itemTime > dateTime) {
                    list.removeAt(i)
                }
            }
            withContext(UI) {
                isInProgress.postValue(false)
                birthdays.postValue(list)
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
                LogUtil.d(TAG, "parseResults: $model")
                return createReminder(model)
            }
        }
        return null
    }

    fun parseResults(matches: ArrayList<*>, isWidget: Boolean) {
        for (i in matches.indices) {
            val key = matches[i]
            val keyStr = key.toString()
            val model = findSuggestion(keyStr)
            if (model != null) {
                LogUtil.d(TAG, "parseResults: $model")
                val types = model.type
                if (types == ActionType.ACTION && isWidget) {
                    val action = model.action
                    when (action) {
                        Action.APP -> getApplication<Application>().startActivity(Intent(getApplication(), SplashScreen::class.java))
                        Action.HELP -> getApplication<Application>().startActivity(Intent(getApplication(), VoiceHelpDialog::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT))
                        Action.BIRTHDAY -> getApplication<Application>().startActivity(Intent(getApplication(), AddBirthdayActivity::class.java))
                        Action.REMINDER -> getApplication<Application>().startActivity(Intent(getApplication(), CreateReminderActivity::class.java))
                        Action.VOLUME -> getApplication<Application>().startActivity(Intent(getApplication(), VolumeDialog::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT))
                        Action.TRASH -> emptyTrash(true)
                        Action.DISABLE -> disableAllReminders(true)
                        Action.SETTINGS -> {
                            val startActivityIntent = Intent(getApplication(), MainActivity::class.java)
                            startActivityIntent.putExtra(Constants.INTENT_POSITION, R.id.nav_settings)
                            getApplication<Application>().startActivity(startActivityIntent)
                        }
                        Action.REPORT -> {
                            val startActivityIntent = Intent(getApplication(), MainActivity::class.java)
                            startActivityIntent.putExtra(Constants.INTENT_POSITION, R.id.nav_feedback)
                            getApplication<Application>().startActivity(startActivityIntent)
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
            getApplication<Application>().startActivity(Intent(getApplication(), VoiceResultDialog::class.java)
                    .putExtra(Constants.INTENT_ID, reminder.uuId)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP))
        } else {
            Toast.makeText(getApplication(), R.string.saved, Toast.LENGTH_SHORT).show()
        }
    }

    fun disableAllReminders(showToast: Boolean) {
        isInProgress.postValue(true)
        launch(CommonPool) {
            for (reminder in appDb.reminderDao().getAll(true, false)) {
                stopReminder(reminder)
            }
            withContext(UI) {
                isInProgress.postValue(false)
                result.postValue(Commands.DELETED)
                if (showToast) {
                    Toast.makeText(getApplication(), R.string.all_reminders_were_disabled, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun emptyTrash(showToast: Boolean) {
        isInProgress.postValue(true)
        launch(CommonPool) {
            val archived = appDb.reminderDao().getAll(false, true)
            for (reminder in archived) {
                deleteReminder(reminder, false)
                calendarUtils.deleteEvents(reminder.uniqueId)
            }
            withContext(UI) {
                isInProgress.postValue(false)
                result.postValue(Commands.TRASH_CLEARED)
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
            eventTime = timeCount.getNextWeekdayTime(TimeUtil.getDateTimeFromGmt(startTime), weekdays, 0)
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
        val item = defaultReminderGroup.value
        var categoryId = ""
        if (item != null) {
            categoryId = item.groupUuId
        }
        val isCal = prefs.getBoolean(PrefsConstants.EXPORT_TO_CALENDAR)
        val isStock = prefs.getBoolean(PrefsConstants.EXPORT_TO_STOCK)
        val reminder = Reminder()
        reminder.type = typeT
        reminder.summary = summary
        reminder.groupUuId = categoryId
        reminder.weekdays = weekdays
        reminder.repeatInterval = repeat
        reminder.target = number
        reminder.eventTime = TimeUtil.getGmtFromDateTime(eventTime)
        reminder.startTime = TimeUtil.getGmtFromDateTime(eventTime)
        reminder.exportToCalendar = isCalendar && (isCal || isStock)
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
        launch(CommonPool) {
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
        val def = defaultReminderGroup.value
        if (def != null) {
            mReminder.groupUuId = def.groupUuId
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
        launch(CommonPool) {
            appDb.reminderGroupDao().insert(model)
        }
            if (showToast) {
                Toast.makeText(getApplication(), R.string.saved, Toast.LENGTH_SHORT).show()
            }
    }

    inner class ContactHelper : ContactsInterface {

        override fun findEmail(input: String): ContactOutput? {
            var input = input
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
                    val c = getApplication<Application>().contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
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

        override fun findNumber(input: String): ContactOutput? {
            var input = input
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
                    val c = getApplication<Application>().contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
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

    companion object {

        private const val TAG = "ConversationViewModel"
    }
}
