package com.elementary.tasks.navigation.fragments

import com.elementary.tasks.core.arch.BaseFragment
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.navigation.BackPressHandler
import com.elementary.tasks.navigation.DefaultBackPressHandler
import com.elementary.tasks.navigation.onBackStackResume
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.ui.common.activity.LightThemedActivity
import com.github.naz013.ui.common.theme.ThemeProvider
import org.koin.android.ext.android.inject

/**
 * Content-agnostic navigation behavior shared by every in-depth navigation fragment, regardless
 * of whether its content is produced from a [androidx.viewbinding.ViewBinding] or Jetpack Compose.
 */
abstract class NavigationFragment :
  BaseFragment(),
  BackPressHandler by DefaultBackPressHandler() {
  protected val prefs by inject<Prefs>()
  protected val themeProvider by inject<ThemeProvider>()
  protected val isDark: Boolean
    get() {
      return themeProvider.isDark
    }
  protected val analyticsEventSender by inject<AnalyticsEventSender>()

  protected fun moveBack() {
    val activity = activity
    if (activity is LightThemedActivity) {
      activity.invokeBackPress()
    } else {
      activity?.onBackPressedDispatcher?.onBackPressed()
    }
  }

  override fun onResume() {
    super.onResume()
    onBackStackResumed()
  }

  open fun onBackStackResumed() {
    onBackStackResume()
  }
}
