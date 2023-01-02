package com.elementary.tasks.birthdays.create

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.birthdays.work.SingleBackupWorker
import com.elementary.tasks.core.analytics.AnalyticsEventSender
import com.elementary.tasks.core.analytics.Feature
import com.elementary.tasks.core.analytics.FeatureUsedEvent
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayEditAdapter
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayEdit
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.contacts.ContactsReader
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.utils.DispatcherProvider
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import java.util.UUID

class AddBirthdayViewModel(
  private val id: String,
  private val birthdaysDao: BirthdaysDao,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val notifier: Notifier,
  private val contactsReader: ContactsReader,
  private val dateTimeManager: DateTimeManager,
  private val analyticsEventSender: AnalyticsEventSender,
  private val uiBirthdayEditAdapter: UiBirthdayEditAdapter,
  private val contextProvider: ContextProvider
) : BaseProgressViewModel(dispatcherProvider) {

  private val _birthday = mutableLiveDataOf<UiBirthdayEdit>()
  val birthday = _birthday.toLiveData()

  var editableBirthday: Birthday? = null

  private val _formattedDate = mutableLiveDataOf<String>()
  val formattedDate = _formattedDate.toLiveData()

  private val _isContactAttached = mutableLiveDataOf<Boolean>()
  val isContactAttached = _isContactAttached.toLiveData()

  var isEdited = false
  var hasSameInDb = false
  var isFromFile = false
  var selectedDate: LocalDate = LocalDate.now()

  init {
    load()
  }

  fun load() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val birthday = birthdaysDao.getById(id) ?: return@launch
      onBirthdayLoaded(birthday)
    }
  }

  fun onIntent(birthday: Birthday?) {
    if (birthday != null) {
      onBirthdayLoaded(birthday)
      isFromFile = true
      findSame(birthday.uuId)
    }
  }

  fun onFile(uri: Uri) {
    viewModelScope.launch(dispatcherProvider.default()) {
      runCatching {
        if (ContentResolver.SCHEME_CONTENT != uri.scheme) {
          val any = MemoryUtil.readFromUri(contextProvider.context, uri, FileConfig.FILE_NAME_BIRTHDAY)
          if (any != null && any is Birthday) {
            onBirthdayLoaded(any)
          }
        }
      }
    }
  }

  fun onContactAttached(value: Boolean) {
    _isContactAttached.postValue(value)
  }

  fun onDateChanged(localDate: LocalDate) {
    selectedDate = localDate
    _formattedDate.postValue(dateTimeManager.formatBirthdayDate(selectedDate))
  }

  fun save(name: String, number: String?, newId: Boolean = false) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val contactId = contactsReader.getIdFromNumber(number)
      val formattedDate = dateTimeManager.formatBirthdayDate(selectedDate)
      val birthday = editableBirthday?.copy(
        name = name,
        contactId = contactId,
        date = formattedDate,
        number = number ?: "",
        day = selectedDate.dayOfMonth,
        month = selectedDate.monthValue - 1,
        dayMonth = "${selectedDate.dayOfMonth}|${selectedDate.monthValue - 1}"
      ) ?: Birthday(
        name = name,
        contactId = contactId,
        date = formattedDate,
        number = number ?: "",
        day = selectedDate.dayOfMonth,
        month = selectedDate.monthValue - 1,
        dayMonth = "${selectedDate.dayOfMonth}|${selectedDate.monthValue - 1}"
      )
      if (newId) {
        birthday.uuId = UUID.randomUUID().toString()
      }
      analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_BIRTHDAY))
      saveBirthday(birthday)
    }
  }

  fun deleteBirthday() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      birthdaysDao.delete(id)
      notifier.showBirthdayPermanent()
      workerLauncher.startWork(BirthdayDeleteBackupWorker::class.java, Constants.INTENT_ID, id)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  private fun onBirthdayLoaded(birthday: Birthday) {
    if (!isEdited) {
      isEdited = true
      editableBirthday = birthday
      selectedDate = dateTimeManager.parseBirthdayDate(birthday.date) ?: LocalDate.now()
      _formattedDate.postValue(dateTimeManager.formatBirthdayDate(selectedDate))
      _birthday.postValue(uiBirthdayEditAdapter.convert(birthday))
    }
  }

  private fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val birthday = birthdaysDao.getById(id)
      hasSameInDb = birthday != null
    }
  }

  private fun saveBirthday(birthday: Birthday) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      birthday.updatedAt = dateTimeManager.getNowGmtDateTime()
      birthdaysDao.insert(birthday)
      notifier.showBirthdayPermanent()
      workerLauncher.startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, birthday.uuId)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }
}
