package com.elementary.tasks.reminder.preview

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toSingleEvent
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.usecase.reminders.GetReminderByIdUseCase
import kotlinx.coroutines.launch

class FullScreenMapViewModel(
  arguments: Bundle?,
  dispatcherProvider: DispatcherProvider,
  private val getReminderByIdUseCase: GetReminderByIdUseCase
) : BaseProgressViewModel(dispatcherProvider) {

  val id = arguments?.getString(IntentKeys.INTENT_ID) ?: ""

  private val _reminder = mutableLiveDataOf<Reminder>()
  val reminder = _reminder.toSingleEvent()
  var placeIndex = 0

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    viewModelScope.launch(dispatcherProvider.default()) {
      getReminderByIdUseCase(id)?.also {
        _reminder.postValue(it)
      }
    }
  }
}
