package com.elementary.tasks.reminder.create.fragments.recur

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.models.RecurPreset
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.repository.RecurPresetRepository
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.recurrence.ByDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByHourRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByMinuteRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByMonthDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByMonthRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByWeekNumberRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByYearDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.CountRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.DateTimeStartTag
import com.elementary.tasks.core.utils.datetime.recurrence.DayValue
import com.elementary.tasks.core.utils.datetime.recurrence.FreqRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.FreqType
import com.elementary.tasks.core.utils.datetime.recurrence.IntervalRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.RecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.RecurParamType
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceManager
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceRuleTag
import com.elementary.tasks.core.utils.datetime.recurrence.RuleMap
import com.elementary.tasks.core.utils.datetime.recurrence.Tag
import com.elementary.tasks.core.utils.datetime.recurrence.TagType
import com.elementary.tasks.core.utils.datetime.recurrence.UntilRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.UtcDateTime
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.reminder.create.fragments.recur.adapter.ParamToTextAdapter
import com.elementary.tasks.reminder.create.fragments.recur.intdialog.Number
import com.elementary.tasks.reminder.create.fragments.recur.preview.PreviewData
import com.elementary.tasks.reminder.create.fragments.recur.preview.PreviewItem
import com.elementary.tasks.reminder.create.fragments.recur.preview.Style
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import timber.log.Timber

class RecurBuilderViewModel(
  dispatcherProvider: DispatcherProvider,
  private val paramToTextAdapter: ParamToTextAdapter,
  private val recurrenceManager: RecurrenceManager,
  private val dateTimeManager: DateTimeManager,
  private val recurPresetRepository: RecurPresetRepository
) : BaseProgressViewModel(dispatcherProvider) {

  private val builderParamLogic = BuilderParamLogic()

  private val _availableParams = mutableLiveDataOf<List<UiBuilderParam<*>>>()
  val availableParams = _availableParams.toLiveData()

  private val _usedParams = mutableLiveDataOf<List<UiBuilderParam<*>>>()
  val usedParams = _usedParams.toLiveData()

  private val _supportedFreq = mutableLiveDataOf<List<UiFreqParam>>()
  val supportedFreq = _supportedFreq.toLiveData()

  private val _previewData = mutableLiveDataOf<PreviewData>()
  val previewData = _previewData.toLiveData()

  private val _dateTime = mutableLiveDataOf<LocalDateTime>()
  val dateTime = _dateTime.toLiveData()

  private var nowDateTime = LocalDateTime.now()
  private var startDateTime = LocalDateTime.now()

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      builderParamLogic.setAllParams(RecurParamType.values().map { it.toBuilderParam() })
      builderParamLogic.addOrUpdateParam(BuilderParam(RecurParamType.COUNT, 10))

      _availableParams.postValue(createAvailableDataList(builderParamLogic.getAvailable()))
      _supportedFreq.postValue(getSupportedFreq())

      val used = createUsedDataList(builderParamLogic.getUsed())
      calculateEvents(used)
      _usedParams.postValue(used)
      _dateTime.postValue(startDateTime)
    }
  }

  fun onPresetSelected(presetId: String) {
    Timber.d("onPresetSelected: $presetId")
    viewModelScope.launch(dispatcherProvider.default()) {
      val preset = recurPresetRepository.getById(presetId) ?: return@launch

      val params = runCatching { recurrenceManager.parseObject(preset.recurObject) }.getOrNull()
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
      }
    }
  }

  fun addPreset(recurObject: String, name: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val preset = RecurPreset(
        recurObject = recurObject,
        name = name
      )
      recurPresetRepository.save(preset)
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

    Timber.d("calculateEvents: params = $usedParams")

    val ruleMap = createRuleMap(usedParams)

    Timber.d("calculateEvents: map = $ruleMap")

    val recurObject = runCatching {
      recurrenceManager.createObject(ruleMap)
    }.getOrNull() ?: return null

    val dates = runCatching { recurrenceManager.generate(ruleMap) }.getOrNull() ?: emptyList()
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
      dateTimeManager.fromGmtToLocal(reminder.eventTime)?.also {
        nowDateTime = it
      }

      Timber.d("onEdit: recurDataObject = ${reminder.recurDataObject}")

      val rules = runCatching {
        recurrenceManager.parseObject(reminder.recurDataObject)
      }.getOrNull()

      rules?.map?.values?.forEach { tag ->
        when (tag) {
          is RecurrenceRuleTag -> {
            tag.params.map { it.toBuilderParam() }.also {
              Timber.d("onEdit: builder params = $it")
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

  private fun calculateEvents(used: List<UiBuilderParam<*>>) {
    val (dates, scrollPosition) = generateDates(used)

    _previewData.postValue(PreviewData(scrollPosition, dates))
  }

  private fun generateDates(used: List<UiBuilderParam<*>>): Pair<List<PreviewItem>, Int> {
    val usedParams = used.map { it.param }

    Timber.d("calculateEvents: params = $usedParams")

    val ruleMap = createRuleMap(usedParams)

    return generateFromMap(ruleMap)
  }

  private fun generateFromMap(ruleMap: RuleMap): Pair<List<PreviewItem>, Int> {
    Timber.d("calculateEvents: map = $ruleMap")

    val generated = runCatching { recurrenceManager.generate(ruleMap) }.getOrNull() ?: emptyList()

    return convertForUi(generated)
  }

  private fun findPosition(generated: List<UtcDateTime>): Int {
    if (generated.isEmpty()) return -1

    val nowDateTime = nowDateTime.withSecond(0).withNano(0)

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
    val nowDateTime = nowDateTime.withSecond(0).withNano(0)

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

    Timber.d("calculateEvents: scrollPosition = $scrollPosition, dates = ${dates.size}")
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
    return FreqType.values().map {
      UiFreqParam(
        text = paramToTextAdapter.getFreqText(it),
        freqType = it
      )
    }.sortedBy { it.text }
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
  }
}

private fun RecurParamType.toBuilderParam(): BuilderParam<*> {
  return when (this) {
    RecurParamType.COUNT -> BuilderParam(this, 0)
    RecurParamType.INTERVAL -> BuilderParam(this, 0)
    RecurParamType.FREQ -> BuilderParam(this, FreqType.DAILY)
    RecurParamType.UNTIL -> BuilderParam(this, UtcDateTime(LocalDateTime.now()))
    RecurParamType.BYDAY -> BuilderParam(this, emptyList<DayValue>())
    RecurParamType.BYMONTH -> BuilderParam(this, emptyList<Int>())
    RecurParamType.BYMONTHDAY -> BuilderParam(this, emptyList<Int>())
    RecurParamType.BYHOUR -> BuilderParam(this, emptyList<Int>())
    RecurParamType.BYMINUTE -> BuilderParam(this, emptyList<Int>())
    RecurParamType.BYYEARDAY -> BuilderParam(this, emptyList<Int>())
    RecurParamType.BYWEEKNO -> BuilderParam(this, emptyList<Int>())
  }
}

data class EventData(
  val startDateTime: LocalDateTime,
  val recurObject: String
)
