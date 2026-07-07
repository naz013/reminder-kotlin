package com.elementary.tasks.birthdays.preview

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.usecase.DeleteBirthdayUseCase
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayPreviewAdapter
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.BirthdayRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PreviewBirthdayViewModel(
  private val id: String,
  private val birthdayRepository: BirthdayRepository,
  dispatcherProvider: DispatcherProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  private val uiBirthdayPreviewAdapter: UiBirthdayPreviewAdapter,
  private val deleteBirthdayUseCase: DeleteBirthdayUseCase,
) : BaseProgressViewModel(dispatcherProvider) {
  val state: StateFlow<PreviewBirthdayState> field = MutableStateFlow(PreviewBirthdayState())

  /** Confetti should only auto-play once per time this screen is shown, even though [onResume]
   *  reloads the birthday every time the fragment resumes. */
  private var canShowAnimation = true

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    load()
  }

  fun onDeleteClick() {
    state.update { it.copy(showDeleteConfirm = true) }
  }

  fun onDeleteDismiss() {
    state.update { it.copy(showDeleteConfirm = false) }
  }

  fun onDeleteConfirmed() {
    state.update { it.copy(showDeleteConfirm = false) }
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      deleteBirthdayUseCase(id)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val birthday = birthdayRepository.getById(id) ?: return@launch
      analyticsEventSender.send(FeatureUsedEvent(Feature.BIRTHDAY_PREVIEW))
      val uiBirthday = uiBirthdayPreviewAdapter.convert(birthday)
      val shouldPlayConfetti = uiBirthday.hasBirthdayToday && canShowAnimation
      if (shouldPlayConfetti) canShowAnimation = false
      state.update { it.copy(birthday = uiBirthday, playConfetti = shouldPlayConfetti) }
    }
  }
}
