package com.elementary.tasks.navigation.nav3

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.safeNavigation

/**
 * Transitional bridge between the outer Nav3 backstack (hosting screens promoted out of
 * `home_nav.xml`) and the legacy Navigation Component graph still embedded via
 * [LegacyHomeHostEntry] for screens not yet promoted.
 *
 * - A still-legacy Fragment pushes onto the outer backstack via [navigate] instead of
 *   `safeNavigation(resId, Bundle)` when the target has already been promoted.
 * - A promoted Nav3 entry reaches a still-legacy destination via [navigateLegacy] instead of
 *   `findNavController().navigate(resId, Bundle)`.
 *
 * [navigateLegacy] has to account for [LegacyHomeHostEntry] not currently being the visible
 * outer-backstack entry: its [NavHostFragment][androidx.navigation.fragment.NavHostFragment] gets
 * `detach()`-ed whenever a promoted screen is on top (see [LegacyHomeHostEntry]'s `onRelease`), so
 * navigating on a stale, detached [NavController] silently fails (the exception is swallowed by
 * [safeNavigation]'s try/catch). When that's the case, [navigateLegacy] queues the request and
 * brings [LegacyHomeNavKey] back to the top of the outer backstack first; [attachLegacyNavController]
 * drains the queued request once the Fragment is reattached and its fresh [NavController] is ready.
 *
 * Both ends are attached once (by [AppNavGraph] and [LegacyHomeHostEntry] respectively) and
 * detached when their Compose entry leaves composition. This bridge shrinks - and eventually goes
 * away entirely - as more screens are promoted out of `home_nav.xml`.
 */
class AppNavBridge {
  private var outerBackStack: MutableList<NavKey>? = null
  private var legacyNavController: NavController? = null
  private var pendingLegacyNavigation: (() -> Unit)? = null

  fun attachOuterBackStack(backStack: MutableList<NavKey>) {
    outerBackStack = backStack
  }

  fun detachOuterBackStack(backStack: MutableList<NavKey>) {
    if (outerBackStack === backStack) outerBackStack = null
  }

  fun attachLegacyNavController(navController: NavController) {
    legacyNavController = navController
    pendingLegacyNavigation?.let { pending ->
      pendingLegacyNavigation = null
      pending()
    }
  }

  fun navigate(vararg keys: NavKey) {
    val stack = outerBackStack ?: return
    keys.forEach { stack.add(it) }
  }

  fun navigateLegacy(
    resId: Int,
    args: Bundle? = null,
    navOptions: NavOptions? = NavigationAnimations.inDepthNavOptions(),
  ) {
    val stack = outerBackStack
    if (stack != null && stack.lastOrNull() != LegacyHomeNavKey) {
      // Replace the legacy graph's start destination rather than pushing on top of it - see
      // replacingHomeNavOptions() kdoc.
      pendingLegacyNavigation = {
        val controller = legacyNavController
        val options =
          controller?.graph?.startDestinationId?.let { NavigationAnimations.replacingHomeNavOptions(it) } ?: navOptions
        controller?.safeNavigation(resId, args, options)
      }
      // At most one LegacyHomeNavKey may be on the stack at a time - move it to the top rather
      // than pushing a second instance (it's a singleton retained Fragment, not a fresh screen).
      stack.remove(LegacyHomeNavKey)
      stack.add(LegacyHomeNavKey)
    } else {
      legacyNavController?.safeNavigation(resId, args, navOptions)
    }
  }
}
