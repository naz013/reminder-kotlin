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
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.ads.AdBanner
import com.elementary.tasks.ads.NormalAdBanner
import com.elementary.tasks.calendar.monthview.CalendarNavKey
import com.elementary.tasks.calendar.monthview.calendarEntries
import com.elementary.tasks.core.os.datapicker.compose.rememberContactPicker
import com.elementary.tasks.groups.groupDetailsEntries
import com.elementary.tasks.home.HomeNavKey
import com.elementary.tasks.home.homeEntries
import com.elementary.tasks.places.placesEntries
import com.elementary.tasks.telephony.rememberPhoneCaller
import com.elementary.tasks.telephony.rememberSmsSender
import com.elementary.tasks.reminder.build.BuildReminderNavKey
import com.elementary.tasks.reminder.build.buildReminderEntries
import com.elementary.tasks.reminder.lists.removed.remindersArchiveEntries
import com.elementary.tasks.reminder.preview.reminderPreviewEntries
import com.elementary.tasks.reminder.todo.todoEditEntries
import com.elementary.tasks.settings.SettingsNavKey
import com.elementary.tasks.settings.export.exportEntries
import com.elementary.tasks.settings.location.locationEntries
import com.elementary.tasks.settings.other.otherEntries
import com.elementary.tasks.settings.security.securityEntries
import com.elementary.tasks.settings.settingsEntries
import com.github.naz013.feature.birthday.birthdaysEntries
import com.github.naz013.feature.googletask.GoogleTasksNavKey
import com.github.naz013.feature.googletask.googleTasksEntries
import com.github.naz013.feature.note.NotesNavKey
import com.github.naz013.feature.note.notesEntries
import com.github.naz013.feature.workflow.WorkflowNavKey
import com.github.naz013.feature.workflow.workflowEntries
import com.github.naz013.group.GroupsNavKey
import com.github.naz013.group.groupsEntries
import com.github.naz013.insights.insightsEntries
import com.github.naz013.localbackup.localBackupEntries
import com.github.naz013.tags.tagsEntries
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.navigation.AppDestination
import org.koin.compose.viewmodel.koinViewModel

/**
 * Root of the app's single Nav3 graph, hosted directly by
 * [BottomNavActivity][com.elementary.tasks.navigation.BottomNavActivity]. [HomeNavKey.Main] is the
 * graph's own start destination. Every screen registers its own entries here via a
 * `fun EntryProviderScope<NavKey>.xyzEntries(backStack)` extension in that feature's own
 * `XyzNavGraph.kt`; [AppNavBridge] lets a screen several NavEntries deep reach a destination
 * belonging to a different feature's graph without holding the backstack directly.
 *
 * [listDetailSceneStrategy] is registered here so any feature's `XyzNavGraph.kt` can opt an
 * entry pair into a two-pane list-detail layout on Medium+ width just by tagging its `entry<>()`
 * calls with `ListDetailSceneStrategy.listPane()`/`.detailPane()` - no other wiring needed. It is
 * a no-op today: no entry anywhere in the graph carries that metadata yet.
 *
 * [PersistentNavRailSceneDecoratorStrategy] is registered the same way for the nav rail: it wraps
 * every scene (not just specific tagged entries) in a persistent navigation rail on Medium+
 * width, with the app's top-level sections (Home, Calendar, Notes, ...) as selectable items - see
 * that class's doc for how selection and navigation work off the shared [backStack].
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppNavGraph(initialKeys: List<NavKey> = emptyList()) {
  val backStack = rememberNavBackStack(HomeNavKey.Main, *initialKeys.toTypedArray())
  val appNavBridge = rememberAppNavBridge()
  val phoneCaller = rememberPhoneCaller()
  val smsSender = rememberSmsSender()
  val listDetailSceneStrategy = rememberListDetailSceneStrategy<NavKey>()
  val viewModel = koinViewModel<AppNavGraphViewModel>()
  val state by viewModel.state.collectAsStateWithLifecycle()
  // Remembered here, above NavDisplay, rather than inside AppNavigationScaffold itself - NavDisplay
  // disposes and recreates each scene's composition on navigation, so a rail state remembered
  // inside the decorated content would silently reset to collapsed on every navigation.
  val navRailState = rememberWideNavigationRailState()
  val persistentNavRailStrategy =
    PersistentNavRailSceneDecoratorStrategy(
      destinations = appRailDestinations(isWorkflowEnabled = state.isWorkflowEnabled),
      backStack = backStack,
      railState = navRailState,
      onNavigate = { key -> backStack.navigateToRailDestination(key) },
    )

  DisposableEffect(backStack) {
    appNavBridge.attachOuterBackStack(backStack)
    onDispose { appNavBridge.detachOuterBackStack(backStack) }
  }

  NavDisplay(
    backStack = backStack,
    modifier = Modifier.fillMaxSize(),
    onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
    sceneStrategies = listOf(listDetailSceneStrategy),
    sceneDecoratorStrategies = listOf(persistentNavRailStrategy),
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
        notesEntries(
          backStack = backStack,
          applicationId = BuildConfig.APPLICATION_ID,
          onOpenNoteSettings = { title -> backStack.add(SettingsNavKey.Note(title)) },
          onEditReminder = { id -> backStack.add(BuildReminderNavKey.Main(id = id)) },
          adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), AdBanner.NotePreview) },
        )
        groupsEntries(
          backStack = backStack,
          adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), AdBanner.Group) },
          onNotificationHelpClick = { backStack.add(SettingsNavKey.NotificationCustomizationHelp) },
        )
        groupDetailsEntries(backStack)
        placesEntries(backStack)
        birthdaysEntries(
          backStack = backStack,
          adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), AdBanner.Birthday) },
          onCallClick = { number -> phoneCaller.call(number) },
          onSmsClick = { number -> smsSender.send(number, null) },
          rememberContactPicker = { onContactPicked -> rememberContactPicker(onContactPicked) },
        )
        googleTasksEntries(
          backStack = backStack,
          adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), AdBanner.GoogleTask) }
        )
        buildReminderEntries(backStack)
        todoEditEntries(backStack)
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

/**
 * The app's top-level sections for [PersistentNavRailSceneDecoratorStrategy] - static
 * icon/label only, no live counts (see that class's doc for why). Mirrors Home's header
 * navigation grid (`GetNavigationItemsUseCase`) plus an explicit Home item, which - as the
 * graph's start destination - is always selected by default.
 *
 * [isWorkflowEnabled] comes from [AppNavGraphViewModel] rather than this Composable reading
 * `WorkflowConfig` (or any other config source) itself.
 */
@Composable
private fun appRailDestinations(isWorkflowEnabled: Boolean): List<AppDestination<NavKey>> =
  buildList {
    add(
      AppDestination(
        key = HomeNavKey.Main,
        icon = painterResource(R.drawable.ic_fluent_home),
        labelRes = R.string.home,
      ),
    )
    add(
      AppDestination(
        key = CalendarNavKey.Month,
        icon = painterResource(R.drawable.ic_fluent_calendar),
        labelRes = R.string.calendar,
      ),
    )
    add(
      AppDestination(
        key = HomeNavKey.Agenda,
        icon = painterResource(R.drawable.ic_fluent_timeline),
        labelRes = R.string.agenda,
      ),
    )
    add(
      AppDestination(
        key = NotesNavKey.List,
        icon = painterResource(R.drawable.ic_fluent_note),
        labelRes = R.string.notes,
      ),
    )
    add(
      AppDestination(
        key = GoogleTasksNavKey.List,
        icon = painterResource(R.drawable.ic_builder_google_task_list),
        labelRes = R.string.google_tasks,
      ),
    )
    add(
      AppDestination(
        key = GroupsNavKey.List,
        icon = painterResource(R.drawable.ic_fluent_group),
        labelRes = R.string.groups,
      ),
    )
    if (isWorkflowEnabled) {
      add(
        AppDestination(
          key = WorkflowNavKey.Gallery,
          icon = painterResource(R.drawable.ic_fluent_arrow_repeat_all),
          labelRes = R.string.workflow_automations,
        ),
      )
    }
    add(
      AppDestination(
        key = SettingsNavKey.Hub,
        icon = painterResource(R.drawable.ic_fluent_settings),
        labelRes = R.string.action_settings,
      ),
    )
  }

/**
 * Click behavior for a [PersistentNavRailSceneDecoratorStrategy] item: if [key] is already
 * somewhere on the backstack, pop back to it (dropping everything pushed above it, like tapping
 * an already-selected tab); otherwise push it. Keeps the single flat backstack from growing
 * unbounded as sections are revisited.
 */
private fun MutableList<NavKey>.navigateToRailDestination(key: NavKey) {
  val existingIndex = indexOf(key)
  if (existingIndex >= 0) {
    while (size > existingIndex + 1) removeLastOrNull()
  } else {
    add(key)
  }
}

private fun navScreenSpring() =
  spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow,
  )

private const val NAV_ANIM_FADE_DURATION_MS = 250
private const val NAV_ANIM_ENTER_SCALE = 0.92f
private const val NAV_ANIM_EXIT_SCALE = 1.08f
