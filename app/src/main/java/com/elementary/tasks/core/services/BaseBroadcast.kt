package com.elementary.tasks.core.services

import android.content.BroadcastReceiver
import com.elementary.tasks.core.utils.Prefs
import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class BaseBroadcast : BroadcastReceiver(), KoinComponent {
    protected val prefs: Prefs by inject()
}