package com.elementary.tasks.navigation.nav3

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation.NavController
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.elementary.tasks.birthdays.birthdaysEntries
import com.elementary.tasks.calendar.monthview.calendarEntries
import com.elementary.tasks.googletasks.googleTasksEntries
import com.elementary.tasks.groups.groupsEntries
import com.elementary.tasks.home.HomeNavKey
import com.elementary.tasks.home.homeEntries
import com.elementary.tasks.notes.notesEntries
import com.elementary.tasks.places.placesEntries
import com.elementary.tasks.reminder.build.buildReminderEntries
import com.elementary.tasks.reminder.lists.removed.remindersArchiveEntries
import com.elementary.tasks.reminder.preview.reminderPreviewEntries
import org.koin.compose.koinInject

/**
 * Root of the app's single Nav3 graph, hosted directly by
 * [BottomNavActivity][com.elementary.tasks.home.BottomNavActivity] - replaces the
 * `NavHostFragment`/`home_nav.xml` Navigation Component graph that used to be the Activity's
 * entire content view. [HomeNavKey.Main] is the graph's own start destination.
 *
 * [LegacyHomeNavKey] embeds what's *left* of the legacy Fragment graph (now just the Settings tree
 * and a handful of other screens) as one entry (see [LegacyHomeHostEntry]), reached on demand via
 * [AppNavBridge.navigateLegacy] rather than being seeded into the backstack up front. Screens
 * promoted out of it register their own entries here via a
 * `fun EntryProviderScope<NavKey>.xyzEntries(backStack)` extension in that feature's own
 * `XyzNavGraph.kt`, mirroring what `notesEntries`/`groupsEntries` do today. [AppNavBridge] carries
 * navigation requests across the promoted/legacy boundary in both directions while it still exists.
 */
@Composable
fun AppNavGraph(
  initialKeys: List<NavKey> = emptyList(),
  onLegacyNavHostReady: (NavController) -> Unit,
) {
  val backStack = rememberNavBackStack(HomeNavKey.Main, *initialKeys.toTypedArray())
  val appNavBridge = koinInject<AppNavBridge>()

  DisposableEffect(backStack) {
    appNavBridge.attachOuterBackStack(backStack)
    onDispose { appNavBridge.detachOuterBackStack(backStack) }
  }

  NavDisplay(
    backStack = backStack,
    modifier = Modifier.fillMaxSize(),
    onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
    entryDecorators =
      listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
      ),
    transitionSpec = {
      (
        fadeIn(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleIn(animationSpec = navScreenSpring(), initialScale = NAV_ANIM_ENTER_SCALE)
      ) togetherWith (
        fadeOut(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleOut(animationSpec = navScreenSpring(), targetScale = NAV_ANIM_EXIT_SCALE)
      )
    },
    popTransitionSpec = {
      (
        fadeIn(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleIn(animationSpec = navScreenSpring(), initialScale = NAV_ANIM_EXIT_SCALE)
      ) togetherWith (
        fadeOut(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleOut(animationSpec = navScreenSpring(), targetScale = NAV_ANIM_ENTER_SCALE)
      )
    },
    predictivePopTransitionSpec = {
      (
        fadeIn(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleIn(animationSpec = navScreenSpring(), initialScale = NAV_ANIM_EXIT_SCALE)
      ) togetherWith (
        fadeOut(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleOut(animationSpec = navScreenSpring(), targetScale = NAV_ANIM_ENTER_SCALE)
      )
    },
    entryProvider =
      entryProvider {
        entry<LegacyHomeNavKey> { LegacyHomeHostEntry(onNavHostReady = onLegacyNavHostReady) }
        homeEntries(backStack)
        notesEntries(backStack)
        groupsEntries(backStack)
        placesEntries(backStack)
        birthdaysEntries(backStack)
        googleTasksEntries(backStack)
        buildReminderEntries(backStack)
        calendarEntries(backStack)
        reminderPreviewEntries(backStack)
        remindersArchiveEntries(backStack)
      },
  )
}

private fun navScreenSpring() =
  spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow,
  )

private const val NAV_ANIM_FADE_DURATION_MS = 250
private const val NAV_ANIM_ENTER_SCALE = 0.92f
private const val NAV_ANIM_EXIT_SCALE = 1.08f
