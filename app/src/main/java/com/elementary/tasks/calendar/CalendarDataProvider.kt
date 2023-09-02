package com.elementary.tasks.calendar

import androidx.lifecycle.Observer
import com.elementary.tasks.core.data.adapter.UiReminderListAdapter
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayListAdapter
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceDateTimeTag
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceManager
import com.elementary.tasks.core.utils.datetime.recurrence.TagType
import com.elementary.tasks.core.utils.plusMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

class CalendarDataProvider(
  birthdaysDao: BirthdaysDao,
  reminderDao: ReminderDao,
  private val uiBirthdayListAdapter: UiBirthdayListAdapter,
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val dateTimeManager: DateTimeManager,
  private val recurrenceManager: RecurrenceManager,
  private val dispatcherProvider: DispatcherProvider,
  private val scope: CoroutineScope = CoroutineScope(Job())
) {

  private val birthdaysLiveData = birthdaysDao.loadAll()
  private val remindersLiveData = reminderDao.loadType(active = true, removed = false)
  private val birthdayObserver: Observer<in List<Birthday>> = Observer { mapBirthdays(it) }
  private val reminderObserver: Observer<in List<Reminder>> = Observer { mapReminders(it) }

  private val monthBirthdayMap = ConcurrentHashMap<LocalDate, MutableList<BirthdayEventModel>>()
  private val dayBirthdayMap = ConcurrentHashMap<LocalDate, MutableList<BirthdayEventModel>>()

  private val monthReminderMap = ConcurrentHashMap<LocalDate, MutableList<ReminderEventModel>>()
  private val monthFutureReminderMap =
    ConcurrentHashMap<LocalDate, MutableList<ReminderEventModel>>()
  private val dayReminderMap = ConcurrentHashMap<LocalDate, MutableList<ReminderEventModel>>()
  private val dayFutureReminderMap = ConcurrentHashMap<LocalDate, MutableList<ReminderEventModel>>()

  private val observersMap = ConcurrentHashMap<Class<*>, DataChangeObserver>()

  private var reminderMappingJob: Job? = null
  private var birthdayMappingJob: Job? = null

  init {
    birthdaysLiveData.observeForever(birthdayObserver)
    remindersLiveData.observeForever(reminderObserver)
  }

  fun getReminderMode(includeReminders: Boolean, calculateFuture: Boolean): ReminderMode {
    return when {
      includeReminders && calculateFuture -> ReminderMode.INCLUDE_FUTURE
      includeReminders -> ReminderMode.INCLUDE
      else -> ReminderMode.DO_NOT_INCLUDE
    }
  }

  fun getByMonth(
    localDate: LocalDate,
    reminderMode: ReminderMode
  ): List<EventModel> {
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
    return (birthdays + reminders).also {
      Timber.d("getByMonth: date=$monthKey, result=${it.size}")
    }
  }

  fun getByDateRange(
    dateStart: LocalDate,
    dateEnd: LocalDate,
    reminderMode: ReminderMode
  ): List<EventModel> {
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

    return resultList.also {
      Timber.d("getByDateRange: dateStart=$dateStart, dateEnd=$dateEnd, result=${it.size}")
    }
  }

  fun observe(clazz: Class<*>, observer: DataChangeObserver) {
    observersMap[clazz] = observer
  }

  fun removeObserver(clazz: Class<*>) {
    observersMap.remove(clazz)
  }

  fun onDestroy() {
    reminderMappingJob?.cancel()
    birthdayMappingJob?.cancel()
    birthdaysLiveData.removeObserver(birthdayObserver)
    remindersLiveData.removeObserver(reminderObserver)
  }

  private fun getNonNullList(
    map: Map<LocalDate, List<EventModel>>,
    key: LocalDate
  ): List<EventModel> {
    return map[key] ?: emptyList()
  }

  private fun notifyObservers() {
    observersMap.values.forEach { it.onCalendarDataChanged() }
  }

  private fun mapReminders(reminders: List<Reminder>) {
    reminderMappingJob?.cancel()
    reminderMappingJob = scope.launch(dispatcherProvider.default()) {
      val millis = System.currentTimeMillis()

      clearReminderMaps()
      val filtered = reminders.filterNot { UiReminderType(it.type).isGpsType() }

      filtered.forEach { mapReminder(it) }

      Timber.d("calculate end, took ${System.currentTimeMillis() - millis} millis")

      withContext(dispatcherProvider.main()) {
        notifyObservers()
      }

      filtered.forEach { mapFutureReminder(it) }

      Timber.d("calculate future end, took ${System.currentTimeMillis() - millis} millis")

      withContext(dispatcherProvider.main()) {
        notifyObservers()
      }
    }
  }

  private fun mapReminder(reminder: Reminder) {
    addReminderToMaps(uiReminderListAdapter.create(reminder))
  }

  private fun mapFutureReminder(reminder: Reminder) {
    val type = reminder.type
    if (Reminder.isBase(type, Reminder.BY_WEEK)) {
      calculateFutureRemindersForWeekType(reminder)
    } else if (Reminder.isBase(type, Reminder.BY_MONTH)) {
      calculateFutureRemindersForMonthType(reminder)
    } else if (Reminder.isBase(type, Reminder.BY_RECUR)) {
      calculateFutureRemindersForRecurType(reminder)
    } else {
      calculateFutureRemindersForOtherTypes(reminder)
    }
  }

  private fun calculateFutureRemindersForOtherTypes(reminder: Reminder) {
    val eventTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: return
    var dateTime = dateTimeManager.fromGmtToLocal(reminder.startTime) ?: return
    val repeatTime = reminder.repeatInterval
    val limit = reminder.repeatLimit.toLong()
    val count = reminder.eventCount

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
      if (eventTime == dateTime) {
        continue
      }
      days++
      val localItem = Reminder(reminder, true).apply {
        this.eventTime = dateTimeManager.getGmtFromDateTime(dateTime)
      }
      addFutureReminderToMaps(uiReminderListAdapter.create(localItem))
    } while (days < max)
  }

  private fun calculateFutureRemindersForRecurType(reminder: Reminder) {
    val dates = runCatching {
      recurrenceManager.parseObject(reminder.recurDataObject)
    }.getOrNull()?.getTagOrNull<RecurrenceDateTimeTag>(TagType.RDATE)?.values

    val baseTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)
    var localItem = reminder

    dates?.mapNotNull { it.dateTime }
      ?.forEach { localDateTime ->
        if (baseTime != localDateTime) {
          localItem = Reminder(localItem, true).apply {
            this.eventTime = dateTimeManager.getGmtFromDateTime(localDateTime)
          }
          addFutureReminderToMaps(uiReminderListAdapter.create(localItem))
        }
      }
  }

  private fun calculateFutureRemindersForMonthType(reminder: Reminder) {
    val eventTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: return
    var dateTime: LocalDateTime

    val limit = reminder.repeatLimit.toLong()
    val count = reminder.eventCount

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
      if (dateTime == baseTime) {
        continue
      }
      days++
      localItem = Reminder(localItem, true).apply {
        this.eventTime = dateTimeManager.getGmtFromDateTime(dateTime)
      }
      addFutureReminderToMaps(uiReminderListAdapter.create(localItem))
    } while (days < max)
  }

  private fun calculateFutureRemindersForWeekType(reminder: Reminder) {
    val eventTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: return
    var dateTime = dateTimeManager.fromGmtToLocal(reminder.startTime) ?: return

    val limit = reminder.repeatLimit.toLong()
    val count = reminder.eventCount

    var days: Long = 0
    var max = Configs.MAX_DAYS_COUNT
    if (reminder.isLimited()) {
      max = limit - count
    }
    val weekdays = reminder.weekdays
    val baseTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: return
    do {
      dateTime = dateTime.plusDays(1)
      if (dateTime == baseTime) {
        continue
      }
      val weekDay = dateTimeManager.localDayOfWeekToOld(eventTime.dayOfWeek)
      if (weekdays[weekDay - 1] == 1) {
        days++
        val localItem = Reminder(reminder, true).apply {
          this.eventTime = dateTimeManager.getGmtFromDateTime(dateTime)
        }
        addFutureReminderToMaps(uiReminderListAdapter.create(localItem))
      }
    } while (days < max)
  }

  private fun mapBirthdays(list: List<Birthday>) {
    birthdayMappingJob?.cancel()
    birthdayMappingJob = scope.launch(dispatcherProvider.default()) {
      clearBirthdayMaps()

      val millis = System.currentTimeMillis()

      list.forEach { mapBirthday(it) }

      Timber.d("mapBirthdays: end, took ${System.currentTimeMillis() - millis} millis")

      withContext(dispatcherProvider.main()) {
        notifyObservers()
      }
    }
  }

  private fun clearBirthdayMaps() {
    monthBirthdayMap.clear()
    dayBirthdayMap.clear()
  }

  private fun clearReminderMaps() {
    monthReminderMap.clear()
    monthFutureReminderMap.clear()
    dayReminderMap.clear()
    dayFutureReminderMap.clear()
  }

  private fun mapBirthday(birthday: Birthday) {
    val date = dateTimeManager.parseBirthdayDate(birthday.date) ?: return
    val time = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now()
    if (birthday.ignoreYear) {
      // Add only one birthday to calendar for Birthday without a year
      uiBirthdayListAdapter.convert(birthday).also { addBirthdayToMaps(it) }
    } else {
      val realNowDateTime = dateTimeManager.getCurrentDateTime()
      var slidingNowDateTime = LocalDateTime.of(date, time)
      while (slidingNowDateTime < realNowDateTime) {
        uiBirthdayListAdapter.convert(
          birthday = birthday,
          nowDateTime = slidingNowDateTime
        ).also { addBirthdayToMaps(it) }
        slidingNowDateTime = slidingNowDateTime.plusYears(1)
      }
    }
  }

  private fun addBirthdayToMaps(uiBirthdayList: UiBirthdayList) {
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

  private fun addReminderToMaps(uiReminderList: UiReminderListData) {
    val futureReminderDateTime = uiReminderList.due?.localDateTime ?: return
    val model = uiReminderList.toEventModel(futureReminderDateTime)
    val dayKey = futureReminderDateTime.toLocalDate()
    val monthKey = createMonthKey(dayKey)
    addReminderToMap(monthKey, model, monthReminderMap)
    addReminderToMap(dayKey, model, dayReminderMap)
  }

  private fun addFutureReminderToMaps(uiReminderList: UiReminderListData) {
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

  interface DataChangeObserver {
    fun onCalendarDataChanged()
  }

  enum class ReminderMode {
    DO_NOT_INCLUDE, INCLUDE, INCLUDE_FUTURE
  }
}
