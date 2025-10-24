package com.elementary.tasks.home.eventsview

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.arch.BindingFragment
import com.elementary.tasks.navigation.fragments.FragmentNavigationController
import com.elementary.tasks.navigation.topfragment.FragmentMenuController
import com.github.naz013.analytics.AnalyticsEventSender
import org.koin.android.ext.android.inject

abstract class BaseSubEventsFragment<B : ViewBinding> : BindingFragment<B>() {

  protected var fragmentMenuController: FragmentMenuController? = null
  private var fragmentNavigationController: FragmentNavigationController? = null

  protected val analyticsEventSender by inject<AnalyticsEventSender>()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (fragmentMenuController == null) {
      runCatching {
        fragmentMenuController = parentFragment as? FragmentMenuController
      }
      runCatching {
        fragmentNavigationController = parentFragment as? FragmentNavigationController
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    fragmentMenuController = null
  }

  protected fun safeNavigation(function: () -> NavDirections) {
    fragmentNavigationController?.safeNavigation(function)
  }

  protected fun navigate(block: NavController.() -> Unit) {
    fragmentNavigationController?.navigate(block)
  }

  protected fun addMenu(
    menuRes: Int?,
    onMenuItemListener: (MenuItem) -> Boolean,
    menuModifier: ((Menu) -> Unit)? = null
  ) {
    fragmentMenuController?.addMenu(menuRes, onMenuItemListener, menuModifier)
  }

  protected fun removeMenu() {
    fragmentMenuController?.removeMenu()
  }
}
