package com.elementary.tasks.reminder.lists.data

import com.elementary.tasks.R
import com.elementary.tasks.core.data.adapter.UiReminderCommonAdapter
import com.github.naz013.ui.group.UiGroupListAdapter
import com.elementary.tasks.core.data.ui.UiTextElement
import com.elementary.tasks.core.data.ui.reminder.UiReminderDueData
import com.elementary.tasks.core.text.UiTextFormat
import com.elementary.tasks.core.text.UiTextStyle
import com.github.naz013.logic.reminder.RecurEventManager
import com.elementary.tasks.reminder.build.formatter.factory.PlaceFormatterFactory
import com.elementary.tasks.reminder.build.formatter.`object`.PlaceFormatter
import com.elementary.tasks.reminder.build.formatter.`object`.ShopItemsFormatter
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.ui.common.UnitsConverter
import com.github.naz013.ui.common.adjustAlpha
import com.github.naz013.ui.common.theme.ColorProvider

class UiReminderListAdapter(
  private val uiReminderCommonAdapter: UiReminderCommonAdapter,
  private val unitsConverter: UnitsConverter,
  private val colorProvider: ColorProvider,
  private val textProvider: TextProvider,
  private val shopItemsFormatter: ShopItemsFormatter,
  private val uiGroupListAdapter: UiGroupListAdapter,
  private val dateTimeManager: DateTimeManager,
  private val recurEventManager: RecurEventManager,
  private val placeFormatterFactory: PlaceFormatterFactory,
) {
  private val placeFormatter: PlaceFormatter by lazy { placeFormatterFactory.create() }

  /** [group] is resolved by the caller (V2 doesn't denormalize group title/color onto the
   * reminder the way V1 does). */
  fun createV2(
    data: ReminderV2,
    group: GroupV2?,
  ): UiReminderList {
    val due = uiReminderCommonAdapter.getDueV2(data)
    return UiReminderList(
      id = data.uuId,
      noteId = data.noteId.takeIf { it.isNotEmpty() },
      dueDateTime = due.localDateTime,
      mainText = createMainTextV2(data),
      secondaryText = createSecondaryTextV2(due, data),
      tertiaryText = createTertiaryTextV2(data),
      tags =
        listOfNotNull(
          createRepeatBadge(due),
          createRemainingBadge(due),
          createGroupBadgeV2(group),
        ),
      actions =
        UiReminderListActions(
          canSkip = data.isActive && !data.isRemoved && canSkipV2(data),
          canDelete = !data.isRemoved,
          canToggle = true,
          canEdit = true,
          canOpen = !data.isRemoved,
        ),
      state =
        UiReminderListState(
          isActive = data.isActive,
          isRemoved = data.isRemoved,
          isGps = data.location != null,
        ),
    )
  }

  private fun canSkipV2(data: ReminderV2): Boolean {
    if (data.location != null) return false
    return when (val rule = data.recurrence) {
      is RecurrenceRule.Weekly,
      is RecurrenceRule.Monthly,
      is RecurrenceRule.RelativeMonthly,
      is RecurrenceRule.Yearly,
      is RecurrenceRule.Daily,
      -> true

      is RecurrenceRule.ICalendar -> hasNextRecurV2(data, rule)
      is RecurrenceRule.Countdown -> rule.repeatInterval > 0
      RecurrenceRule.Once, RecurrenceRule.LocationEnter, RecurrenceRule.LocationExit -> false
    }
  }

  private fun hasNextRecurV2(
    reminder: ReminderV2,
    rule: RecurrenceRule.ICalendar,
  ): Boolean {
    val localEventDateTime = reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) }
    return recurEventManager.getNextAfterDateTime(localEventDateTime, rule.rrule) != null
  }

  private fun createGroupBadgeV2(group: GroupV2?): UiTextElement? =
    group?.let { uiGroupListAdapter.convert(it) }?.let {
      UiTextElement(
        text = it.title,
        textFormat =
          UiTextFormat(
            fontSize = unitsConverter.spToPx(12f),
            textStyle = UiTextStyle.BOLD,
            textColor = colorProvider.getColorOnSecondaryContainer(),
          ),
      )
    }

  private fun createTertiaryTextV2(reminder: ReminderV2): UiTextElement? =
    if (reminder.action is ReminderAction.Shopping) {
      UiTextElement(
        text = formatSubTasksV2(reminder),
        textFormat =
          UiTextFormat(
            fontSize = unitsConverter.spToPx(14f),
            textStyle = UiTextStyle.NORMAL,
            textColor = colorProvider.getColorOnSurface(),
          ),
      )
    } else {
      getTargetFromActionV2(reminder.action)?.let {
        UiTextElement(
          text = it,
          textFormat =
            UiTextFormat(
              fontSize = unitsConverter.spToPx(14f),
              textStyle = UiTextStyle.NORMAL,
              textColor = colorProvider.getColorOnSurface(),
            ),
        )
      }
    }

  private fun formatSubTasksV2(reminder: ReminderV2): String {
    val itemsToShow = reminder.shoppingItems.filter { !it.isChecked && !it.isDeleted }
    return if (itemsToShow.size > 5) {
      shopItemsFormatter.formatV2(itemsToShow.take(5)) + "\n..."
    } else {
      shopItemsFormatter.formatV2(itemsToShow)
    }
  }

  private fun createSecondaryTextV2(
    dueData: UiReminderDueData?,
    data: ReminderV2,
  ): UiTextElement? {
    return if (data.location != null) {
      val place = data.places.firstOrNull() ?: return null
      UiTextElement(
        text = placeFormatter.format(place),
        textFormat =
          UiTextFormat(
            fontSize = unitsConverter.spToPx(14f),
            textStyle = UiTextStyle.NORMAL,
            textColor = colorProvider.getColorOnSurface(),
          ),
      )
    } else {
      if (dueData?.localDateTime == null) {
        return null
      }
      UiTextElement(
        text = dueData.formattedDateTime ?: "",
        textFormat =
          UiTextFormat(
            fontSize = unitsConverter.spToPx(14f),
            textStyle = UiTextStyle.NORMAL,
            textColor = colorProvider.getColorOnSurface(),
          ),
      )
    }
  }

  private fun createMainTextV2(reminder: ReminderV2): UiTextElement {
    val summary = reminder.summary
    return if (summary.isEmpty()) {
      val text = reminder.description ?: getTextFromRecurrenceV2(reminder)
      UiTextElement(
        text = "($text)",
        textFormat =
          UiTextFormat(
            fontSize = unitsConverter.spToPx(16f),
            textStyle = UiTextStyle.ITALIC,
            textColor = colorProvider.getColorOnSurface().adjustAlpha(75),
          ),
      )
    } else {
      UiTextElement(
        text = summary,
        textFormat =
          UiTextFormat(
            fontSize = unitsConverter.spToPx(16f),
            textStyle = UiTextStyle.NORMAL,
            textColor = colorProvider.getColorOnSurface(),
          ),
      )
    }
  }

  private fun getTextFromRecurrenceV2(reminder: ReminderV2): String =
    when (reminder.action) {
      is ReminderAction.Shopping -> textProvider.getText(R.string.builder_sub_tasks)
      is ReminderAction.Link -> textProvider.getText(R.string.open_link)
      is ReminderAction.App -> textProvider.getText(R.string.application)
      is ReminderAction.Email -> textProvider.getText(R.string.e_mail)
      is ReminderAction.Sms -> textProvider.getText(R.string.send_sms)
      is ReminderAction.Call -> textProvider.getText(R.string.make_call)
      ReminderAction.None ->
        when (reminder.recurrence) {
          is RecurrenceRule.Yearly -> textProvider.getText(R.string.yearly)
          is RecurrenceRule.Weekly -> textProvider.getText(R.string.alarm)
          is RecurrenceRule.Monthly, is RecurrenceRule.RelativeMonthly -> textProvider.getText(R.string.day_of_month)
          is RecurrenceRule.Countdown -> textProvider.getText(R.string.timer)
          else -> textProvider.getText(R.string.schedule_empty_summary)
        }
    }

  private fun getTargetFromActionV2(action: ReminderAction): String? =
    when (action) {
      is ReminderAction.Sms -> action.target
      is ReminderAction.Call -> action.target
      is ReminderAction.Link -> action.target
      is ReminderAction.App -> action.target
      is ReminderAction.Email -> "${action.target}\n${action.subject}"
      ReminderAction.Shopping, ReminderAction.None -> null
    }

  private fun createRepeatBadge(dueData: UiReminderDueData?): UiTextElement? {
    if (dueData?.localDateTime == null) {
      return null
    }
    return UiTextElement(
      text = dueData.repeat,
      textFormat =
        UiTextFormat(
          fontSize = unitsConverter.spToPx(12f),
          textStyle = UiTextStyle.BOLD,
          textColor = colorProvider.getColorOnSecondaryContainer(),
        ),
    )
  }

  private fun createRemainingBadge(dueData: UiReminderDueData?): UiTextElement? {
    if (dueData?.localDateTime == null) {
      return null
    }
    return dueData.remaining?.let {
      UiTextElement(
        text = it,
        textFormat =
          UiTextFormat(
            fontSize = unitsConverter.spToPx(12f),
            textStyle = UiTextStyle.BOLD,
            textColor = colorProvider.getColorOnSecondaryContainer(),
          ),
      )
    }
  }
}
