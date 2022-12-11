package com.elementary.tasks.core.view_models.missed_calls

import com.elementary.tasks.core.data.dao.MissedCallsDao
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider

class MissedCallViewModel(
  number: String,
  prefs: Prefs,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  private val jobScheduler: JobScheduler,
  private val missedCallsDao: MissedCallsDao
) : BaseDbViewModel(prefs, dispatcherProvider, workManagerProvider) {

  val missedCall = missedCallsDao.loadByNumber(number)

  fun deleteMissedCall(missedCall: MissedCall) {
    postInProgress(true)
    launchDefault {
      missedCallsDao.delete(missedCall)
      jobScheduler.cancelMissedCall(missedCall.number)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
