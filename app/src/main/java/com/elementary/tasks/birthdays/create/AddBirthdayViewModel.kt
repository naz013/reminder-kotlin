package com.elementary.tasks.birthdays.create

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.birthdays.work.SingleBackupWorker
import com.elementary.tasks.core.analytics.AnalyticsEventSender
import com.elementary.tasks.core.analytics.Feature
import com.elementary.tasks.core.analytics.FeatureUsedEvent
import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayEditAdapter
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayEdit
import com.elementary.tasks.core.os.IntentDataHolder
import com.elementary.tasks.core.os.contacts.ContactsReader
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.UriReader
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.work.WorkerLauncher
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import timber.log.Timber
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
  private val uriReader: UriReader,
  private val updatesHelper: UpdatesHelper,
  private val intentDataHolder: IntentDataHolder,
  private val uiBirthdayDateFormatter: UiBirthdayDateFormatter
) : BaseProgressViewModel(dispatcherProvider) {

  private val _birthday = mutableLiveDataOf<UiBirthdayEdit>()
  val birthday = _birthday.toLiveData()

  private var editableBirthday: Birthday? = null

  private val _formattedDate = mutableLiveDataOf<String>()
  val formattedDate = _formattedDate.toLiveData()

  private val _isContactAttached = mutableLiveDataOf<Boolean>()
  val isContactAttached = _isContactAttached.toLiveData()

  var isEdited = false
  var hasSameInDb = false
  var isFromFile = false
  var selectedDate: LocalDate = dateTimeManager.getCurrentDate()

  fun load() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val birthday = birthdaysDao.getById(id) ?: return@launch
      Traces.logEvent("Birthday loaded from DB")
      onBirthdayLoaded(birthday)
    }
  }

  fun onIntent() {
    intentDataHolder.get(Constants.INTENT_ITEM, Birthday::class.java)?.run {
      Traces.logEvent("Birthday loaded from intent")
      onBirthdayLoaded(this)
      isFromFile = true
      findSame(uuId)
    }
  }

  fun onFile(uri: Uri) {
    viewModelScope.launch(dispatcherProvider.default()) {
      runCatching {
        uriReader.readBirthdayObject(uri)?.also {
          Traces.logEvent("Birthday loaded from file")
          onBirthdayLoaded(it)
          isFromFile = true
          findSame(it.uuId)
        } ?: run { onDateChanged(dateTimeManager.getCurrentDate()) }
      }
    }
  }

  fun onContactAttached(value: Boolean) {
    _isContactAttached.postValue(value)
  }

  fun onDateChanged(localDate: LocalDate) {
    Timber.d("onDateChanged: $localDate")
    selectedDate = localDate
    _formattedDate.postValue(uiBirthdayDateFormatter.getDateFormatted(localDate))
  }

  fun onYearCheckChanged(ignoreYear: Boolean) {
    uiBirthdayDateFormatter.changeShowYear(!ignoreYear)
    onDateChanged(selectedDate)
  }

  fun save(name: String, number: String?, newId: Boolean = false, ignoreYear: Boolean) {
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
        dayMonth = "${selectedDate.dayOfMonth}|${selectedDate.monthValue - 1}",
        uuId = editableBirthday?.uuId?.takeIf { !newId } ?: UUID.randomUUID().toString(),
        updatedAt = dateTimeManager.getNowGmtDateTime(),
        ignoreYear = ignoreYear
      ) ?: Birthday(
        name = name,
        contactId = contactId,
        date = formattedDate,
        number = number ?: "",
        day = selectedDate.dayOfMonth,
        month = selectedDate.monthValue - 1,
        dayMonth = "${selectedDate.dayOfMonth}|${selectedDate.monthValue - 1}",
        updatedAt = dateTimeManager.getNowGmtDateTime(),
        ignoreYear = ignoreYear
      )
      Traces.logEvent("Birthday saved")
      analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_BIRTHDAY))
      saveBirthday(birthday)
    }
  }

  fun deleteBirthday() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      birthdaysDao.delete(id)
      notifier.showBirthdayPermanent()
      updatesHelper.updateTasksWidget()
      updatesHelper.updateBirthdaysWidget()
      workerLauncher.startWork(BirthdayDeleteBackupWorker::class.java, Constants.INTENT_ID, id)
      Traces.logEvent("Birthday deleted")
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  private fun onBirthdayLoaded(birthday: Birthday) {
    if (!isEdited) {
      isEdited = true
      editableBirthday = birthday
      onDateChanged(
        dateTimeManager.parseBirthdayDate(birthday.date) ?: dateTimeManager.getCurrentDate()
      )
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
      birthdaysDao.insert(birthday)
      notifier.showBirthdayPermanent()
      updatesHelper.updateBirthdaysWidget()
      updatesHelper.updateTasksWidget()
      workerLauncher.startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, birthday.uuId)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }
}
