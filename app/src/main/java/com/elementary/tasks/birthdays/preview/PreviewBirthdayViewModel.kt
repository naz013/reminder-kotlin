package com.elementary.tasks.birthdays.preview

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.usecase.DeleteBirthdayUseCase
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayPreviewAdapter
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayPreview
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.repository.BirthdayRepository
import kotlinx.coroutines.launch

class PreviewBirthdayViewModel(
  private val id: String,
  private val birthdayRepository: BirthdayRepository,
  dispatcherProvider: DispatcherProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  private val uiBirthdayPreviewAdapter: UiBirthdayPreviewAdapter,
  private val deleteBirthdayUseCase: DeleteBirthdayUseCase
) : BaseProgressViewModel(dispatcherProvider) {

  private val _birthday = mutableLiveDataOf<UiBirthdayPreview>()
  val birthday = _birthday.toLiveData()

  var canShowAnimation: Boolean = true

  fun hasId(): Boolean = id.isNotEmpty()

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    load()
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val birthday = birthdayRepository.getById(id) ?: return@launch

      analyticsEventSender.send(FeatureUsedEvent(Feature.BIRTHDAY_PREVIEW))
      _birthday.postValue(uiBirthdayPreviewAdapter.convert(birthday))
    }
  }

  fun deleteBirthday() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      deleteBirthdayUseCase(id)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
