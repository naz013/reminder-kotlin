package com.elementary.tasks.reminder.build

import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.ReminderAnalyticsTracker
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.preset.UiPresetListAdapter
import com.elementary.tasks.core.data.ui.preset.UiPresetList
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.deeplink.DeepLinkDataParser
import com.elementary.tasks.core.deeplink.ReminderDatetimeTypeDeepLinkData
import com.elementary.tasks.core.deeplink.ReminderTodoTypeDeepLinkData
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.reminder.build.bi.BiComparator
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.elementary.tasks.reminder.build.bi.BiFilter
import com.elementary.tasks.reminder.build.bi.constraint.PermissionConstraint
import com.elementary.tasks.reminder.build.logic.BuilderItemsLogic
import com.elementary.tasks.reminder.build.logic.UiBuilderItemsAdapter
import com.elementary.tasks.reminder.build.logic.UiSelectorItemsAdapter
import com.elementary.tasks.reminder.build.logic.builderstate.ReminderPrediction
import com.elementary.tasks.reminder.build.logic.builderstate.ReminderPredictionCalculator
import com.elementary.tasks.reminder.build.preset.BuilderItemsToBuilderPresetAdapter
import com.elementary.tasks.reminder.build.preset.BuilderPresetToBiAdapter
import com.elementary.tasks.reminder.build.preset.RecurParamsToBiAdapter
import com.elementary.tasks.reminder.build.reminder.BiToReminderAdapter
import com.elementary.tasks.reminder.build.reminder.ReminderToBiDecomposer
import com.elementary.tasks.reminder.build.reminder.validation.PermissionValidator
import com.elementary.tasks.reminder.build.selectordialog.SelectorDialogDataHolder
import com.elementary.tasks.reminder.build.valuedialog.ValueDialogDataHolder
import com.elementary.tasks.reminder.work.ReminderDeleteBackupWorker
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.analytics.PresetAction
import com.github.naz013.analytics.PresetUsed
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.PresetType
import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.livedata.toSingleEvent
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.icalendar.ICalendarApi
import com.github.naz013.icalendar.RecurrenceRuleTag
import com.github.naz013.icalendar.TagType
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.RecurPresetRepository
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.ReminderRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class BuildReminderViewModel(
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val reminderGroupRepository: ReminderGroupRepository,
  private val reminderRepository: ReminderRepository,
  private val placeRepository: PlaceRepository,
  private val analyticsEventSender: AnalyticsEventSender,
  private val reminderAnalyticsTracker: ReminderAnalyticsTracker,
  private val biFactory: BiFactory,
  private val builderItemsLogic: BuilderItemsLogic,
  private val selectorDialogDataHolder: SelectorDialogDataHolder,
  private val uiBuilderItemsAdapter: UiBuilderItemsAdapter,
  private val uiSelectorItemsAdapter: UiSelectorItemsAdapter,
  private val valueDialogDataHolder: ValueDialogDataHolder,
  private val biToReminderAdapter: BiToReminderAdapter,
  private val permissionValidator: PermissionValidator,
  private val reminderToBiDecomposer: ReminderToBiDecomposer,
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
  private val textProvider: TextProvider,
  private val intentDataReader: IntentDataReader
) : BaseProgressViewModel(dispatcherProvider) {

  private val _builderItems = mutableLiveDataOf<List<UiBuilderItem>>()
  val builderItems = _builderItems.toLiveData()

  private val _askPermissions = mutableLiveDataOf<Event<List<String>>>()
  val askPermissions = _askPermissions.toLiveData()

  private val _askEditPermissions = mutableLiveDataOf<Event<List<String>>>()
  val askEditPermissions = _askEditPermissions.toLiveData()

  private val _showEditDialog = mutableLiveDataOf<Event<Pair<Int, BuilderItem<*>>>>()
  val showEditDialog = _showEditDialog.toLiveData()

  private val _showPrediction = mutableLiveDataOf<ReminderPrediction>()
  val showPrediction = _showPrediction.toSingleEvent()

  private val _canSaveAsPreset = mutableLiveDataOf<Boolean>()
  val canSaveAsPreset = _canSaveAsPreset.toSingleEvent()

  private val _canSave = mutableLiveDataOf<Boolean>()
  val canSave = _canSave.toSingleEvent()

  var hasSameInDb: Boolean = false
  var isFromFile: Boolean = false
  private var isEdited: Boolean = false
  private var isPaused: Boolean = false
  var saveAsPreset: Boolean = false
  var presetName: String = ""

  val isRemoved: Boolean
    get() {
      return original?.isRemoved ?: false
    }
  val canRemove: Boolean
    get() {
      return isEdited && original != null && !isFromFile
    }

  private var isSaving: Boolean = false
  private var original: Reminder? = null

  private var requestedNewId = false
  private var requestedPermissionsFor: Pair<Int, BuilderItem<*>>? = null

  init {
    initBuilder()
    loadPresets()
  }

  override fun onDestroy(owner: LifecycleOwner) {
    super.onDestroy(owner)
    if (isPaused && !isSaving) {
      original?.let { resumeReminder(it) }
    }
    appWidgetUpdater.updateAllWidgets()
    appWidgetUpdater.updateCalendarWidget()
  }

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    reminderAnalyticsTracker.startTracking()
  }

  override fun onCleared() {
    super.onCleared()
    selectorDialogDataHolder.selectorBuilderItems = emptyList()
  }

  fun onConfigurationChanged() {
    Logger.d(TAG, "On configuration changed")
    viewModelScope.launch(dispatcherProvider.default()) {
      val used = builderItemsLogic.getUsed()

      val allTypes = BiType.entries.map { biFactory.create(it) }
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

  fun saveReminder(newId: Boolean) {
    postInProgress(true)
    Logger.i(TAG, "Start reminder saving, use new ID = $newId")
    viewModelScope.launch(dispatcherProvider.default()) {
      val builderItems = builderItemsLogic.getUsed().toMutableList()
      Logger.d(TAG, "saveReminder: builderItems=$builderItems")
      Logger.i(TAG, "Number of builder items = ${builderItems.size}")

      val allValid = builderItems.all { it.modifier.isCorrect() }
      Logger.i(TAG, "Are all builder items valid = $allValid")

      if (!allValid) {
        postInProgress(false)
        return@launch
      }

      val permissionResult = permissionValidator(builderItems)
      if (permissionResult is PermissionValidator.Result.Failure) {
        Logger.i(TAG, "Not all permissions granted. Request for = ${permissionResult.permissions}")
        requestedNewId = newId
        _askPermissions.postValue(Event(permissionResult.permissions))
        postInProgress(false)
        return@launch
      }

      Logger.i(TAG, "All permissions granted")

      if (!hasGroupBuilderItem(builderItems)) {
        Logger.i(TAG, "Does not have group builder item")
        getGroupBuilderItem()?.also {
          Logger.i(TAG, "Add group builder item")
          builderItems.add(it)
        } ?: run {
          Logger.i(TAG, "Group builder item not found")
        }
      }

      val reminder = original ?: Reminder()
      when (val buildResult = biToReminderAdapter(reminder, builderItems, newId)) {
        is BiToReminderAdapter.BuildResult.Success -> {
          Logger.i(TAG, "Reminder build success")

          saveAndStartReminder(buildResult.reminder, isEdit = isEdited)

          if (saveAsPreset && presetName.isNotEmpty()) {
            savePreset(builderItems)
          }

          postCommand(Commands.SAVED)
        }

        is BiToReminderAdapter.BuildResult.Error -> {
          Logger.i(TAG, "Reminder build failed with error = ${buildResult.error}")
        }
      }
      postInProgress(false)
    }
  }

  fun handleDeepLink(intent: Intent?) {
    Logger.i(TAG, "Handle reminder Deep Link: $intent")
    if (intent == null) {
      return
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      val intentId = intent.getStringExtra(IntentKeys.INTENT_ID)
      val action = intent.action
      when {
        action == Intent.ACTION_SEND && "text/plain" == intent.type -> {
          Logger.i(TAG, "Handle reminder text Deep Link")
          handleSendText(intent)
        }

        intent.getBooleanExtra(IntentKeys.INTENT_ITEM, false) -> {
          Logger.i(TAG, "Handle reminder object Deep Link")
          readObjectFromIntent()
        }

        intent.getBooleanExtra(IntentKeys.INTENT_DEEP_LINK, false) -> {
          readDeepLink(intent)
        }

        !intentId.isNullOrEmpty() -> {
          Logger.i(TAG, "Handle reminder ID Deep Link")
          editReminderIfNeeded(intentId)
        }
      }
    }
  }

  fun onPresetSelected(presetList: UiPresetList) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val preset = recurPresetRepository.getById(presetList.id) ?: return@launch

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

  fun onItemEditedClicked(position: Int, builderItem: BuilderItem<*>) {
    Logger.i(TAG, "On builder item edit clicked, type = ${builderItem.biType}")
    val pair = position to builderItem
    val permissions = builderItem.constraints.filterIsInstance<PermissionConstraint>()
    if (permissions.isNotEmpty()) {
      val permissionResult = permissionValidator(listOf(builderItem))
      if (permissionResult is PermissionValidator.Result.Success) {
        valueDialogDataHolder.data = builderItem
        _showEditDialog.postValue(Event(pair))
      } else if (permissionResult is PermissionValidator.Result.Failure) {
        requestedPermissionsFor = pair
        _askEditPermissions.postValue(Event(permissionResult.permissions))
      }
    } else {
      valueDialogDataHolder.data = builderItem
      _showEditDialog.postValue(Event(pair))
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

  fun removeItem(position: Int, builderItem: BuilderItem<*>) {
    Logger.i(TAG, "Remove builder item, type = ${builderItem.biType}")
    viewModelScope.launch(dispatcherProvider.default()) {
      builderItem.modifier.setDefault()
      builderItemsLogic.update(position, builderItem)
      builderItemsLogic.remove(position)
      updateSelector()
    }
  }

  fun updateValue(position: Int, builderItem: BuilderItem<*>) {
    Logger.i(TAG, "Update VALUE for builder item, type = ${builderItem.biType}")
    viewModelScope.launch(dispatcherProvider.default()) {
      builderItemsLogic.update(position, builderItem)
      updateSelector()
    }
  }

  private suspend fun readDeepLink(intent: Intent) {
    while (builderItemsLogic.getAvailable().isEmpty()) {
      delay(50)
    }
    runCatching {
      val parser = DeepLinkDataParser()
      when (val deepLinkData = parser.readDeepLinkData(intent)) {
        is ReminderDatetimeTypeDeepLinkData -> {
          if (deepLinkData.type == Reminder.BY_DATE) {
            Logger.i(TAG, "Handle reminder date/time Deep Link")
            addDateItemToBuilder(deepLinkData.dateTime.toLocalDate())
            addTimeItemToBuilder(deepLinkData.dateTime.toLocalTime())
            updateSelector()
          }
        }

        is ReminderTodoTypeDeepLinkData -> {
          Logger.i(TAG, "Handle reminder todo Deep Link")
          addSubTasksItemToBuilder()
          updateSelector()
        }

        else -> {}
      }
    }
  }

  private fun addDateItemToBuilder(date: LocalDate) {
    val itemIndex = builderItemsLogic.getUsed().indexOfFirst { it.biType == BiType.DATE }
    Logger.i(TAG, "Add Date builder item")
    if (itemIndex == -1) {
      builderItemsLogic.getAvailable().firstOrNull { it.biType == BiType.DATE }
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
      builderItemsLogic.getAvailable().firstOrNull { it.biType == BiType.TIME }
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
    intentDataReader.get(IntentKeys.INTENT_ITEM, Reminder::class.java)?.run {
      Logger.logEvent("Reminder loaded from intent")
      isFromFile = true
      editReminder(this)
    }
  }

  private suspend fun handleSendText(intent: Intent) {
    while (builderItemsLogic.getAvailable().isEmpty()) {
      delay(50)
    }
    intent.getStringExtra(Intent.EXTRA_TEXT)?.let { string ->
      addSummaryItemToBuilder(string)
      updateSelector()
    }
  }

  private fun addSubTasksItemToBuilder() {
    val itemIndex = builderItemsLogic.getUsed().indexOfFirst { it.biType == BiType.SUB_TASKS }
    Logger.i(TAG, "Add Sub tasks builder item")
    if (itemIndex == -1) {
      builderItemsLogic.getAvailable().firstOrNull { it.biType == BiType.SUB_TASKS }
        ?.let { it as SubTasksBuilderItem }
        ?.also { builderItemsLogic.addNew(it) }
    } else {
      val item = builderItemsLogic.getUsed()[itemIndex] as? SubTasksBuilderItem ?: return
      builderItemsLogic.update(itemIndex, item)
    }
  }

  private fun addSummaryItemToBuilder(text: String) {
    val itemIndex = builderItemsLogic.getUsed().indexOfFirst { it.biType == BiType.SUMMARY }
    Logger.i(TAG, "Add Summary builder item")
    if (itemIndex == -1) {
      builderItemsLogic.getAvailable().firstOrNull { it.biType == BiType.SUMMARY }
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
      val reminder = reminderRepository.getById(id) ?: return@launch
      editReminder(reminder)
      pauseReminder(reminder)
    }
  }

  private suspend fun editReminder(reminder: Reminder) {
    Logger.i(TAG, "Edit reminder, id = ${reminder.uuId}")

    isEdited = true
    original = reminder

    if (isFromFile) {
      findSame(reminder.uuId)
    }

    val builderItems = reminderToBiDecomposer(reminder)
    Logger.d(TAG, "editReminder: builderItems=$builderItems")

    if (builderItems.isNotEmpty()) {
      builderItemsLogic.setAll(builderItems)
      updateSelector()
    }
  }

  private suspend fun findSame(id: String) {
    val reminder = reminderRepository.getById(id)
    hasSameInDb = reminder != null
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
    val params = runCatching { iCalendarApi.parseObject(preset.recurObject) }.getOrNull()
      ?.getTagOrNull<RecurrenceRuleTag>(TagType.RRULE)
      ?.params
      ?.let { recurParamsToBiAdapter(it) }
      ?: emptyList()

    if (params.isNotEmpty()) {
      val used = builderItemsLogic.getUsed().mapIndexed { index, builderItem ->
        builderItem.biType to Pair(index, builderItem)
      }.toMap()

      val summaryBuilderItem = used[BiType.SUMMARY]?.second

      builderItemsLogic.setAll(params)

      summaryBuilderItem?.also { builderItemsLogic.addNew(it) }

      analyticsEventSender.send(PresetUsed(PresetAction.USE))
      updateSelector()
    }
  }

  private fun loadPresets() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val recurPresets = recurPresetRepository.getAllByType(presetType = PresetType.RECUR)
        .map { uiPresetListAdapter.create(it) }
      val presets = recurPresetRepository.getAllByType(presetType = PresetType.BUILDER)
        .map { uiPresetListAdapter.create(it) }

      withUIContext {
        selectorDialogDataHolder.presets = presets
        selectorDialogDataHolder.recurPresets = recurPresets
      }
    }
  }

  private suspend fun updateSelector() {
    val usedItems = builderItemsLogic.getUsed().let {
      uiBuilderItemsAdapter.calculateStates(it)
    }
    _builderItems.postValue(usedItems)

    val errors = usedItems.asSequence().filter { it.state is UiLitBuilderItemState.ErrorState }
      .map { it.state }
      .map { it as UiLitBuilderItemState.ErrorState }
      .map { it.errors }
      .flatten()
      .toSet()

    Logger.d(TAG, "updateSelector: errors=${errors.toList()}")

    val uiSelectorItems = uiSelectorItemsAdapter.calculateStates(
      builderItemsLogic.getUsed(),
      builderItemsLogic.getAvailable()
    )

    updateBuilderState()

    withUIContext {
      selectorDialogDataHolder.selectorBuilderItems = uiSelectorItems
    }
  }

  private fun initBuilder() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val allTypes = BiType.entries.map { biFactory.create(it) }
        .filter { biFilter(it) }
        .sortedWith(BiComparator())
      builderItemsLogic.setAllAvailable(allTypes)
      updateSelector()
    }
  }

  private suspend fun updateBuilderState() {
    val builderItems = builderItemsLogic.getUsed().toMutableList()
    Logger.d(TAG, "updateBuilderState: builderItems=$builderItems")

    val allValid = builderItems.all { it.modifier.isCorrect() }
    Logger.d(TAG, "updateBuilderState: allValid=$allValid")

    if (!allValid) {
      return
    }

    val permissionResult = permissionValidator(builderItems)
    if (permissionResult is PermissionValidator.Result.Failure) {
      return
    }

    val reminder = Reminder()
    when (val buildResult = biToReminderAdapter(reminder, builderItems, false)) {
      is BiToReminderAdapter.BuildResult.Success -> {
        _showPrediction.postValue(reminderPredictionCalculator(reminder))
        _canSaveAsPreset.postValue(true)
        _canSave.postValue(true)
      }

      is BiToReminderAdapter.BuildResult.Error -> {
        _showPrediction.postValue(
          ReminderPrediction.FailedPrediction(
            icon = R.drawable.ic_fluent_error_circle,
            message = textProvider.getText(R.string.builder_error_create_reminder)
          )
        )
        _canSaveAsPreset.postValue(false)
        _canSave.postValue(false)
        Logger.d(TAG, "updateBuilderState: build failed ${buildResult.error}")
      }
    }
  }

  private fun getGroupBuilderItem(): GroupBuilderItem? {
    return builderItemsLogic.getAvailable()
      .firstOrNull { it.biType == BiType.GROUP } as? GroupBuilderItem
  }

  private fun hasGroupBuilderItem(items: List<BuilderItem<*>>): Boolean {
    items.forEach {
      if (it is GroupBuilderItem) {
        return true
      }
    }
    return false
  }

  private suspend fun savePreset(items: List<BuilderItem<*>>) {
    Logger.i(TAG, "Save new preset")
    val preset = RecurPreset(
      recurObject = "",
      name = presetName,
      type = PresetType.BUILDER,
      createdAt = dateTimeManager.getCurrentDateTime(),
      useCount = 1,
      builderScheme = builderItemsToBuilderPresetAdapter(items),
      description = null
    )
    recurPresetRepository.save(preset)
    analyticsEventSender.send(PresetUsed(PresetAction.CREATE))
  }

  private suspend fun saveAndStartReminder(reminder: Reminder, isEdit: Boolean = true) {
    Logger.i(
      TAG,
      "Start reminder saving, id = ${reminder.uuId} and group id = ${reminder.groupUuId}"
    )
    if (reminder.groupUuId.isEmpty()) {
      val group = reminderGroupRepository.defaultGroup()
      Logger.i(TAG, "Reminder does not have a group, get default = $group")
      if (group != null) {
        reminder.groupColor = group.groupColor
        reminder.groupTitle = group.groupTitle
        reminder.groupUuId = group.groupUuId
      }
    }
    reminderRepository.save(reminder)
    if (!isEdit) {
      if (Reminder.isGpsType(reminder.type)) {
        val places = reminder.places
        if (places.isNotEmpty()) {
          placeRepository.save(places[0])
        }
      }
    }
    eventControlFactory.getController(reminder).justStart()
    Logger.i(TAG, "Reminder saved, id = ${reminder.uuId}")

    analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_REMINDER))
    reminderAnalyticsTracker.sendEvent(UiReminderType(reminder.type).getEventType())
    Logger.i(TAG, "Reminder saved, type = ${reminder.type}")
    backupReminder(reminder.uuId)
  }

  private suspend fun pauseReminder(reminder: Reminder) {
    Logger.i(TAG, "Pause reminder, id = ${reminder.uuId}")
    isPaused = true
    eventControlFactory.getController(reminder).pause()
  }

  private fun resumeReminder(reminder: Reminder) {
    Logger.i(TAG, "Resume reminder, id = ${reminder.uuId}")
    viewModelScope.launch(dispatcherProvider.default()) {
      eventControlFactory.getController(reminder).resume()
    }
  }

  fun moveToTrash() {
    val reminder = original
    if (reminder == null) {
      postCommand(Commands.DELETED)
      return
    }

    Logger.i(TAG, "Move reminder to Archive, id = ${reminder.uuId}")

    withResultSuspend {
      reminder.isRemoved = true
      eventControlFactory.getController(reminder).disable()
      reminderRepository.save(reminder)
      backupReminder(reminder.uuId)
      Commands.DELETED
    }
  }

  fun deleteReminder(showMessage: Boolean) {
    val reminder = original
    if (reminder == null) {
      postCommand(Commands.DELETED)
      return
    }

    Logger.i(TAG, "Delete reminder, id = ${reminder.uuId}")

    if (showMessage) {
      withResultSuspend {
        eventControlFactory.getController(reminder).disable()
        reminderRepository.delete(reminder.uuId)
        googleCalendarUtils.deleteEvents(reminder.uuId)
        workerLauncher.startWork(
          ReminderDeleteBackupWorker::class.java,
          IntentKeys.INTENT_ID,
          reminder.uuId
        )
        Commands.DELETED
      }
    } else {
      withProgressSuspend {
        eventControlFactory.getController(reminder).disable()
        reminderRepository.delete(reminder.uuId)
        googleCalendarUtils.deleteEvents(reminder.uuId)
        workerLauncher.startWork(
          ReminderDeleteBackupWorker::class.java,
          IntentKeys.INTENT_ID,
          reminder.uuId
        )
      }
    }
  }

  private fun backupReminder(uuId: String) {
    Logger.i(TAG, "Schedule reminder backup work")
    workerLauncher.startWork(ReminderSingleBackupWorker::class.java, IntentKeys.INTENT_ID, uuId)
  }

  companion object {
    private const val TAG = "BuildReminderViewModel"
  }
}
