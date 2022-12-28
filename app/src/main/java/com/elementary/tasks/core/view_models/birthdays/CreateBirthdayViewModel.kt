package com.elementary.tasks.core.view_models.birthdays

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.birthdays.work.SingleBackupWorker
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.contacts.ContactsReader
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.view_models.BaseProgressViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import java.util.UUID

class CreateBirthdayViewModel(
  id: String,
  private val birthdaysDao: BirthdaysDao,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val notifier: Notifier,
  private val contactsReader: ContactsReader,
  private val dateTimeManager: DateTimeManager
) : BaseProgressViewModel(dispatcherProvider) {

  val birthday = birthdaysDao.loadById(id)
  var editableBirthday: Birthday = Birthday()

  private val _formattedDate = mutableLiveDataOf<String>()
  val formattedDate = _formattedDate.toLiveData()

  private val _isContactAttached = mutableLiveDataOf<Boolean>()
  val isContactAttached = _isContactAttached.toLiveData()

  var isEdited = false
  var hasSameInDb = false
  var isFromFile = false
  var selectedDate = LocalDate.now()

  private var preparedBirthday: Birthday? = null

  fun editBirthday(birthday: Birthday) {
    editableBirthday = birthday
    selectedDate = dateTimeManager.parseBirthdayDate(birthday.date)
    _formattedDate.postValue(dateTimeManager.formatBirthdayDate(selectedDate))
  }

  fun onContactAttached(value: Boolean) {
    _isContactAttached.postValue(value)
  }

  fun onDateChanged(localDate: LocalDate) {
    selectedDate = localDate
    _formattedDate.postValue(dateTimeManager.formatBirthdayDate(selectedDate))
  }

  fun onDateChanged(millis: Long) {
    selectedDate = dateTimeManager.fromMillis(millis).toLocalDate()
    _formattedDate.postValue(dateTimeManager.formatBirthdayDate(selectedDate))
  }

  fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val birthday = birthdaysDao.getById(id)
      hasSameInDb = birthday != null
    }
  }

  fun save() {
    preparedBirthday?.also {
      saveBirthday(it)
    }
  }

  fun prepare(name: String, number: String?, dateString: String?, newId: Boolean = false) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val contactId = contactsReader.getIdFromNumber(number)
      val birthday = editableBirthday.apply {
        this.name = name
        this.contactId = contactId
        this.date = dateString ?: ""
        this.number = number ?: ""
        this.day = selectedDate.dayOfMonth
        this.month = selectedDate.monthValue - 1
        this.dayMonth = "${selectedDate.dayOfMonth}|${selectedDate.monthValue - 1}"
      }
      if (newId) {
        birthday.uuId = UUID.randomUUID().toString()
      }
      preparedBirthday = birthday
    }
  }

  private fun saveBirthday(birthday: Birthday) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      birthday.updatedAt = DateTimeManager.gmtDateTime
      birthdaysDao.insert(birthday)
      notifier.showBirthdayPermanent()
      workerLauncher.startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, birthday.uuId)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  fun deleteBirthday(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      birthdaysDao.delete(id)
      notifier.showBirthdayPermanent()
      workerLauncher.startWork(BirthdayDeleteBackupWorker::class.java, Constants.INTENT_ID, id)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
