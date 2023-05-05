package com.elementary.tasks.core.services.action.missedcall

import android.os.Build
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.data.repository.MissedCallRepository
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.services.action.WearNotification
import com.elementary.tasks.core.services.action.missedcall.cancel.MissedCallCancelHandler
import com.elementary.tasks.core.services.action.missedcall.cancel.MissedCallCancelHandlerQ
import com.elementary.tasks.core.services.action.missedcall.process.MissedCallHandler
import com.elementary.tasks.core.services.action.missedcall.process.MissedCallHandlerQ
import com.elementary.tasks.core.services.action.missedcall.process.MissedCallHandlerSilent
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.contacts.ContactsReader
import com.elementary.tasks.core.utils.params.Prefs

@Deprecated("After S")
class MissedCallHandlerFactory(
  private val missedCallDataProvider: MissedCallDataProvider,
  private val contextProvider: ContextProvider,
  private val textProvider: TextProvider,
  private val notifier: Notifier,
  private val prefs: Prefs,
  private val missedCallRepository: MissedCallRepository,
  private val wearNotification: WearNotification,
  private val contactsReader: ContactsReader
) {

  fun createAction(canPlaySound: Boolean): ActionHandler<MissedCall> {
    return if (canPlaySound) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MissedCallHandlerQ(
          missedCallDataProvider,
          contextProvider,
          textProvider,
          notifier,
          prefs,
          wearNotification,
          contactsReader
        )
      } else {
        MissedCallHandler(contextProvider)
      }
    } else {
      MissedCallHandlerSilent(
        missedCallDataProvider,
        contextProvider,
        textProvider,
        notifier,
        prefs,
        wearNotification,
        contactsReader
      )
    }
  }

  fun createCancel(): ActionHandler<MissedCall> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      MissedCallCancelHandlerQ(missedCallRepository)
    } else {
      MissedCallCancelHandler(missedCallRepository, contextProvider)
    }
  }
}
