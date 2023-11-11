package com.elementary.tasks.settings

import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import org.koin.android.ext.android.inject

abstract class BaseCalendarFragment<B : ViewBinding> : BaseSettingsFragment<B>() {
  protected val googleCalendarUtils by inject<GoogleCalendarUtils>()
}
