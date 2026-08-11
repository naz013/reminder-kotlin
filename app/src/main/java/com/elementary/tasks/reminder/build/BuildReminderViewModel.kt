package com.elementary.tasks.reminder.build

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.data.adapter.preset.UiPresetListAdapter
import com.elementary.tasks.core.data.ui.preset.UiPresetList
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.module.analytics.ReminderAnalyticsTracker
import com.elementary.tasks.reminder.build.adapter.BuilderErrorToTextAdapter
import com.elementary.tasks.reminder.build.bi.BiComparator
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
import com.elementary.tasks.reminder.IsSimpleTodoReminderUseCase
import com.elementary.tasks.reminder.build.selectordialog.SelectorDialogDataHolder
import com.elementary.tasks.reminder.scheduling.usecase.ResumeReminderUseCase
import com.elementary.tasks.reminder.todo.TodoSeedHolder
import com.elementary.tasks.reminder.usecase.MoveReminderToArchiveUseCase
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.AnalyticsReminderType
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.analytics.PresetAction
import com.github.naz013.analytics.PresetUsed
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.TextProvider
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.PresetType
import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.Tag
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.files.DataType
import com.github.naz013.icalendar.ICalendarApi
import com.github.naz013.icalendar.RecurParamType
import com.github.naz013.icalendar.RecurrenceRuleTag
import com.github.naz013.icalendar.TagType
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
import com.github.naz013.logic.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.logic.reminder.usecase.PauseReminderUseCase
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.logic.tag.ToggleTagAssignmentUseCase
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.RecurPresetRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.reviews.AppSource
import com.github.naz013.ui.tag.TagChipState
import com.github.naz013.ui.tag.TagChipStateAdapter
import com.github.naz013.usecase.reminders.GetReminderV2ByIdUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

class BuildReminderViewModel(
  private val navKey: BuildReminderNavKey.Main,
  private val dispatcherProvider: DispatcherProvider,
  private val placeRepository: PlaceRepository,
  private val analyticsEventSender: AnalyticsEventSender,
  private val reminderAnalyticsTracker: ReminderAnalyticsTracker,
  private val biFactory: BiFactory,
  private val builderItemsLogic: BuilderItemsLogic,
  private val selectorDialogDataHolder: SelectorDialogDataHolder,
  private val uiBuilderItemsAdapter: UiBuilderItemsAdapter,
  private val uiSelectorItemsAdapter: UiSelectorItemsAdapter,
  private val biToReminderAdapter: BiToReminderAdapter,
  private val permissionValidator: PermissionValidator,
  private val reminderToBiDecomposer: ReminderToBiDecomposer,
  private val getReminderV2ByIdUseCase: GetReminderV2ByIdUseCase,
  private val biFilter: BiFilter,
  private val uiPresetListAdapter: UiPresetListAdapter,
  private val recurPresetRepository: RecurPresetRepository,
  private val iCalendarApi: ICalendarApi,
  private val recurParamsToBiAdapter: RecurParamsToBiAdapter,
  private val builderPresetToBiAdapter: BuilderPresetToBiAdapter,
  private val reminderPredictionCalculator: ReminderPredictionCalculator,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val builderItemsToBuilderPresetAdapter: BuilderItemsToBuilderPresetAdapter,
  private val dateTimeManager: DateTimeManager,
  private val intentDataReader: IntentDataReader,
  private val builderErrorFinder: BuilderErrorFinder,
  private val builderErrorToTextAdapter: BuilderErrorToTextAdapter,
  private val prefs: Prefs,
  private val deleteReminderUseCase: DeleteReminderUseCase,
  private val moveReminderToArchiveUseCase: MoveReminderToArchiveUseCase,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
  private val activateReminderUseCase: ActivateReminderUseCase,
  private val pauseReminderUseCase: PauseReminderUseCase,
  private val resumeReminderUseCase: ResumeReminderUseCase,
  private val textProvider: TextProvider,
  private val featureFlags: FeatureFlags,
  private val buildInfo: BuildInfo,
  private val quickStartItemsProvider: QuickStartItemsProvider,
  private val tagRepository: TagRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val toggleTagAssignmentUseCase: ToggleTagAssignmentUseCase,
  private val tagChipStateAdapter: TagChipStateAdapter,
  private val findGroupUseCase: FindGroupUseCase,
  private val todoSeedHolder: TodoSeedHolder,
  private val isSimpleTodoReminderUseCase: IsSimpleTodoReminderUseCase,
) : ViewModel() {

  val initialId = navKey.id
  val id: String = initialId

  /** Stable for the whole editing session, unlike [newBlankReminderV2]'s old behavior of
   *  generating a fresh [ReminderV2.uuId] on every call - tags need one fixed id to attach to
   *  from the very first frame, well before the reminder is actually saved. */
  private val stableReminderId: String = initialId.ifEmpty { UUID.randomUUID().toString() }

  /** True only when this session will go through [editReminderIfNeeded] - i.e. none of the
   *  other deep-link branches in [handleDeepLink] apply and an id was given. Computed eagerly
   *  (not inside handleDeepLink()'s async launch) so the very first frame already knows to show
   *  a loading state instead of briefly flashing the empty-state illustration while
   *  editReminderIfNeeded asynchronously decides whether to redirect to the Todo screen. */
  private val isEditingById: Boolean =
    initialId.isNotEmpty() &&
      !navKey.fromIntentItem &&
      !(navKey.deepLinkDateTimeType != null && navKey.deepLinkDateTimeMillis != null) &&
      !navKey.deepLinkTodo &&
      !navKey.seedFromTodoEdit &&
      navKey.deepLinkText == null &&
      navKey.groupUuId == null

  private val _state = MutableStateFlow(BuildReminderState(isLoadingForEdit = isEditingById))
  val state: StateFlow<BuildReminderState> = _state.asStateFlow()

  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  private var isEdited: Boolean = false
  private var isPaused: Boolean = false
  private var isSaving: Boolean = false
  private var originalV2: ReminderV2? = null
  private var isFromFile: Boolean = false

  private var requestedNewId = false
  private var requestedPermissionsFor: Pair<Int, BuilderItem<*>>? = null

  /** For [resumeReminder], which onCleared() calls after AndroidX has already cancelled
   *  [viewModelScope]'s Job as part of clearing this ViewModel - launching there would create a
   *  coroutine that never runs its body, silently leaving a paused reminder inactive forever. */
  private val cleanupScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

  init {
    _state.update {
      it.copy(
        is24HourFormat = prefs.is24HourFormat,
        hapticFeedbackEnabled = prefs.hapticsEnabled,
      )
    }
    reminderAnalyticsTracker.startTracking()
    initBuilder()
    loadPresets()
    handleDeepLink()
    observeTags()
  }

  private fun observeTags() {
    viewModelScope.launch(dispatcherProvider.default()) {
      tagRepository.observeAll()
        .map { tags ->
          tags.map { tagChipStateAdapter(it) }
        }
        .collect { tags ->
          _state.update { it.copy(allTags = tags) }
        }
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      tagAssignmentRepository.observeTagsForItem(stableReminderId, TaggedItemType.REMINDER).collect { tags ->
        _state.update { it.copy(selectedTagIds = tags.map(Tag::id).toSet()) }
      }
    }
  }

  fun onTagToggle(tag: TagChipState) {
    val isSelected = tag.id in _state.value.selectedTagIds
    viewModelScope.launch(dispatcherProvider.io()) {
      toggleTagAssignmentUseCase(stableReminderId, TaggedItemType.REMINDER, tag.id, isSelected)
    }
  }

  fun onManageTagsClick() {
    event.emit(ViewModelEvent.OpenManageTags)
  }

  override fun onCleared() {
    super.onCleared()
    Logger.i(TAG, "View model cleared")
    selectorDialogDataHolder.selectorBuilderItems = emptyList()
    todoSeedHolder.pendingSeed = null
    if (isPaused && !isSaving) {
      originalV2?.let { resumeReminder(it) }
    }
    appWidgetUpdater.updateAllWidgets()
    appWidgetUpdater.updateCalendarWidget()
  }

  fun onReportAnIssueClicked() {
    askReview(R.string.report_an_issue)
  }

  fun onConfigurationChanged() {
    Logger.d(TAG, "On configuration changed")
    viewModelScope.launch(dispatcherProvider.default()) {
      val used = builderItemsLogic.getUsed()

      val allTypes =
        BiType.entries
          .map { biFactory.create(it) }
          .filter { biFilter(it) }
          .sortedWith(BiComparator())

      builderItemsLogic.setAllAvailable(allTypes)
      builderItemsLogic.setAll(used.filter { biFilter(it) })

      updateSelector()
    }
  }

  fun onPermissionsGranted() {
    Logger.i(TAG, "Permissions granted")
    saveReminder(requestedNewId)
  }

  fun onSaveAsPresetChange(checked: Boolean) {
    _state.update { it.copy(saveAsPresetChecked = checked) }
  }

  fun onPresetNameChange(name: String) {
    _state.update { it.copy(presetName = name) }
  }

  fun onEditDialogDismissed() {
    _state.update { it.copy(editingItem = null) }
  }

  fun saveReminder(newId: Boolean) {
    Logger.i(TAG, "Start reminder saving, use new ID = $newId")
    viewModelScope.launch(dispatcherProvider.default()) {
      val builderItems = builderItemsLogic.getUsed().toMutableList()
      Logger.d(TAG, "saveReminder: builderItems=$builderItems")
      Logger.i(TAG, "Number of builder items = ${builderItems.size}")

      val allValid = builderItems.all { it.modifier.isCorrect() }
      Logger.i(TAG, "Are all builder items valid = $allValid")

      if (!allValid) {
        withContext(dispatcherProvider.main()) {
          event.emit(ViewModelEvent.ShowMessage(R.string.builder_error_create_reminder))
        }
        return@launch
      }

      val permissionResult = permissionValidator(builderItems)
      if (permissionResult is PermissionValidator.Result.Failure) {
        Logger.i(TAG, "Not all permissions granted. Request for = ${permissionResult.permissions}")
        requestedNewId = newId
        withContext(dispatcherProvider.main()) {
          event.emit(ViewModelEvent.AskPermissions(permissionResult.permissions))
        }
        return@launch
      }

      Logger.i(TAG, "All permissions granted")

      val baseV2 = originalV2 ?: newBlankReminderV2()
      when (val buildResult = biToReminderAdapter(baseV2, builderItems, isEdited)) {
        is BiToReminderAdapter.BuildResult.Success -> {
          Logger.i(TAG, "Reminder build success")

          val finalV2 =
            if (newId) {
              buildResult.reminderV2.copy(uuId = UUID.randomUUID().toString())
            } else {
              buildResult.reminderV2
            }

          isSaving = true
          saveAndStartReminder(finalV2, isEdit = isEdited)

          if (_state.value.saveAsPresetChecked && _state.value.presetName.isNotEmpty()) {
            savePreset(builderItems)
          }

          withContext(dispatcherProvider.main()) {
            event.emit(ViewModelEvent.MoveBack)
          }
        }

        is BiToReminderAdapter.BuildResult.Error -> {
          Logger.i(TAG, "Reminder build failed with error = ${buildResult.error}")
        }
      }
    }
  }

  private fun handleDeepLink() {
    Logger.i(TAG, "Handle reminder Deep Link: $navKey")
    viewModelScope.launch(dispatcherProvider.default()) {
      when {
        navKey.fromIntentItem -> {
          Logger.i(TAG, "Handle reminder object Deep Link")
          readObjectFromIntent()
        }

        navKey.deepLinkDateTimeType != null && navKey.deepLinkDateTimeMillis != null -> {
          readDateTimeDeepLink(navKey.deepLinkDateTimeType, navKey.deepLinkDateTimeMillis)
        }

        navKey.deepLinkTodo -> readTodoDeepLink()

        navKey.seedFromTodoEdit -> readTodoEditSeed()

        navKey.deepLinkText != null -> readTextDeepLink(navKey.deepLinkText)

        navKey.groupUuId != null -> readGroupDeepLink(navKey.groupUuId)

        isEditingById -> {
          Logger.i(TAG, "Handle reminder ID Deep Link")
          editReminderIfNeeded(id)
        }
      }
    }
  }

  fun onPresetSelected(presetList: UiPresetList) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val preset = recurPresetRepository.getById(presetList.id) ?: return@launch

      Logger.i(TAG, "On preset selected: ${preset.name}, type = ${preset.type}")

      if (preset.type == PresetType.BUILDER) {
        useBuilderPreset(preset)
      } else {
        useRecurPreset(preset)
      }
    }
  }

  fun onEditPermissionsGranted() {
    Logger.i(TAG, "On builder item edit Permission granted")
    viewModelScope.launch(dispatcherProvider.default()) {
      updateSelector()
    }
    requestedPermissionsFor?.also { onItemEditedClicked(it.first, it.second) }
    requestedPermissionsFor = null
  }

  fun onItemEditedClicked(
    position: Int,
    builderItem: BuilderItem<*>,
  ) {
    Logger.i(TAG, "On builder item edit clicked, type = ${builderItem.biType}")
    val pair = position to builderItem
    val permissions = builderItem.constraints.filterIsInstance<PermissionConstraint>()
    if (permissions.isNotEmpty()) {
      val permissionResult = permissionValidator(listOf(builderItem))
      if (permissionResult is PermissionValidator.Result.Success) {
        _state.update { it.copy(editingItem = pair) }
      } else if (permissionResult is PermissionValidator.Result.Failure) {
        requestedPermissionsFor = pair
        viewModelScope.launch(dispatcherProvider.main()) {
          event.emit(ViewModelEvent.AskEditPermissions(permissionResult.permissions))
        }
      }
    } else {
      _state.update { it.copy(editingItem = pair) }
    }
  }

  fun onQuickStartSelected(option: QuickStartOption) {
    Logger.i(TAG, "Quick start option selected: $option")
    viewModelScope.launch(dispatcherProvider.default()) {
      builderItemsLogic.setAll(quickStartItemsProvider.itemsFor(option))
      updateSelector()
    }
  }

  fun addItem(builderItem: BuilderItem<*>) {
    Logger.i(TAG, "Add builder item, type = ${builderItem.biType}")
    viewModelScope.launch(dispatcherProvider.default()) {
      builderItemsLogic.addNew(builderItem)

      val position = builderItemsLogic.getUsed().size - 1
      onItemEditedClicked(position, builderItem)

      updateSelector()
    }
  }

  fun removeItem(
    position: Int,
    builderItem: BuilderItem<*>,
  ) {
    Logger.i(TAG, "Remove builder item, type = ${builderItem.biType}")
    viewModelScope.launch(dispatcherProvider.default()) {
      builderItem.modifier.setDefault()
      builderItemsLogic.update(position, builderItem)
      builderItemsLogic.remove(position)
      updateSelector()
    }
  }

  fun updateValue(
    position: Int,
    builderItem: BuilderItem<*>,
  ) {
    Logger.i(TAG, "Update VALUE for builder item, type = ${builderItem.biType}")
    viewModelScope.launch(dispatcherProvider.default()) {
      builderItemsLogic.update(position, builderItem)
      updateSelector()
    }
  }

  /** Applies a package name picked on [BuildReminderNavKey.SelectApplication] - a separate Nav3
   *  entry, so it can't reach the [ApplicationBuilderItem] being edited directly. The sheet is
   *  dismissed before navigating there (see [BuildReminderNavGraph]), so this looks the item up by
   *  [position] rather than relying on `editingItem` still being set. */
  fun onApplicationPicked(
    position: Int,
    packageName: String,
  ) {
    Logger.i(TAG, "Application picked for position = $position")
    viewModelScope.launch(dispatcherProvider.default()) {
      val item = builderItemsLogic.getUsed().getOrNull(position) as? ApplicationBuilderItem ?: return@launch
      item.modifier.update(packageName)
      builderItemsLogic.update(position, item)
      updateSelector()
    }
  }

  private suspend fun readDateTimeDeepLink(
    type: BuildReminderNavKey.Main.DateTimeType,
    millis: Long,
  ) {
    while (builderItemsLogic.getAvailable().isEmpty()) {
      delay(50.milliseconds)
    }
    if (type == BuildReminderNavKey.Main.DateTimeType.Date) {
      Logger.i(TAG, "Handle reminder date/time Deep Link")
      val dateTime = dateTimeManager.fromMillis(millis)
      addDateItemToBuilder(dateTime.toLocalDate())
      addTimeItemToBuilder(dateTime.toLocalTime())
      addEmptySummaryItemToBuilderIfNeeded()
      updateSelector()
    }
  }

  private suspend fun readTodoDeepLink() {
    while (builderItemsLogic.getAvailable().isEmpty()) {
      delay(50.milliseconds)
    }
    Logger.i(TAG, "Handle reminder todo Deep Link")
    addSubTasksItemToBuilder()
    addEmptySummaryItemToBuilderIfNeeded()
    updateSelector()
  }

  /** Seeds the builder from a [TodoSeedHolder.pendingSeed] left by TodoEditViewModel's Extend
   *  action. For a brand-new, not-yet-persisted todo ([navKey.isEditingExtend] false) this
   *  deliberately does not call [editReminder] - that would set [isEdited]/[originalV2]/
   *  `canRemove`, treating a reminder that was never saved as an existing DB row. [originalV2]
   *  staying null means [saveReminder] falls through to [newBlankReminderV2], which reuses
   *  [stableReminderId] - equal to [navKey]'s id here - so tags already attached from the Todo
   *  screen stay attached once this reminder is actually saved.
   *
   *  When extending a todo that was already being *edited* ([navKey.isEditingExtend] true), [seed]
   *  is a real, already-persisted reminder - TodoEditViewModel paused it on load and resumed it
   *  the moment its own screen was popped (before this one even mounts), so this must re-pause it
   *  here too, otherwise it sits live/unpaused for the rest of this builder session. */
  private suspend fun readTodoEditSeed() {
    while (builderItemsLogic.getAvailable().isEmpty()) {
      delay(50.milliseconds)
    }
    Logger.i(TAG, "Handle reminder todo edit seed")
    val seed = todoSeedHolder.pendingSeed ?: return
    todoSeedHolder.pendingSeed = null
    if (navKey.isEditingExtend) {
      isEdited = true
      originalV2 = seed
      _state.update { it.copy(canRemove = true, isRemoved = seed.isRemoved) }
      pauseReminder(seed)
    }
    val builderItems = reminderToBiDecomposer(seed)
    if (builderItems.isNotEmpty()) {
      builderItemsLogic.setAll(builderItems)
      updateSelector()
    }
  }

  private suspend fun readTextDeepLink(text: String) {
    while (builderItemsLogic.getAvailable().isEmpty()) {
      delay(50.milliseconds)
    }
    Logger.i(TAG, "Handle reminder text Deep Link")
    addSummaryItemToBuilder(text)
    updateSelector()
  }

  private suspend fun readGroupDeepLink(groupUuId: String) {
    while (builderItemsLogic.getAvailable().isEmpty()) {
      delay(50.milliseconds)
    }
    Logger.i(TAG, "Handle group deep link, $groupUuId")
    addGroupItemToBuilder(groupUuId)
    updateSelector()
  }

  private suspend fun addGroupItemToBuilder(groupUuId: String) {
    val group = withContext(dispatcherProvider.io()) {
      findGroupUseCase(groupUuId)
    } ?: return

    val itemIndex = builderItemsLogic.getUsed().indexOfFirst { it.biType == BiType.GROUP }

    Logger.i(TAG, "Add Group builder item")
    if (itemIndex == -1) {
      builderItemsLogic
        .getAvailable()
        .firstOrNull { it.biType == BiType.GROUP }
        ?.let { it as GroupBuilderItem }
        ?.apply { modifier.update(group) }
        ?.also { builderItemsLogic.addNew(it) }
    } else {
      val item = builderItemsLogic.getUsed()[itemIndex] as? GroupBuilderItem ?: return
      item.modifier.update(group)
      builderItemsLogic.update(itemIndex, item)
    }
  }

  private fun addDateItemToBuilder(date: LocalDate) {
    val itemIndex = builderItemsLogic.getUsed().indexOfFirst { it.biType == BiType.DATE }
    Logger.i(TAG, "Add Date builder item")
    if (itemIndex == -1) {
      builderItemsLogic
        .getAvailable()
        .firstOrNull { it.biType == BiType.DATE }
        ?.let { it as DateBuilderItem }
        ?.apply { modifier.update(date) }
        ?.also { builderItemsLogic.addNew(it) }
    } else {
      val item = builderItemsLogic.getUsed()[itemIndex] as? DateBuilderItem ?: return
      item.modifier.update(date)
      builderItemsLogic.update(itemIndex, item)
    }
  }

  private fun addTimeItemToBuilder(time: LocalTime) {
    val itemIndex = builderItemsLogic.getUsed().indexOfFirst { it.biType == BiType.TIME }
    Logger.i(TAG, "Add Time builder item")
    if (itemIndex == -1) {
      builderItemsLogic
        .getAvailable()
        .firstOrNull { it.biType == BiType.TIME }
        ?.let { it as TimeBuilderItem }
        ?.apply { modifier.update(time) }
        ?.also { builderItemsLogic.addNew(it) }
    } else {
      val item = builderItemsLogic.getUsed()[itemIndex] as? TimeBuilderItem ?: return
      item.modifier.update(time)
      builderItemsLogic.update(itemIndex, item)
    }
  }

  private suspend fun readObjectFromIntent() {
    while (builderItemsLogic.getAvailable().isEmpty()) {
      delay(50)
    }
    intentDataReader.get(IntentKeys.INTENT_ITEM, ReminderV2::class.java)?.run {
      Logger.logEvent("Reminder loaded from intent")
      isFromFile = true
      _state.update { it.copy(isFromFile = true) }
      editReminder(reminderV2 = this)
    }
  }

  private fun addSubTasksItemToBuilder() {
    val itemIndex = builderItemsLogic.getUsed().indexOfFirst { it.biType == BiType.SUB_TASKS }
    Logger.i(TAG, "Add Sub tasks builder item")
    if (itemIndex == -1) {
      builderItemsLogic
        .getAvailable()
        .firstOrNull { it.biType == BiType.SUB_TASKS }
        ?.let { it as SubTasksBuilderItem }
        ?.also { builderItemsLogic.addNew(it) }
    } else {
      val item = builderItemsLogic.getUsed()[itemIndex] as? SubTasksBuilderItem ?: return
      builderItemsLogic.update(itemIndex, item)
    }
  }

  private fun addEmptySummaryItemToBuilderIfNeeded() {
    val itemIndex = builderItemsLogic.getUsed().indexOfFirst { it.biType == BiType.SUMMARY }
    Logger.i(TAG, "Add Empty Summary builder item")
    if (itemIndex == -1) {
      builderItemsLogic
        .getAvailable()
        .firstOrNull { it.biType == BiType.SUMMARY }
        ?.let { it as SummaryBuilderItem }
        ?.apply { modifier.update("") }
        ?.also { builderItemsLogic.addNew(it) }
    } else {
      val item = builderItemsLogic.getUsed()[itemIndex] as? SummaryBuilderItem ?: return
      item.modifier.update("")
      builderItemsLogic.update(itemIndex, item)
    }
  }

  private fun addSummaryItemToBuilder(text: String) {
    val itemIndex = builderItemsLogic.getUsed().indexOfFirst { it.biType == BiType.SUMMARY }
    Logger.i(TAG, "Add Summary builder item")
    if (itemIndex == -1) {
      builderItemsLogic
        .getAvailable()
        .firstOrNull { it.biType == BiType.SUMMARY }
        ?.let { it as SummaryBuilderItem }
        ?.apply { modifier.update(text) }
        ?.also { builderItemsLogic.addNew(it) }
    } else {
      val item = builderItemsLogic.getUsed()[itemIndex] as? SummaryBuilderItem ?: return
      item.modifier.update(text)
      builderItemsLogic.update(itemIndex, item)
    }
  }

  private fun editReminderIfNeeded(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminderV2 = getReminderV2ByIdUseCase(id)
      if (reminderV2 == null) {
        _state.update { it.copy(isLoadingForEdit = false) }
        return@launch
      }

      if (isSimpleTodoReminderUseCase(reminderV2)) {
        Logger.i(TAG, "Reminder is a simple todo, redirecting to Todo edit screen, id = $id")
        withContext(dispatcherProvider.main()) {
          event.emit(ViewModelEvent.RedirectToTodoEdit(id))
        }
        return@launch
      }

      Logger.i(TAG, "Edit reminder by ID Deep Link, id = $id")

      editReminder(reminderV2 = reminderV2)
      pauseReminder(reminderV2)
    }
  }

  private suspend fun editReminder(reminderV2: ReminderV2) {
    Logger.i(TAG, "Edit reminder, id = ${reminderV2.uuId}")

    isEdited = true
    originalV2 = reminderV2

    if (isFromFile) {
      findSame(reminderV2.uuId)
    }

    val builderItems = reminderToBiDecomposer(reminderV2)

    Logger.d(TAG, "Edit reminder with builder items: $builderItems")

    _state.update {
      it.copy(
        canRemove = !isFromFile,
        isRemoved = reminderV2.isRemoved,
        isLoadingForEdit = false,
      )
    }

    if (builderItems.isNotEmpty()) {
      builderItemsLogic.setAll(builderItems)
      updateSelector()
    }
  }

  private suspend fun findSame(id: String) {
    val reminder = getReminderV2ByIdUseCase(id)
    _state.update { it.copy(hasSameInDb = reminder != null) }
    reminder?.also { pauseReminder(it) }
  }

  private suspend fun useBuilderPreset(preset: RecurPreset) {
    Logger.i(TAG, "Use reminder builder preset")
    val items = builderPresetToBiAdapter(preset)
    if (items.isNotEmpty()) {
      builderItemsLogic.setAll(items)
      analyticsEventSender.send(PresetUsed(PresetAction.USE_BUILDER))
      updateSelector()
    }
  }

  private suspend fun useRecurPreset(preset: RecurPreset) {
    Logger.i(TAG, "Use reminder RECUR preset")

    val recurObject = preset.recurObject

    val params =
      runCatching { iCalendarApi.parseObject(recurObject) }
        .getOrNull()
        ?.getTagOrNull<RecurrenceRuleTag>(TagType.RRULE)
        ?.params
        ?.let { recurParamsToBiAdapter(it) }
        ?: emptyList()

    if (params.isNotEmpty()) {
      val used =
        builderItemsLogic
          .getUsed()
          .mapIndexed { index, builderItem ->
            builderItem.biType to Pair(index, builderItem)
          }.toMap()

      val summaryBuilderItem = used[BiType.SUMMARY]?.second

      builderItemsLogic.setAll(params)

      summaryBuilderItem?.also { builderItemsLogic.addNew(it) }

      val usedItemsMap = builderItemsLogic.getUsed().associateBy { it.biType }

      if (preset.isDefault) {
        Logger.d(TAG, "Trying to add runtime params to builder: ${preset.recurItemsToAdd}")
        getRuntimeParams(preset.recurItemsToAdd).forEach { biType ->
          if (!usedItemsMap.containsKey(biType)) {
            biFactory.create(biType).also { builderItemsLogic.addNew(it) }
          }
        }
      }

      analyticsEventSender.send(PresetUsed(PresetAction.USE))
      updateSelector()
    }
  }

  private fun getRuntimeParams(paramsToAdd: String?): List<BiType> {
    val params = paramsToAdd?.split(";") ?: emptyList()
    val result = mutableListOf<BiType>()

    params.forEach {
      result.addAll(getTagTypeParams(runCatching { TagType.fromValue(it) }.getOrNull()))
      result.addAll(
        getRecurParamTypeParams(runCatching { RecurParamType.fromValue(it) }.getOrNull()),
      )
    }

    return result
  }

  private fun getRecurParamTypeParams(recurParamType: RecurParamType?): List<BiType> {
    val result = mutableListOf<BiType>()
    when (recurParamType) {
      RecurParamType.BYHOUR -> {
        result.add(BiType.ICAL_BYHOUR)
      }

      RecurParamType.BYMINUTE -> {
        result.add(BiType.ICAL_BYMINUTE)
      }

      RecurParamType.BYDAY -> {
        result.add(BiType.ICAL_BYDAY)
      }

      RecurParamType.BYMONTH -> {
        result.add(BiType.ICAL_BYMONTH)
      }

      else -> {}
    }
    return result
  }

  private fun getTagTypeParams(tagType: TagType?): List<BiType> {
    val result = mutableListOf<BiType>()
    when (tagType) {
      TagType.DTSTART -> {
        result.add(BiType.ICAL_START_DATE)
        result.add(BiType.ICAL_START_TIME)
      }

      TagType.DTEND -> {
        result.add(BiType.ICAL_UNTIL_DATE)
        result.add(BiType.ICAL_UNTIL_TIME)
      }

      else -> {}
    }
    return result
  }

  private fun loadPresets() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val recurPresets =
        recurPresetRepository
          .getAllByType(presetType = PresetType.RECUR)
          .map { uiPresetListAdapter.create(it) }
      val presets =
        recurPresetRepository
          .getAllByType(presetType = PresetType.BUILDER)
          .map { uiPresetListAdapter.create(it) }

      withUIContext {
        selectorDialogDataHolder.presets = presets
        selectorDialogDataHolder.recurPresets = recurPresets
      }
    }
  }

  private suspend fun updateSelector() {
    val usedItems =
      builderItemsLogic.getUsed().let {
        uiBuilderItemsAdapter.calculateStates(it)
      }

    Logger.d(TAG, "Update selector: usedItems=${usedItems.size}")
    _state.update { it.copy(builderItems = usedItems) }

    val errors =
      usedItems
        .asSequence()
        .filter { it.state is UiListBuilderItemState.ErrorState }
        .map { it.state }
        .map { it as UiListBuilderItemState.ErrorState }
        .map { it.errors }
        .flatten()
        .toSet()

    Logger.d(TAG, "Update selector: errors=$errors")

    val uiSelectorItems =
      uiSelectorItemsAdapter.calculateStates(
        builderItemsLogic.getUsed(),
        builderItemsLogic.getAvailable(),
      )

    Logger.d(TAG, "Update selector: uiSelectorItems=${uiSelectorItems.size}")

    updateBuilderState()

    withUIContext {
      selectorDialogDataHolder.selectorBuilderItems = uiSelectorItems
    }
  }

  private fun initBuilder() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val allTypes =
        BiType.entries
          .map { biFactory.create(it) }
          .filter { biFilter(it) }
          .sortedWith(BiComparator())

      Logger.i(TAG, "Init builder with available types: ${allTypes.size}")

      builderItemsLogic.setAllAvailable(allTypes)
      updateSelector()
    }
  }

  private suspend fun updateBuilderState() {
    val builderItems = builderItemsLogic.getUsed().toMutableList()
    Logger.d(TAG, "Update builder state: builderItems=${builderItems.size}")

    val allValid = builderItems.all { it.modifier.isCorrect() }
    Logger.i(TAG, "Are all builder items valid = $allValid")

    if (!allValid) {
      Logger.e(TAG, "Not all builder items are valid, skip updating builder state")
      setFailedPrediction(textProvider.getString(R.string.builder_error_create_reminder))
      return
    }

    val permissionResult = permissionValidator(builderItems)
    if (permissionResult is PermissionValidator.Result.Failure) {
      Logger.i(TAG, "Not all permissions granted. Skip updating builder state")
      setFailedPrediction(textProvider.getString(R.string.builder_permissions_required_message))
      return
    }

    val baseV2 = originalV2 ?: newBlankReminderV2()
    when (val buildResult = biToReminderAdapter(baseV2, builderItems, false)) {
      is BiToReminderAdapter.BuildResult.Success -> {
        _state.update {
          it.copy(
            prediction = reminderPredictionCalculator(buildResult.reminderV2),
            canSaveAsPreset = true,
            canSave = true,
          )
        }
      }

      is BiToReminderAdapter.BuildResult.Error -> {
        setFailedPrediction(builderErrorToTextAdapter(builderErrorFinder(baseV2, builderItems)))
        Logger.i(TAG, "Failed to update builder state with error = ${buildResult.error}")
      }
    }
  }

  /** Keeps `canSave`/`canSaveAsPreset`/`prediction` in sync with the current failure instead of
   *  leaving them at whatever they were the last time [updateBuilderState] succeeded - otherwise
   *  Save can look enabled and the forecast can show a stale success message while editing a
   *  field has actually made the reminder unbuildable. */
  private fun setFailedPrediction(message: String) {
    _state.update {
      it.copy(
        prediction = ReminderPrediction.FailedPrediction(icon = R.drawable.ic_fluent_error_circle, message = message),
        canSaveAsPreset = false,
        canSave = false,
      )
    }
  }

  private fun newBlankReminderV2(): ReminderV2 =
    ReminderV2(
      uuId = stableReminderId,
      schedule = ReminderSchedule(startDateTime = dateTimeManager.localToUtc(dateTimeManager.getCurrentDateTime())),
    )

  private suspend fun savePreset(items: List<BuilderItem<*>>) {
    Logger.i(TAG, "Save new preset")
    val preset =
      RecurPreset(
        recurObject = "",
        name = _state.value.presetName,
        type = PresetType.BUILDER,
        createdAt = dateTimeManager.getCurrentDateTime(),
        useCount = 1,
        builderScheme = builderItemsToBuilderPresetAdapter(items),
        description = null,
        isDefault = false,
        recurItemsToAdd = null,
        syncState = SyncState.WaitingForUpload,
        version = 1,
      )
    recurPresetRepository.save(preset)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Upload,
      dataType = DataType.RecurPresets,
      id = preset.id,
      ids = null,
    )
    analyticsEventSender.send(PresetUsed(PresetAction.CREATE))
  }

  private suspend fun saveAndStartReminder(
    reminder: ReminderV2,
    isEdit: Boolean = true,
  ) {
    Logger.i(
      TAG,
      "Start reminder saving, id = ${reminder.uuId} and group id = ${reminder.groupId}",
    )
    if (!isEdit && reminder.places.isNotEmpty()) {
      placeRepository.save(reminder.places[0])
    }
    activateReminderUseCase(reminder, startAnyway = true)
    Logger.i(TAG, "Reminder saved, id = ${reminder.uuId}")

    if (!isEdit) {
      analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_REMINDER))
      reminderAnalyticsTracker.sendEvent(reminder.toAnalyticsReminderType())
    }

    // Track reminder creation, show the feedback-form review dialog after 4 reminders, then -
    // staggered after that, never in the same session - nudge the real Play Store review flow
    // after 10, since the feedback form never touches the actual Play Store rating.
    if (!isEdit) {
      val currentCount = prefs.remindersCreatedCount
      val newCount = currentCount + 1
      prefs.remindersCreatedCount = newCount
      Logger.i(TAG, "Reminder creation count: $newCount")

      if (!prefs.reviewDialogShown && newCount >= 4) {
        Logger.i(TAG, "Showing review dialog after 4 reminders created")
        withContext(dispatcherProvider.main()) {
          askReview(R.string.share_your_experience)
        }
        prefs.reviewDialogShown = true
      } else if (prefs.reviewDialogShown && !prefs.playReviewFlowShown && newCount >= 10) {
        Logger.i(TAG, "Launching Play Store review flow after 10 reminders created")
        withContext(dispatcherProvider.main()) {
          event.emit(ViewModelEvent.ShowPlayReviewFlow)
        }
        prefs.playReviewFlowShown = true
      }
    }
  }

  /** Mirrors [UiReminderType.getEventType]'s priority order, reading straight off [ReminderV2]'s
   * sealed fields instead of a derived V1 type int. */
  private fun ReminderV2.toAnalyticsReminderType(): AnalyticsReminderType = when {
    recurrence is RecurrenceRule.ICalendar -> AnalyticsReminderType.Recur
    action is ReminderAction.Email -> AnalyticsReminderType.Email
    action is ReminderAction.Link -> AnalyticsReminderType.WebLink
    action is ReminderAction.App -> AnalyticsReminderType.App
    action is ReminderAction.Call -> AnalyticsReminderType.Call
    action is ReminderAction.Sms -> AnalyticsReminderType.Sms
    places.isNotEmpty() -> AnalyticsReminderType.Gps
    recurrence is RecurrenceRule.Monthly -> AnalyticsReminderType.Monthly
    recurrence is RecurrenceRule.Weekly -> AnalyticsReminderType.Weekday
    recurrence is RecurrenceRule.Countdown -> AnalyticsReminderType.Timer
    recurrence is RecurrenceRule.Yearly -> AnalyticsReminderType.Yearly
    recurrence is RecurrenceRule.Once || recurrence is RecurrenceRule.Daily -> AnalyticsReminderType.ByDate
    else -> AnalyticsReminderType.Other
  }

  private fun askReview(titleRes: Int) {
    event.emit(
      ViewModelEvent.ShowReviewDialog(
        title = textProvider.getString(titleRes),
        appSource = if (buildInfo.isPro) AppSource.PRO else AppSource.FREE,
        canAttachLogs = featureFlags.isEnabled(FeatureFlag.LOGS_IN_REVIEWS),
      )
    )
  }

  private suspend fun pauseReminder(reminder: ReminderV2) {
    Logger.i(TAG, "Pause reminder, id = ${reminder.uuId}")
    isPaused = true
    pauseReminderUseCase(reminder)
  }

  private fun resumeReminder(reminder: ReminderV2) {
    Logger.i(TAG, "Resume reminder, id = ${reminder.uuId}")
    cleanupScope.launch {
      isPaused = false
      resumeReminderUseCase(reminder)
    }
  }

  fun moveToTrash() {
    val reminder = originalV2
    if (reminder == null) {
      event.emit(ViewModelEvent.MoveBack)
      return
    }

    Logger.i(TAG, "Move reminder to Archive, id = ${reminder.uuId}")

    viewModelScope.launch(dispatcherProvider.default()) {
      isSaving = true
      moveReminderToArchiveUseCase(reminder.uuId)
      withContext(dispatcherProvider.main()) {
        event.emit(ViewModelEvent.MoveBack)
      }
    }
  }

  fun deleteReminder(showMessage: Boolean) {
    val reminder = originalV2
    if (reminder == null) {
      event.emit(ViewModelEvent.MoveBack)
      return
    }

    Logger.i(TAG, "Delete reminder, id = ${reminder.uuId}")

    viewModelScope.launch(dispatcherProvider.default()) {
      isSaving = true
      deleteReminderUseCase(reminder)
      if (showMessage) {
        withContext(dispatcherProvider.main()) {
          event.emit(ViewModelEvent.MoveBack)
        }
      }
    }
  }

  sealed interface ViewModelEvent {
    data class AskPermissions(
      val permissions: List<String>,
    ) : ViewModelEvent

    data class AskEditPermissions(
      val permissions: List<String>,
    ) : ViewModelEvent

    data object MoveBack : ViewModelEvent

    data class ShowMessage(
      val messageRes: Int,
    ) : ViewModelEvent

    data object ShowPlayReviewFlow : ViewModelEvent

    data class ShowReviewDialog(
      val title: String,
      val appSource: AppSource,
      val canAttachLogs: Boolean,
    ) : ViewModelEvent

    data object OpenManageTags : ViewModelEvent

    data class RedirectToTodoEdit(
      val id: String,
    ) : ViewModelEvent
  }

  companion object {
    private const val TAG = "BuildReminderViewModel"
  }
}
