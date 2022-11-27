package com.elementary.tasks.core.view_models.birthdays

import android.content.Context
import androidx.lifecycle.LiveData
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.Contacts
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.github.naz013.calendarext.getDayOfMonth
import com.github.naz013.calendarext.getYear
import com.github.naz013.calendarext.newCalendar
import java.util.*

class BirthdayViewModel(
  id: String,
  appDb: AppDb,
  prefs: Prefs,
  context: Context,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider
) : BaseBirthdaysViewModel(appDb, prefs, context, dispatcherProvider, workManagerProvider) {

  val birthday = appDb.birthdaysDao().loadById(id)
  var editableBirthday: Birthday = Birthday()

  private val _date = mutableLiveDataOf<Calendar>()
  val date: LiveData<Calendar> = _date

  private val _isContactAttached = mutableLiveDataOf<Boolean>()
  val isContactAttached: LiveData<Boolean> = _isContactAttached

  var isEdited = false
  var hasSameInDb = false
  var isFromFile = false

  fun editBirthday(birthday: Birthday) {
    editableBirthday = birthday
  }

  fun onContactAttached(value: Boolean) {
    _isContactAttached.postValue(value)
  }

  fun onDateChanged(calendar: Calendar) {
    _date.postValue(calendar)
  }

  fun onDateChanged(millis: Long) {
    newCalendar(millis).also { _date.postValue(it) }
  }

  fun findSame(id: String) {
    launchDefault {
      val birthday = appDb.birthdaysDao().getById(id)
      hasSameInDb = birthday != null
    }
  }

  fun save(name: String, number: String?, dateString: String?, newId: Boolean = false) {
    val contactId = Contacts.getIdFromNumber(number, context)
    val calendar = date.value ?: newCalendar()
    val birthday = editableBirthday.apply {
      this.name = name
      this.contactId = contactId
      this.date = dateString ?: ""
      this.number = number ?: ""
      this.day = calendar.getDayOfMonth()
      this.month = calendar.getYear()
      this.dayMonth = "${this.day}|${this.month}"
    }
    if (newId) {
      birthday.uuId = UUID.randomUUID().toString()
    }
    saveBirthday(birthday)
  }
}
