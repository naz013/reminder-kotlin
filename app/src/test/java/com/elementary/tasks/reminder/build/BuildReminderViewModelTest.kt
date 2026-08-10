package com.elementary.tasks.reminder.build

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelStore
import com.elementary.tasks.BaseTest
import com.elementary.tasks.R
import com.elementary.tasks.core.data.adapter.preset.UiPresetListAdapter
import com.elementary.tasks.core.data.ui.preset.UiPresetList
import com.github.naz013.featureflags.FeatureFlags
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.mockDispatcherProvider
import com.elementary.tasks.module.analytics.ReminderAnalyticsTracker
import com.elementary.tasks.reminder.build.adapter.BuilderErrorToTextAdapter
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.elementary.tasks.reminder.build.bi.BiFilter
import com.elementary.tasks.reminder.build.bi.constraint.PermissionConstraint
import com.elementary.tasks.reminder.build.logic.BuilderItemsLogic
import com.elementary.tasks.reminder.build.logic.UiBuilderItemsAdapter
import com.elementary.tasks.reminder.build.logic.UiSelectorItemsAdapter
import com.elementary.tasks.reminder.build.logic.builderstate.BuilderErrorFinder
import com.elementary.tasks.reminder.build.logic.builderstate.ReminderPrediction
import com.elementary.tasks.reminder.build.logic.builderstate.ReminderPredictionCalculator
import com.elementary.tasks.reminder.build.preset.BuilderItemsToBuilderPresetAdapter
import com.elementary.tasks.reminder.build.preset.BuilderPresetToBiAdapter
import com.elementary.tasks.reminder.build.preset.RecurParamsToBiAdapter
import com.elementary.tasks.reminder.build.quickstart.FindGroupUseCase
import com.elementary.tasks.reminder.build.quickstart.QuickStartItemsProvider
import com.elementary.tasks.reminder.build.quickstart.QuickStartOption
import com.elementary.tasks.reminder.build.reminder.BiToReminderAdapter
import com.elementary.tasks.reminder.build.reminder.ReminderToBiDecomposer
import com.elementary.tasks.reminder.build.reminder.validation.PermissionValidator
import com.elementary.tasks.reminder.build.selectordialog.SelectorDialogDataHolder
import com.elementary.tasks.reminder.scheduling.usecase.ResumeReminderUseCase
import com.elementary.tasks.reminder.usecase.MoveReminderToArchiveUseCase
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.PresetAction
import com.github.naz013.analytics.PresetUsed
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.TextProvider
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.PresetType
import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.icalendar.ICalendarApi
import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
import com.github.naz013.logic.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.logic.reminder.usecase.PauseReminderUseCase
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.tag.ToggleTagAssignmentUseCase
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.RecurPresetRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.ui.tag.TagChipState
import com.github.naz013.ui.tag.TagChipStateAdapter
import com.github.naz013.usecase.reminders.GetReminderV2ByIdUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class BuildReminderViewModelTest : BaseTest() {
  private val placeRepository = mockk<PlaceRepository>(relaxed = true)
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val reminderAnalyticsTracker = mockk<ReminderAnalyticsTracker>(relaxed = true)
  private val biFactory = mockk<BiFactory>()
  private val builderItemsLogic = mockk<BuilderItemsLogic>(relaxed = true)
  private val selectorDialogDataHolder = SelectorDialogDataHolder()
  private val uiBuilderItemsAdapter = mockk<UiBuilderItemsAdapter>(relaxed = true)
  private val uiSelectorItemsAdapter = mockk<UiSelectorItemsAdapter>(relaxed = true)
  private val biToReminderAdapter = mockk<BiToReminderAdapter>()
  private val permissionValidator = mockk<PermissionValidator>()
  private val reminderToBiDecomposer = mockk<ReminderToBiDecomposer>()
  private val getReminderV2ByIdUseCase = mockk<GetReminderV2ByIdUseCase>()
  private val biFilter = mockk<BiFilter>()
  private val uiPresetListAdapter = mockk<UiPresetListAdapter>(relaxed = true)
  private val recurPresetRepository = mockk<RecurPresetRepository>(relaxed = true)
  private val iCalendarApi = mockk<ICalendarApi>(relaxed = true)
  private val recurParamsToBiAdapter = mockk<RecurParamsToBiAdapter>(relaxed = true)
  private val builderPresetToBiAdapter = mockk<BuilderPresetToBiAdapter>()
  private val reminderPredictionCalculator = mockk<ReminderPredictionCalculator>(relaxed = true)
  private val appWidgetUpdater = mockk<AppWidgetUpdater>(relaxed = true)
  private val builderItemsToBuilderPresetAdapter = mockk<BuilderItemsToBuilderPresetAdapter>(relaxed = true)
  private val dateTimeManager = mockk<DateTimeManager>(relaxed = true)
  private val intentDataReader = mockk<IntentDataReader>(relaxed = true)
  private val builderErrorFinder = mockk<BuilderErrorFinder>(relaxed = true)
  private val builderErrorToTextAdapter = mockk<BuilderErrorToTextAdapter>(relaxed = true)
  private val prefs = mockk<Prefs>(relaxed = true)
  private val deleteReminderUseCase = mockk<DeleteReminderUseCase>(relaxed = true)
  private val moveReminderToArchiveUseCase = mockk<MoveReminderToArchiveUseCase>(relaxed = true)
  private val scheduleBackgroundWorkUseCase = mockk<ScheduleBackgroundWorkUseCase>(relaxed = true)
  private val activateReminderUseCase = mockk<ActivateReminderUseCase>(relaxed = true)
  private val pauseReminderUseCase = mockk<PauseReminderUseCase>(relaxed = true)
  private val resumeReminderUseCase = mockk<ResumeReminderUseCase>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val featureFlags = mockk<FeatureFlags>(relaxed = true)
  private val buildInfo = mockk<BuildInfo>(relaxed = true)
  private val quickStartItemsProvider = mockk<QuickStartItemsProvider>()
  private val tagRepository = mockk<TagRepository>()
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>()
  private val toggleTagAssignmentUseCase = mockk<ToggleTagAssignmentUseCase>()
  private val tagChipStateAdapter = mockk<TagChipStateAdapter>()
  private val findGroupUseCase = mockk<FindGroupUseCase>(relaxed = true)

  @Before
  override fun setUp() {
    super.setUp()
    every { prefs.is24HourFormat } returns true
    every { tagRepository.observeAll() } returns flowOf(emptyList())
    every { tagAssignmentRepository.observeTagsForItem(any(), any()) } returns flowOf(emptyList())
    coEvery { biFactory.create(any()) } returns summaryItem()
    every { biFilter(any()) } returns true
    coEvery { recurPresetRepository.getAllByType(any()) } returns emptyList()
    // Falls back to reminder.toReminderV2() when there's no V2 row (which is what happens by
    // default here); its output is non-deterministic when the V1 fixture has no explicit
    // startTime (falls back to LocalDateTime.now()), so reminderToBiDecomposer stubs below match
    // on any() rather than an exact value.
    coEvery { getReminderV2ByIdUseCase(any()) } returns null
    every { builderItemsLogic.getUsed() } returns emptyList()
    every { builderItemsLogic.getAvailable() } returns listOf(summaryItem())
    // updateSelector() -> updateBuilderState() runs after almost every action; default it to a
    // harmless success so individual tests only need to override it when they care.
    every { permissionValidator(any()) } returns PermissionValidator.Result.Success
    every { biToReminderAdapter(any(), any(), any()) } returns
      BiToReminderAdapter.BuildResult.Success(reminderV2Fixture())
  }

  private fun reminderV2Fixture(uuId: String = "v2") =
    ReminderV2(uuId = uuId, schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))

  private fun summaryItem(title: String = "s") = SummaryBuilderItem(title = title, description = null)

  private fun groupItem() =
    GroupBuilderItem(title = "g", description = null, groups = emptyList(), defaultGroup = null)

  private fun createViewModel(
    initialId: String = "",
    fromIntentItem: Boolean = false,
    deepLinkDateTimeType: BuildReminderNavKey.Main.DateTimeType? = null,
    deepLinkDateTimeMillis: Long? = null,
    deepLinkTodo: Boolean = false,
    deepLinkText: String? = null,
  ): BuildReminderViewModel =
    BuildReminderViewModel(
      navKey = BuildReminderNavKey.Main(
        id = initialId,
        fromIntentItem = fromIntentItem,
        deepLinkDateTimeType = deepLinkDateTimeType,
        deepLinkDateTimeMillis = deepLinkDateTimeMillis,
        deepLinkTodo = deepLinkTodo,
        deepLinkText = deepLinkText,
      ),
      dispatcherProvider = mockDispatcherProvider(),
      placeRepository = placeRepository,
      analyticsEventSender = analyticsEventSender,
      reminderAnalyticsTracker = reminderAnalyticsTracker,
      biFactory = biFactory,
      builderItemsLogic = builderItemsLogic,
      selectorDialogDataHolder = selectorDialogDataHolder,
      uiBuilderItemsAdapter = uiBuilderItemsAdapter,
      uiSelectorItemsAdapter = uiSelectorItemsAdapter,
      biToReminderAdapter = biToReminderAdapter,
      permissionValidator = permissionValidator,
      reminderToBiDecomposer = reminderToBiDecomposer,
      getReminderV2ByIdUseCase = getReminderV2ByIdUseCase,
      biFilter = biFilter,
      uiPresetListAdapter = uiPresetListAdapter,
      recurPresetRepository = recurPresetRepository,
      iCalendarApi = iCalendarApi,
      recurParamsToBiAdapter = recurParamsToBiAdapter,
      builderPresetToBiAdapter = builderPresetToBiAdapter,
      reminderPredictionCalculator = reminderPredictionCalculator,
      appWidgetUpdater = appWidgetUpdater,
      builderItemsToBuilderPresetAdapter = builderItemsToBuilderPresetAdapter,
      dateTimeManager = dateTimeManager,
      intentDataReader = intentDataReader,
      builderErrorFinder = builderErrorFinder,
      builderErrorToTextAdapter = builderErrorToTextAdapter,
      prefs = prefs,
      deleteReminderUseCase = deleteReminderUseCase,
      moveReminderToArchiveUseCase = moveReminderToArchiveUseCase,
      scheduleBackgroundWorkUseCase = scheduleBackgroundWorkUseCase,
      activateReminderUseCase = activateReminderUseCase,
      pauseReminderUseCase = pauseReminderUseCase,
      resumeReminderUseCase = resumeReminderUseCase,
      textProvider = textProvider,
      featureFlags = featureFlags,
      buildInfo = buildInfo,
      quickStartItemsProvider = quickStartItemsProvider,
      tagRepository = tagRepository,
      tagAssignmentRepository = tagAssignmentRepository,
      toggleTagAssignmentUseCase = toggleTagAssignmentUseCase,
      tagChipStateAdapter = tagChipStateAdapter,
      findGroupUseCase = findGroupUseCase,
    )

  @Test
  fun `init loads is24HourFormat from prefs`() {
    every { prefs.is24HourFormat } returns false

    val viewModel = createViewModel()

    assertEquals(false, viewModel.state.value.is24HourFormat)
  }

  @Test
  fun `init starts analytics tracking and initializes the available builder items`() {
    createViewModel()

    verify(exactly = 1) { reminderAnalyticsTracker.startTracking() }
    verify(exactly = 1) { builderItemsLogic.setAllAvailable(any()) }
  }

  @Test
  fun `init loads presets and recur presets into the selector data holder`() {
    val preset = mockk<UiPresetList>(relaxed = true)
    coEvery { recurPresetRepository.getAllByType(PresetType.BUILDER) } returns
      listOf(mockk(relaxed = true))
    every { uiPresetListAdapter.create(any()) } returns preset

    createViewModel()

    assertEquals(listOf(preset), selectorDialogDataHolder.presets)
  }

  @Test
  fun `init with an id deep link edits the existing reminder`() {
    val reminder = reminderV2Fixture(uuId = "42")
    coEvery { getReminderV2ByIdUseCase("42") } returns reminder
    coEvery { reminderToBiDecomposer(any()) } returns listOf(summaryItem())

    val viewModel = createViewModel(initialId = "42")

    assertEquals(false, viewModel.state.value.isRemoved)
    assertEquals(true, viewModel.state.value.canRemove)
    verify { builderItemsLogic.setAll(listOf(summaryItem())) }
    coVerify(exactly = 1) { pauseReminderUseCase(match { it.uuId == "42" }) }
  }

  @Test
  fun `init with an unknown id deep link leaves state at its defaults`() {
    coEvery { getReminderV2ByIdUseCase("missing") } returns null

    val viewModel = createViewModel(initialId = "missing")

    assertEquals(false, viewModel.state.value.canRemove)
    coVerify(exactly = 0) { pauseReminderUseCase(any()) }
  }

  @Test
  fun `onReportAnIssueClicked posts ShowReviewDialog with the report-an-issue title`() {
    every { textProvider.getString(R.string.report_an_issue) } returns "Report an issue"
    every { buildInfo.isPro } returns true
    val viewModel = createViewModel()

    viewModel.onReportAnIssueClicked()

    val event = viewModel.event.value?.peekContent()
    assertTrue(event is BuildReminderViewModel.ViewModelEvent.ShowReviewDialog)
    assertEquals("Report an issue", (event as BuildReminderViewModel.ViewModelEvent.ShowReviewDialog).title)
  }

  @Test
  fun `onConfigurationChanged refreshes the available and used builder item lists`() =
    runTest {
      val viewModel = createViewModel()

      viewModel.onConfigurationChanged()

      verify(atLeast = 2) { builderItemsLogic.setAllAvailable(any()) }
      verify(atLeast = 1) { builderItemsLogic.setAll(any()) }
    }

  @Test
  fun `onSaveAsPresetChange updates the checked flag`() {
    val viewModel = createViewModel()

    viewModel.onSaveAsPresetChange(true)

    assertEquals(true, viewModel.state.value.saveAsPresetChecked)
  }

  @Test
  fun `onPresetNameChange updates the preset name`() {
    val viewModel = createViewModel()

    viewModel.onPresetNameChange("My preset")

    assertEquals("My preset", viewModel.state.value.presetName)
  }

  @Test
  fun `onEditDialogDismissed clears the editing item`() {
    val viewModel = createViewModel()
    viewModel.onItemEditedClicked(0, summaryItem())

    viewModel.onEditDialogDismissed()

    assertNull(viewModel.state.value.editingItem)
  }

  @Test
  fun `saveReminder does not save and shows a message when a builder item is invalid`() =
    runTest {
      val invalid = mockk<BuilderItem<Any>>(relaxed = true)
      every { invalid.modifier.isCorrect() } returns false
      every { builderItemsLogic.getUsed() } returns listOf(invalid)
      val viewModel = createViewModel()

      viewModel.saveReminder(newId = false)

      coVerify(exactly = 0) { activateReminderUseCase(any(), any()) }
      assertEquals(
        BuildReminderViewModel.ViewModelEvent.ShowMessage(R.string.builder_error_create_reminder),
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `saveReminder asks for permissions when validation fails`() =
    runTest {
      every { permissionValidator(any()) } returns PermissionValidator.Result.Failure(listOf("perm.X"))
      val viewModel = createViewModel()

      viewModel.saveReminder(newId = false)

      coVerify(exactly = 0) { activateReminderUseCase(any(), any()) }
      assertEquals(
        BuildReminderViewModel.ViewModelEvent.AskPermissions(listOf("perm.X")),
        viewModel.event.value?.peekContent(),
      )
    }

  @Test
  fun `saveReminder does not add a group item when the builder list has none`() =
    runTest {
      every { builderItemsLogic.getUsed() } returns listOf(summaryItem())
      every { builderItemsLogic.getAvailable() } returns listOf(groupItem())
      val viewModel = createViewModel()

      viewModel.saveReminder(newId = false)

      verify {
        biToReminderAdapter(any(), match { items -> items.none { it is GroupBuilderItem } }, any())
      }
    }

  @Test
  fun `saveReminder activates the reminder and posts MoveBack on success`() =
    runTest {
      val built = reminderV2Fixture(uuId = "new")
      every { biToReminderAdapter(any(), any(), any()) } returns
        BiToReminderAdapter.BuildResult.Success(built)
      val viewModel = createViewModel()

      viewModel.saveReminder(newId = false)

      coVerify(exactly = 1) { activateReminderUseCase(match { it.uuId == "new" }, startAnyway = true) }
      assertEquals(BuildReminderViewModel.ViewModelEvent.MoveBack, viewModel.event.value?.peekContent())
    }

  @Test
  fun `saveReminder with newId regenerates the uuId instead of reusing the built one`() =
    runTest {
      val built = reminderV2Fixture(uuId = "duplicate-source-id")
      every { biToReminderAdapter(any(), any(), any()) } returns
        BiToReminderAdapter.BuildResult.Success(built)
      val viewModel = createViewModel()

      viewModel.saveReminder(newId = true)

      coVerify(exactly = 1) {
        activateReminderUseCase(match { it.uuId.isNotEmpty() && it.uuId != "duplicate-source-id" }, startAnyway = true)
      }
    }

  @Test
  fun `saveReminder without newId keeps the built uuId`() =
    runTest {
      val built = reminderV2Fixture(uuId = "kept-id")
      every { biToReminderAdapter(any(), any(), any()) } returns
        BiToReminderAdapter.BuildResult.Success(built)
      val viewModel = createViewModel()

      viewModel.saveReminder(newId = false)

      coVerify(exactly = 1) { activateReminderUseCase(match { it.uuId == "kept-id" }, startAnyway = true) }
    }

  @Test
  fun `saveReminder saves a preset when the checkbox is checked and named`() =
    runTest {
      val viewModel = createViewModel()
      viewModel.onSaveAsPresetChange(true)
      viewModel.onPresetNameChange("My preset")

      viewModel.saveReminder(newId = false)

      coVerify(exactly = 1) { recurPresetRepository.save(match { it.name == "My preset" }) }
      coVerify(exactly = 1) { scheduleBackgroundWorkUseCase(any(), any(), any(), any()) }
      verify { analyticsEventSender.send(PresetUsed(PresetAction.CREATE)) }
    }

  @Test
  fun `saveReminder does not save a preset when the checkbox is unchecked`() =
    runTest {
      val viewModel = createViewModel()
      viewModel.onPresetNameChange("My preset")

      viewModel.saveReminder(newId = false)

      coVerify(exactly = 0) { recurPresetRepository.save(any()) }
    }

  @Test
  fun `saveReminder does not emit an event when the build result is an error`() =
    runTest {
      every { biToReminderAdapter(any(), any(), any()) } returns
        BiToReminderAdapter.BuildResult.Error("bad state")
      val viewModel = createViewModel()

      viewModel.saveReminder(newId = false)

      coVerify(exactly = 0) { activateReminderUseCase(any(), any()) }
      assertNull(viewModel.event.value?.peekContent())
    }

  @Test
  fun `saveReminder shows the review dialog after the fourth created reminder`() =
    runTest {
      every { prefs.reviewDialogShown } returns false
      every { prefs.remindersCreatedCount } returns 3
      val viewModel = createViewModel()

      viewModel.saveReminder(newId = false)

      verify { prefs.remindersCreatedCount = 4 }
      verify { prefs.reviewDialogShown = true }
      verify { textProvider.getString(R.string.share_your_experience) }
    }

  @Test
  fun `saveReminder does not show the review dialog before the fourth reminder`() =
    runTest {
      every { prefs.reviewDialogShown } returns false
      every { prefs.remindersCreatedCount } returns 1
      val viewModel = createViewModel()

      viewModel.saveReminder(newId = false)

      verify(exactly = 0) { prefs.reviewDialogShown = true }
    }

  @Test
  fun `onPermissionsGranted retries saving with the previously requested newId flag`() =
    runTest {
      every { permissionValidator(any()) } returns PermissionValidator.Result.Failure(listOf("perm.X"))
      val viewModel = createViewModel()
      viewModel.saveReminder(newId = true)
      every { permissionValidator(any()) } returns PermissionValidator.Result.Success

      viewModel.onPermissionsGranted()

      coVerify(exactly = 1) { activateReminderUseCase(any(), any()) }
    }

  @Test
  fun `onPresetSelected with a builder preset applies it and sends analytics`() =
    runTest {
      val preset =
        RecurPreset(
          recurObject = "",
          name = "Preset",
          type = PresetType.BUILDER,
          createdAt = LocalDateTime.now(),
          useCount = 1,
          description = null,
          recurItemsToAdd = null,
        )
      coEvery { recurPresetRepository.getById("p1") } returns preset
      coEvery { builderPresetToBiAdapter(preset) } returns listOf(summaryItem())
      val viewModel = createViewModel()

      viewModel.onPresetSelected(mockk<UiPresetList>(relaxed = true).also { every { it.id } returns "p1" })

      verify { builderItemsLogic.setAll(listOf(summaryItem())) }
      verify { analyticsEventSender.send(PresetUsed(PresetAction.USE_BUILDER)) }
    }

  @Test
  fun `onPresetSelected does nothing when the preset id is not found`() =
    runTest {
      coEvery { recurPresetRepository.getById("missing") } returns null
      val viewModel = createViewModel()

      viewModel.onPresetSelected(mockk<UiPresetList>(relaxed = true).also { every { it.id } returns "missing" })

      verify(exactly = 0) { analyticsEventSender.send(any<PresetUsed>()) }
    }

  @Test
  fun `onQuickStartSelected populates the builder list with the option's items`() =
    runTest {
      val items = listOf(summaryItem(), groupItem())
      coEvery { quickStartItemsProvider.itemsFor(QuickStartOption.EVERY_WEEKDAY) } returns items
      val viewModel = createViewModel()

      viewModel.onQuickStartSelected(QuickStartOption.EVERY_WEEKDAY)

      verify { builderItemsLogic.setAll(items) }
    }

  @Test
  fun `onQuickStartSelected replaces whatever was previously in the builder list`() =
    runTest {
      coEvery { quickStartItemsProvider.itemsFor(QuickStartOption.ONE_TIME) } returns listOf(summaryItem())
      coEvery { quickStartItemsProvider.itemsFor(QuickStartOption.SHOPPING_LIST) } returns listOf(groupItem())
      val viewModel = createViewModel()

      viewModel.onQuickStartSelected(QuickStartOption.ONE_TIME)
      viewModel.onQuickStartSelected(QuickStartOption.SHOPPING_LIST)

      verify { builderItemsLogic.setAll(listOf(groupItem())) }
    }

  @Test
  fun `onItemEditedClicked without permission constraints opens the editor directly`() {
    val item = summaryItem()
    val viewModel = createViewModel()

    viewModel.onItemEditedClicked(0, item)

    assertEquals(0 to item, viewModel.state.value.editingItem)
  }

  @Test
  fun `onItemEditedClicked with a granted permission constraint opens the editor directly`() {
    val item = mockk<BuilderItem<Any>>(relaxed = true)
    every { item.constraints } returns listOf(mockk<PermissionConstraint>(relaxed = true))
    every { permissionValidator(listOf(item)) } returns PermissionValidator.Result.Success
    val viewModel = createViewModel()

    viewModel.onItemEditedClicked(1, item)

    assertEquals(1 to item, viewModel.state.value.editingItem)
  }

  @Test
  fun `onItemEditedClicked with a denied permission constraint asks for edit permissions instead`() {
    val item = mockk<BuilderItem<Any>>(relaxed = true)
    every { item.constraints } returns listOf(mockk<PermissionConstraint>(relaxed = true))
    every { permissionValidator(listOf(item)) } returns PermissionValidator.Result.Failure(listOf("perm.Y"))
    val viewModel = createViewModel()

    viewModel.onItemEditedClicked(1, item)

    assertNull(viewModel.state.value.editingItem)
    assertEquals(
      BuildReminderViewModel.ViewModelEvent.AskEditPermissions(listOf("perm.Y")),
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onEditPermissionsGranted retries the pending item edit`() =
    runTest {
      val item = mockk<BuilderItem<Any>>(relaxed = true)
      every { item.constraints } returns listOf(mockk<PermissionConstraint>(relaxed = true))
      every { permissionValidator(listOf(item)) } returns PermissionValidator.Result.Failure(listOf("perm.Y"))
      val viewModel = createViewModel()
      viewModel.onItemEditedClicked(2, item)
      every { permissionValidator(listOf(item)) } returns PermissionValidator.Result.Success

      viewModel.onEditPermissionsGranted()

      assertEquals(2 to item, viewModel.state.value.editingItem)
    }

  @Test
  fun `addItem adds the item, opens its editor, and refreshes the selector`() =
    runTest {
      val item = summaryItem()
      every { builderItemsLogic.getUsed() } returns listOf(item)
      val viewModel = createViewModel()

      viewModel.addItem(item)

      verify(exactly = 1) { builderItemsLogic.addNew(item) }
      assertEquals(0 to item, viewModel.state.value.editingItem)
    }

  @Test
  fun `removeItem clears, updates, and removes the item from the builder`() =
    runTest {
      val item = summaryItem()
      val viewModel = createViewModel()

      viewModel.removeItem(3, item)

      verify(exactly = 1) { builderItemsLogic.update(3, item) }
      verify(exactly = 1) { builderItemsLogic.remove(3) }
    }

  @Test
  fun `updateValue updates the item in the builder`() =
    runTest {
      val item = summaryItem()
      val viewModel = createViewModel()

      viewModel.updateValue(4, item)

      verify(exactly = 1) { builderItemsLogic.update(4, item) }
    }

  @Test
  fun `updateValue marks canSave false and shows a failed prediction when the builder becomes invalid`() =
    runTest {
      val invalid = mockk<BuilderItem<Any>>(relaxed = true)
      every { invalid.modifier.isCorrect() } returns false
      val viewModel = createViewModel()
      // Seed a prior success so a stale value would otherwise still read as valid.
      primeCanSaveTrue(viewModel)
      every { builderItemsLogic.getUsed() } returns listOf(invalid)

      viewModel.updateValue(0, invalid)

      assertEquals(false, viewModel.state.value.canSave)
      assertEquals(false, viewModel.state.value.canSaveAsPreset)
      assertTrue(viewModel.state.value.prediction is ReminderPrediction.FailedPrediction)
    }

  @Test
  fun `updateValue marks canSave false and shows a permissions message when a permission is missing`() =
    runTest {
      val item = summaryItem()
      every { builderItemsLogic.getUsed() } returns listOf(item)
      every { permissionValidator(any()) } returns PermissionValidator.Result.Failure(listOf("perm.X"))
      val viewModel = createViewModel()

      viewModel.updateValue(0, item)

      assertEquals(false, viewModel.state.value.canSave)
      assertTrue(viewModel.state.value.prediction is ReminderPrediction.FailedPrediction)
    }

  /** Drives one successful [BuildReminderViewModel.updateValue] round-trip so `canSave` starts
   *  true, matching what a previously-valid builder would look like before a field breaks it. */
  private fun primeCanSaveTrue(viewModel: BuildReminderViewModel) {
    val valid = summaryItem()
    every { builderItemsLogic.getUsed() } returns listOf(valid)
    viewModel.updateValue(0, valid)
    assertEquals(true, viewModel.state.value.canSave)
  }

  @Test
  fun `moveToTrash posts MoveBack immediately when there is no original reminder`() {
    val viewModel = createViewModel()

    viewModel.moveToTrash()

    assertEquals(BuildReminderViewModel.ViewModelEvent.MoveBack, viewModel.event.value?.peekContent())
    coVerify(exactly = 0) { moveReminderToArchiveUseCase(any()) }
  }

  @Test
  fun `moveToTrash archives the original reminder when one was loaded for edit`() =
    runTest {
      val reminder = reminderV2Fixture(uuId = "42")
      coEvery { getReminderV2ByIdUseCase("42") } returns reminder
      coEvery { reminderToBiDecomposer(any()) } returns listOf(summaryItem())
      val viewModel = createViewModel(initialId = "42")

      viewModel.moveToTrash()

      coVerify(exactly = 1) { moveReminderToArchiveUseCase("42") }
      assertEquals(BuildReminderViewModel.ViewModelEvent.MoveBack, viewModel.event.value?.peekContent())
    }

  @Test
  fun `deleteReminder posts MoveBack immediately when there is no original reminder`() {
    val viewModel = createViewModel()

    viewModel.deleteReminder(showMessage = true)

    assertEquals(BuildReminderViewModel.ViewModelEvent.MoveBack, viewModel.event.value?.peekContent())
    coVerify(exactly = 0) { deleteReminderUseCase(any()) }
  }

  @Test
  fun `deleteReminder deletes and posts MoveBack when showMessage is true`() =
    runTest {
      val reminder = reminderV2Fixture(uuId = "42")
      coEvery { getReminderV2ByIdUseCase("42") } returns reminder
      coEvery { reminderToBiDecomposer(any()) } returns listOf(summaryItem())
      val viewModel = createViewModel(initialId = "42")

      viewModel.deleteReminder(showMessage = true)

      coVerify(exactly = 1) { deleteReminderUseCase(reminder) }
      assertEquals(BuildReminderViewModel.ViewModelEvent.MoveBack, viewModel.event.value?.peekContent())
    }

  @Test
  fun `deleteReminder does not emit an event when showMessage is false`() =
    runTest {
      val reminder = reminderV2Fixture(uuId = "42")
      coEvery { getReminderV2ByIdUseCase("42") } returns reminder
      coEvery { reminderToBiDecomposer(any()) } returns listOf(summaryItem())
      val viewModel = createViewModel(initialId = "42")

      viewModel.deleteReminder(showMessage = false)

      coVerify(exactly = 1) { deleteReminderUseCase(reminder) }
      assertNull(viewModel.event.value?.peekContent())
    }

  @Test
  fun `onCleared always refreshes widgets and clears the selector items`() {
    val viewModel = createViewModel()
    selectorDialogDataHolder.selectorBuilderItems = listOf(mockk(relaxed = true))
    val store = ViewModelStore()
    store.put("build", viewModel)

    store.clear()

    verify(exactly = 1) { appWidgetUpdater.updateAllWidgets() }
    verify(exactly = 1) { appWidgetUpdater.updateCalendarWidget() }
    assertEquals(emptyList<Any>(), selectorDialogDataHolder.selectorBuilderItems)
  }

  @Test
  fun `onCleared resumes the reminder when it was paused and not saving`() {
    val reminder = reminderV2Fixture(uuId = "42")
    coEvery { getReminderV2ByIdUseCase("42") } returns reminder
    coEvery { reminderToBiDecomposer(any()) } returns listOf(summaryItem())
    val viewModel = createViewModel(initialId = "42")
    val store = ViewModelStore()
    store.put("build", viewModel)

    store.clear()

    coVerify(exactly = 1) { resumeReminderUseCase(match { it.uuId == "42" }) }
  }

  @Test
  fun `onCleared does not resume the reminder while a save is in flight`() =
    runTest {
      val reminder = reminderV2Fixture(uuId = "42")
      coEvery { getReminderV2ByIdUseCase("42") } returns reminder
      coEvery { reminderToBiDecomposer(any()) } returns listOf(summaryItem())
      every { biToReminderAdapter(any(), any(), any()) } returns
        BiToReminderAdapter.BuildResult.Success(reminderV2Fixture(uuId = "42"))
      val viewModel = createViewModel(initialId = "42")
      viewModel.saveReminder(newId = false)
      val store = ViewModelStore()
      store.put("build", viewModel)

      store.clear()

      coVerify(exactly = 0) { resumeReminderUseCase(any()) }
    }

  @Test
  fun `onTagToggle attaches an unselected tag and schedules an upload of the tag assignments snapshot`() =
    runTest {
      coEvery { toggleTagAssignmentUseCase.invoke(any(), any(), any(), any()) } returns Unit
      val viewModel = createViewModel()

      viewModel.onTagToggle(TagChipState(id = "tag-1", name = "Work", color = Color.Unspecified))

      coVerify { toggleTagAssignmentUseCase.invoke(any(), any(), "tag-1", false) }
    }
}
