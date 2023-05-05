package com.elementary.tasks.core.services.action.missedcall.cancel

import android.os.Build
import androidx.annotation.RequiresApi
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.data.repository.MissedCallRepository
import com.elementary.tasks.core.services.action.ActionHandler

@Deprecated("After S")
@RequiresApi(Build.VERSION_CODES.Q)
class MissedCallCancelHandlerQ(
  private val missedCallRepository: MissedCallRepository
) : ActionHandler<MissedCall> {

  override fun handle(data: MissedCall) {
    missedCallRepository.delete(data)
  }
}
