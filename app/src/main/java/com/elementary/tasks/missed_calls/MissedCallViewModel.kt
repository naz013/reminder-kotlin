package com.elementary.tasks.missed_calls

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.adapter.missedcall.UiMissedCallShowAdapter
import com.elementary.tasks.core.data.dao.MissedCallsDao
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.data.ui.missedcall.UiMissedCallShow
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.view_models.BaseProgressViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import kotlinx.coroutines.launch

class MissedCallViewModel(
  private val number: String,
  dispatcherProvider: DispatcherProvider,
  private val jobScheduler: JobScheduler,
  private val missedCallsDao: MissedCallsDao,
  private val uiMissedCallShowAdapter: UiMissedCallShowAdapter
) : BaseProgressViewModel(dispatcherProvider) {

  private val _missedCall = mutableLiveDataOf<UiMissedCallShow>()
  val missedCall = _missedCall.toLiveData()

  var isEventShowed = false

  init {
    load()
  }

  fun getNumber(): String? {
    return missedCall.value?.number
  }

  fun deleteMissedCall() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      jobScheduler.cancelMissedCall(number)
      val missedCall = missedCallsDao.getByNumber(number)
      if (missedCall == null) {
        postInProgress(false)
        postCommand(Commands.DELETED)
        return@launch
      }
      missedCallsDao.delete(missedCall)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  fun loadTest(missedCall: MissedCall?) {
    if (missedCall != null) {
      viewModelScope.launch(dispatcherProvider.default()) {
        onLoaded(uiMissedCallShowAdapter.convert(missedCall))
      }
    }
  }

  private fun onLoaded(missedCall: UiMissedCallShow) {
    _missedCall.postValue(missedCall)
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val missedCall = missedCallsDao.getByNumber(number) ?: return@launch
      onLoaded(uiMissedCallShowAdapter.convert(missedCall))
    }
  }
}
