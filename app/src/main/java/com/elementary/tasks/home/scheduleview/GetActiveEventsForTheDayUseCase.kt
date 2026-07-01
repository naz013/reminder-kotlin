package com.elementary.tasks.home.scheduleview

import androidx.compose.ui.graphics.Color
import com.elementary.tasks.R
import com.elementary.tasks.core.data.adapter.UiReminderCommonAdapter
import com.elementary.tasks.core.data.ui.reminder.UiAppTarget
import com.elementary.tasks.core.data.ui.reminder.UiCallTarget
import com.elementary.tasks.core.data.ui.reminder.UiEmailTarget
import com.elementary.tasks.core.data.ui.reminder.UiLinkTarget
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.data.ui.reminder.UiSmsTarget
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.eventaction.ResolveReminderEventActionUseCase
import com.elementary.tasks.eventaction.ResolvedEventAction
import com.elementary.tasks.home.HomeEvent
import com.elementary.tasks.reminder.build.formatter.`object`.ShopItemsFormatter
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.ui.common.compose.toColor
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import com.github.naz013.ui.common.theme.ThemeProvider
import kotlinx.coroutines.CoroutineScope
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class GetActiveEventsForTheDayUseCase(
  private val dispatcherProvider: DispatcherProvider,
  private val dateTimeManager: DateTimeManager,
  private val reminderRepository: ReminderRepository,
  private val birthdayRepository: BirthdayRepository,
  private val reminderGroupRepository: ReminderGroupRepository,
  private val modelDateTimeFormatter: ModelDateTimeFormatter,
  private val textProvider: TextProvider,
  private val uiReminderCommonAdapter: UiReminderCommonAdapter,
  private val shopItemsFormatter: ShopItemsFormatter,
  private val contextProvider: ContextProvider,
  private val prefs: Prefs,
  private val resolveReminderEventActionUseCase: ResolveReminderEventActionUseCase,
) {

  suspend operator fun invoke(
    scope: CoroutineScope,
    day: LocalDateTime
  ): List<HomeEvent> {
    val reminders = loadReminders(day)
    val birthdays = loadBirthdays(day)
    return (reminders + birthdays).sortedBy { it.time }
  }

  private suspend fun loadReminders(day: LocalDateTime): List<HomeEvent> {
    val groupsMap = reminderGroupRepository.getAll().associateBy { it.groupUuId }
    val reminders = reminderRepository.getAllTypesInRange(
      active = true,
      removed = false,
      fromTime = dateTimeManager.getDayStart(day),
      toTime = dateTimeManager.getDayEnd(day),
    )
    return reminders.mapNotNull { toHomeEvent(it, groupsMap[it.groupUuId]) }
  }

  private suspend fun loadBirthdays(day: LocalDateTime): List<HomeEvent> {
    val birthdays = birthdayRepository.getAll(dateTimeManager.getBirthdayDayMonth(day))
    return birthdays.map { toHomeEvent(it) }
  }

  private fun toHomeEvent(
    reminder: Reminder,
    group: ReminderGroup?
  ): HomeEvent? {
    val type = UiReminderType(reminder.type)
    val due = uiReminderCommonAdapter.getDue(reminder, type)
    val dueDateTime = due.localDateTime ?: return null

    val color = group?.groupColor?.let {
      ThemeProvider.themedColor(contextProvider.themedContext, it)
    }?.toColor() ?: Color.Black

    return HomeEvent(
      id = reminder.uuId,
      text = createMainText(type, reminder),
      description = createSecondaryText(type, reminder),
      groupName = group?.groupTitle,
      color = color,
      remaining = due.remaining,
      date = dueDateTime.toLocalDate(),
      time = dueDateTime.toLocalTime(),
      action = getActionFromType(type, reminder),
      type = HomeEvent.EventType.Reminder,
    )
  }

  private fun createMainText(
    type: UiReminderType,
    reminder: Reminder
  ): String {
    val summary = reminder.summary
    return summary.ifEmpty {
      val text = reminder.description ?: getTextFromType(type)
      "($text)"
    }
  }

  private fun getTextFromType(
    type: UiReminderType
  ): String {
    return when {
      type.isSubTasks() -> textProvider.getText(R.string.builder_sub_tasks)
      type.isApp() -> textProvider.getText(R.string.open_app)
      type.isLink() -> textProvider.getText(R.string.open_link)
      type.isEmail() -> textProvider.getText(R.string.e_mail)
      type.isSms() -> textProvider.getText(R.string.send_sms)
      type.isCall() -> textProvider.getText(R.string.make_call)
      type.isYearly() -> textProvider.getText(R.string.yearly)
      type.isByWeekday() -> textProvider.getText(R.string.alarm)
      type.isMonthly() -> textProvider.getText(R.string.day_of_month)
      type.isTimer() -> textProvider.getText(R.string.timer)
      else -> textProvider.getText(R.string.schedule_empty_summary)
    }
  }

  private fun createSecondaryText(
    type: UiReminderType,
    reminder: Reminder
  ): String? {
    return if (type.isSubTasks()) {
      formatSubTasks(reminder)
    } else {
      getTargetFromType(type, reminder)
    }
  }

  private fun getTargetFromType(
    type: UiReminderType,
    reminder: Reminder
  ): String? {
    return when (val target = uiReminderCommonAdapter.getTarget(reminder, type)) {
      is UiSmsTarget -> target.target
      is UiCallTarget -> target.target
      is UiAppTarget -> target.name ?: target.target
      is UiLinkTarget -> target.target
      is UiEmailTarget -> {
        target.target + "\n" + target.subject
      }

      else -> null
    }
  }

  private fun getActionFromType(
    type: UiReminderType,
    reminder: Reminder
  ): HomeEvent.EventAction? {
    val resolvedEventAction = resolveReminderEventActionUseCase(reminder) ?: return null
    return when (resolvedEventAction) {
      is ResolvedEventAction.SendSms -> {
        HomeEvent.EventAction(
          icon = HomeEvent.EventAction.SendSms,
          value = resolvedEventAction
        )
      }
      is ResolvedEventAction.MakeCall -> {
        HomeEvent.EventAction(
          icon = HomeEvent.EventAction.MakeCall,
          value = resolvedEventAction
        )
      }
      is ResolvedEventAction.SendEmail -> {
        HomeEvent.EventAction(
          icon = HomeEvent.EventAction.SendEmail,
          value = resolvedEventAction
        )
      }
      is ResolvedEventAction.OpenApp -> {
        HomeEvent.EventAction(
          icon = HomeEvent.EventAction.OpenApp,
          value = resolvedEventAction
        )
      }
      is ResolvedEventAction.OpenLink -> {
        HomeEvent.EventAction(
          icon = HomeEvent.EventAction.OpenLink,
          value = resolvedEventAction
        )
      }
    }
  }

  private fun formatSubTasks(reminder: Reminder): String {
    val itemsToShow = reminder.shoppings.filter { !it.isChecked && !it.isDeleted }
    return if (itemsToShow.size > 5) {
      shopItemsFormatter.format(itemsToShow.take(5)) + "\n..."
    } else {
      shopItemsFormatter.format(itemsToShow)
    }
  }

  private fun toHomeEvent(
    birthday: Birthday,
    nowDateTime: LocalDateTime = dateTimeManager.getCurrentDateTime()
  ): HomeEvent {
    val birthTime = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now()
    val birthdayDate = dateTimeManager.parseBirthdayDate(birthday.date) ?: LocalDate.now()
    val futureBirthdayDateTime = modelDateTimeFormatter.getFutureBirthdayDate(
      birthdayTime = birthTime,
      birthdayDate = birthdayDate,
      nowDateTime = nowDateTime,
      birthday = birthday
    )
    val remainingTime = modelDateTimeFormatter.getBirthdayRemaining(
      futureBirthdayDateTime = futureBirthdayDateTime,
      ignoreYear = birthday.ignoreYear,
      nowDateTime = nowDateTime
    )
    val color = ThemeProvider.colorBirthdayCalendar(contextProvider.context, prefs.birthdayColor)
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

  private fun getBirthdayAction(birthday: Birthday): HomeEvent.EventAction? {
    return ResolvedEventAction.MakeCall(birthday.number).takeIf { birthday.number.isNotBlank() }
      ?.let { HomeEvent.EventAction(HomeEvent.EventAction.MakeCall, it) }
  }

  companion object {
    private const val TAG = "GetActiveEventsForTheDayUseCase"
  }
}
