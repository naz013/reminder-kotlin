package com.elementary.tasks.home.scheduleview

import androidx.compose.ui.graphics.Color
import com.elementary.tasks.R
import com.elementary.tasks.eventaction.ResolvedEventAction
import com.elementary.tasks.home.HomeEvent
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.reminder.build.formatter.`object`.ShopItemsFormatter
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.ui.common.compose.toColor
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.usecase.reminders.GetRemindersV2InRangeUseCase
import kotlinx.coroutines.CoroutineScope
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class GetActiveEventsForTheDayUseCase(
  private val dispatcherProvider: DispatcherProvider,
  private val dateTimeManager: DateTimeManager,
  private val getRemindersV2InRangeUseCase: GetRemindersV2InRangeUseCase,
  private val birthdayRepository: BirthdayRepository,
  private val groupV2Repository: GroupV2Repository,
  private val modelDateTimeFormatter: ModelDateTimeFormatter,
  private val textProvider: TextProvider,
  private val shopItemsFormatter: ShopItemsFormatter,
  private val contextProvider: ContextProvider,
  private val prefs: Prefs,
) {
  suspend operator fun invoke(
    scope: CoroutineScope,
    day: LocalDateTime,
  ): List<HomeEvent> {
    val reminders = loadReminders(day)
    val birthdays = loadBirthdays(day)
    return (reminders + birthdays).sortedBy { it.time }
  }

  private suspend fun loadReminders(day: LocalDateTime): List<HomeEvent> {
    val groupsMap = groupV2Repository.getAll().associateBy { it.uuId }
    val dayStart = day.toLocalDate().atStartOfDay()
    val reminders = getRemindersV2InRangeUseCase(
      dateTimeManager.localToUtc(dayStart),
      dateTimeManager.localToUtc(dayStart.plusDays(1)),
    )
    return reminders.mapNotNull { toHomeEvent(it, it.groupId?.let { groupId -> groupsMap[groupId] }) }
  }

  private suspend fun loadBirthdays(day: LocalDateTime): List<HomeEvent> {
    val birthdays = birthdayRepository.getAll(dateTimeManager.getBirthdayDayMonth(day))
    return birthdays.map { toHomeEvent(it) }
  }

  private fun toHomeEvent(
    reminder: ReminderV2,
    group: GroupV2?,
  ): HomeEvent? {
    val dueDateTime = reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) } ?: return null

    val color =
      group
        ?.color
        ?.let {
          ThemeProvider.themedColor(contextProvider.themedContext, it)
        }?.toColor() ?: Color.Black

    return HomeEvent(
      id = reminder.uuId,
      text = createMainText(reminder),
      description = createSecondaryText(reminder),
      groupName = group?.title,
      color = color,
      remaining = modelDateTimeFormatter.getRemaining(dueDateTime, dateTimeManager.getCurrentDateTime()),
      date = dueDateTime.toLocalDate(),
      time = dueDateTime.toLocalTime(),
      action = getActionFromReminderAction(reminder.action),
      type = HomeEvent.EventType.Reminder,
    )
  }

  private fun createMainText(reminder: ReminderV2): String {
    val summary = reminder.summary
    return summary.ifEmpty {
      val text = reminder.description ?: textProvider.getText(R.string.schedule_empty_summary)
      "($text)"
    }
  }

  private fun createSecondaryText(reminder: ReminderV2): String? =
    if (reminder.action is ReminderAction.Shopping) {
      formatSubTasks(reminder)
    } else {
      getTargetFromAction(reminder.action)
    }

  private fun getTargetFromAction(action: ReminderAction): String? =
    when (action) {
      is ReminderAction.Call -> action.target
      is ReminderAction.Sms -> action.target
      is ReminderAction.Link -> action.target
      is ReminderAction.App -> action.target
      is ReminderAction.Email -> "${action.target}\n${action.subject}"
      ReminderAction.Shopping, ReminderAction.None -> null
    }

  private fun getActionFromReminderAction(action: ReminderAction): HomeEvent.EventAction? {
    val resolvedEventAction = when (action) {
      is ReminderAction.Call -> ResolvedEventAction.MakeCall(action.target)
      is ReminderAction.Sms -> ResolvedEventAction.SendSms(action.target, action.subject)
      is ReminderAction.Link -> ResolvedEventAction.OpenLink(action.target)
      is ReminderAction.App -> ResolvedEventAction.OpenApp(action.target)
      is ReminderAction.Email -> ResolvedEventAction.SendEmail(action.target, action.subject, body = "", attachmentFile = null)
      ReminderAction.Shopping, ReminderAction.None -> null
    } ?: return null
    return when (resolvedEventAction) {
      is ResolvedEventAction.SendSms -> {
        HomeEvent.EventAction(
          icon = HomeEvent.EventAction.SendSms,
          value = resolvedEventAction,
        )
      }
      is ResolvedEventAction.MakeCall -> {
        HomeEvent.EventAction(
          icon = HomeEvent.EventAction.MakeCall,
          value = resolvedEventAction,
        )
      }
      is ResolvedEventAction.SendEmail -> {
        HomeEvent.EventAction(
          icon = HomeEvent.EventAction.SendEmail,
          value = resolvedEventAction,
        )
      }
      is ResolvedEventAction.OpenApp -> {
        HomeEvent.EventAction(
          icon = HomeEvent.EventAction.OpenApp,
          value = resolvedEventAction,
        )
      }
      is ResolvedEventAction.OpenLink -> {
        HomeEvent.EventAction(
          icon = HomeEvent.EventAction.OpenLink,
          value = resolvedEventAction,
        )
      }
    }
  }

  private fun formatSubTasks(reminder: ReminderV2): String {
    val itemsToShow = reminder.shoppingItems.filter { !it.isChecked && !it.isDeleted }
    return if (itemsToShow.size > 5) {
      shopItemsFormatter.formatV2(itemsToShow.take(5)) + "\n..."
    } else {
      shopItemsFormatter.formatV2(itemsToShow)
    }
  }

  private fun toHomeEvent(
    birthday: Birthday,
    nowDateTime: LocalDateTime = dateTimeManager.getCurrentDateTime(),
  ): HomeEvent {
    val birthTime = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now()
    val birthdayDate = dateTimeManager.parseBirthdayDate(birthday.date) ?: LocalDate.now()
    val futureBirthdayDateTime =
      modelDateTimeFormatter.getFutureBirthdayDate(
        birthdayTime = birthTime,
        birthdayDate = birthdayDate,
        nowDateTime = nowDateTime,
        birthday = birthday,
      )
    val remainingTime =
      modelDateTimeFormatter.getBirthdayRemaining(
        futureBirthdayDateTime = futureBirthdayDateTime,
        ignoreYear = birthday.ignoreYear,
        nowDateTime = nowDateTime,
      )
    val color =
      ThemeProvider
        .colorBirthdayCalendar(contextProvider.context, prefs.birthdayColor)
        .toColor()
    return HomeEvent(
      id = birthday.uuId,
      text = birthday.name,
      description = birthday.number.takeIf { it.isNotBlank() },
      groupName = textProvider.getString(R.string.birthday),
      remaining = remainingTime,
      color = color,
      action = getBirthdayAction(birthday),
      date = futureBirthdayDateTime.toLocalDate(),
      time = futureBirthdayDateTime.toLocalTime(),
      type = HomeEvent.EventType.Birthday,
    )
  }

  private fun getBirthdayAction(birthday: Birthday): HomeEvent.EventAction? =
    ResolvedEventAction
      .MakeCall(birthday.number)
      .takeIf { birthday.number.isNotBlank() }
      ?.let { HomeEvent.EventAction(HomeEvent.EventAction.MakeCall, it) }

  companion object {
    private const val TAG = "GetActiveEventsForTheDayUseCase"
  }
}
