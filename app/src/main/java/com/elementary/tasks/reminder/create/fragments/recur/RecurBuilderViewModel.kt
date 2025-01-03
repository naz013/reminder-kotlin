package com.elementary.tasks.reminder.create.fragments.recur

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.github.naz013.feature.common.livedata.toSingleEvent
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.icalendar.ByDayRecurParam
import com.github.naz013.icalendar.ByHourRecurParam
import com.github.naz013.icalendar.ByMinuteRecurParam
import com.github.naz013.icalendar.ByMonthDayRecurParam
import com.github.naz013.icalendar.ByMonthRecurParam
import com.github.naz013.icalendar.BySetPosRecurParam
import com.github.naz013.icalendar.ByWeekNumberRecurParam
import com.github.naz013.icalendar.ByYearDayRecurParam
import com.github.naz013.icalendar.CountRecurParam
import com.github.naz013.icalendar.DateTimeStartTag
import com.github.naz013.icalendar.Day
import com.github.naz013.icalendar.DayValue
import com.github.naz013.icalendar.FreqRecurParam
import com.github.naz013.icalendar.FreqType
import com.github.naz013.icalendar.IntervalRecurParam
import com.github.naz013.icalendar.RecurParam
import com.github.naz013.icalendar.RecurParamType
import com.github.naz013.icalendar.ICalendarApi
import com.github.naz013.icalendar.RecurrenceRuleTag
import com.github.naz013.icalendar.RuleMap
import com.github.naz013.icalendar.Tag
import com.github.naz013.icalendar.TagType
import com.github.naz013.icalendar.UntilRecurParam
import com.github.naz013.icalendar.UtcDateTime
import com.github.naz013.icalendar.WeekStartRecurParam
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.common.livedata.toLiveData
import com.elementary.tasks.reminder.create.fragments.recur.adapter.ParamToTextAdapter
import com.elementary.tasks.reminder.create.fragments.recur.intdialog.Number
import com.elementary.tasks.reminder.create.fragments.recur.preview.PreviewData
import com.elementary.tasks.reminder.create.fragments.recur.preview.PreviewItem
import com.elementary.tasks.reminder.create.fragments.recur.preview.Style
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.PresetAction
import com.github.naz013.analytics.PresetUsed
import com.github.naz013.domain.PresetType
import com.github.naz013.domain.RecurPreset
import com.github.naz013.logging.Logger
import com.github.naz013.repository.RecurPresetRepository
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import java.util.Timer
import java.util.TimerTask

class RecurBuilderViewModel(
  dispatcherProvider: DispatcherProvider,
  private val paramToTextAdapter: ParamToTextAdapter,
  private val ICalendarApi: ICalendarApi,
  private val dateTimeManager: DateTimeManager,
  private val recurPresetRepository: RecurPresetRepository,
  private val prefs: Prefs,
  private val textProvider: TextProvider,
  private val analyticsEventSender: AnalyticsEventSender
) : BaseProgressViewModel(dispatcherProvider) {

  private val builderParamLogic = BuilderParamLogic()
  private val refreshTimer = RefreshTimer { reCalculate() }

  private val _availableParams = mutableLiveDataOf<List<UiBuilderParam<*>>>()
  val availableParams = _availableParams.toLiveData()

  private val _usedParams = mutableLiveDataOf<List<UiBuilderParam<*>>>()
  val usedParams = _usedParams.toLiveData()

  private val _supportedFreq = mutableLiveDataOf<List<UiFreqParam>>()
  val supportedFreq = _supportedFreq.toLiveData()

  private val _supportedDays = mutableLiveDataOf<List<UiDayParam>>()
  val supportedDays = _supportedDays.toLiveData()

  private val _previewData = mutableLiveDataOf<PreviewData>()
  val previewData = _previewData.toLiveData()

  private val _dateTime = mutableLiveDataOf<LocalDateTime>()
  val dateTime = _dateTime.toLiveData()

  private val _previewError = mutableLiveDataOf<String?>()
  val previewError = _previewError.toSingleEvent()

  private var startDateTime = LocalDateTime.now()

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      builderParamLogic.setAllParams(RecurParamType.entries.map { it.toBuilderParam() })

      _availableParams.postValue(createAvailableDataList(builderParamLogic.getAvailable()))
      _supportedFreq.postValue(getSupportedFreq())
      _supportedDays.postValue(getSupportedDays())

      val used = createUsedDataList(builderParamLogic.getUsed())
      calculateEvents(used)
      _usedParams.postValue(used)
      _dateTime.postValue(startDateTime)
    }
  }

  fun showAdvancedDayDialog(): Boolean {
    return prefs.showAdvancedDayDialog
  }

  fun setShowAdvancedDayDialog(boolean: Boolean) {
    prefs.showAdvancedDayDialog = boolean
  }

  fun onPresetSelected(presetId: String) {
    Logger.d("onPresetSelected: $presetId")
    viewModelScope.launch(dispatcherProvider.default()) {
      val preset = recurPresetRepository.getById(presetId) ?: return@launch

      val params = runCatching { ICalendarApi.parseObject(preset.recurObject) }.getOrNull()
        ?.getTagOrNull<RecurrenceRuleTag>(TagType.RRULE)
        ?.params
        ?.map { it.toBuilderParam() }
        ?: emptyList()

      if (params.isNotEmpty()) {
        builderParamLogic.clearUsed()
        builderParamLogic.addOrUpdateParams(params)

        val used = createUsedDataList(builderParamLogic.getUsed())
        calculateEvents(used)

        _usedParams.postValue(used)
        _availableParams.postValue(createAvailableDataList(builderParamLogic.getAvailable()))

        analyticsEventSender.send(PresetUsed(PresetAction.USE))
      }
    }
  }

  fun addPreset(recurObject: String, name: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val preset = RecurPreset(
        recurObject = recurObject,
        name = name,
        type = PresetType.RECUR,
        createdAt = dateTimeManager.getCurrentDateTime(),
        useCount = 1,
        description = null
      )
      recurPresetRepository.save(preset)
      analyticsEventSender.send(PresetUsed(PresetAction.CREATE))
    }
  }

  fun generateNumbers(
    minValue: Int,
    maxValue: Int,
    excludedValues: IntArray,
    selectedValues: List<Int>
  ): List<Number> {
    val list = mutableListOf<Number>()
    for (i in minValue..maxValue) {
      val number = Number(i)
      if (selectedValues.contains(i)) {
        number.isSelected = true
      }
      list.add(number)
    }
    excludedValues.forEach {
      list.remove(Number(it))
    }
    return list
  }

  fun getEventData(): EventData? {
    val usedParams = _usedParams.value?.map { it.param } ?: return null

    Logger.d("calculateEvents: params = $usedParams")

    val ruleMap = createRuleMap(usedParams)

    Logger.d("calculateEvents: map = $ruleMap")

    val recurObject = runCatching {
      ICalendarApi.createObject(ruleMap)
    }.getOrNull() ?: return null

    val dates = runCatching { ICalendarApi.generate(ruleMap) }.getOrNull() ?: emptyList()
    val position = findPosition(dates)

    return dates[position].dateTime?.let {
      EventData(
        startDateTime = it,
        recurObject = recurObject
      )
    }
  }

  fun onEdit(reminder: Reminder) {
    viewModelScope.launch(dispatcherProvider.default()) {
      Logger.d("onEdit: recurDataObject = ${reminder.recurDataObject}")

      val rules = runCatching {
        ICalendarApi.parseObject(reminder.recurDataObject)
      }.getOrNull()

      rules?.map?.values?.forEach { tag ->
        when (tag) {
          is RecurrenceRuleTag -> {
            tag.params.map { it.toBuilderParam() }.also {
              Logger.d("onEdit: builder params = $it")
              builderParamLogic.addOrUpdateParams(it)
            }
          }

          is DateTimeStartTag -> {
            tag.value.dateTime?.also {
              startDateTime = it
            }
          }

          else -> {}
        }
      }

      val used = createUsedDataList(builderParamLogic.getUsed())
      calculateEvents(used)

      _usedParams.postValue(used)
      _availableParams.postValue(createAvailableDataList(builderParamLogic.getAvailable()))
      _dateTime.postValue(startDateTime)
    }
  }

  fun onDateTimeChanged(dateTime: LocalDateTime) {
    this.startDateTime = dateTime

    viewModelScope.launch(dispatcherProvider.default()) {
      calculateEvents(_usedParams.value ?: emptyList())
    }
  }

  fun selectOrUpdateParam(builderParam: BuilderParam<*>) {
    viewModelScope.launch(dispatcherProvider.default()) {
      builderParamLogic.addOrUpdateParam(builderParam)

      val used = createUsedDataList(builderParamLogic.getUsed())
      calculateEvents(used)

      _usedParams.postValue(used)
      _availableParams.postValue(createAvailableDataList(builderParamLogic.getAvailable()))
    }
  }

  fun unSelectParam(builderParam: BuilderParam<*>) {
    viewModelScope.launch(dispatcherProvider.default()) {
      builderParamLogic.removeParam(builderParam)

      val used = createUsedDataList(builderParamLogic.getUsed())
      calculateEvents(used)

      _usedParams.postValue(used)
      _availableParams.postValue(createAvailableDataList(builderParamLogic.getAvailable()))
    }
  }

  private fun reCalculate() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val used = createUsedDataList(builderParamLogic.getUsed())
      calculateEvents(used)
    }
  }

  private fun calculateEvents(used: List<UiBuilderParam<*>>) {
    _previewError.postValue(null)

    val (dates, scrollPosition) = generateDates(used)

    if (dates.isEmpty()) {
      refreshTimer.stop()

      val hasUntilRule = used.firstOrNull {
        it.param.recurParamType == RecurParamType.UNTIL
      } != null

      if (hasUntilRule) {
        _previewError.postValue(textProvider.getText(R.string.recur_change_until_param_error))
      }
    } else {
      refreshTimer.start()
    }

    _previewData.postValue(PreviewData(scrollPosition, dates))
  }

  private fun generateDates(used: List<UiBuilderParam<*>>): Pair<List<PreviewItem>, Int> {
    val usedParams = used.map { it.param }
    Logger.d("calculateEvents: params = $usedParams")

    if (!hasLimit(usedParams)) {
      Logger.d("calculateEvents: no limit, show error")
      _previewError.postValue(textProvider.getText(R.string.recur_no_limit_error))
      return Pair(emptyList(), 0)
    }

    val ruleMap = createRuleMap(usedParams)
    return generateFromMap(ruleMap)
  }

  private fun hasLimit(params: List<BuilderParam<*>>): Boolean {
    return params.firstOrNull { it.recurParamType == RecurParamType.COUNT } != null ||
      params.firstOrNull { it.recurParamType == RecurParamType.UNTIL } != null
  }

  private fun generateFromMap(ruleMap: RuleMap): Pair<List<PreviewItem>, Int> {
    Logger.d("calculateEvents: map = $ruleMap")

    val generated = runCatching { ICalendarApi.generate(ruleMap) }.getOrNull() ?: emptyList()

    return convertForUi(generated)
  }

  private fun findPosition(generated: List<UtcDateTime>): Int {
    if (generated.isEmpty()) return -1

    val nowDateTime = dateTimeManager.getCurrentDateTime().withNano(0)

    var nowSelected = false
    var position = -1

    generated.forEachIndexed { index, utcDateTime ->
      val dateTime = utcDateTime.dateTime
      if (dateTime != null) {
        if (!nowSelected) {
          if (dateTime.isEqual(nowDateTime) || dateTime.isAfter(nowDateTime)) {
            position = index
            nowSelected = true
          }
        }
      }
    }

    return position
  }

  private fun convertForUi(generated: List<UtcDateTime>): Pair<List<PreviewItem>, Int> {
    val nowDateTime = dateTimeManager.getCurrentDateTime().withNano(0)

    var nowSelected = false

    val dates = generated.mapNotNull { it.dateTime }.map {
      val isNext = if (!nowSelected) {
        if (it.isEqual(nowDateTime) || it.isAfter(nowDateTime)) {
          nowSelected = true
          true
        } else {
          false
        }
      } else {
        false
      }
      val style = if (isNext) {
        Style.BOLD
      } else {
        if (it.isBefore(nowDateTime)) {
          Style.DISABLED
        } else {
          Style.NORMAL
        }
      }
      PreviewItem(
        text = dateTimeManager.getFullDateTime(it),
        style = style
      )
    }

    val scrollPosition = dates.indexOfFirst { it.style == Style.BOLD }

    Logger.d("calculateEvents: scrollPosition = $scrollPosition, dates = ${dates.size}")
    return Pair(dates, scrollPosition)
  }

  private fun createRuleMap(params: List<BuilderParam<*>>): RuleMap {
    val map = mutableMapOf<TagType, Tag>().apply {
      put(TagType.DTSTART, DateTimeStartTag(UtcDateTime(startDateTime)))
    }

    createRruleTag(params)?.also {
      map[it.tagType] = it
    }

    return RuleMap(map)
  }

  @Suppress("UNCHECKED_CAST")
  private fun createRruleTag(params: List<BuilderParam<*>>): RecurrenceRuleTag? {
    if (params.isEmpty()) return null

    val recurParams = mutableListOf<RecurParam>()

    params.forEach { builderParam ->
      val value = builderParam.value
      if (value != null) {
        when (builderParam.recurParamType) {
          RecurParamType.FREQ -> {
            recurParams.add(FreqRecurParam(value as FreqType))
          }

          RecurParamType.INTERVAL -> {
            recurParams.add(IntervalRecurParam(value as Int))
          }

          RecurParamType.COUNT -> {
            recurParams.add(CountRecurParam(value as Int))
          }

          RecurParamType.UNTIL -> {
            recurParams.add(UntilRecurParam(value as UtcDateTime))
          }

          RecurParamType.BYMONTH -> {
            recurParams.add(ByMonthRecurParam(value as List<Int>))
          }

          RecurParamType.WEEKSTART -> {
            recurParams.add(WeekStartRecurParam(value as DayValue))
          }

          RecurParamType.BYDAY -> {
            recurParams.add(ByDayRecurParam(value as List<DayValue>))
          }

          RecurParamType.BYMONTHDAY -> {
            recurParams.add(ByMonthDayRecurParam(value as List<Int>))
          }

          RecurParamType.BYHOUR -> {
            recurParams.add(ByHourRecurParam(value as List<Int>))
          }

          RecurParamType.BYMINUTE -> {
            recurParams.add(ByMinuteRecurParam(value as List<Int>))
          }

          RecurParamType.BYYEARDAY -> {
            recurParams.add(ByYearDayRecurParam(value as List<Int>))
          }

          RecurParamType.BYWEEKNO -> {
            recurParams.add(ByWeekNumberRecurParam(value as List<Int>))
          }

          RecurParamType.BYSETPOS -> {
            recurParams.add(BySetPosRecurParam(value as List<Int>))
          }
        }
      }
    }

    return RecurrenceRuleTag(recurParams)
  }

  private fun createAvailableDataList(list: List<BuilderParam<*>>): List<UiBuilderParam<*>> {
    return list.map { UiBuilderParam(paramToTextAdapter.createText(it), it) }
      .sortedBy { it.text }
  }

  private fun createUsedDataList(list: List<BuilderParam<*>>): List<UiBuilderParam<*>> {
    return list.map { UiBuilderParam(paramToTextAdapter.createTextWithValues(it), it) }
  }

  private fun getSupportedFreq(): List<UiFreqParam> {
    return FreqType.entries.map {
      UiFreqParam(
        text = paramToTextAdapter.getFreqText(it),
        freqType = it
      )
    }.sortedBy { it.text }
  }

  private fun getSupportedDays(): List<UiDayParam> {
    return Day.entries.map { DayValue(it) }.map {
      UiDayParam(
        text = paramToTextAdapter.getDayFullText(it),
        dayValue = it
      )
    }
  }

  inner class RefreshTimer(
    private val onRefreshListener: () -> Unit
  ) {
    private var timer: Timer? = null

    fun start() {
      runCatching { timer?.cancel() }

      timer = Timer()
      timer?.scheduleAtFixedRate(
        /* task = */ object : TimerTask() {
          override fun run() {
            onRefreshListener.invoke()
          }
        },
        /* delay = */ 15 * 1000L,
        /* period = */ 15 * 1000L
      )
    }

    fun stop() {
      runCatching { timer?.cancel() }
    }
  }
}

private fun RecurParam.toBuilderParam(): BuilderParam<*> {
  return when (this) {
    is CountRecurParam -> BuilderParam(this.recurParamType, this.value)
    is IntervalRecurParam -> BuilderParam(this.recurParamType, this.value)
    is FreqRecurParam -> BuilderParam(this.recurParamType, this.value)
    is UntilRecurParam -> BuilderParam(this.recurParamType, this.value)
    is ByDayRecurParam -> BuilderParam(this.recurParamType, this.value)
    is ByMonthRecurParam -> BuilderParam(this.recurParamType, this.value)
    is ByMonthDayRecurParam -> BuilderParam(this.recurParamType, this.value)
    is ByHourRecurParam -> BuilderParam(this.recurParamType, this.value)
    is ByMinuteRecurParam -> BuilderParam(this.recurParamType, this.value)
    is ByYearDayRecurParam -> BuilderParam(this.recurParamType, this.value)
    is ByWeekNumberRecurParam -> BuilderParam(this.recurParamType, this.value)
    is BySetPosRecurParam -> BuilderParam(this.recurParamType, this.value)
    is WeekStartRecurParam -> BuilderParam(this.recurParamType, this.value)
  }
}

private fun RecurParamType.toBuilderParam(): BuilderParam<*> {
  return when (this) {
    RecurParamType.COUNT -> BuilderParam(this, 1)
    RecurParamType.INTERVAL -> BuilderParam(this, 1)
    RecurParamType.FREQ -> BuilderParam(this, FreqType.DAILY)
    RecurParamType.UNTIL -> BuilderParam(this, UtcDateTime(LocalDateTime.now()))
    RecurParamType.BYDAY -> BuilderParam(this, emptyList<DayValue>())
    RecurParamType.BYMONTH -> BuilderParam(this, emptyList<Int>())
    RecurParamType.BYMONTHDAY -> BuilderParam(this, emptyList<Int>())
    RecurParamType.BYHOUR -> BuilderParam(this, emptyList<Int>())
    RecurParamType.BYMINUTE -> BuilderParam(this, emptyList<Int>())
    RecurParamType.BYYEARDAY -> BuilderParam(this, emptyList<Int>())
    RecurParamType.BYWEEKNO -> BuilderParam(this, emptyList<Int>())
    RecurParamType.BYSETPOS -> BuilderParam(this, emptyList<Int>())
    RecurParamType.WEEKSTART -> BuilderParam(this, DayValue(Day.MO))
  }
}

data class EventData(
  val startDateTime: LocalDateTime,
  val recurObject: String
)
