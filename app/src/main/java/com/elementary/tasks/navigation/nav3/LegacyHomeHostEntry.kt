package com.elementary.tasks.navigation.nav3

import android.view.View
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.elementary.tasks.R
import org.koin.compose.koinInject

private const val LEGACY_NAV_HOST_TAG = "legacy_home_nav_host"

/**
 * Generated once per process, not via `rememberSaveable` - the retained [NavHostFragment] (kept
 * alive across full unmount/remount cycles of [LegacyHomeHostEntry] by [LEGACY_NAV_HOST_TAG], per
 * this file's kdoc) remembers the container id it was first attached to internally. Now that
 * `LegacyHomeNavKey` is only added to the outer backstack on demand (see [AppNavGraph]) instead of
 * being permanently seeded, this composable's own `rememberSaveable` state gets cleared every time
 * the key leaves the backstack - a fresh [View.generateViewId] on the next bridge hop wouldn't
 * match the id the retained fragment still expects, crashing `attach()` with
 * "No view found for id ... for fragment NavHostFragment". A file-scoped constant sidesteps that
 * entirely by never changing for the lifetime of the process, matching the fragment's own actual
 * retention scope.
 */
private val LEGACY_NAV_HOST_CONTAINER_ID = View.generateViewId()

/**
 * Hosts the legacy `home_nav.xml` [NavHostFragment] inside a single Nav3 entry, the same way
 * [com.elementary.tasks.reminder.build.valuedialog.editor.MapValueEditor] embeds
 * `SimpleMapFragment`: an `AndroidView`-wrapped [FragmentContainerView], with the fragment
 * transaction run from the `update` block since that's the point the container is guaranteed to
 * already be attached.
 *
 * Once other screens are promoted onto the outer backstack (see [AppNavBridge]), this entry can be
 * scrolled off-screen and back (e.g. Home -> promoted Notes island -> back to Home) without fully
 * tearing down the legacy graph's state: `update`/`onRelease` use [android.app.FragmentManager]
 * `attach`/`detach` (not `remove`) against a *stable* tag/container id, so the same
 * [NavHostFragment] instance - and everything nested inside it - survives being temporarily
 * removed from composition instead of being recreated from scratch every time.
 *
 * [onNavHostReady] fires once the [NavHostFragment] is (re)attached and its [NavController] is
 * available - the host Activity can't read it synchronously in `onCreate()` anymore now that
 * attachment happens during Compose composition instead of during XML inflation.
 *
 * [AppNavBridge.navigateLegacy] can bring this entry back to the top of the outer backstack from a
 * promoted screen (e.g. Notes -> Settings), using `replacingHomeNavOptions()` to *replace*
 * `home_nav.xml`'s start destination rather than push on top of it. `onRelease` detects that case
 * specifically - the start destination is no longer anywhere in the internal back stack - and
 * resets to it before detaching, so a *later* reattachment starts fresh instead of resurrecting
 * whatever a bridge hop last navigated to. A normal internal navigation depth (e.g.
 * Settings -> Cloud Backup, reached without the bridge) always keeps the start destination in the
 * stack and is left untouched, so it's still there on the next reattachment.
 */
@Composable
internal fun LegacyHomeHostEntry(onNavHostReady: (NavController) -> Unit) {
  val activity = LocalActivity.current as FragmentActivity
  val appNavBridge = koinInject<AppNavBridge>()
  val containerId = LEGACY_NAV_HOST_CONTAINER_ID

  AndroidView(
    modifier = Modifier.fillMaxSize(),
    factory = { context -> FragmentContainerView(context).apply { id = containerId } },
    update = {
      val fragmentManager = activity.supportFragmentManager
      val existing = fragmentManager.findFragmentByTag(LEGACY_NAV_HOST_TAG) as? NavHostFragment
      val navHostFragment =
        existing?.also {
          fragmentManager
            .beginTransaction()
            .attach(it)
            .setPrimaryNavigationFragment(it)
            .commitNowAllowingStateLoss()
        } ?: NavHostFragment.create(R.navigation.home_nav).also {
          fragmentManager
            .beginTransaction()
            .add(containerId, it, LEGACY_NAV_HOST_TAG)
            .setPrimaryNavigationFragment(it)
            .commitNow()
        }
      appNavBridge.attachLegacyNavController(navHostFragment.navController)
      onNavHostReady(navHostFragment.navController)
    },
    onRelease = {
      val fragmentManager = activity.supportFragmentManager
      if (fragmentManager.isDestroyed) return@AndroidView
      val existing = fragmentManager.findFragmentByTag(LEGACY_NAV_HOST_TAG) as? NavHostFragment ?: return@AndroidView
      // Only reset when the graph's start destination has been replaced out of the stack entirely
      // (a bridge hop's replacingHomeNavOptions()) - a normal internal navigation depth (e.g.
      // Settings -> Cloud Backup) always keeps it in the stack and must be left untouched so it's
      // still there next time.
      val navController = existing.navController
      val startDestinationId = navController.graph.startDestinationId
      val startStillInStack = navController.currentBackStack.value.any { it.destination.id == startDestinationId }
      if (!startStillInStack) {
        navController.navigate(
          startDestinationId,
          null,
          NavOptions.Builder().setPopUpTo(navController.graph.id, true).build(),
        )
      }
      fragmentManager.beginTransaction().detach(existing).commitNowAllowingStateLoss()
    },
  )
}
