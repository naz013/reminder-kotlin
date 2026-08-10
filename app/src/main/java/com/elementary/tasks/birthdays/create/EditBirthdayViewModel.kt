package com.elementary.tasks.birthdays.create

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdaysNavKey
import com.elementary.tasks.birthdays.usecase.DeleteBirthdayUseCase
import com.elementary.tasks.birthdays.usecase.SaveBirthdayUseCase
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayEditAdapter
import com.elementary.tasks.core.os.data.ContactData
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.common.contacts.ContactsReader
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Tag
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.logic.tag.ToggleTagAssignmentUseCase
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.ui.tag.TagChipState
import com.github.naz013.ui.tag.TagChipStateAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import java.util.UUID

class EditBirthdayViewModel(
  private val key: BirthdaysNavKey.Edit,
  private val birthdayRepository: BirthdayRepository,
  private val dispatcherProvider: DispatcherProvider,
  private val contactsReader: ContactsReader,
  private val dateTimeManager: DateTimeManager,
  private val analyticsEventSender: AnalyticsEventSender,
  private val uiBirthdayEditAdapter: UiBirthdayEditAdapter,
  private val intentDataReader: IntentDataReader,
  private val uiBirthdayDateFormatter: UiBirthdayDateFormatter,
  private val deleteBirthdayUseCase: DeleteBirthdayUseCase,
  private val saveBirthdayUseCase: SaveBirthdayUseCase,
  private val textProvider: TextProvider,
  private val tagRepository: TagRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val toggleTagAssignmentUseCase: ToggleTagAssignmentUseCase,
  private val tagChipStateAdapter: TagChipStateAdapter,
) : ViewModel() {

  private val _state = MutableStateFlow(EditBirthdayState())
  val state = _state.stateInWhileSubscribed(EditBirthdayState())
    .onStart { checkArguments() }

  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  init {
    _state.update {
      it.copy(
        id = key.id ?: UUID.randomUUID().toString(),
        hasId = key.id.isNullOrEmpty().not()
      )
    }
    observeTags()
  }

  private fun observeTags() {
    viewModelScope.launch(dispatcherProvider.default()) {
      tagRepository.observeAll()
        .map { tags -> tags.map { tagChipStateAdapter(it) } }
        .collect { tags ->
          _state.update { it.copy(allTags = tags) }
        }
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      tagAssignmentRepository.observeTagsForItem(_state.value.id, TaggedItemType.BIRTHDAY).collect { tags ->
        _state.update { it.copy(selectedTagIds = tags.map(Tag::id).toSet()) }
      }
    }
  }

  fun onTagToggle(tag: TagChipState) {
    val isSelected = tag.id in _state.value.selectedTagIds
    viewModelScope.launch(dispatcherProvider.io()) {
      toggleTagAssignmentUseCase(_state.value.id, TaggedItemType.BIRTHDAY, tag.id, isSelected)
    }
  }

  fun onManageTagsClick() {
    event.emit(ViewModelEvent.OpenManageTags)
  }

  fun onDateClicked() {
    event.emit(
      ViewModelEvent.OpenDatePicker(
        title = textProvider.getString(R.string.select_date),
        date = _state.value.selectedDate,
      )
    )
  }

  private fun checkArguments() {
    when {
      key.fromIntentData -> onIntent()
      key.prefillDateEpochDay != null -> onDateChanged(LocalDate.ofEpochDay(key.prefillDateEpochDay))
      key.id.isNullOrEmpty() -> onDateChanged(LocalDate.now())
      else -> load()
    }
  }

  fun onNameChanged(text: String) {
    _state.update { it.copy(name = text, nameError = false) }
  }

  fun onNumberChanged(text: String) {
    _state.update { it.copy(number = text) }
    if (text.isNotEmpty()) resolveContactInfo(text) else clearContactInfo()
  }

  fun onContactPicked(contactData: ContactData) {
    _state.update {
      it.copy(
        number = contactData.phone,
        name = it.name.ifBlank { contactData.name },
        nameError = false,
      )
    }
    resolveContactInfo(contactData.phone, knownName = contactData.name)
  }

  fun onDateChanged(localDate: LocalDate) {
    Logger.d(TAG, "Date changed: $localDate")
    _state.update {
      it.copy(
        dateText = uiBirthdayDateFormatter.getDateFormatted(localDate, !_state.value.ignoreYear),
        selectedDate = localDate,
      )
    }
  }

  fun onYearCheckChanged(ignoreYear: Boolean) {
    _state.update { it.copy(ignoreYear = ignoreYear) }
    onDateChanged(_state.value.selectedDate)
  }

  fun onSaveClick() {
    val name = _state.value.name.trim()
    if (name.isEmpty()) {
      _state.update { it.copy(nameError = true) }
      return
    }
    if (_state.value.isFromFile && _state.value.hasSameInDb) {
      _state.update { it.copy(dialog = EditBirthdayDialog.CopyConflict) }
      return
    }
    performSave(name, newId = false)
  }

  fun onCopyKeepClick() {
    dismissDialog()
    performSave(_state.value.name.trim(), newId = true)
  }

  fun onCopyReplaceClick() {
    dismissDialog()
    performSave(_state.value.name.trim(), newId = false)
  }

  fun onDeleteMenuClick() {
    _state.update { it.copy(dialog = EditBirthdayDialog.DeleteConfirm) }
  }

  fun onDeleteConfirmed() {
    dismissDialog()
    if (!_state.value.canDelete) return
    val id = _state.value.id
    Logger.i(TAG, "Deleting birthday, id: $id")
    viewModelScope.launch(dispatcherProvider.io()) {
      deleteBirthdayUseCase(id)

      withContext(dispatcherProvider.main()) {
        event.emit(ViewModelEvent.MoveBack)
      }
    }
  }

  fun onDialogDismiss() {
    dismissDialog()
  }

  private fun dismissDialog() {
    _state.update { it.copy(dialog = null) }
  }

  private fun load() {
    val id = key.id ?: run {
      Logger.w(TAG, "Id is null")
      return
    }

    viewModelScope.launch(dispatcherProvider.io()) {
      val birthday = birthdayRepository.getById(id) ?: return@launch

      Logger.i(TAG, "Birthday loaded from DB")
      onBirthdayLoaded(birthday)
    }
  }

  private fun onIntent() {
    viewModelScope.launch(dispatcherProvider.default()) {
      intentDataReader.get(IntentKeys.INTENT_ITEM, Birthday::class.java)?.run {
        Logger.i(TAG, "Birthday loaded from intent, id: $uuId")

        withContext(dispatcherProvider.main()) {
          _state.update {
            it.copy(
              isFromFile = true
            )
          }
        }
        onBirthdayLoaded(this)
        findSame(uuId)
      }
    }
  }

  private fun performSave(name: String, newId: Boolean) {
    val state = _state.value
    val number = state.number.takeIf { it.isNotEmpty() }
    viewModelScope.launch(dispatcherProvider.default()) {
      val contactId = contactsReader.getIdFromNumber(number)
      val formattedDate = dateTimeManager.formatBirthdayDate(state.selectedDate)
      val ignoreYear = state.ignoreYear
      val oldBirthday = birthdayRepository.getById(state.id)
      val birthday = oldBirthday?.copy(
          name = name,
          contactId = contactId,
          date = formattedDate,
          number = number ?: "",
          day = state.selectedDate.dayOfMonth,
          month = state.selectedDate.monthValue - 1,
          dayMonth = "${state.selectedDate.dayOfMonth}|${state.selectedDate.monthValue - 1}",
          uuId = oldBirthday.uuId.takeIf { !newId } ?: UUID.randomUUID().toString(),
          updatedAt = dateTimeManager.getNowGmtDateTime(),
          ignoreYear = ignoreYear,
        ) ?: Birthday(
          uuId = state.id,
          name = name,
          contactId = contactId,
          date = formattedDate,
          number = number ?: "",
          day = state.selectedDate.dayOfMonth,
          month = state.selectedDate.monthValue - 1,
          dayMonth = "${state.selectedDate.dayOfMonth}|${state.selectedDate.monthValue - 1}",
          updatedAt = dateTimeManager.getNowGmtDateTime(),
          ignoreYear = ignoreYear,
          syncState = SyncState.WaitingForUpload,
          version = 0,
        )
      analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_BIRTHDAY))
      Logger.i(TAG, "Saving the birthday with id: ${birthday.uuId}")
      saveBirthdayUseCase(birthday)

      withContext(dispatcherProvider.main()) {
        event.emit(ViewModelEvent.MoveBack)
      }
    }
  }

  private suspend fun onBirthdayLoaded(birthday: Birthday) {
    val uiBirthday = uiBirthdayEditAdapter.convert(birthday)
    if (uiBirthday.number.isNotEmpty()) resolveContactInfo(uiBirthday.number)

    withContext(dispatcherProvider.main()) {
      onDateChanged(
        dateTimeManager.parseBirthdayDate(birthday.date) ?: dateTimeManager.getCurrentDate()
      )
      _state.update {
        it.copy(
          id = birthday.uuId,
          name = uiBirthday.name,
          number = uiBirthday.number,
          ignoreYear = uiBirthday.isYearIgnored,
          canDelete = !_state.value.isFromFile,
        )
      }
    }
  }

  private suspend fun findSame(id: String) {
    withContext(dispatcherProvider.io()) {
      val birthday = birthdayRepository.getById(id)
      if (birthday != null) {
        _state.update { it.copy(hasSameInDb = true) }
      }
    }
  }

  private fun resolveContactInfo(
    number: String,
    knownName: String? = null,
  ) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val contactId = contactsReader.getIdFromNumber(number)
      if (contactId == 0L) {
        clearContactInfo()
        return@launch
      }
      val photo: Bitmap? = contactsReader.getPhotoBitmap(contactId)
      val name = knownName ?: contactsReader.getNameFromNumber(number)

      withContext(dispatcherProvider.main()) {
        _state.update { it.copy(contactName = name, contactPhoto = photo) }
      }
    }
  }

  private fun clearContactInfo() {
    _state.update { it.copy(contactName = null, contactPhoto = null) }
  }

  sealed interface ViewModelEvent {
    data object MoveBack : ViewModelEvent

    data class OpenDatePicker(
      val title: String,
      val date: LocalDate
    ) : ViewModelEvent

    data object OpenManageTags : ViewModelEvent
  }

  companion object {
    private const val TAG = "EditBirthdayViewModel"
  }
}
