package com.elementary.tasks.google_tasks.create

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.Reminder
import com.github.naz013.calendarext.newCalendar
import java.util.*

class GoogleTasksStateViewModel : ViewModel(), LifecycleObserver {

  var date: MutableLiveData<Calendar> = MutableLiveData()
  var time: MutableLiveData<Calendar> = MutableLiveData()
  var isReminder: MutableLiveData<Boolean> = MutableLiveData()
  var isDateEnabled: MutableLiveData<Boolean> = MutableLiveData()
  var reminderValue: MutableLiveData<Reminder> = MutableLiveData()

  var isEdited = false
  var isReminderEdited = false
  var listId: String = ""
  var action: String = ""

  var isLoading = false
  var editedItem: GoogleTask? = null

  fun takeDate() = date.value ?: newCalendar()

  fun takeTime() = time.value ?: newCalendar()
}