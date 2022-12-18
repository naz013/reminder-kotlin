package com.elementary.tasks.core.view_models.birthdays

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.utils.contacts.ContactsReader
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.github.naz013.calendarext.getDayOfMonth
import com.github.naz013.calendarext.getYear
import com.github.naz013.calendarext.newCalendar
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

class BirthdayViewModel(
  id: String,
  birthdaysDao: BirthdaysDao,
  dispatcherProvider: DispatcherProvider,
  workerLauncher: WorkerLauncher,
  notifier: Notifier,
  private val contactsReader: ContactsReader
) : BaseBirthdaysViewModel(birthdaysDao, dispatcherProvider, workerLauncher, notifier) {

  val birthday = birthdaysDao.loadById(id)
  var editableBirthday: Birthday = Birthday()

  private val _date = mutableLiveDataOf<Calendar>()
  val date: LiveData<Calendar> = _date

  var isEdited = false
  var hasSameInDb = false
  var isFromFile = false

  fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val birthday = birthdaysDao.getById(id)
      hasSameInDb = birthday != null
    }
  }

  fun save(name: String, number: String?, dateString: String?, newId: Boolean = false) {
    val contactId = contactsReader.getIdFromNumber(number)
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
