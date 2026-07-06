package com.elementary.tasks.places.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.SideEffect
import androidx.fragment.app.Fragment
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.elementary.tasks.navigation.BackPressHandler
import com.elementary.tasks.navigation.onBackStackResume
import com.elementary.tasks.places.PlacesNavGraph
import com.elementary.tasks.places.PlacesNavKey
import com.elementary.tasks.simplemap.SimpleMapFragment
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.compose.composeView
import org.koin.android.ext.android.inject

/**
 * Hosts the Places feature as a self-contained Navigation 3 "island": this Fragment owns the
 * internal backstack and the Android-framework glue (dialogs, the embedded classic map Fragment
 * used for picking a location) that only a Fragment can provide, and is otherwise responsible only
 * for screen-to-screen navigation. The actual screens and their wiring live in [PlacesNavGraph].
 */
class PlacesFragment :
  Fragment(),
  BackPressHandler {
  internal val dialogues by inject<Dialogues>()

  /** Bridge from the Fragment's plain methods into the Compose-owned Nav3 backstack — the
   *  backstack itself can only be created via [rememberNavBackStack] inside composition. */
  private var currentBackStack: MutableList<NavKey>? = null

  /** The embedded map Fragment on the currently displayed Edit entry, if any — used so the
   *  visible top-bar back button can collapse an open map layer (style/radius/recent places)
   *  before leaving the screen, mirroring the legacy `EditPlaceFragment.canGoBack()` behavior. */
  internal var activeGoogleMap: SimpleMapFragment? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View =
    composeView {
      val backStack = rememberNavBackStack(*initialBackStackKeys().toTypedArray())
      SideEffect { currentBackStack = backStack }
      PlacesNavGraph(backStack)
    }

  override fun onDestroyView() {
    super.onDestroyView()
    currentBackStack = null
  }

  /** Registers this fragment as the Activity's "current fragment" for hardware/gesture back-press
   *  routing (see [com.elementary.tasks.home.BottomNavActivity.handleBackPress]) — without this,
   *  that tracking stays stuck on whichever screen was last resumed before this island, which can
   *  route a back press to the wrong handler entirely. */
  override fun onResume() {
    super.onResume()
    onBackStackResume()
  }

  /** Seeds the island's backstack from a place deep link / global search result, read once from
   *  [getArguments] — see [com.elementary.tasks.home.ScreenDestinationIdResolver] and
   *  [com.elementary.tasks.home.BottomNavActivity], which both route place deep links to this
   *  fragment (`R.id.placesFragment`) instead of a dedicated `editPlaceFragment` destination. */
  private fun initialBackStackKeys(): List<PlacesNavKey> {
    val args = arguments ?: return listOf(PlacesNavKey.List)
    val id = args.getString(IntentKeys.INTENT_ID)
    return when {
      args.getBoolean(ARG_OPEN_EDIT, false) -> listOf(PlacesNavKey.List, PlacesNavKey.Edit(id ?: ""))
      id != null -> listOf(PlacesNavKey.List, PlacesNavKey.Edit(id))
      else -> listOf(PlacesNavKey.List)
    }
  }

  override fun canGoBack(): Boolean = (currentBackStack?.size ?: 1) <= 1

  companion object {
    const val ARG_OPEN_EDIT = "places_open_edit"
  }
}
