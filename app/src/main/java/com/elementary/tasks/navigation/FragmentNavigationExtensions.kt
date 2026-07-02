package com.elementary.tasks.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.findNavController
import com.github.naz013.logging.Logger

private const val TAG = "FragmentNavigationExtension"

fun Fragment.safeNavigation(
  resId: Int,
  args: Bundle? = null,
  navOptions: NavOptions? = NavigationAnimations.inDepthNavOptions(),
  navigatorExtras: Navigator.Extras? = null
) {
  try {
    findNavController().navigate(
      resId = resId,
      args = args,
      navOptions = navOptions,
      navigatorExtras = navigatorExtras,
    )
  } catch (e: Throwable) {
    Logger.e(TAG, "Navigation error, safeNavigation()", e)
  }
}

fun Fragment.safeNavigation(
  directions: NavDirections,
  navOptions: NavOptions? = NavigationAnimations.inDepthNavOptions(),
) {
  try {
    findNavController().navigate(
      directions = directions,
      navOptions = navOptions,
    )
  } catch (e: Throwable) {
    Logger.e(TAG, "Navigation error, safeNavigation()", e)
  }
}

@Deprecated("Use safeNavigation(directions: NavDirections, navOptions: NavOptions?) instead")
fun Fragment.safeNavigation(navDirections: NavDirections) {
  safeNavigation { navDirections }
}

@Deprecated("Use safeNavigation(directions: NavDirections, navOptions: NavOptions?) instead")
fun Fragment.safeNavigation(function: () -> NavDirections) {
  try {
    findNavController().navigate(function())
  } catch (e: Throwable) {
    Logger.e(TAG, "Navigation error, safeNavigation()", e)
  }
}

@Deprecated("Use safeNavigation(resId: Int, args: Bundle?, navOptions: NavOptions?) instead")
fun Fragment.navigate(block: NavController.() -> Unit) {
  try {
    findNavController().block()
  } catch (e: Throwable) {
    Logger.e(TAG, "Navigation error, navigate()", e)
  }
}

fun Fragment.onBackStackResume() {
  (context as? FragmentCallback)?.setCurrentFragment(this)
}
