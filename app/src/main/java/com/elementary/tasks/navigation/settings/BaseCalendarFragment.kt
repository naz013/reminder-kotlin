package com.elementary.tasks.navigation.settings

import androidx.databinding.ViewDataBinding
import com.elementary.tasks.core.utils.CalendarUtils
import org.koin.android.ext.android.inject

abstract class BaseCalendarFragment<B : ViewDataBinding> : BaseSettingsFragment<B>() {

  protected val calendarUtils by inject<CalendarUtils>()
}
