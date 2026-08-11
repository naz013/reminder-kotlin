package com.elementary.tasks.navigation

import com.elementary.tasks.BaseTest
import com.elementary.tasks.calendar.occurrence.MigrateExistingEventOccurrencesUseCase
import com.github.naz013.feature.note.image.NoteImageMigration
import com.elementary.tasks.core.utils.ActivateAllActiveRemindersUseCase
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.PresetInitProcessor
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.groups.GroupsUtil
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.feature.workflow.WorkflowRulesUtil
import com.github.naz013.appwidgets.AppWidgetPreviewUpdater
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.common.PackageManagerWrapper
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

  private var occurrenceMigrated = false
  private var noteMigrationDone = false
  private val savedVersions = mutableSetOf<String>()

  private lateinit var viewModel: BottomNavInitViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { featureFlags.isEnabled(FeatureFlag.GOOGLE_TASKS) } returns true
    every { googleTasksAuthManager.isAuthorized() } returns true
    every { packageManagerWrapper.getVersionName() } returns "1.0.0"

    every { prefs.occurrenceMigrated } answers { occurrenceMigrated }
    every { prefs.occurrenceMigrated = any() } answers { occurrenceMigrated = firstArg() }
    every { prefs.noteMigrationDone } answers { noteMigrationDone }
    every { prefs.noteMigrationDone = any() } answers { noteMigrationDone = firstArg() }
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
    assertEquals(BottomNavInitState.Ready(requiresLogin = true), vm.state.value)
  }

  @Test
  fun `init does not show the notification when it is disabled in prefs`() {
    every { prefs.isSbNotificationEnabled } returns false
    every { prefs.hasPinCode } returns false

    val vm = createViewModel()

    verify(exactly = 0) { notifier.sendShowReminderPermanent() }
    assertEquals(BottomNavInitState.Ready(requiresLogin = false), vm.state.value)
  }

  @Test
  fun `each guarded one-time migration only runs once across repeated cold starts`() {
    // viewModel from setUp() already ran the migrations once; a second cold start (e.g. after
    // AppRestartController.restartApp() recreates BottomNavActivity) should see the prefs flags
    // already set and skip them.
    createViewModel()

    coVerify(exactly = 1) { migrateExistingEventOccurrencesUseCase() }
    coVerify(exactly = 1) { noteImageMigration.migrate() }
    verify(exactly = 1) { activateAllActiveRemindersUseCase.run() }
    assertTrue(occurrenceMigrated)
    assertTrue(noteMigrationDone)
    assertTrue(savedVersions.contains("1.0.0"))
  }
}
