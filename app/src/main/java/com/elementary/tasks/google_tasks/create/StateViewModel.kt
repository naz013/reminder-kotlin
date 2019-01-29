package com.elementary.tasks.google_tasks.create

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.data.models.Reminder

class StateViewModel : ViewModel(), LifecycleObserver {

    var date: MutableLiveData<Long> = MutableLiveData()
    var time: MutableLiveData<Long> = MutableLiveData()
    var isReminder: MutableLiveData<Boolean> = MutableLiveData()
    var isDateEnabled: MutableLiveData<Boolean> = MutableLiveData()
    var reminderValue: MutableLiveData<Reminder> = MutableLiveData()

    var isEdited = false
    var isReminderEdited = false
    var listId: String = ""
    var action: String = ""
    var isLogged = false
}