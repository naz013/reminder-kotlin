package com.elementary.tasks.navigation.fragments

import android.content.Context
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.arch.BindingFragment
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.navigation.FragmentCallback
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.activity.LightThemedActivity
import org.koin.android.ext.android.inject

abstract class BaseNavigationFragment<B : ViewBinding> :
  BindingFragment<B>(),
  FragmentNavigationController {

  var callback: FragmentCallback? = null
    private set

  protected val currentStateHolder by inject<CurrentStateHolder>()
  protected val prefs = currentStateHolder.preferences
  protected val isDark: Boolean
    get() {
      return currentStateHolder.theme.isDark
    }
  protected val analyticsEventSender by inject<AnalyticsEventSender>()
  protected val themeProvider = currentStateHolder.theme

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (callback == null) {
      runCatching { callback = context as FragmentCallback? }
    }
    callback?.onCreateFragment(this)
  }

  protected fun moveBack() {
    val activity = activity
    if (activity is LightThemedActivity) {
      activity.invokeBackPress()
    } else {
      activity?.onBackPressedDispatcher?.onBackPressed()
    }
  }

  open fun canGoBack(): Boolean = true

  open fun onBackStackResume() {
    callback?.setCurrentFragment(this)
  }

  override fun onResume() {
    super.onResume()
    onBackStackResume()
  }

  protected fun safeNavigation(navDirections: NavDirections) {
    safeNavigation { navDirections }
  }

  override fun safeNavigation(function: () -> NavDirections) {
    try {
      findNavController().navigate(function())
    } catch (e: Throwable) {
      Logger.e("BaseNavigationFragment", "Navigation error, safeNavigation()", e)
    }
  }

  override fun navigate(block: NavController.() -> Unit) {
    try {
      findNavController().block()
    } catch (e: Throwable) {
      Logger.e("BaseNavigationFragment", "Navigation error, navigate()", e)
    }
  }
}
