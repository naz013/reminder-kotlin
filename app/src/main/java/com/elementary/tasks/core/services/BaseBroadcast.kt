package com.elementary.tasks.core.services

import android.content.BroadcastReceiver
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BaseBroadcast : BroadcastReceiver(), KoinComponent {
  protected val prefs by inject<Prefs>()
  protected val notifier by inject<Notifier>()
  protected val updatesHelper by inject<UpdatesHelper>()
}
