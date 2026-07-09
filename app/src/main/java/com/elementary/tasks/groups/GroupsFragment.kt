package com.elementary.tasks.groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.SideEffect
import androidx.fragment.app.Fragment
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.elementary.tasks.navigation.BackPressHandler
import com.elementary.tasks.navigation.topfragment.RootFragment
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.compose.composeView
import org.koin.android.ext.android.inject

/**
 * Hosts the Groups feature (List + Edit) as a self-contained Navigation 3 "island": this Fragment
 * owns the internal backstack and the Android-framework glue (confirmation dialogs) that only a
 * Fragment can provide, and is otherwise responsible only for screen-to-screen navigation. The
 * actual screens and their wiring live in [GroupsNavGraph].
 */
class GroupsFragment :
  Fragment(),
  RootFragment,
  BackPressHandler {
  internal val dialogues by inject<Dialogues>()

  /** Bridge from the Fragment's plain methods into the Compose-owned Nav3 backstack — the
   *  backstack itself can only be created via [rememberNavBackStack] inside composition. */
  private var currentBackStack: MutableList<NavKey>? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View =
    composeView {
      val backStack = rememberNavBackStack(*initialBackStackKeys().toTypedArray())
      SideEffect { currentBackStack = backStack }
      GroupsNavGraph(backStack)
    }

  override fun onDestroyView() {
    super.onDestroyView()
    currentBackStack = null
  }

  /** Seeds the island's backstack from a group deep link / cross-feature link, read once from
   *  [getArguments] — see [com.elementary.tasks.home.ScreenDestinationIdResolver] and
   *  [com.elementary.tasks.home.HomeFragment]/[com.elementary.tasks.home.eventsview.HomeEventsFragment],
   *  which route to this fragment (`R.id.groupsFragment`). The `List` entry always stays at the
   *  bottom of the stack so Edit's back arrow can simply pop, unlike the Birthdays island. */
  private fun initialBackStackKeys(): List<GroupsNavKey> {
    val args = arguments ?: return listOf(GroupsNavKey.List)
    val id = args.getString(IntentKeys.INTENT_ID)
    return when {
      id != null -> listOf(GroupsNavKey.List, GroupsNavKey.Edit(id))
      args.getBoolean(IntentKeys.INTENT_ITEM, false) -> listOf(GroupsNavKey.List, GroupsNavKey.Edit())
      else -> listOf(GroupsNavKey.List)
    }
  }

  override fun canGoBack(): Boolean = (currentBackStack?.size ?: 1) <= 1
}
