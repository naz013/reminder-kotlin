package com.elementary.tasks.reminder.create

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.data.models.ShopItem
import java.util.*

class StateViewModel : ViewModel(), LifecycleObserver {

    var shopItems: List<ShopItem> = listOf()
    var weekdays: List<Int> = listOf()
    var reminder: Reminder = Reminder()
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
        setDateTime()
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
}