package com.elementary.tasks.navigation.fragments

import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.arch.BindingFragment
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.navigation.BackPressHandler
import com.elementary.tasks.navigation.DefaultBackPressHandler
import com.elementary.tasks.navigation.onBackStackResume
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.ui.common.activity.LightThemedActivity
import com.github.naz013.ui.common.theme.ThemeProvider
import org.koin.android.ext.android.inject

/**
 * Base fragment for all navigation-enabled fragments in the app.
 *
 * This class provides core navigation functionality with safe navigation methods
 * that handle errors gracefully. Fragments extending this class should use the
 * navigation graph animations defined in XML or programmatically set NavOptions.
 *
 * Navigation animations are configured based on fragment hierarchy:
 * - Top-level destinations (extending [com.elementary.tasks.navigation.topfragment.BaseTopFragment])
 *   use fade animations for lateral navigation
 * - In-depth navigation uses slide-from-right animations for hierarchical navigation
 * - Modal screens use slide-from-bottom animations
 *
 * @param B The ViewBinding type for this fragment
 */
abstract class BaseNavigationFragment<B : ViewBinding> :
  BindingFragment<B>(),
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
