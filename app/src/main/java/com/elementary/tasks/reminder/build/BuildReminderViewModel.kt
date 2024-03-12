package com.elementary.tasks.reminder.build

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.AnalyticsEventSender
import com.elementary.tasks.core.analytics.Feature
import com.elementary.tasks.core.analytics.FeatureUsedEvent
import com.elementary.tasks.core.analytics.PresetAction
import com.elementary.tasks.core.analytics.PresetUsed
import com.elementary.tasks.core.analytics.ReminderAnalyticsTracker
import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.preset.UiPresetListAdapter
import com.elementary.tasks.core.data.dao.PlacesDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.livedata.Event
import com.elementary.tasks.core.data.livedata.toSingleEvent
import com.elementary.tasks.core.data.models.PresetType
import com.elementary.tasks.core.data.models.RecurPreset
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.repository.RecurPresetRepository
import com.elementary.tasks.core.data.ui.preset.UiPresetList
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.deeplink.DeepLinkDataParser
import com.elementary.tasks.core.deeplink.ReminderDatetimeTypeDeepLinkData
import com.elementary.tasks.core.os.readParcelable
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceManager
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceRuleTag
import com.elementary.tasks.core.utils.datetime.recurrence.TagType
import com.elementary.tasks.core.utils.io.UriReader
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.reminder.build.bi.BiComparator
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.elementary.tasks.reminder.build.bi.BiFilter
import com.elementary.tasks.reminder.build.bi.BiType
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
import com.elementary.tasks.voice.VoiceCommandProcessor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import timber.log.Timber

class BuildReminderViewModel(
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val reminderGroupDao: ReminderGroupDao,
  private val reminderDao: ReminderDao,
  private val placesDao: PlacesDao,
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
  private val recurrenceManager: RecurrenceManager,
  private val recurParamsToBiAdapter: RecurParamsToBiAdapter,
  private val builderPresetToBiAdapter: BuilderPresetToBiAdapter,
  private val reminderPredictionCalculator: ReminderPredictionCalculator,
  private val uriReader: UriReader,
  private val updatesHelper: UpdatesHelper,
  private val voiceCommandProcessor: VoiceCommandProcessor,
  private val builderItemsToBuilderPresetAdapter: BuilderItemsToBuilderPresetAdapter,
  private val dateTimeManager: DateTimeManager,
  private val textProvider: TextProvider
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
    updatesHelper.updateWidgets()
    updatesHelper.updateCalendarWidget()
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

  fun processVoiceResult(matches: List<String>) {
    Timber.d("processVoiceResult: $matches")
    viewModelScope.launch(dispatcherProvider.default()) {
      when (val result = voiceCommandProcessor(matches)) {
        is VoiceCommandProcessor.ProcessResult.ReminderSuccess -> {
          editReminder(result.reminder)
          isEdited = false
          original = null
        }

        is VoiceCommandProcessor.ProcessResult.TextSuccess -> {
          addSummaryItemToBuilder(result.text)
        }

        is VoiceCommandProcessor.ProcessResult.Error -> {
        }
      }
    }
  }

  fun onPermissionsGranted() {
    saveReminder(requestedNewId)
  }

  fun saveReminder(newId: Boolean) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val builderItems = builderItemsLogic.getUsed().toMutableList()
      Timber.d("saveReminder: builderItems=$builderItems")

      val allValid = builderItems.all { it.modifier.isCorrect() }
      Timber.d("saveReminder: allValid=$allValid")

      if (!allValid) {
        postInProgress(false)
        return@launch
      }

      val permissionResult = permissionValidator(builderItems)
      if (permissionResult is PermissionValidator.Result.Failure) {
        requestedNewId = newId
        _askPermissions.postValue(Event(permissionResult.permissions))
        postInProgress(false)
        return@launch
      }

      if (!hasGroupBuilderItem(builderItems)) {
        getGroupBuilderItem()?.also {
          builderItems.add(it)
        }
      }

      val reminder = original ?: Reminder()
      when (val buildResult = biToReminderAdapter(reminder, builderItems, newId)) {
        is BiToReminderAdapter.BuildResult.Success -> {
          saveAndStartReminder(buildResult.reminder, isEdit = isEdited)

          if (saveAsPreset && presetName.isNotEmpty()) {
            savePreset(builderItems)
          }

          postCommand(Commands.SAVED)
        }

        is BiToReminderAdapter.BuildResult.Error -> {
          Timber.d("saveReminder: build failed ${buildResult.error}")
        }
      }
      postInProgress(false)
    }
  }

  fun handleDeepLink(intent: Intent?) {
    Timber.d("handleDeepLink: $intent")
    if (intent == null) {
      return
    }
    viewModelScope.launch(dispatcherProvider.default()) {
      val intentId = intent.getStringExtra(Constants.INTENT_ID)
      val action = intent.action
      when {
        action == Intent.ACTION_SEND && "text/plain" == intent.type -> {
          handleSendText(intent)
        }

        intent.data != null -> {
          readFromIntent(intent.data)
        }

        intent.hasExtra(Constants.INTENT_ITEM) -> {
          readObjectFromIntent(intent)
        }

        intent.getBooleanExtra(Constants.INTENT_DEEP_LINK, false) -> {
          readDeepLink(intent)
        }

        !intentId.isNullOrEmpty() -> {
          editReminderIfNeeded(intentId)
        }
      }
    }
  }

  fun onPresetSelected(presetList: UiPresetList) {
    Timber.d("onPresetSelected: $presetList")
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
    viewModelScope.launch(dispatcherProvider.default()) {
      updateSelector()
    }
    requestedPermissionsFor?.also { onItemEditedClicked(it.first, it.second) }
    requestedPermissionsFor = null
  }

  fun onItemEditedClicked(position: Int, builderItem: BuilderItem<*>) {
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
    viewModelScope.launch(dispatcherProvider.default()) {
      builderItemsLogic.addNew(builderItem)

      val position = builderItemsLogic.getUsed().size - 1
      onItemEditedClicked(position, builderItem)

      updateSelector()
    }
  }

  fun removeItem(position: Int, builderItem: BuilderItem<*>) {
    viewModelScope.launch(dispatcherProvider.default()) {
      builderItem.modifier.setDefault()
      builderItemsLogic.update(position, builderItem)
      builderItemsLogic.remove(position)
      updateSelector()
    }
  }

  fun updateValue(position: Int, builderItem: BuilderItem<*>) {
    viewModelScope.launch(dispatcherProvider.default()) {
      builderItemsLogic.update(position, builderItem)
      updateSelector()
    }
  }

  private suspend fun readDeepLink(intent: Intent) {
    Timber.d("readDeepLink: ")
    while (builderItemsLogic.getAvailable().isEmpty()) {
      delay(50)
    }
    runCatching {
      val parser = DeepLinkDataParser()
      when (val deepLinkData = parser.readDeepLinkData(intent)) {
        is ReminderDatetimeTypeDeepLinkData -> {
          if (deepLinkData.type == Reminder.BY_DATE) {
            addDateItemToBuilder(deepLinkData.dateTime.toLocalDate())
            addTimeItemToBuilder(deepLinkData.dateTime.toLocalTime())
            updateSelector()
          }
        }

        else -> {}
      }
    }
  }

  private fun addDateItemToBuilder(date: LocalDate) {
    val itemIndex = builderItemsLogic.getUsed().indexOfFirst { it.biType == BiType.DATE }
    Timber.d("addDateItemToBuilder: itemIndex=$itemIndex")
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
    Timber.d("addTimeItemToBuilder: itemIndex=$itemIndex")
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

  private suspend fun readObjectFromIntent(intent: Intent) {
    while (builderItemsLogic.getAvailable().isEmpty()) {
      delay(50)
    }
    runCatching {
      val reminder = intent.readParcelable(Constants.INTENT_ITEM, Reminder::class.java)
      if (reminder != null) {
        isFromFile = true
        editReminder(reminder)
      }
    }
  }

  private suspend fun readFromIntent(uri: Uri?) {
    while (builderItemsLogic.getAvailable().isEmpty()) {
      delay(50)
    }
    if (uri == null) return
    try {
      if (ContentResolver.SCHEME_CONTENT != uri.scheme) {
        val any = uriReader.readObject(uri, FileConfig.FILE_NAME_REMINDER)
        if (any != null && any is Reminder) {
          isFromFile = true
          editReminder(any)
        }
      }
    } catch (e: Throwable) {
      Timber.d("readFromIntent: ${e.message}")
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

  private fun addSummaryItemToBuilder(text: String) {
    val itemIndex = builderItemsLogic.getUsed().indexOfFirst { it.biType == BiType.SUMMARY }
    Timber.d("addTimeItemToBuilder: itemIndex=$itemIndex, text=$text")
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
      val reminder = reminderDao.getById(id) ?: return@launch
      Timber.d("editReminderIfNeeded: reminder=$reminder")
      editReminder(reminder)
      pauseReminder(reminder)
    }
  }

  private suspend fun editReminder(reminder: Reminder) {
    isEdited = true
    original = reminder

    if (isFromFile) {
      findSame(reminder.uuId)
    }

    val builderItems = reminderToBiDecomposer(reminder)
    Timber.d("editReminder: builderItems=$builderItems")

    if (builderItems.isNotEmpty()) {
      builderItemsLogic.setAll(builderItems)
      updateSelector()
    }
  }

  private suspend fun findSame(id: String) {
    val reminder = reminderDao.getById(id)
    hasSameInDb = reminder != null
    reminder?.also { pauseReminder(it) }
  }

  private suspend fun useBuilderPreset(preset: RecurPreset) {
    val items = builderPresetToBiAdapter(preset)
    if (items.isNotEmpty()) {
      builderItemsLogic.setAll(items)
      analyticsEventSender.send(PresetUsed(PresetAction.USE_BUILDER))
      updateSelector()
    }
  }

  private suspend fun useRecurPreset(preset: RecurPreset) {
    val params = runCatching { recurrenceManager.parseObject(preset.recurObject) }.getOrNull()
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
      val recurPresets = recurPresetRepository.getAll(presetType = PresetType.RECUR)
        .map { uiPresetListAdapter.create(it) }
      val presets = recurPresetRepository.getAll(presetType = PresetType.BUILDER)
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

    Timber.d("updateSelector: errors=${errors.toList()}")

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
    Timber.d("updateBuilderState: builderItems=$builderItems")

    val allValid = builderItems.all { it.modifier.isCorrect() }
    Timber.d("updateBuilderState: allValid=$allValid")

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
        Timber.d("updateBuilderState: build failed ${buildResult.error}")
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
    val preset = RecurPreset(
      recurObject = "",
      name = presetName,
      type = PresetType.BUILDER,
      createdAt = dateTimeManager.getCurrentDateTime(),
      useCount = 1,
      builderScheme = builderItemsToBuilderPresetAdapter(items)
    )
    recurPresetRepository.save(preset)
    analyticsEventSender.send(PresetUsed(PresetAction.CREATE))
  }

  private suspend fun saveAndStartReminder(reminder: Reminder, isEdit: Boolean = true) {
    runBlocking {
      Timber.d("saveAndStartReminder: save START")
      if (reminder.groupUuId == "") {
        val group = reminderGroupDao.defaultGroup()
        if (group != null) {
          reminder.groupColor = group.groupColor
          reminder.groupTitle = group.groupTitle
          reminder.groupUuId = group.groupUuId
        }
      }
      reminderDao.insert(reminder)
      if (!isEdit) {
        if (Reminder.isGpsType(reminder.type)) {
          val places = reminder.places
          if (places.isNotEmpty()) {
            placesDao.insert(places[0])
          }
        }
      }
      eventControlFactory.getController(reminder).justStart()
      Timber.d("saveAndStartReminder: save DONE")

      analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_REMINDER))
      reminderAnalyticsTracker.sendEvent(UiReminderType(reminder.type))
      Traces.logEvent("Reminder saved, type = ${reminder.type}")
    }
    backupReminder(reminder.uuId)
  }

  private suspend fun pauseReminder(reminder: Reminder) {
    isPaused = true
    eventControlFactory.getController(reminder).pause()
  }

  private fun resumeReminder(reminder: Reminder) {
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

    withResult {
      reminder.isRemoved = true
      eventControlFactory.getController(reminder).disable()
      reminderDao.insert(reminder)
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

    if (showMessage) {
      withResult {
        eventControlFactory.getController(reminder).disable()
        reminderDao.delete(reminder)
        googleCalendarUtils.deleteEvents(reminder.uuId)
        workerLauncher.startWork(
          ReminderDeleteBackupWorker::class.java,
          Constants.INTENT_ID,
          reminder.uuId
        )
        Commands.DELETED
      }
    } else {
      withProgress {
        eventControlFactory.getController(reminder).disable()
        reminderDao.delete(reminder)
        googleCalendarUtils.deleteEvents(reminder.uuId)
        workerLauncher.startWork(
          ReminderDeleteBackupWorker::class.java,
          Constants.INTENT_ID,
          reminder.uuId
        )
      }
    }
  }

  private fun backupReminder(uuId: String) {
    Timber.d("backupReminder: start backup")
    workerLauncher.startWork(ReminderSingleBackupWorker::class.java, Constants.INTENT_ID, uuId)
  }
}
