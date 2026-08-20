package com.github.naz013.feature.birthday.preview

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.logic.birthday.DeleteBirthdayUseCase
import com.github.naz013.ui.birthday.UiBirthdayPreviewAdapter
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.ui.tag.TagChipStateAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class PreviewBirthdayViewModel(
  private val id: String,
  private val birthdayRepository: BirthdayRepository,
  private val dispatcherProvider: DispatcherProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  private val uiBirthdayPreviewAdapter: UiBirthdayPreviewAdapter,
  private val deleteBirthdayUseCase: DeleteBirthdayUseCase,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val tagChipStateAdapter: TagChipStateAdapter,
) : ViewModel() {

  private val _state = MutableStateFlow(PreviewBirthdayState())
  val state = _state.stateInWhileSubscribed(PreviewBirthdayState())
    .onStart { load() }

  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      tagAssignmentRepository.observeTagsForItem(id, TaggedItemType.BIRTHDAY)
        .map { tags -> tags.map { tagChipStateAdapter(it) } }
        .collect { tagChips ->
          _state.update { it.copy(tags = tagChips) }
        }
    }
  }

  fun onSmsClicked() {
    _state.value.birthday?.number?.also {
      event.emit(ViewModelEvent.SendSms(it))
    }
  }

  fun onCallClicked() {
    _state.value.birthday?.number?.also {
      event.emit(ViewModelEvent.MakeCall(it))
    }
  }

  fun onDeleteClick() {
    _state.update { it.copy(showDeleteConfirm = true) }
  }

  fun onDeleteDismiss() {
    _state.update { it.copy(showDeleteConfirm = false) }
  }

  fun onDeleteConfirmed() {
    _state.update { it.copy(showDeleteConfirm = false) }
    viewModelScope.launch(dispatcherProvider.io()) {
      deleteBirthdayUseCase(id)

      withContext(dispatcherProvider.main()) {
        event.emit(ViewModelEvent.MoveBack)
      }
    }
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.io()) {
      val birthday = birthdayRepository.getById(id) ?: return@launch

      analyticsEventSender.send(FeatureUsedEvent(Feature.BIRTHDAY_PREVIEW))

      val uiBirthday = uiBirthdayPreviewAdapter.convert(birthday)
      val shouldPlayConfetti = uiBirthday.hasBirthdayToday && _state.value.canShowAnimation
      _state.update {
        it.copy(
          birthday = uiBirthday,
          playConfetti = shouldPlayConfetti,
          canShowAnimation = false,
        )
      }
    }
  }

  sealed interface ViewModelEvent {
    data object MoveBack : ViewModelEvent

    data class SendSms(val number: String) : ViewModelEvent

    data class MakeCall(val number: String) : ViewModelEvent
  }
}
