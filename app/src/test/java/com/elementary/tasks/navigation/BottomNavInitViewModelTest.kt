package com.elementary.tasks.navigation

import com.elementary.tasks.BaseTest
import com.github.naz013.feature.calendar.occurrence.MigrateExistingEventOccurrencesUseCase
import com.github.naz013.feature.note.image.NoteImageMigration
import com.elementary.tasks.core.utils.ActivateAllActiveRemindersUseCase
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.feature.reminder.build.preset.PresetInitProcessor
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.params.RemotePrefs
import com.github.naz013.ui.group.GroupsUtil
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.feature.workflow.WorkflowRulesUtil
import com.github.naz013.appwidgets.AppWidgetPreviewUpdater
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.logic.demodata.InsertDemoDataUseCase
import com.github.naz013.repository.migration.GroupV2BackfillUseCase
import com.github.naz013.repository.migration.ReminderV2BackfillUseCase
import com.github.naz013.scheduler.JobSchedulerApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// mockDispatcherProvider() uses Dispatchers.Unconfined, so the init{} coroutine below runs
// synchronously and `state` is already Ready by the time createViewModel() returns.
class BottomNavInitViewModelTest : BaseTest() {
  private val googleTasksAuthManager = mockk<GoogleTasksAuthManager>()
  private val prefs = mockk<Prefs>(relaxed = true)
  private val remotePrefs = mockk<RemotePrefs>(relaxed = true)
  private val activateAllActiveRemindersUseCase = mockk<ActivateAllActiveRemindersUseCase>(relaxed = true)
  private val notifier = mockk<Notifier>(relaxed = true)
  private val featureFlags = mockk<FeatureFlags>()
  private val packageManagerWrapper = mockk<PackageManagerWrapper>()
  private val groupsUtil = mockk<GroupsUtil>(relaxed = true)
  private val noteImageMigration = mockk<NoteImageMigration>(relaxed = true)
  private val presetInitProcessor = mockk<PresetInitProcessor>(relaxed = true)
  private val appWidgetPreviewUpdater = mockk<AppWidgetPreviewUpdater>(relaxed = true)
  private val migrateExistingEventOccurrencesUseCase =
    mockk<MigrateExistingEventOccurrencesUseCase>(relaxed = true)
  private val groupV2BackfillUseCase = mockk<GroupV2BackfillUseCase>(relaxed = true)
  private val reminderV2BackfillUseCase = mockk<ReminderV2BackfillUseCase>(relaxed = true)
  private val workflowRulesUtil = mockk<WorkflowRulesUtil>(relaxed = true)
  private val jobScheduler = mockk<JobSchedulerApi>(relaxed = true)
  private val insertDemoDataUseCase = mockk<InsertDemoDataUseCase>(relaxed = true)

  private var occurrenceMigrated = false
  private var noteMigrationDone = false
  private var demoDataInserted = false
  private var hasSeenOnboarding = false
  private val savedVersions = mutableSetOf<String>()

  private lateinit var viewModel: BottomNavInitViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { featureFlags.isEnabled(FeatureFlag.GOOGLE_TASKS) } returns true
    every { featureFlags.isEnabled(FeatureFlag.ROUTINE_ENABLED) } returns false
    every { featureFlags.isEnabled(FeatureFlag.WORKFLOW_ENABLED) } returns false
    every { prefs.workflowRulesScheduled } returns false
    every { prefs.workflowUnacknowledgedRulesScheduled } returns false
    every { prefs.googleCalendarScanFallbackScheduled } returns true
    every { googleTasksAuthManager.isAuthorized() } returns true
    every { packageManagerWrapper.getVersionName() } returns "1.0.0"

    every { prefs.occurrenceMigrated } answers { occurrenceMigrated }
    every { prefs.occurrenceMigrated = any() } answers { occurrenceMigrated = firstArg() }
    every { prefs.noteMigrationDone } answers { noteMigrationDone }
    every { prefs.noteMigrationDone = any() } answers { noteMigrationDone = firstArg() }
    every { prefs.isDemoDataInserted } answers { demoDataInserted }
    every { prefs.isDemoDataInserted = any() } answers { demoDataInserted = firstArg() }
    every { prefs.hasSeenOnboarding } answers { hasSeenOnboarding }
    every { prefs.hasSeenOnboarding = any() } answers { hasSeenOnboarding = firstArg() }
    every { prefs.getVersion(any()) } answers { savedVersions.contains(firstArg()) }
    every { prefs.saveVersionBoolean(any()) } answers { savedVersions.add(firstArg()) }
    every { prefs.isSbNotificationEnabled } returns false
    every { prefs.hasPinCode } returns false

    coEvery { groupsUtil.initDefaultIfEmpty() } returns Unit

    viewModel = createViewModel()
  }

  private fun createViewModel(): BottomNavInitViewModel =
    BottomNavInitViewModel(
      googleTasksAuthManager = googleTasksAuthManager,
      prefs = prefs,
      remotePrefs = remotePrefs,
      activateAllActiveRemindersUseCase = activateAllActiveRemindersUseCase,
      dispatcherProvider = mockDispatcherProvider(),
      notifier = notifier,
      featureFlags = featureFlags,
      packageManagerWrapper = packageManagerWrapper,
      groupsUtil = groupsUtil,
      noteImageMigration = noteImageMigration,
      presetInitProcessor = presetInitProcessor,
      appWidgetPreviewUpdater = appWidgetPreviewUpdater,
      migrateExistingEventOccurrencesUseCase = migrateExistingEventOccurrencesUseCase,
      groupV2BackfillUseCase = groupV2BackfillUseCase,
      reminderV2BackfillUseCase = reminderV2BackfillUseCase,
      workflowRulesUtil = workflowRulesUtil,
      jobScheduler = jobScheduler,
      insertDemoDataUseCase = insertDemoDataUseCase,
    )

  @Test
  fun `isGoogleTasksEnabled is true when the feature is enabled and the user is authorized`() {
    assertTrue(viewModel.isGoogleTasksEnabled)
  }

  @Test
  fun `isGoogleTasksEnabled is false when the feature flag is disabled`() {
    every { featureFlags.isEnabled(FeatureFlag.GOOGLE_TASKS) } returns false

    val vm = createViewModel()

    assertFalse(vm.isGoogleTasksEnabled)
  }

  @Test
  fun `isGoogleTasksEnabled is false when the user is not authorized`() {
    every { googleTasksAuthManager.isAuthorized() } returns false

    val vm = createViewModel()

    assertFalse(vm.isGoogleTasksEnabled)
  }

  @Test
  fun `init runs the preset init processor`() {
    coVerify(exactly = 1) { presetInitProcessor.run() }
  }

  @Test
  fun `init initializes default groups and updates the widget preview`() {
    coVerify(exactly = 1) { groupsUtil.initDefaultIfEmpty() }
    coVerify(exactly = 1) { appWidgetPreviewUpdater.updateEventsWidgetPreview() }
  }

  @Test
  fun `init shows the permanent notification and requires login based on prefs`() {
    every { prefs.isSbNotificationEnabled } returns true
    every { prefs.hasPinCode } returns true

    val vm = createViewModel()

    verify(exactly = 1) { notifier.sendShowReminderPermanent() }
    // setUp()'s initial createViewModel() call already flipped isDemoDataInserted to true, so
    // this second viewModel no longer sees a fresh install.
    assertEquals(BottomNavInitState.Ready(requiresLogin = true, shouldShowOnboarding = false), vm.state.value)
  }

  @Test
  fun `init does not show the notification when it is disabled in prefs`() {
    every { prefs.isSbNotificationEnabled } returns false
    every { prefs.hasPinCode } returns false

    val vm = createViewModel()

    verify(exactly = 0) { notifier.sendShowReminderPermanent() }
    assertEquals(BottomNavInitState.Ready(requiresLogin = false, shouldShowOnboarding = false), vm.state.value)
  }

  @Test
  fun `schedules the routine recurrence reset check when the feature flag is enabled`() {
    every { featureFlags.isEnabled(FeatureFlag.ROUTINE_ENABLED) } returns true
    every { prefs.routineRecurrenceResetScheduled } returns false

    createViewModel()

    verify(exactly = 1) { jobScheduler.scheduleRoutineRecurrenceResetCheck() }
    verify(exactly = 1) { prefs.routineRecurrenceResetScheduled = true }
  }

  @Test
  fun `does not schedule the routine recurrence reset check when the feature flag is disabled`() {
    every { featureFlags.isEnabled(FeatureFlag.ROUTINE_ENABLED) } returns false

    createViewModel()

    verify(exactly = 0) { jobScheduler.scheduleRoutineRecurrenceResetCheck() }
  }

  @Test
  fun `does not reschedule the routine recurrence reset check once already scheduled`() {
    every { featureFlags.isEnabled(FeatureFlag.ROUTINE_ENABLED) } returns true
    every { prefs.routineRecurrenceResetScheduled } returns true

    createViewModel()

    verify(exactly = 0) { jobScheduler.scheduleRoutineRecurrenceResetCheck() }
  }

  @Test
  fun `seeds default workflow rules and schedules workflow checks when the feature flag is enabled`() {
    every { featureFlags.isEnabled(FeatureFlag.WORKFLOW_ENABLED) } returns true

    createViewModel()

    coVerify(exactly = 1) { workflowRulesUtil.initDefaultIfEmpty() }
    verify(exactly = 1) { jobScheduler.scheduleWorkflowRulesCheck() }
    verify(exactly = 1) { jobScheduler.scheduleWorkflowUnacknowledgedCheck() }
    verify(exactly = 1) { prefs.workflowRulesScheduled = true }
    verify(exactly = 1) { prefs.workflowUnacknowledgedRulesScheduled = true }
  }

  @Test
  fun `does not seed workflow rules or schedule workflow checks when the feature flag is disabled`() {
    coVerify(exactly = 0) { workflowRulesUtil.initDefaultIfEmpty() }
    verify(exactly = 0) { jobScheduler.scheduleWorkflowRulesCheck() }
    verify(exactly = 0) { jobScheduler.scheduleWorkflowUnacknowledgedCheck() }
  }

  @Test
  fun `each guarded one-time migration only runs once across repeated cold starts`() {
    // viewModel from setUp() already ran the migrations once; a second cold start (e.g. after
    // AppRestartController.restartApp() recreates BottomNavActivity) should see the prefs flags
    // already set and skip them.
    createViewModel()

    coVerify(exactly = 1) { migrateExistingEventOccurrencesUseCase() }
    coVerify(exactly = 1) { noteImageMigration.migrate() }
    coVerify(exactly = 1) { insertDemoDataUseCase() }
    verify(exactly = 1) { activateAllActiveRemindersUseCase.run() }
    assertTrue(occurrenceMigrated)
    assertTrue(noteMigrationDone)
    assertTrue(demoDataInserted)
    assertTrue(savedVersions.contains("1.0.0"))
  }

  @Test
  fun `schedules the Google Calendar scan fallback check on first run`() {
    every { prefs.googleCalendarScanFallbackScheduled } returns false

    createViewModel()

    verify(exactly = 1) { jobScheduler.scheduleGoogleCalendarScanFallbackCheck() }
    verify(exactly = 1) { prefs.googleCalendarScanFallbackScheduled = true }
  }

  @Test
  fun `does not reschedule the Google Calendar scan fallback check once already scheduled`() {
    every { prefs.googleCalendarScanFallbackScheduled } returns true

    createViewModel()

    verify(exactly = 0) { jobScheduler.scheduleGoogleCalendarScanFallbackCheck() }
  }

  @Test
  fun `inserts demo data once on a fresh install`() {
    // viewModel from setUp() already ran init on a fresh (demoDataInserted = false) start.
    coVerify(exactly = 1) { insertDemoDataUseCase() }
    verify(exactly = 1) { prefs.isDemoDataInserted = true }
  }

  @Test
  fun `does not insert demo data when it was already inserted`() {
    demoDataInserted = true

    createViewModel()

    coVerify(exactly = 1) { insertDemoDataUseCase() } // still just the one call from setUp()
  }

  @Test
  fun `shows onboarding on a fresh install that has not seen it yet`() {
    demoDataInserted = false
    hasSeenOnboarding = false

    val vm = createViewModel()

    assertTrue(vm.state.value.let { it as BottomNavInitState.Ready }.shouldShowOnboarding)
  }

  @Test
  fun `does not show onboarding again on a fresh install once already seen`() {
    demoDataInserted = false
    hasSeenOnboarding = true

    val vm = createViewModel()

    assertFalse(vm.state.value.let { it as BottomNavInitState.Ready }.shouldShowOnboarding)
  }

  @Test
  fun `auto-marks onboarding as seen for an existing install instead of showing it`() {
    // demoDataInserted already true means this is not the device's first-ever cold start.
    demoDataInserted = true
    hasSeenOnboarding = false

    val vm = createViewModel()

    assertFalse(vm.state.value.let { it as BottomNavInitState.Ready }.shouldShowOnboarding)
    assertTrue(hasSeenOnboarding)
  }
}
