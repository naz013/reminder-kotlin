package com.elementary.tasks.calendar.data

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.Observer
import com.elementary.tasks.core.data.adapter.UiReminderListAdapter
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayListAdapter
import com.elementary.tasks.core.data.observeTable
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.utils.Configs
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceDateTimeTag
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceManager
import com.elementary.tasks.core.utils.datetime.recurrence.TagType
import com.github.naz013.feature.common.livedata.getNonNullList
import com.github.naz013.feature.common.plusMillis
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.repository.observer.TableChangeListenerFactory
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class CalendarDataEngine(
  birthdayRepository: BirthdayRepository,
  reminderRepository: ReminderRepository,
  private val uiBirthdayListAdapter: UiBirthdayListAdapter,
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val dateTimeManager: DateTimeManager,
  private val recurrenceManager: RecurrenceManager,
  private val dispatcherProvider: DispatcherProvider,
  private val calendarDataEngineBroadcast: CalendarDataEngineBroadcast,
  private val tableChangeListenerFactory: TableChangeListenerFactory
) {

  private val scope: CoroutineScope = CoroutineScope(Job())

  private val birthdaysLiveData = scope.observeTable(
    table = Table.Birthday,
    tableChangeListenerFactory = tableChangeListenerFactory,
    queryProducer = { birthdayRepository.getAll() }
  )
  private val birthdayObserver: Observer<List<Birthday>> = Observer { processData(birthdays = it) }

  private val monthBirthdayMap = mutableMapOf<LocalDate, MutableList<BirthdayEventModel>>()
  private val dayBirthdayMap = mutableMapOf<LocalDate, MutableList<BirthdayEventModel>>()

  private val remindersLiveData = scope.observeTable(
    table = Table.Reminder,
    tableChangeListenerFactory = tableChangeListenerFactory,
    queryProducer = { reminderRepository.getAll(active = true, removed = false) }
  )
  private val reminderObserver: Observer<List<Reminder>> = Observer { processData(reminders = it) }

  private val monthReminderMap = mutableMapOf<LocalDate, MutableList<ReminderEventModel>>()
  private val monthFutureReminderMap = mutableMapOf<LocalDate, MutableList<ReminderEventModel>>()
  private val dayReminderMap = mutableMapOf<LocalDate, MutableList<ReminderEventModel>>()
  private val dayFutureReminderMap = mutableMapOf<LocalDate, MutableList<ReminderEventModel>>()

  private var processingJob: Job? = null

  private var state: EngineState = EngineState.NONE

  @MainThread
  fun initEngine() {
    if (state != EngineState.NONE) {
      return
    }
    birthdaysLiveData.observeForever(birthdayObserver)
    remindersLiveData.observeForever(reminderObserver)
    state = EngineState.INITIALIZING
  }

  @MainThread
  fun destroy() {
    processingJob?.cancel()
    birthdaysLiveData.removeObserver(birthdayObserver)
    remindersLiveData.removeObserver(reminderObserver)
  }

  fun getReminderMode(
    includeReminders: Boolean,
    calculateFuture: Boolean
  ): ReminderMode {
    return when {
      includeReminders && calculateFuture -> ReminderMode.INCLUDE_FUTURE
      includeReminders -> ReminderMode.INCLUDE
      else -> ReminderMode.DO_NOT_INCLUDE
    }
  }

  @WorkerThread
  fun getByMonth(
    localDate: LocalDate,
    reminderMode: ReminderMode
  ): List<EventModel> {
    if (state == EngineState.NONE || state == EngineState.INITIALIZING) {
      return emptyList()
    }
    val monthKey = createMonthKey(localDate)
    val birthdays = getNonNullList(monthBirthdayMap, monthKey)

    val reminders: List<EventModel> = when (reminderMode) {
      ReminderMode.DO_NOT_INCLUDE -> {
        emptyList()
      }

      ReminderMode.INCLUDE -> {
        getNonNullList(monthReminderMap, monthKey)
      }

      ReminderMode.INCLUDE_FUTURE -> {
        getNonNullList(monthReminderMap, monthKey) +
          getNonNullList(monthFutureReminderMap, monthKey)
      }
    }
    return birthdays + reminders
  }

  @WorkerThread
  fun getByDateRange(
    dateStart: LocalDate,
    dateEnd: LocalDate,
    reminderMode: ReminderMode
  ): List<EventModel> {
    if (state == EngineState.NONE || state == EngineState.INITIALIZING) {
      return emptyList()
    }
    val resultList = mutableListOf<EventModel>()
    var date = dateStart
    while (date <= dateEnd) {
      resultList.addAll(getNonNullList(dayBirthdayMap, date))
      val reminders: List<EventModel> = when (reminderMode) {
        ReminderMode.DO_NOT_INCLUDE -> {
          emptyList()
        }

        ReminderMode.INCLUDE -> {
          getNonNullList(dayReminderMap, date)
        }

        ReminderMode.INCLUDE_FUTURE -> {
          getNonNullList(dayReminderMap, date) + getNonNullList(dayFutureReminderMap, date)
        }
      }
      resultList.addAll(reminders)
      date = date.plusDays(1)
    }

    return resultList
  }

  private fun processData(
    reminders: List<Reminder> = remindersLiveData.getNonNullList(),
    birthdays: List<Birthday> = birthdaysLiveData.getNonNullList()
  ) {
    if (state == EngineState.READY) {
      state = EngineState.REFRESH
    }
    processingJob?.cancel()
    processingJob = scope.launch(dispatcherProvider.default()) {
      val millis = System.currentTimeMillis()
      awaitAll(
        async { mapReminders(reminders) },
        async { mapBirthdays(birthdays) }
      )
      val duration = System.currentTimeMillis() - millis
      Logger.d("processData: duration=$duration millis")
      state = EngineState.READY
      withContext(dispatcherProvider.main()) {
        calendarDataEngineBroadcast.sendEvent()
      }
    }
  }

  private fun getNonNullList(
    map: Map<LocalDate, List<EventModel>>,
    key: LocalDate
  ): List<EventModel> {
    return map[key] ?: emptyList()
  }

  private suspend fun mapReminders(
    reminders: List<Reminder>
  ) = withContext(dispatcherProvider.default()) {
    var millis = System.currentTimeMillis()

    val filtered = reminders.filterNot { UiReminderType(it.type).isGpsType() }

    val monthMap = mutableMapOf<LocalDate, MutableList<ReminderEventModel>>()
    val dayMap = mutableMapOf<LocalDate, MutableList<ReminderEventModel>>()

    if (!isActive) {
      Logger.d("mapReminders: cancelled, return")
      return@withContext
    }

    filtered.forEach { mapReminder(it, monthMap, dayMap) }

    if (!isActive) {
      Logger.d("mapReminders: cancelled, return")
      return@withContext
    }

    synchronized(monthReminderMap) {
      monthReminderMap.clear()
      monthReminderMap.putAll(monthMap)
    }

    if (!isActive) {
      Logger.d("mapReminders: cancelled, return")
      return@withContext
    }

    synchronized(dayReminderMap) {
      dayReminderMap.clear()
      dayReminderMap.putAll(dayMap)
    }

    Logger.d("mapReminders: took ${System.currentTimeMillis() - millis} millis")

    if (!isActive) {
      Logger.d("mapReminders: cancelled, return")
      return@withContext
    }

    millis = System.currentTimeMillis()
    monthMap.clear()
    dayMap.clear()

    if (!isActive) {
      Logger.d("mapReminders: cancelled, return")
      return@withContext
    }

    filtered.forEach { mapFutureReminder(it, monthMap, dayMap) }

    if (!isActive) {
      Logger.d("mapReminders: cancelled, return")
      return@withContext
    }

    synchronized(monthFutureReminderMap) {
      monthFutureReminderMap.clear()
      monthFutureReminderMap.putAll(monthMap)
    }

    if (!isActive) {
      Logger.d("mapReminders: cancelled, return")
      return@withContext
    }

    synchronized(dayFutureReminderMap) {
      dayFutureReminderMap.clear()
      dayFutureReminderMap.putAll(dayMap)
    }

    monthMap.clear()
    dayMap.clear()

    Logger.d("mapReminders: future: took ${System.currentTimeMillis() - millis} millis")
  }

  private fun mapReminder(
    reminder: Reminder,
    monthReminderMap: MutableMap<LocalDate, MutableList<ReminderEventModel>>,
    dayReminderMap: MutableMap<LocalDate, MutableList<ReminderEventModel>>
  ) {
    addReminderToMaps(
      uiReminderList = uiReminderListAdapter.create(reminder),
      monthReminderMap = monthReminderMap,
      dayReminderMap = dayReminderMap
    )
  }

  private fun mapFutureReminder(
    reminder: Reminder,
    monthFutureReminderMap: MutableMap<LocalDate, MutableList<ReminderEventModel>>,
    dayFutureReminderMap: MutableMap<LocalDate, MutableList<ReminderEventModel>>
  ) {
    val type = reminder.type
    if (Reminder.isBase(type, Reminder.BY_WEEK)) {
      calculateFutureRemindersForWeekType(reminder, monthFutureReminderMap, dayFutureReminderMap)
    } else if (Reminder.isBase(type, Reminder.BY_MONTH)) {
      calculateFutureRemindersForMonthType(reminder, monthFutureReminderMap, dayFutureReminderMap)
    } else if (Reminder.isBase(type, Reminder.BY_RECUR)) {
      calculateFutureRemindersForRecurType(reminder, monthFutureReminderMap, dayFutureReminderMap)
    } else {
      calculateFutureRemindersForOtherTypes(reminder, monthFutureReminderMap, dayFutureReminderMap)
    }
  }

  private fun maxDateTime(): LocalDateTime {
    return dateTimeManager.getCurrentDateTime()
      .plusYears(5)
      .withDayOfMonth(1)
      .withMonth(1)
  }

  private fun calculateFutureRemindersForOtherTypes(
    reminder: Reminder,
    monthFutureReminderMap: MutableMap<LocalDate, MutableList<ReminderEventModel>>,
    dayFutureReminderMap: MutableMap<LocalDate, MutableList<ReminderEventModel>>
  ) {
    val eventTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: return
    var dateTime = dateTimeManager.fromGmtToLocal(reminder.startTime) ?: return
    val repeatTime = reminder.repeatInterval
    val limit = reminder.repeatLimit.toLong()
    val count = reminder.eventCount
    val maxDateTime = maxDateTime()

    if (repeatTime == 0L) {
      return
    }
    var days: Long = 0
    var max = Configs.MAX_DAYS_COUNT
    if (reminder.isLimited()) {
      max = limit - count
    }
    do {
      dateTime = dateTime.plusMillis(repeatTime)
      if (dateTime > maxDateTime) {
        break
      }
      if (eventTime == dateTime) {
        continue
      }
      days++
      val localItem = Reminder(reminder, true, DateTimeManager.gmtDateTime).apply {
        this.eventTime = dateTimeManager.getGmtFromDateTime(dateTime)
      }
      addFutureReminderToMaps(
        uiReminderList = uiReminderListAdapter.create(localItem),
        monthFutureReminderMap = monthFutureReminderMap,
        dayFutureReminderMap = dayFutureReminderMap
      )
    } while (days < max)
  }

  private fun calculateFutureRemindersForRecurType(
    reminder: Reminder,
    monthFutureReminderMap: MutableMap<LocalDate, MutableList<ReminderEventModel>>,
    dayFutureReminderMap: MutableMap<LocalDate, MutableList<ReminderEventModel>>
  ) {
    val dates = runCatching {
      recurrenceManager.parseObject(reminder.recurDataObject)
    }.getOrNull()?.getTagOrNull<RecurrenceDateTimeTag>(TagType.RDATE)?.values

    val baseTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)
    var localItem = reminder

    dates?.mapNotNull { it.dateTime }
      ?.forEach { localDateTime ->
        if (baseTime != localDateTime) {
          localItem = Reminder(localItem, true, DateTimeManager.gmtDateTime).apply {
            this.eventTime = dateTimeManager.getGmtFromDateTime(localDateTime)
          }
          addFutureReminderToMaps(
            uiReminderList = uiReminderListAdapter.create(localItem),
            monthFutureReminderMap = monthFutureReminderMap,
            dayFutureReminderMap = dayFutureReminderMap
          )
        }
      }
  }

  private fun calculateFutureRemindersForMonthType(
    reminder: Reminder,
    monthFutureReminderMap: MutableMap<LocalDate, MutableList<ReminderEventModel>>,
    dayFutureReminderMap: MutableMap<LocalDate, MutableList<ReminderEventModel>>
  ) {
    val eventTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: return
    var dateTime: LocalDateTime

    val limit = reminder.repeatLimit.toLong()
    val count = reminder.eventCount

    val maxDateTime = maxDateTime()

    var days: Long = 0
    var max = Configs.MAX_DAYS_COUNT
    if (reminder.isLimited()) {
      max = limit - count
    }
    val baseTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: return
    var localItem = reminder
    var fromTime = eventTime
    do {
      dateTime = dateTimeManager.getNewNextMonthDayTime(
        reminder = localItem,
        fromTime = fromTime
      )
      fromTime = dateTime
      if (dateTime > maxDateTime) {
        break
      }
      if (dateTime == baseTime) {
        continue
      }
      days++
      localItem = Reminder(localItem, true, DateTimeManager.gmtDateTime).apply {
        this.eventTime = dateTimeManager.getGmtFromDateTime(dateTime)
      }
      addFutureReminderToMaps(
        uiReminderList = uiReminderListAdapter.create(localItem),
        monthFutureReminderMap = monthFutureReminderMap,
        dayFutureReminderMap = dayFutureReminderMap
      )
    } while (days < max)
  }

  private fun calculateFutureRemindersForWeekType(
    reminder: Reminder,
    monthFutureReminderMap: MutableMap<LocalDate, MutableList<ReminderEventModel>>,
    dayFutureReminderMap: MutableMap<LocalDate, MutableList<ReminderEventModel>>
  ) {
    val eventTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: return
    var dateTime = dateTimeManager.fromGmtToLocal(reminder.startTime) ?: return

    val limit = reminder.repeatLimit.toLong()
    val count = reminder.eventCount

    val maxDateTime = maxDateTime()

    var days: Long = 0
    var max = Configs.MAX_DAYS_COUNT
    if (reminder.isLimited()) {
      max = limit - count
    }
    val weekdays = reminder.weekdays
    val baseTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: return
    do {
      dateTime = dateTime.plusDays(1)
      if (dateTime > maxDateTime) {
        break
      }
      if (dateTime == baseTime) {
        continue
      }
      val weekDay = dateTimeManager.localDayOfWeekToOld(eventTime.dayOfWeek)
      if (weekdays[weekDay - 1] == 1) {
        days++
        val localItem = Reminder(reminder, true, DateTimeManager.gmtDateTime).apply {
          this.eventTime = dateTimeManager.getGmtFromDateTime(dateTime)
        }
        addFutureReminderToMaps(
          uiReminderList = uiReminderListAdapter.create(localItem),
          monthFutureReminderMap = monthFutureReminderMap,
          dayFutureReminderMap = dayFutureReminderMap
        )
      }
    } while (days < max)
  }

  private suspend fun mapBirthdays(
    list: List<Birthday>
  ) = withContext(dispatcherProvider.default()) {
    val dayBirthdaysMap = mutableMapOf<LocalDate, MutableList<BirthdayEventModel>>()
    val monthBirthdaysMap = mutableMapOf<LocalDate, MutableList<BirthdayEventModel>>()

    val millis = System.currentTimeMillis()

    list.forEach { mapBirthday(it, monthBirthdaysMap, dayBirthdaysMap) }

    if (!isActive) {
      Logger.d("mapBirthdays: cancelled, return")
      return@withContext
    }

    synchronized(monthBirthdayMap) {
      monthBirthdayMap.clear()
      monthBirthdayMap.putAll(monthBirthdaysMap)
    }

    if (!isActive) {
      Logger.d("mapBirthdays: cancelled, return")
      return@withContext
    }

    synchronized(dayBirthdayMap) {
      dayBirthdayMap.clear()
      dayBirthdayMap.putAll(dayBirthdaysMap)
    }

    Logger.d("mapBirthdays: took ${System.currentTimeMillis() - millis} millis")
  }

  private fun mapBirthday(
    birthday: Birthday,
    monthBirthdayMap: MutableMap<LocalDate, MutableList<BirthdayEventModel>>,
    dayBirthdayMap: MutableMap<LocalDate, MutableList<BirthdayEventModel>>
  ) {
    val date = dateTimeManager.parseBirthdayDate(birthday.date) ?: return
    val time = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now()
    if (birthday.ignoreYear) {
      // Add only one birthday to calendar for Birthday without a year
      uiBirthdayListAdapter.convert(birthday).also {
        addBirthdayToMaps(it, monthBirthdayMap, dayBirthdayMap)
      }
    } else {
      val realNowDateTimePlus2Year = dateTimeManager.getCurrentDateTime().plusYears(3)
      var slidingDateTime = LocalDateTime.of(date, time)
      while (slidingDateTime < realNowDateTimePlus2Year) {
        uiBirthdayListAdapter.convert(
          birthday = birthday,
          nowDateTime = slidingDateTime
        ).also { addBirthdayToMaps(it, monthBirthdayMap, dayBirthdayMap) }
        slidingDateTime = slidingDateTime.plusYears(1)
      }
    }
  }

  private fun addBirthdayToMaps(
    uiBirthdayList: UiBirthdayList,
    monthBirthdayMap: MutableMap<LocalDate, MutableList<BirthdayEventModel>>,
    dayBirthdayMap: MutableMap<LocalDate, MutableList<BirthdayEventModel>>
  ) {
    val model = uiBirthdayList.toEventModel()
    val futureBirthdayDateTime = model.model.nextBirthdayDate
    val dayKey = futureBirthdayDateTime.toLocalDate()
    val monthKey = createMonthKey(dayKey)
    addBirthdayToMap(monthKey, model, monthBirthdayMap)
    addBirthdayToMap(dayKey, model, dayBirthdayMap)
  }

  private fun addBirthdayToMap(
    key: LocalDate,
    birthdayEventModel: BirthdayEventModel,
    mutableMap: MutableMap<LocalDate, MutableList<BirthdayEventModel>>
  ) {
    val list = mutableMap[key] ?: mutableListOf()
    list.add(birthdayEventModel)
    mutableMap[key] = list
  }

  private fun addReminderToMaps(
    uiReminderList: UiReminderListData,
    monthReminderMap: MutableMap<LocalDate, MutableList<ReminderEventModel>>,
    dayReminderMap: MutableMap<LocalDate, MutableList<ReminderEventModel>>
  ) {
    val futureReminderDateTime = uiReminderList.due?.localDateTime ?: return
    val model = uiReminderList.toEventModel(futureReminderDateTime)
    val dayKey = futureReminderDateTime.toLocalDate()
    val monthKey = createMonthKey(dayKey)
    addReminderToMap(monthKey, model, monthReminderMap)
    addReminderToMap(dayKey, model, dayReminderMap)
  }

  private fun addFutureReminderToMaps(
    uiReminderList: UiReminderListData,
    monthFutureReminderMap: MutableMap<LocalDate, MutableList<ReminderEventModel>>,
    dayFutureReminderMap: MutableMap<LocalDate, MutableList<ReminderEventModel>>
  ) {
    val futureReminderDateTime = uiReminderList.due?.localDateTime ?: return
    val model = uiReminderList.toEventModel(futureReminderDateTime)
    val dayKey = futureReminderDateTime.toLocalDate()
    val monthKey = createMonthKey(dayKey)
    addReminderToMap(monthKey, model, monthFutureReminderMap)
    addReminderToMap(dayKey, model, dayFutureReminderMap)
  }

  private fun addReminderToMap(
    key: LocalDate,
    reminderEventModel: ReminderEventModel,
    mutableMap: MutableMap<LocalDate, MutableList<ReminderEventModel>>
  ) {
    val list = mutableMap[key] ?: mutableListOf()
    list.add(reminderEventModel)
    mutableMap[key] = list
  }

  private fun UiReminderListData.toEventModel(dateTime: LocalDateTime): ReminderEventModel {
    return ReminderEventModel(
      model = this,
      day = dateTime.dayOfMonth,
      monthValue = dateTime.monthValue,
      year = dateTime.year
    )
  }

  private fun UiBirthdayList.toEventModel(): BirthdayEventModel {
    return BirthdayEventModel(
      model = this,
      day = nextBirthdayDate.dayOfMonth,
      monthValue = nextBirthdayDate.monthValue,
      year = nextBirthdayDate.year
    )
  }

  private fun createMonthKey(localDate: LocalDate): LocalDate {
    return localDate.withDayOfMonth(1)
  }

  enum class ReminderMode {
    DO_NOT_INCLUDE, INCLUDE, INCLUDE_FUTURE
  }

  internal enum class EngineState {
    NONE,
    INITIALIZING,
    READY,
    REFRESH
  }
}
