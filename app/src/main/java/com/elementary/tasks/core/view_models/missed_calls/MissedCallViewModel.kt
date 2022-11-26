package com.elementary.tasks.core.view_models.missed_calls

import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.services.EventJobScheduler
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider

class MissedCallViewModel(
  number: String,
  appDb: AppDb,
  prefs: Prefs,
  dispatcherProvider: DispatcherProvider
) : BaseDbViewModel(appDb, prefs, dispatcherProvider) {

  val missedCall = appDb.missedCallsDao().loadByNumber(number)

  fun deleteMissedCall(missedCall: MissedCall) {
    postInProgress(true)
    launchDefault {
      appDb.missedCallsDao().delete(missedCall)
      EventJobScheduler.cancelMissedCall(missedCall.number)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
