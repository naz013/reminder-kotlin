package com.elementary.tasks.reminder.create

import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.PrefsConstants
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.reminder.create.selector.Option
import com.elementary.tasks.reminder.create.selector.OptionsFactory
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class StateViewModel : ViewModel(), LifecycleObserver, KoinComponent, (String) -> Unit {

    private val prefs: Prefs by inject()
    private val context: Context by inject()

    private val _options = mutableLiveDataOf<List<Option>>()
    val options: LiveData<List<Option>> = _options

    var shopItems: List<ShopItem> = listOf()
    var weekdays: List<Int> = listOf()
    var reminder: Reminder = Reminder()
        set(value) {
            field = value
            initOptions(value.type)
        }
    var group: ReminderGroup? = null

    var isShopItemsEdited: Boolean = false
    var isLogged: Boolean = false
    var isEdited: Boolean = false
    var isExpanded: Boolean = false
    var isLink: Boolean = false
    var isMessage: Boolean = false
    var isDelayAdded: Boolean = false
    var isEmailOrSubjectChanged: Boolean = false
    var isLeave: Boolean = false
    var isLastDay: Boolean = false
    var isWeekdaysSaved: Boolean = false
    var isAppSaved: Boolean = false

    var app: String = ""
    var link: String = ""
    var email: String = ""
    var subject: String = ""
    var skypeContact: String = ""

    var day: Int = 0
    var month: Int = 0
    var year: Int = 0

    var hour: Int = 0
    var minute: Int = 0

    var timer: Long = 0
    var isPaused: Boolean = false
    var original: Reminder? = null
    var isSaving: Boolean = false
    var isFromFile: Boolean = false

    init {
        prefs.addObserver(PrefsConstants.REMINDER_OPTIONS, this)
        initOptions()
        setDateTime()
    }

    private fun initOptions(type: Int = 0) {
        val keys = prefs.reminderOptions
        _options.postValue(OptionsFactory.createList(
                context,
                keys,
                if (type != 0) OptionsFactory.keyFromType(type) else "",
                prefs.lastUsedReminder
        ))
    }

    private fun setDateTime(millis: Long = System.currentTimeMillis()) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        day = calendar.get(Calendar.DAY_OF_MONTH)
        month = calendar.get(Calendar.MONTH)
        year = calendar.get(Calendar.YEAR)
        hour = calendar.get(Calendar.HOUR)
        minute = calendar.get(Calendar.MINUTE)
    }

    override fun onCleared() {
        super.onCleared()
        prefs.removeObserver(PrefsConstants.REMINDER_OPTIONS, this)
    }

    override fun invoke(p1: String) {
        initOptions(reminder.type)
    }
}