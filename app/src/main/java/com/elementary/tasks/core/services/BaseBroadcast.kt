package com.elementary.tasks.core.services

import android.content.BroadcastReceiver
import com.elementary.tasks.core.utils.Prefs
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BaseBroadcast : BroadcastReceiver(), KoinComponent {
  protected val prefs by inject<Prefs>()
}