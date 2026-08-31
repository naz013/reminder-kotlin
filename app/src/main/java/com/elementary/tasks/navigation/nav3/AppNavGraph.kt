package com.elementary.tasks.navigation.nav3

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.ads.AdBanner
import com.elementary.tasks.ads.NormalAdBanner
import com.elementary.tasks.birthdays.dialog.BirthdayActionActivity
import com.github.naz013.feature.agenda.AgendaNavKey
import com.github.naz013.feature.agenda.agendaEntries
import com.github.naz013.feature.calendar.monthview.CalendarNavKey
import com.github.naz013.feature.calendar.monthview.calendarEntries
import com.elementary.tasks.core.os.datapicker.compose.rememberContactPhonePicker
import com.elementary.tasks.eventaction.rememberEventActionDispatcher
import com.github.naz013.feature.home.HomeNavKey
import com.github.naz013.feature.home.homeEntries
import com.elementary.tasks.navigation.BottomNavActivity
import com.github.naz013.feature.places.PlacesNavKey
import com.github.naz013.feature.places.placesEntries
import com.elementary.tasks.reminder.dialog.ReminderActionActivity
import com.elementary.tasks.settings.BirthdayCrossFeatureEntry
import com.elementary.tasks.settings.ManagePresetsCrossFeatureEntry
import com.elementary.tasks.settings.RemindersCrossFeatureEntry
import com.github.naz013.ui.common.compose.foundation.share.rememberFileIntentSender
import com.github.naz013.ui.common.compose.foundation.telephony.rememberApplicationLauncher
import com.github.naz013.ui.common.compose.foundation.telephony.rememberPhoneCaller
import com.github.naz013.ui.common.compose.foundation.telephony.rememberSmsSender
import com.github.naz013.ui.common.compose.foundation.telephony.rememberUrlLauncher
import com.github.naz013.feature.birthday.BirthdaysNavKey
import com.github.naz013.feature.birthday.birthdaysEntries
import com.github.naz013.feature.googletask.GoogleTasksNavKey
import com.github.naz013.feature.googletask.googleTasksEntries
import com.github.naz013.feature.note.NotesNavKey
import com.github.naz013.feature.note.notesEntries
import com.github.naz013.feature.reminder.build.BuildReminderNavKey
import com.github.naz013.feature.reminder.build.buildReminderEntries
import com.github.naz013.feature.reminder.lists.removed.RemindersArchiveNavKey
import com.github.naz013.feature.reminder.lists.removed.remindersArchiveEntries
import com.github.naz013.feature.reminder.preview.ReminderPreviewNavKey
import com.github.naz013.feature.reminder.preview.reminderPreviewEntries
import com.github.naz013.feature.reminder.settings.help.NotificationCustomizationHelpScreen
import com.github.naz013.feature.reminder.todo.TodoEditNavKey
import com.github.naz013.feature.reminder.todo.todoEditEntries
import com.github.naz013.feature.settings.SettingsNavKey
import com.github.naz013.feature.settings.export.ExportNavKey
import com.github.naz013.feature.settings.export.exportEntries
import com.github.naz013.feature.settings.location.locationEntries
import com.github.naz013.feature.settings.other.OtherNavKey
import com.github.naz013.feature.settings.other.otherEntries
import com.github.naz013.ui.common.compose.foundation.intent.rememberSendIntentResolver
import com.github.naz013.ui.common.compose.foundation.isDesktopScreen
import com.github.naz013.ui.common.compose.foundation.isTabletScreen
import com.github.naz013.feature.settings.security.securityEntries
import com.github.naz013.feature.settings.settingsEntries
import com.github.naz013.feature.workflow.WorkflowNavKey
import com.github.naz013.feature.workflow.workflowEntries
import com.github.naz013.group.GroupsNavKey
import com.github.naz013.group.groupsEntries
import com.github.naz013.insights.insightsEntries
import com.github.naz013.localbackup.LocalBackupNavKey
import com.github.naz013.localbackup.localBackupEntries
import com.github.naz013.feature.routine.RoutineNavKey
import com.github.naz013.feature.routine.routineEntries
import com.github.naz013.tags.TagsNavKey
import com.github.naz013.tags.tagsEntries
import com.github.naz013.domain.home.HeaderNavigationSection
import com.github.naz013.feature.home.HomePreferences
import com.github.naz013.logic.routine.RoutineConfig
import com.github.naz013.logic.workflow.WorkflowConfig
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.navigation.AppDestination
import org.koin.compose.koinInject

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
 * calls with `ListDetailSceneStrategy.listPane()`/`.detailPane()` - no other wiring needed. Home's
 * `HomeNavKey.Main` and the reminder/birthday preview screens are the first adopters - see
 * `docs/home-two-pane-design.md`.
 *
 * [sidePanelSceneStrategy] is a second, independent two-pane mechanism for flows that want a
 * floating Material 3 side sheet (https://m3.material.io/components/side-sheets/overview) instead
 * of a fixed-width shared column: the host entry stays full-screen and is never adapted (tag it
 * `sidePanelHost()`), while the entry pushed directly on top of it floats over it with a scrim (tag
 * it `sidePanelSupporting()`). Deliberately a small hand-rolled `SceneStrategy` rather than the M3
 * adaptive library's own `SupportingPaneSceneStrategy` - see `SidePanelSceneStrategy.kt`'s doc for
 * why. Calendar's build-reminder/edit-birthday/preview screens are the only adopters so far - see
 * `CalendarNavGraph.kt`.
 *
 * [PersistentNavRailSceneDecoratorStrategy] is registered the same way for the nav rail: it wraps
 * every scene (not just specific tagged entries) in a persistent navigation rail on Medium+
 * width, with the app's top-level sections (Home, Calendar, Notes, ...) as selectable items - see
 * that class's doc for how selection and navigation work off the shared [backStack].
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppNavGraph(initialKeys: List<NavKey> = emptyList()) {
  val context = LocalContext.current
  val backStack = rememberNavBackStack(HomeNavKey.Main, *initialKeys.toTypedArray())
  val appNavBridge = rememberAppNavBridge()
  val eventActionDispatcher = rememberEventActionDispatcher()
  val phoneCaller = rememberPhoneCaller()
  val smsSender = rememberSmsSender()
  val applicationLauncher = rememberApplicationLauncher()
  val urlLauncher = rememberUrlLauncher()
  val fileIntentSender = rememberFileIntentSender()
  val intentResolver = rememberSendIntentResolver()
  val listDetailSceneStrategy = rememberListDetailSceneStrategy<NavKey>()
  // Remembered here, above NavDisplay, rather than inside AppNavigationScaffold itself - NavDisplay
  // disposes and recreates each scene's composition on navigation, so a rail state remembered
  // inside the decorated content would silently reset to collapsed on every navigation.
  val navRailState = rememberWideNavigationRailState()
  // Read directly here (not cached in a ViewModel-owned StateFlow set once at init) so that popping
  // the header-items settings screen off this same backStack - which recomposes this function -
  // immediately reflects the user's latest order/visibility choice, with no app restart needed.
  val homePreferences = koinInject<HomePreferences>()
  val routineConfig = koinInject<RoutineConfig>()
  val workflowConfig = koinInject<WorkflowConfig>()
  val visibleHeaderSections =
    HeaderNavigationSection.pinned +
      homePreferences.headerNavigationOrder.filter { section ->
        section !in homePreferences.disabledHeaderNavigationSections &&
          when (section) {
            HeaderNavigationSection.ROUTINES -> routineConfig.isEnabled
            HeaderNavigationSection.WORKFLOW -> workflowConfig.isEnabled
            else -> true
          }
      }
  val railDestinations = appRailDestinations(visibleSections = visibleHeaderSections)
  val railRootKeys = remember(railDestinations) { railDestinations.map { it.key }.toSet() }
  val persistentNavRailStrategy =
    PersistentNavRailSceneDecoratorStrategy(
      destinations = railDestinations,
      backStack = backStack,
      railState = navRailState,
      onNavigate = { key -> backStack.navigateToRailDestination(key, railRootKeys) },
    )

  // Derived here, not in the feature modules - only this module sees every concrete NavKey type.
  val selectedEventId =
    backStack.lastOrNull()?.let { key ->
      when (key) {
        is ReminderPreviewNavKey.Preview -> key.id
        is BirthdaysNavKey.Preview -> key.id
        is BuildReminderNavKey.Main -> key.id.takeIf { it.isNotBlank() }
        is BirthdaysNavKey.Edit -> key.id
        else -> null
      }
    }
  val isMediumOrWiderWidth = isTabletScreen() || isDesktopScreen()
  val isRenderedAsDetailPane: (NavKey) -> Boolean = { key ->
    isMediumOrWiderWidth &&
      backStack.lastOrNull() == key &&
      backStack.getOrNull(backStack.lastIndex - 1).let {
        it == HomeNavKey.Main ||
          it == AgendaNavKey.List ||
          it == RemindersArchiveNavKey.List ||
          it == BirthdaysNavKey.List ||
          it == GroupsNavKey.List ||
          it == TagsNavKey.Manage ||
          it == GoogleTasksNavKey.List ||
          it == RoutineNavKey.List ||
          it == WorkflowNavKey.Gallery ||
          it == SettingsNavKey.Hub ||
          it == PlacesNavKey.List
      }
  }
  // Distinct from [isRenderedAsDetailPane]: Calendar never becomes a flat list pane (it always
  // stays full screen - see `CalendarNavGraph.kt`'s doc), so a build-reminder/edit-birthday/preview
  // screen reached from it is offered a different rendering: a Material 3 side sheet floating over
  // Calendar (see [sidePanelSceneStrategy] below), not a fixed-width column beside a shrunk list.
  // Both predicates can be true for the same key at different points in its lifetime, never
  // simultaneously, since they key off mutually exclusive predecessor sets.
  val isRenderedAsSidePanel: (NavKey) -> Boolean = { key ->
    isMediumOrWiderWidth &&
      backStack.lastOrNull() == key &&
      backStack.getOrNull(backStack.lastIndex - 1).let { it == CalendarNavKey.Home || it is CalendarNavKey.DayAt }
  }
  // A dedicated hand-rolled SceneStrategy rather than the M3 adaptive library's own
  // SupportingPaneSceneStrategy - see SidePanelSceneStrategy.kt's doc for why the library one
  // doesn't work for this case. Not remember()'d (matching persistentNavRailStrategy below) so the
  // captured isMediumOrWiderWidth lambda always reads the current recomposition's value rather than
  // freezing to whatever it was on first composition.
  val sidePanelSceneStrategy = SidePanelSceneStrategy(isMediumOrWiderWidth = { isMediumOrWiderWidth })

  DisposableEffect(backStack) {
    appNavBridge.attachOuterBackStack(backStack)
    onDispose { appNavBridge.detachOuterBackStack(backStack) }
  }

  NavDisplay(
    backStack = backStack,
    // The two-pane scaffold's gap between panes has no background of its own - without this it
    // shows the raw window canvas (black) instead of the theme's background.
    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
    // sidePanelSceneStrategy tried first: ListDetailSceneStrategy would otherwise phantom-claim
    // Calendar's build-reminder/edit-birthday/preview screens too (they're also tagged
    // ListDetailSceneStrategy.detailPane() for their Home/Agenda/etc. callers) even when reached
    // from Calendar, which was never tagged listPane() - see SidePanelSceneStrategy.kt's doc.
    // sidePanelSceneStrategy only ever claims a scene when its own host+panel tags both match, so
    // trying it first doesn't affect any of listDetailSceneStrategy's own (unrelated) pairings.
    sceneStrategies = listOf(sidePanelSceneStrategy, listDetailSceneStrategy),
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
        homeEntries(
          backStack = backStack,
          selectedEventId = selectedEventId,
          onOpenReminderPreview = { id -> backStack.navigateToDetailPane(ReminderPreviewNavKey.Preview(id)) },
          onOpenBirthdayPreview = { id -> backStack.navigateToDetailPane(BirthdaysNavKey.Preview(id)) },
          onOpenSettings = { backStack.add(SettingsNavKey.Hub) },
          onOpenHeaderItemsSettings = { backStack.add(SettingsNavKey.HeaderItems) },
          onOpenCreateReminder = { backStack.add(BuildReminderNavKey.Main()) },
          onOpenCreateBirthday = { backStack.add(BirthdaysNavKey.Edit()) },
          onOpenCreateGoogleTask = {
            backStack.add(GoogleTasksNavKey.List)
            backStack.add(GoogleTasksNavKey.TaskEdit())
          },
          onOpenCalendar = { backStack.add(CalendarNavKey.Home) },
          onOpenAgenda = { backStack.add(AgendaNavKey.List) },
          onOpenNotes = { backStack.add(NotesNavKey.List) },
          onOpenBirthdays = { backStack.add(BirthdaysNavKey.List) },
          onOpenGoogleTasks = { backStack.add(GoogleTasksNavKey.List) },
          onOpenGroups = { backStack.add(GroupsNavKey.List) },
          onOpenTags = { backStack.add(TagsNavKey.Manage) },
          onOpenRoutines = { backStack.add(RoutineNavKey.List) },
          onOpenWorkflowGallery = { backStack.add(WorkflowNavKey.Gallery) },
          onOpenPrivacyPolicy = { backStack.add(OtherNavKey.PrivacyPolicy) },
          onOpenCloudDrives = { backStack.add(ExportNavKey.CloudServices) },
          onOpenWhatsNew = { backStack.add(OtherNavKey.WhatsNew) },
          onOpenCreateNote = {
            backStack.add(NotesNavKey.List)
            backStack.add(NotesNavKey.Edit())
          },
          onOpenCreateTodo = { backStack.add(TodoEditNavKey.Main()) },
          onEventAction = { action -> eventActionDispatcher.dispatch(action) },
        )
        notesEntries(
          backStack = backStack,
          applicationId = BuildConfig.APPLICATION_ID,
          onOpenNoteSettings = { title -> backStack.add(SettingsNavKey.Note(title)) },
          onEditReminder = { id -> backStack.add(BuildReminderNavKey.Main(id = id)) },
          adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), AdBanner.NotePreview) },
        )
        groupsEntries(
          backStack = backStack,
          isRenderedAsDetailPane = isRenderedAsDetailPane,
          adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), AdBanner.Group) },
          onNotificationHelpClick = { backStack.add(SettingsNavKey.NotificationCustomizationHelp) },
          onNewReminderClick = { backStack.add(BuildReminderNavKey.Main(groupUuId = it)) },
          onReminderPreviewClick = { backStack.add(ReminderPreviewNavKey.Preview(it)) },
          onRulesForGroupClick = { backStack.add(WorkflowNavKey.RulesForGroup(it)) }
        )
        routineEntries(
          backStack = backStack,
          isRenderedAsDetailPane = isRenderedAsDetailPane,
          adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), AdBanner.Routine) },
          onManageTagsClick = { backStack.add(TagsNavKey.Manage) },
        )
        placesEntries(
          backStack = backStack,
          isRenderedAsDetailPane = isRenderedAsDetailPane,
          adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), AdBanner.Place) },
        )
        birthdaysEntries(
          backStack = backStack,
          isRenderedAsDetailPane = isRenderedAsDetailPane,
          isRenderedAsSidePanel = isRenderedAsSidePanel,
          adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), AdBanner.Birthday) },
          onCallClick = { number -> phoneCaller.call(number) },
          onSmsClick = { number -> smsSender.send(number, null) },
        )
        googleTasksEntries(
          backStack = backStack,
          isRenderedAsDetailPane = isRenderedAsDetailPane,
          adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), AdBanner.GoogleTask) }
        )
        buildReminderEntries(
          backStack = backStack,
          isRenderedAsDetailPane = isRenderedAsDetailPane,
          isRenderedAsSidePanel = isRenderedAsSidePanel,
          rememberContactPhonePicker = { rememberContactPhonePicker() },
        )
        todoEditEntries(
          backStack = backStack,
          navigateBeyondBackStack = { key -> appNavBridge.navigate(key) },
        )
        calendarEntries(
          backStack = backStack,
          onOpenNewReminder = { dateMillis ->
            backStack.navigateToDetailPane(
              BuildReminderNavKey.Main(
                deepLinkDateTimeType = BuildReminderNavKey.Main.DateTimeType.Date,
                deepLinkDateTimeMillis = dateMillis,
              ),
            )
          },
          onOpenReminderPreview = { id -> backStack.navigateToDetailPane(ReminderPreviewNavKey.Preview(id)) },
          onOpenNewBirthday = { epochDay ->
            backStack.navigateToDetailPane(BirthdaysNavKey.Edit(prefillDateEpochDay = epochDay))
          },
          onOpenBirthdayPreview = { id -> backStack.navigateToDetailPane(BirthdaysNavKey.Preview(id)) },
          onOpenSettings = { title -> backStack.add(SettingsNavKey.Calendar(title)) },
        )
        agendaEntries(
          backStack = backStack,
          selectedItemId = selectedEventId,
          onOpenReminderPreview = { id -> backStack.navigateToDetailPane(ReminderPreviewNavKey.Preview(id)) },
          onOpenReminderEdit = { id -> backStack.navigateToEditDetailPane(BuildReminderNavKey.Main(id = id)) { it is ReminderPreviewNavKey.Preview && it.id == id } },
          onOpenNewReminder = { backStack.add(BuildReminderNavKey.Main()) },
          onOpenNewTodo = { backStack.add(TodoEditNavKey.Main()) },
          onOpenBirthdayPreview = { id -> backStack.navigateToDetailPane(BirthdaysNavKey.Preview(id)) },
          onOpenBirthdayEdit = { id -> backStack.navigateToEditDetailPane(BirthdaysNavKey.Edit(id)) { it is BirthdaysNavKey.Preview && it.id == id } },
          onOpenNewBirthday = { backStack.add(BirthdaysNavKey.Edit()) },
          onOpenArchive = { backStack.add(RemindersArchiveNavKey.List) },
          onOpenGroups = { backStack.add(GroupsNavKey.List) },
          onOpenTags = { backStack.add(TagsNavKey.Manage) },
        )
        reminderPreviewEntries(
          backStack = backStack,
          isRenderedAsDetailPane = isRenderedAsDetailPane,
          isRenderedAsSidePanel = isRenderedAsSidePanel,
          navigateBeyondBackStack = { keys -> appNavBridge.navigate(*keys.toTypedArray()) },
          adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), AdBanner.ReminderPreview) },
          onShareFile = { title, file -> fileIntentSender.send(title, file) },
          onOpenIntent = { intent, title -> intentResolver.resolve(intent, title) },
          onOpenNote = { noteId -> appNavBridge.navigate(NotesNavKey.List, NotesNavKey.Preview(noteId)) },
          onOpenGoogleTask = { taskId ->
            appNavBridge.navigate(GoogleTasksNavKey.List, GoogleTasksNavKey.TaskEdit(id = taskId))
          },
          onOpenWorkflowRules = { reminderId -> backStack.add(WorkflowNavKey.RulesForReminder(reminderId)) },
          onCallClick = { number -> phoneCaller.call(number) },
          onSmsClick = { target, message -> smsSender.send(target, message) },
          onAppClick = { target -> applicationLauncher.launch(target) },
          onUrlClick = { target -> urlLauncher.launch(target) },
        )
        remindersArchiveEntries(
          backStack = backStack,
          selectedItemId = selectedEventId,
          onOpenEdit = { id -> backStack.navigateToDetailPane(BuildReminderNavKey.Main(id = id)) },
        )
        settingsEntries(
          backStack = backStack,
          applicationId = BuildConfig.APPLICATION_ID,
          restartActivityClass = BottomNavActivity::class.java,
          isRenderedAsDetailPane = isRenderedAsDetailPane,
          remindersEntry = { key, entryBackStack, renderAsDetailPane ->
            RemindersCrossFeatureEntry(key, entryBackStack, renderAsDetailPane)
          },
          birthdayEntry = { key, entryBackStack, renderAsDetailPane ->
            BirthdayCrossFeatureEntry(key, entryBackStack, renderAsDetailPane)
          },
          managePresetsEntry = { entryBackStack -> ManagePresetsCrossFeatureEntry(entryBackStack) },
          notificationCustomizationHelpEntry = { onBackClick ->
            NotificationCustomizationHelpScreen(onBackClick = onBackClick)
          },
          onOpenLocalBackupExport = { uri -> backStack.add(LocalBackupNavKey.Export(uri)) },
          onOpenLocalBackupImport = { uri -> backStack.add(LocalBackupNavKey.Import(uri)) },
          onOpenReminderActionTest = { reminderId -> ReminderActionActivity.mockTest(context, reminderId) },
          onOpenBirthdayActionTest = { birthdayId -> BirthdayActionActivity.mockTest(context, birthdayId) },
        )
        securityEntries(backStack = backStack, isRenderedAsDetailPane = isRenderedAsDetailPane)
        locationEntries(
          backStack = backStack,
          onOpenPlaces = { backStack.add(PlacesNavKey.List) },
        )
        otherEntries(
          backStack = backStack,
          isRenderedAsDetailPane = isRenderedAsDetailPane,
          onOpenTroubleshooting = { backStack.add(SettingsNavKey.Troubleshooting) },
          onOpenProVersion = { backStack.add(SettingsNavKey.ProVersion) },
        )
        exportEntries(backStack)
        workflowEntries(backStack = backStack, isRenderedAsDetailPane = isRenderedAsDetailPane)
        tagsEntries(
          backStack = backStack,
          isRenderedAsDetailPane = isRenderedAsDetailPane,
          adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), AdBanner.Tag) },
          onReminderPreviewClick = { backStack.add(ReminderPreviewNavKey.Preview(it)) },
          onNotePreviewClick = { id -> backStack.add(NotesNavKey.Preview(id)) },
          onBirthdayPreviewClick = { id -> backStack.add(BirthdaysNavKey.Preview(id)) },
          onGoogleTaskPreviewClick = { id -> backStack.add(GoogleTasksNavKey.TaskPreview(id)) },
        )
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
 * [visibleSections] is the same user-ordered, flag-filtered [HeaderNavigationSection] list Home's
 * header grid renders (see `GetNavigationItemsUseCase`) - computed by the caller so this Composable
 * doesn't need to read preferences/config itself.
 */
@Composable
private fun appRailDestinations(visibleSections: List<HeaderNavigationSection>): List<AppDestination<NavKey>> =
  buildList {
    add(
      AppDestination(
        key = HomeNavKey.Main,
        icon = painterResource(R.drawable.ic_fluent_home),
        labelRes = R.string.home,
      ),
    )
    visibleSections.forEach { section -> add(headerSectionRailDestination(section)) }
    add(
      AppDestination(
        key = SettingsNavKey.Hub,
        icon = painterResource(R.drawable.ic_fluent_settings),
        labelRes = R.string.action_settings,
      ),
    )
  }

@Composable
private fun headerSectionRailDestination(section: HeaderNavigationSection): AppDestination<NavKey> =
  when (section) {
    HeaderNavigationSection.CALENDAR ->
      AppDestination(
        key = CalendarNavKey.Home,
        icon = AppIcons.Fluent.Calendar,
        labelRes = R.string.calendar,
      )
    HeaderNavigationSection.AGENDA ->
      AppDestination(
        key = AgendaNavKey.List,
        icon = AppIcons.Fluent.Timeline,
        labelRes = R.string.agenda,
      )
    HeaderNavigationSection.NOTES ->
      AppDestination(
        key = NotesNavKey.List,
        icon = AppIcons.Fluent.Note,
        labelRes = R.string.notes,
      )
    HeaderNavigationSection.BIRTHDAYS ->
      AppDestination(
        key = BirthdaysNavKey.List,
        icon = AppIcons.Fluent.FoodCake,
        labelRes = R.string.birthdays,
      )
    HeaderNavigationSection.GOOGLE_TASKS ->
      AppDestination(
        key = GoogleTasksNavKey.List,
        icon = AppIcons.Builder.GoogleTaskList,
        labelRes = R.string.google_tasks,
      )
    HeaderNavigationSection.GROUPS ->
      AppDestination(
        key = GroupsNavKey.List,
        icon = AppIcons.Fluent.Group,
        labelRes = R.string.groups,
      )
    HeaderNavigationSection.TAG ->
      AppDestination(
        key = TagsNavKey.Manage,
        icon = AppIcons.Builder.Tag,
        labelRes = R.string.tags,
      )
    HeaderNavigationSection.ROUTINES ->
      AppDestination(
        key = RoutineNavKey.List,
        icon = AppIcons.Builder.Timer,
        labelRes = R.string.routines,
      )
    HeaderNavigationSection.WORKFLOW ->
      AppDestination(
        key = WorkflowNavKey.Gallery,
        icon = AppIcons.Fluent.ArrowRepeatAll,
        labelRes = R.string.workflow_automations,
      )
  }

/**
 * Click behavior for a [PersistentNavRailSceneDecoratorStrategy] item: if [key] is already
 * somewhere on the backstack, pop back to it (dropping everything pushed above it, like tapping
 * an already-selected tab). Otherwise, first collapse the tab currently on top back to its own
 * root - using [railRootKeys], the set of every rail destination's key - before pushing the new
 * tab's root. Without this, a preview/edit screen left open in the tab being switched away from
 * (e.g. Birthdays with a birthday preview open) stays on the backstack underneath the new tab, and
 * resurfaces via system back after switching tabs instead of the tab's plain list. Keeps the
 * single flat backstack from growing unbounded as sections are revisited.
 */
private fun MutableList<NavKey>.navigateToRailDestination(key: NavKey, railRootKeys: Set<NavKey>) {
  val existingIndex = indexOf(key)
  if (existingIndex >= 0) {
    while (size > existingIndex + 1) removeLastOrNull()
  } else {
    val currentRootIndex = indexOfLast { it in railRootKeys }
    if (currentRootIndex >= 0) {
      while (size > currentRootIndex + 1) removeLastOrNull()
    }
    add(key)
  }
}

/**
 * Navigation for a two-pane list's detail pane (Home, Agenda, Reminders Archive): if the current
 * top entry is itself a reminder/birthday preview or edit form, replace it instead of stacking
 * another one on top.
 * Only ever matters in two-pane mode - on Compact width the list isn't visible while a preview or
 * edit form is showing, so this can't be reached with one already on top. Without it, picking a
 * second list item (or its Edit action) pushes a second `detailPane()`-tagged entry, which
 * [ListDetailSceneStrategy] doesn't support (it expects at most one entry per pane role).
 */
private fun MutableList<NavKey>.navigateToDetailPane(key: NavKey) {
  val top = lastOrNull()
  if (
    top is ReminderPreviewNavKey.Preview ||
    top is BirthdaysNavKey.Preview ||
    top is BuildReminderNavKey.Main ||
    top is BirthdaysNavKey.Edit
  ) {
    removeLastOrNull()
  }
  add(key)
}

/**
 * Navigation for a two-pane list's detail pane (Agenda's row menu) into an Edit screen: if the
 * detail pane is currently showing a Preview of that very same item ([isSameItemPreview] matches
 * the top entry), push Edit on top of it instead of replacing it - Preview stays on the backstack
 * underneath, so the edit screen's plain single-entry back arrow naturally reveals Preview again
 * once editing is done (mirrors Preview's own Edit button - see the matching comment in
 * ReminderPreviewNavGraph.kt/BirthdaysNavGraph.kt). Otherwise - a different item's Preview/Edit is
 * showing, or nothing is - swap it via [navigateToDetailPane] like any other detail-pane nav,
 * since there's no prior preview of *this* item worth returning to.
 */
private fun MutableList<NavKey>.navigateToEditDetailPane(key: NavKey, isSameItemPreview: (NavKey) -> Boolean) {
  val top = lastOrNull()
  if (top != null && isSameItemPreview(top)) {
    add(key)
  } else {
    navigateToDetailPane(key)
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
