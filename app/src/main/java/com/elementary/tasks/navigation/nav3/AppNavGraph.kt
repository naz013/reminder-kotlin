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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.elementary.tasks.ads.AdBanner
import com.elementary.tasks.ads.NormalAdBanner
import com.elementary.tasks.birthdays.birthdaysEntries
import com.elementary.tasks.calendar.monthview.calendarEntries
import com.elementary.tasks.groups.groupsEntries
import com.elementary.tasks.home.HomeNavKey
import com.elementary.tasks.home.homeEntries
import com.elementary.tasks.notes.notesEntries
import com.elementary.tasks.places.placesEntries
import com.elementary.tasks.reminder.build.buildReminderEntries
import com.elementary.tasks.reminder.lists.removed.remindersArchiveEntries
import com.elementary.tasks.reminder.preview.reminderPreviewEntries
import com.elementary.tasks.settings.export.exportEntries
import com.elementary.tasks.settings.location.locationEntries
import com.elementary.tasks.settings.other.otherEntries
import com.elementary.tasks.settings.security.securityEntries
import com.elementary.tasks.settings.settingsEntries
import com.elementary.tasks.workflow.workflowEntries
import com.github.naz013.feature.googletask.googleTasksEntries
import com.github.naz013.insights.insightsEntries
import com.github.naz013.localbackup.localBackupEntries
import com.github.naz013.tags.tagsEntries

/**
 * Root of the app's single Nav3 graph, hosted directly by
 * [BottomNavActivity][com.elementary.tasks.navigation.BottomNavActivity]. [HomeNavKey.Main] is the
 * graph's own start destination. Every screen registers its own entries here via a
 * `fun EntryProviderScope<NavKey>.xyzEntries(backStack)` extension in that feature's own
 * `XyzNavGraph.kt`; [AppNavBridge] lets a screen several NavEntries deep reach a destination
 * belonging to a different feature's graph without holding the backstack directly.
 */
@Composable
fun AppNavGraph(initialKeys: List<NavKey> = emptyList()) {
  val backStack = rememberNavBackStack(HomeNavKey.Main, *initialKeys.toTypedArray())
  val appNavBridge = rememberAppNavBridge()

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
        homeEntries(backStack)
        notesEntries(backStack)
        groupsEntries(backStack)
        placesEntries(backStack)
        birthdaysEntries(backStack)
        googleTasksEntries(
          backStack = backStack,
          adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), AdBanner.GoogleTask) }
        )
        buildReminderEntries(backStack)
        calendarEntries(backStack)
        reminderPreviewEntries(backStack)
        remindersArchiveEntries(backStack)
        settingsEntries(backStack)
        securityEntries(backStack)
        locationEntries(backStack)
        otherEntries(backStack)
        exportEntries(backStack)
        workflowEntries(backStack)
        tagsEntries(backStack, adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), AdBanner.Tag) })
        insightsEntries(backStack)
        localBackupEntries(backStack)
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
