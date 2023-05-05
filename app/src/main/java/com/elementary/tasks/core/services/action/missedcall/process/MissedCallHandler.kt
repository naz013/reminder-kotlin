package com.elementary.tasks.core.services.action.missedcall.process

import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.missed_calls.MissedCallDialogActivity

@Deprecated("After S")
class MissedCallHandler(
  private val contextProvider: ContextProvider
) : ActionHandler<MissedCall> {

  override fun handle(data: MissedCall) {
    val resultIntent =
      MissedCallDialogActivity.getLaunchIntent(contextProvider.context, data.number)
    contextProvider.context.startActivity(resultIntent)
  }
}
