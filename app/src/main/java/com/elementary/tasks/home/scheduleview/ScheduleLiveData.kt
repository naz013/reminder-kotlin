package com.elementary.tasks.home.scheduleview

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.switchMap
import com.elementary.tasks.R
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.ScheduleTimes
import com.elementary.tasks.core.utils.getNonNullList
import com.elementary.tasks.core.utils.getNonNullMap
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.home.scheduleview.data.UiBirthdayScheduleListAdapter
import com.elementary.tasks.home.scheduleview.data.UiReminderScheduleListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class ScheduleLiveData(
  private val dispatcherProvider: DispatcherProvider,
  private val dateTimeManager: DateTimeManager,
  private val reminderDao: ReminderDao,
  private val birthdaysDao: BirthdaysDao,
  private val uiBirthdayScheduleListAdapter: UiBirthdayScheduleListAdapter,
  private val textProvider: TextProvider,
  private val reminderGoogleTaskLiveData: ReminderGoogleTaskLiveData,
  private val uiReminderScheduleListAdapter: UiReminderScheduleListAdapter
) : MediatorLiveData<List<ScheduleModel>>(), KoinComponent {

  private var selectedDateTime: LocalDateTime = LocalDateTime.now()
  private var transformJob: Job? = null
  private val scope = CoroutineScope(Job())

  private val _dateLiveData = mutableLiveDataOf<DateObject>()
  private val reminderSource = _dateLiveData.switchMap {
    reminderDao.loadAllTypesInRange(
      active = true,
      removed = false,
      fromTime = it.startDateTime,
      toTime = it.endDateTime
    )
  }
  private val birthdaySource = _dateLiveData.switchMap {
    birthdaysDao.loadAll(it.birthdayDayMonth)
  }
  private val noteSource = reminderSource.switchMap {
    get<ReminderNoteLiveData> { parametersOf(it) }
  }

  override fun onActive() {
    super.onActive()
    addSource(reminderSource) { transform(reminders = it) }
    addSource(birthdaySource) { transform(birthdays = it) }
    addSource(reminderGoogleTaskLiveData) { transform(googleTasksMap = it) }
    addSource(noteSource) { transform(notesMap = it) }
  }

  override fun onInactive() {
    super.onInactive()
    removeSource(reminderSource)
    removeSource(birthdaySource)
    removeSource(reminderGoogleTaskLiveData)
    removeSource(noteSource)
  }

  suspend fun onDateSelected(dateTime: LocalDateTime) {
    withContext(dispatcherProvider.default()) {
      selectedDateTime = dateTime
      val dateObject = DateObject(
        dateTime = dateTime,
        startDateTime = dateTimeManager.getDayStart(dateTime),
        endDateTime = dateTimeManager.getDayEnd(dateTime),
        birthdayDayMonth = dateTimeManager.getBirthdayDayMonth(dateTime)
      )
      _dateLiveData.postValue(dateObject)
    }
  }

  private fun transform(
    reminders: List<Reminder> = reminderSource.getNonNullList(),
    birthdays: List<Birthday> = birthdaySource.getNonNullList(),
    googleTasksMap: Map<String, UiGoogleTaskList> = reminderGoogleTaskLiveData.getNonNullMap(),
    notesMap: Map<String, UiNoteList> = noteSource.getNonNullMap()
  ) {
    val dateTime = _dateLiveData.value?.dateTime ?: LocalDateTime.now()
    transformJob?.cancel()
    transformJob = scope.launch(dispatcherProvider.default()) {
      val events = mutableListOf<ScheduleModel>().apply {
        addAll(reminders.map { it.toScheduleModel(googleTasksMap, notesMap) })
        addAll(
          birthdays.map { it.toScheduleModel(selectedDateTime) }
            .filter { it.dateTime.year == dateTime.year }
        )
      }.sortedBy { it.dateTime?.toLocalTime() }
        .let { addMorningHeader(it) }
        .let { addNoonHeader(it) }
        .let { addEveningHeader(it) }
      postValue(events)
    }
  }

  private fun addHeader(
    events: List<ScheduleModel>,
    headerScheduleModel: HeaderScheduleModel,
    predicate: (ScheduleModel) -> Boolean
  ): List<ScheduleModel> {
    val result = mutableListOf<ScheduleModel>()
    var headerAdded = false
    for (i in events.indices) {
      if (!headerAdded && predicate(events[i])) {
        result.add(headerScheduleModel)
        headerAdded = true
      }
      result.add(events[i])
    }
    return result
  }

  private fun addMorningHeader(events: List<ScheduleModel>): List<ScheduleModel> {
    val startMorningTime = newDateTime(selectedDateTime.toLocalDate(), ScheduleTimes.MORNING)
    val noonTime = newDateTime(startMorningTime.toLocalDate(), ScheduleTimes.NOON)
    return addHeader(
      events,
      getHeader(textProvider.getText(R.string.morning), HeaderTimeType.MORNING)
    ) {
      it.dateTime != null && startMorningTime <= it.dateTime && noonTime > it.dateTime
    }
  }

  private fun addNoonHeader(events: List<ScheduleModel>): List<ScheduleModel> {
    val noonTime = newDateTime(selectedDateTime.toLocalDate(), ScheduleTimes.NOON)
    val eveningTime = newDateTime(noonTime.toLocalDate(), ScheduleTimes.EVENING)
    return addHeader(
      events,
      getHeader(textProvider.getText(R.string.schedule_noon), HeaderTimeType.NOON)
    ) {
      it.dateTime != null && noonTime <= it.dateTime && eveningTime > it.dateTime
    }
  }

  private fun addEveningHeader(events: List<ScheduleModel>): List<ScheduleModel> {
    val eveningTime = newDateTime(selectedDateTime.toLocalDate(), ScheduleTimes.EVENING)
    val startMorningTime = newDateTime(
      eveningTime.plusDays(1).toLocalDate(),
      ScheduleTimes.NOON
    )
    return addHeader(
      events,
      getHeader(textProvider.getText(R.string.evening), HeaderTimeType.EVENING)
    ) {
      it.dateTime != null && eveningTime <= it.dateTime && startMorningTime > it.dateTime
    }
  }

  private fun newDateTime(date: LocalDate, time: LocalTime): LocalDateTime {
    return LocalDateTime.of(date, time)
      .withSecond(0)
      .withNano(0)
  }

  private fun getHeader(text: String, headerTimeType: HeaderTimeType): HeaderScheduleModel {
    return HeaderScheduleModel(text, headerTimeType)
  }

  private fun Birthday.toScheduleModel(
    dateTime: LocalDateTime = dateTimeManager.getCurrentDateTime()
  ): BirthdayScheduleModel {
    return BirthdayScheduleModel(
      data = uiBirthdayScheduleListAdapter.create(this, dateTime)
    )
  }

  private fun Reminder.toScheduleModel(
    googleTasksMap: Map<String, UiGoogleTaskList>,
    notesMap: Map<String, UiNoteList>
  ): ScheduleModel {
    val reminder = uiReminderScheduleListAdapter.create(this)
    return when {
      googleTasksMap.containsKey(reminder.id) -> {
        googleTasksMap[reminder.id]?.let {
          ReminderAndGoogleTaskScheduleModel(
            reminder = reminder,
            googleTask = it
          )
        } ?: ReminderScheduleModel(reminder)
      }
      reminder.noteId != null && notesMap.containsKey(reminder.noteId) -> {
        reminder.noteId.let { notesMap[it] }
          ?.let {
            ReminderAndNoteScheduleModel(
              reminder = reminder,
              note = it
            )
          } ?: ReminderScheduleModel(reminder)
      }
      else -> {
        ReminderScheduleModel(reminder)
      }
    }
  }

  internal data class DateObject(
    val dateTime: LocalDateTime,
    val startDateTime: String,
    val endDateTime: String,
    val birthdayDayMonth: String
  )
}
