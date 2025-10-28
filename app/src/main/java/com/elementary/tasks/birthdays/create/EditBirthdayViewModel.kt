package com.elementary.tasks.birthdays.create

import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.usecase.DeleteBirthdayUseCase
import com.elementary.tasks.birthdays.usecase.SaveBirthdayUseCase
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayEditAdapter
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayEdit
import com.elementary.tasks.core.deeplink.BirthdayDateDeepLinkData
import com.elementary.tasks.core.deeplink.DeepLinkDataParser
import com.elementary.tasks.core.utils.io.UriReader
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.common.contacts.ContactsReader
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.BirthdayRepository
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import java.util.UUID

class EditBirthdayViewModel(
  private val id: String,
  private val birthdayRepository: BirthdayRepository,
  dispatcherProvider: DispatcherProvider,
  private val contactsReader: ContactsReader,
  private val dateTimeManager: DateTimeManager,
  private val analyticsEventSender: AnalyticsEventSender,
  private val uiBirthdayEditAdapter: UiBirthdayEditAdapter,
  private val uriReader: UriReader,
  private val intentDataReader: IntentDataReader,
  private val uiBirthdayDateFormatter: UiBirthdayDateFormatter,
  private val deleteBirthdayUseCase: DeleteBirthdayUseCase,
  private val saveBirthdayUseCase: SaveBirthdayUseCase
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

  fun hasId(): Boolean = id.isNotEmpty()

  fun load() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val birthday = birthdayRepository.getById(id) ?: return@launch
      Logger.logEvent("Birthday loaded from DB")
      onBirthdayLoaded(birthday)
    }
  }

  fun onIntent() {
    intentDataReader.get(IntentKeys.INTENT_ITEM, Birthday::class.java)?.run {
      Logger.logEvent("Birthday loaded from intent")
      onBirthdayLoaded(this)
      isFromFile = true
      findSame(uuId)
    }
  }

  fun onDeepLink(bundle: Bundle) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val parser = DeepLinkDataParser()
      when (val deepLinkData = parser.readDeepLinkData(bundle)) {
        is BirthdayDateDeepLinkData -> {
          onDateChanged(deepLinkData.date)
        }

        else -> {
          onDateChanged(LocalDate.now())
        }
      }
    }
  }

  fun onFile(uri: Uri) {
    viewModelScope.launch(dispatcherProvider.default()) {
      runCatching {
        uriReader.readBirthdayObject(uri)?.also {
          Logger.logEvent("Birthday loaded from file")
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
    Logger.d("onDateChanged: $localDate")
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
        ignoreYear = ignoreYear,
        syncState = SyncState.WaitingForUpload,
        version = 0
      )
      analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_BIRTHDAY))
      Logger.i("Saving the birthday with id: ${birthday.uuId}")
      saveBirthday(birthday)
    }
  }

  fun deleteBirthday() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      deleteBirthdayUseCase(id)
      Logger.i("Deleting the birthday with id: $id")
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
      val birthday = birthdayRepository.getById(id)
      hasSameInDb = birthday != null
    }
  }

  private fun saveBirthday(birthday: Birthday) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      saveBirthdayUseCase(birthday)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }
}
