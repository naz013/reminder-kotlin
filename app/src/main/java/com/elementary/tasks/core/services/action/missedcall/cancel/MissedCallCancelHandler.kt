package com.elementary.tasks.core.services.action.missedcall.cancel

import androidx.core.content.ContextCompat
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.data.repository.MissedCallRepository
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.services.EventOperationalService
import com.elementary.tasks.core.services.action.ActionHandler

@Deprecated("After S")
class MissedCallCancelHandler(
  private val missedCallRepository: MissedCallRepository,
  private val contextProvider: ContextProvider
) : ActionHandler<MissedCall> {

  override fun handle(data: MissedCall) {
    missedCallRepository.delete(data)
    ContextCompat.startForegroundService(
      contextProvider.context,
      EventOperationalService.getIntent(
        contextProvider.context,
        data.number,
        EventOperationalService.TYPE_MISSED,
        EventOperationalService.ACTION_STOP,
        data.uniqueId
      )
    )
  }
}
