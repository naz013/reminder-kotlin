package com.elementary.tasks.core.view_models.missed_calls

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.dao.MissedCallsDao
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.view_models.BaseProgressViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import kotlinx.coroutines.launch

class MissedCallViewModel(
  number: String,
  dispatcherProvider: DispatcherProvider,
  private val jobScheduler: JobScheduler,
  private val missedCallsDao: MissedCallsDao
) : BaseProgressViewModel(dispatcherProvider) {

  val missedCall = missedCallsDao.loadByNumber(number)

  fun deleteMissedCall(missedCall: MissedCall) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      missedCallsDao.delete(missedCall)
      jobScheduler.cancelMissedCall(missedCall.number)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
