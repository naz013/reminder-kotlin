package com.elementary.tasks.navigation.settings

import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.utils.CalendarUtils
import org.koin.android.ext.android.inject

abstract class BaseCalendarFragment<B : ViewBinding> : BaseSettingsFragment<B>() {
  protected val calendarUtils by inject<CalendarUtils>()
}
