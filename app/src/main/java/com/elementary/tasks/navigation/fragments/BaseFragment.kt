package com.elementary.tasks.navigation.fragments

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.analytics.AnalyticsEventSender
import com.elementary.tasks.core.arch.BindingFragment
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.arch.ThemedActivity
import com.elementary.tasks.navigation.FragmentCallback
import org.koin.android.ext.android.inject

abstract class BaseFragment<B : ViewBinding> : BindingFragment<B>() {

  var callback: FragmentCallback? = null
    private set

  protected val currentStateHolder by inject<CurrentStateHolder>()
  protected val prefs = currentStateHolder.preferences
  protected val isDark = currentStateHolder.theme.isDark
  protected val analyticsEventSender by inject<AnalyticsEventSender>()
  protected val themeProvider = currentStateHolder.theme

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (callback == null) {
      runCatching { callback = context as FragmentCallback? }
    }
  }

  protected fun moveBack() {
    val activity = activity
    if (activity is ThemedActivity) {
      activity.invokeBackPress()
    } else {
      activity?.onBackPressedDispatcher?.onBackPressed()
    }
  }

  open fun canGoBack(): Boolean = true

  open fun onBackStackResume() {
    callback?.setCurrentFragment(this)
    callback?.onTitleChange(getTitle())
  }

  override fun onResume() {
    super.onResume()
    onBackStackResume()
  }

  abstract fun getTitle(): String

  protected fun navigate(function: () -> NavDirections) {
    safeNavigation(function.invoke())
  }

  protected fun safeNavigation(navDirections: NavDirections) {
    safeNavigation { findNavController().navigate(navDirections) }
  }

  protected fun safeNavigation(function: () -> Unit) {
    try {
      function.invoke()
    } catch (e: Throwable) {
      e.printStackTrace()
    }
  }

  protected fun addMenu(
    menuRes: Int?,
    onMenuItemListener: (MenuItem) -> Boolean,
    menuModifier: ((Menu) -> Unit)? = null
  ) {
    val menuHost: MenuHost = requireActivity()
    menuHost.addMenuProvider(
      object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
          menuRes?.also { menuInflater.inflate(it, menu) }
          menuModifier?.invoke(menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
          return onMenuItemListener(menuItem)
        }
      },
      viewLifecycleOwner,
      Lifecycle.State.RESUMED
    )
  }
}
