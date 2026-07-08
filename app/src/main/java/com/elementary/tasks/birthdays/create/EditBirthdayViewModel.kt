package com.elementary.tasks.birthdays.create

import android.graphics.Bitmap
import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.usecase.DeleteBirthdayUseCase
import com.elementary.tasks.birthdays.usecase.SaveBirthdayUseCase
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayEditAdapter
import com.elementary.tasks.core.deeplink.BirthdayDateDeepLinkData
import com.elementary.tasks.core.deeplink.DeepLinkDataParser
import com.elementary.tasks.core.os.data.ContactData
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.common.contacts.ContactsReader
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.BirthdayRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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
  private val intentDataReader: IntentDataReader,
  private val uiBirthdayDateFormatter: UiBirthdayDateFormatter,
  private val deleteBirthdayUseCase: DeleteBirthdayUseCase,
  private val saveBirthdayUseCase: SaveBirthdayUseCase,
) : BaseProgressViewModel(dispatcherProvider) {
  val state: StateFlow<EditBirthdayState> field = MutableStateFlow(EditBirthdayState())

  private var editableBirthday: Birthday? = null

  private var isEdited = false
  private var hasSameInDb = false
  private var isFromFile = false
  var selectedDate: LocalDate = dateTimeManager.getCurrentDate()
    private set

  init {
    state.update { it.copy(hasId = id.isNotEmpty()) }
    load()
  }

  /** Seeds the screen from a shared-file import / calendar date deep link, read once from the
   *  island Fragment's arguments — mirrors [com.elementary.tasks.googletasks.task.EditGoogleTaskViewModel.checkDeepLink]. */
  fun checkArguments(arguments: Bundle?) {
    val bundle = arguments ?: return
    when {
      bundle.getBoolean(IntentKeys.INTENT_ITEM, false) -> onIntent()
      bundle.getBoolean(IntentKeys.INTENT_DEEP_LINK, false) -> onDeepLink(bundle)
      id.isEmpty() -> onDateChanged(LocalDate.now())
    }
  }

  fun onNameChanged(text: String) {
    state.update { it.copy(name = text, nameError = false) }
  }

  fun onNumberChanged(text: String) {
    state.update { it.copy(number = text) }
    if (text.isNotEmpty()) resolveContactInfo(text) else clearContactInfo()
  }

  fun onContactPicked(contactData: ContactData) {
    state.update {
      it.copy(
        number = contactData.phone,
        name = it.name.ifBlank { contactData.name },
        nameError = false,
      )
    }
    resolveContactInfo(contactData.phone, knownName = contactData.name)
  }

  fun onDateChanged(localDate: LocalDate) {
    Logger.d(TAG, "onDateChanged: $localDate")
    selectedDate = localDate
    state.update { it.copy(dateText = uiBirthdayDateFormatter.getDateFormatted(localDate)) }
  }

  fun onYearCheckChanged(ignoreYear: Boolean) {
    uiBirthdayDateFormatter.changeShowYear(!ignoreYear)
    state.update { it.copy(ignoreYear = ignoreYear) }
    onDateChanged(selectedDate)
  }

  fun onSaveClick() {
    val name = state.value.name.trim()
    if (name.isEmpty()) {
      state.update { it.copy(nameError = true) }
      return
    }
    if (isFromFile && hasSameInDb) {
      state.update { it.copy(dialog = EditBirthdayDialog.CopyConflict) }
      return
    }
    performSave(name, newId = false)
  }

  fun onCopyKeepClick() {
    dismissDialog()
    performSave(state.value.name.trim(), newId = true)
  }

  fun onCopyReplaceClick() {
    dismissDialog()
    performSave(state.value.name.trim(), newId = false)
  }

  fun onDeleteMenuClick() {
    state.update { it.copy(dialog = EditBirthdayDialog.DeleteConfirm) }
  }

  fun onDeleteConfirmed() {
    dismissDialog()
    if (!state.value.canDelete) return
    Logger.i(TAG, "Deleting birthday, id: $id")
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      deleteBirthdayUseCase(id)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  fun onDialogDismiss() {
    dismissDialog()
  }

  private fun dismissDialog() {
    state.update { it.copy(dialog = null) }
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val birthday = birthdayRepository.getById(id) ?: return@launch
      Logger.logEvent("Birthday loaded from DB")
      onBirthdayLoaded(birthday)
    }
  }

  private fun onIntent() {
    intentDataReader.get(IntentKeys.INTENT_ITEM, Birthday::class.java)?.run {
      Logger.logEvent("Birthday loaded from intent")
      isFromFile = true
      onBirthdayLoaded(this)
      findSame(uuId)
    }
  }

  private fun onDeepLink(bundle: Bundle) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val parser = DeepLinkDataParser()
      when (val deepLinkData = parser.readDeepLinkData(bundle)) {
        is BirthdayDateDeepLinkData -> onDateChanged(deepLinkData.date)
        else -> onDateChanged(LocalDate.now())
      }
    }
  }

  private fun performSave(
    name: String,
    newId: Boolean,
  ) {
    val number = state.value.number.takeIf { it.isNotEmpty() }
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val contactId = contactsReader.getIdFromNumber(number)
      val formattedDate = dateTimeManager.formatBirthdayDate(selectedDate)
      val ignoreYear = state.value.ignoreYear
      val birthday =
        editableBirthday?.copy(
          name = name,
          contactId = contactId,
          date = formattedDate,
          number = number ?: "",
          day = selectedDate.dayOfMonth,
          month = selectedDate.monthValue - 1,
          dayMonth = "${selectedDate.dayOfMonth}|${selectedDate.monthValue - 1}",
          uuId = editableBirthday?.uuId?.takeIf { !newId } ?: UUID.randomUUID().toString(),
          updatedAt = dateTimeManager.getNowGmtDateTime(),
          ignoreYear = ignoreYear,
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
          version = 0,
        )
      analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_BIRTHDAY))
      Logger.i(TAG, "Saving the birthday with id: ${birthday.uuId}")
      saveBirthdayUseCase(birthday)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  private fun onBirthdayLoaded(birthday: Birthday) {
    if (isEdited) return
    isEdited = true
    editableBirthday = birthday
    onDateChanged(dateTimeManager.parseBirthdayDate(birthday.date) ?: dateTimeManager.getCurrentDate())
    val uiBirthday = uiBirthdayEditAdapter.convert(birthday)
    state.update {
      it.copy(
        name = uiBirthday.name,
        number = uiBirthday.number,
        ignoreYear = uiBirthday.isYearIgnored,
        canDelete = isEdited && !isFromFile,
      )
    }
    if (uiBirthday.number.isNotEmpty()) resolveContactInfo(uiBirthday.number)
  }

  private fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      hasSameInDb = birthdayRepository.getById(id) != null
    }
  }

  private fun resolveContactInfo(
    number: String,
    knownName: String? = null,
  ) {
    val contactId = contactsReader.getIdFromNumber(number)
    if (contactId == 0L) {
      clearContactInfo()
      return
    }
    val photo: Bitmap? = contactsReader.getPhotoBitmap(contactId)
    val name = knownName ?: contactsReader.getNameFromNumber(number)
    state.update { it.copy(contactName = name, contactPhoto = photo) }
  }

  private fun clearContactInfo() {
    state.update { it.copy(contactName = null, contactPhoto = null) }
  }

  companion object {
    private const val TAG = "EditBirthdayViewModel"
  }
}
