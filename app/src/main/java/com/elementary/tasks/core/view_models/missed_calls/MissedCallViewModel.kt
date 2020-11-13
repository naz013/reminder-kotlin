package com.elementary.tasks.core.view_models.missed_calls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.services.EventJobScheduler
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import kotlinx.coroutines.runBlocking

class MissedCallViewModel private constructor(number: String) : BaseDbViewModel() {

  val missedCall = appDb.missedCallsDao().loadByNumber(number)

  fun deleteMissedCall(missedCall: MissedCall) {
    postInProgress(true)
    launchDefault {
      runBlocking {
        appDb.missedCallsDao().delete(missedCall)
        EventJobScheduler.cancelMissedCall(missedCall.number)
      }
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  class Factory(private val number: String) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return MissedCallViewModel(number) as T
    }
  }
}
