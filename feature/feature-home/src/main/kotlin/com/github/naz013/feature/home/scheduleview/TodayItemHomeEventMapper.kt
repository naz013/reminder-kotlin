package com.github.naz013.feature.home.scheduleview

import androidx.compose.ui.graphics.Color
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.home.HomeEvent
import com.github.naz013.feature.home.HomePreferences
import com.github.naz013.feature.home.ResolvedEventAction
import com.github.naz013.logic.reminder.today.TodayItem
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.toColor
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import com.github.naz013.ui.common.icon.DrawableCatalog
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.reminder.ShopItemsFormatter
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/** Maps every [TodayItem] type to a [HomeEvent] row. */
internal class TodayItemHomeEventMapper(
  private val dateTimeManager: DateTimeManager,
  private val groupV2Repository: GroupV2Repository,
  private val modelDateTimeFormatter: ModelDateTimeFormatter,
  private val textProvider: TextProvider,
  private val shopItemsFormatter: ShopItemsFormatter,
  private val contextProvider: ContextProvider,
  private val homePreferences: HomePreferences,
  private val googleTaskListRepository: GoogleTaskListRepository,
) {
  suspend operator fun invoke(items: List<TodayItem>, now: LocalDateTime): List<HomeEvent> {
    val groups = groupV2Repository.getAll().associateBy { it.uuId }
    val taskLists = if (items.any { it is TodayItem.Task }) {
      googleTaskListRepository.getAll().associateBy { it.listId }
    } else {
      emptyMap()
    }
    return items.map { item ->
      when (item) {
        is TodayItem.Reminder -> toHomeEvent(item, item.reminder.groupId?.let { groups[it] }, now)
        is TodayItem.Birthday -> toHomeEvent(item, now)
        is TodayItem.CalendarEvent -> toHomeEvent(item)
        is TodayItem.Task -> toHomeEvent(item, taskLists[item.task.listId]?.title)
      }
    }
  }

  private fun toHomeEvent(item: TodayItem.CalendarEvent): HomeEvent {
    val event = item.event
    return HomeEvent(
      id = item.id,
      text = event.title.ifBlank { "(${textProvider.getString(R.string.schedule_empty_summary)})" },
      description = event.description.takeIf { it.isNotBlank() },
      groupName = event.calendarName.takeIf { it.isNotBlank() },
      remaining = null,
      color = Color.Black,
      action = null,
      date = item.dateTime.toLocalDate(),
      time = item.dateTime.toLocalTime(),
      type = HomeEvent.EventType.CalendarEvent,
      iconRes = DrawableCatalog.Fluent.Calendar,
    )
  }

  private fun toHomeEvent(item: TodayItem.Task, listTitle: String?): HomeEvent {
    val task = item.task
    return HomeEvent(
      id = item.id,
      text = task.title.ifBlank { "(${textProvider.getString(R.string.schedule_empty_summary)})" },
      description = task.notes.takeIf { it.isNotBlank() },
      groupName = listTitle?.takeIf { it.isNotBlank() },
      remaining = null,
      color = Color.Black,
      action = null,
      date = item.dateTime.toLocalDate(),
      time = item.dateTime.toLocalTime(),
      type = HomeEvent.EventType.Task,
      iconRes = DrawableCatalog.Fluent.CheckmarkCircle,
    )
  }

  private fun createBadge(item: TodayItem.Reminder): String? {
    val parts = buildList {
      if (item.alsoInCalendar) add(textProvider.getString(R.string.home_also_in_calendar))
      if (item.alsoInTasks) add(textProvider.getString(R.string.home_also_in_tasks))
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
  }

  private fun toHomeEvent(item: TodayItem.Reminder, group: GroupV2?, now: LocalDateTime): HomeEvent {
    val reminder = item.reminder
    val color = group?.color
      ?.let { ThemeProvider.themedColor(contextProvider.themedContext, it) }
      ?.toColor() ?: Color.Black
    return HomeEvent(
      id = reminder.uuId,
      text = createMainText(reminder),
      description = createSecondaryText(reminder),
      groupName = group?.title,
      color = color,
      remaining = modelDateTimeFormatter.getRemaining(item.dateTime, now),
      date = item.dateTime.toLocalDate(),
      time = item.dateTime.toLocalTime(),
      action = getActionFromReminderAction(reminder.action),
      type = HomeEvent.EventType.Reminder,
      isOverdue = item.isOverdue || !item.dateTime.isAfter(now),
      iconRes = DrawableCatalog.Fluent.Alert,
      accentColor = color,
      badge = createBadge(item),
    )
  }

  private fun toHomeEvent(item: TodayItem.Birthday, now: LocalDateTime): HomeEvent {
    val birthday = item.birthday
    val birthTime = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now()
    val birthdayDate = dateTimeManager.parseBirthdayDate(birthday.date) ?: LocalDate.now()
    val futureDateTime = modelDateTimeFormatter.getFutureBirthdayDate(
      birthdayTime = birthTime,
      birthdayDate = birthdayDate,
      nowDateTime = now,
      birthday = birthday,
    )
    val color = ThemeProvider
      .colorBirthdayCalendar(contextProvider.context, homePreferences.birthdayColor)
      .toColor()
    return HomeEvent(
      id = birthday.uuId,
      text = birthday.name,
      description = birthday.number.takeIf { it.isNotBlank() },
      groupName = textProvider.getString(R.string.birthday),
      remaining = modelDateTimeFormatter.getBirthdayRemaining(
        futureBirthdayDateTime = futureDateTime,
        ignoreYear = birthday.ignoreYear,
        nowDateTime = now,
      ),
      color = color,
      action = ResolvedEventAction.MakeCall(birthday.number)
        .takeIf { birthday.number.isNotBlank() }
        ?.let { HomeEvent.EventAction(HomeEvent.EventAction.MakeCall, it) },
      date = futureDateTime.toLocalDate(),
      time = futureDateTime.toLocalTime(),
      type = HomeEvent.EventType.Birthday,
      iconRes = DrawableCatalog.Fluent.FoodCake,
    )
  }

  private fun createMainText(reminder: ReminderV2): String =
    reminder.summary.ifEmpty {
      "(${reminder.description ?: textProvider.getText(R.string.schedule_empty_summary)})"
    }

  private fun createSecondaryText(reminder: ReminderV2): String? =
    when (val action = reminder.action) {
      is ReminderAction.Shopping -> formatSubTasks(reminder)
      is ReminderAction.Call -> action.target
      is ReminderAction.Sms -> action.target
      is ReminderAction.Link -> action.target
      is ReminderAction.App -> action.target
      is ReminderAction.Email -> "${action.target}\n${action.subject}"
      ReminderAction.None -> null
    }

  private fun getActionFromReminderAction(action: ReminderAction): HomeEvent.EventAction? =
    when (action) {
      is ReminderAction.Call -> HomeEvent.EventAction(
        HomeEvent.EventAction.MakeCall,
        ResolvedEventAction.MakeCall(action.target),
      )
      is ReminderAction.Sms -> HomeEvent.EventAction(
        HomeEvent.EventAction.SendSms,
        ResolvedEventAction.SendSms(action.target, action.subject),
      )
      is ReminderAction.Link -> HomeEvent.EventAction(
        HomeEvent.EventAction.OpenLink,
        ResolvedEventAction.OpenLink(action.target),
      )
      is ReminderAction.App -> HomeEvent.EventAction(
        HomeEvent.EventAction.OpenApp,
        ResolvedEventAction.OpenApp(action.target),
      )
      is ReminderAction.Email -> HomeEvent.EventAction(
        HomeEvent.EventAction.SendEmail,
        ResolvedEventAction.SendEmail(action.target, action.subject, body = "", attachmentFile = null),
      )
      ReminderAction.Shopping, ReminderAction.None -> null
    }

  private fun formatSubTasks(reminder: ReminderV2): String {
    val itemsToShow = reminder.shoppingItems.filter { !it.isChecked && !it.isDeleted }
    return if (itemsToShow.size > MAX_SHOPPING_ITEMS) {
      shopItemsFormatter.formatV2(itemsToShow.take(MAX_SHOPPING_ITEMS)) + "\n..."
    } else {
      shopItemsFormatter.formatV2(itemsToShow)
    }
  }

  companion object {
    private const val MAX_SHOPPING_ITEMS = 5
  }
}
