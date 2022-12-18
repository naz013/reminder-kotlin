package com.elementary.tasks.settings

import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import org.koin.android.ext.android.inject

abstract class BaseCalendarFragment<B : ViewBinding> : BaseSettingsFragment<B>() {
  protected val googleCalendarUtils by inject<GoogleCalendarUtils>()
}
