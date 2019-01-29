package com.elementary.tasks.reminder.create

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.data.models.Reminder

class StateViewModel : ViewModel(), LifecycleObserver {

    var reminder: Reminder = Reminder()
    var isLogged: Boolean = false
    var isEdited: Boolean = false
    var isExpanded: Boolean = false
}