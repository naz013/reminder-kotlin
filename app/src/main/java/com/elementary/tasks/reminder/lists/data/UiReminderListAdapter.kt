package com.elementary.tasks.reminder.lists.data

import com.elementary.tasks.R
import com.elementary.tasks.core.data.adapter.UiAdapter
import com.elementary.tasks.core.data.adapter.UiReminderCommonAdapter
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.ui.UiTextElement
import com.elementary.tasks.core.data.ui.reminder.UiAppTarget
import com.elementary.tasks.core.data.ui.reminder.UiCallTarget
import com.elementary.tasks.core.data.ui.reminder.UiEmailTarget
import com.elementary.tasks.core.data.ui.reminder.UiLinkTarget
import com.elementary.tasks.core.data.ui.reminder.UiReminderDueData
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.data.ui.reminder.UiSmsTarget
import com.elementary.tasks.core.text.UiTextFormat
import com.elementary.tasks.core.text.UiTextStyle
import com.elementary.tasks.core.utils.datetime.RecurEventManager
import com.elementary.tasks.reminder.build.formatter.factory.PlaceFormatterFactory
import com.elementary.tasks.reminder.build.formatter.`object`.PlaceFormatter
import com.elementary.tasks.reminder.build.formatter.`object`.ShopItemsFormatter
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
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
) : UiAdapter<Reminder, UiReminderList> {
  private val placeFormatter: PlaceFormatter by lazy { placeFormatterFactory.create() }

  override fun create(data: Reminder): UiReminderList {
    val type = UiReminderType(data.type)
    val due = uiReminderCommonAdapter.getDue(data, type)
    val canSkip =
      !type.isGpsType() &&
        (
          data.repeatInterval > 0L ||
            type.isByWeekday() ||
            type.isMonthly() ||
            type.isYearly() ||
            (type.isRecur() && hasNextRecur(data))
        )

    return UiReminderList(
      id = data.uuId,
      noteId = data.noteId.takeIf { it.isNotEmpty() },
      dueDateTime = due.localDateTime,
      mainText = createMainText(type, data),
      secondaryText = createSecondaryText(due, type, data),
      tertiaryText = createTertiaryText(type, data),
      tags =
        listOfNotNull(
          createRepeatBadge(due),
          createRemainingBadge(due),
          createGroupBadge(data),
        ),
      actions =
        UiReminderListActions(
          canSkip = data.isActive && !data.isRemoved && canSkip,
          canDelete = !data.isRemoved,
          canToggle = true,
          canEdit = true,
          canOpen = !data.isRemoved,
        ),
      state =
        UiReminderListState(
          isActive = data.isActive,
          isRemoved = data.isRemoved,
          isGps = type.isGpsType(),
        ),
    )
  }

  /** [group] is resolved by the caller (V2 doesn't denormalize group title/color onto the
   * reminder the way V1 does), otherwise mirrors [create] field for field. */
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
      RecurrenceRule.Once, is RecurrenceRule.Countdown, RecurrenceRule.LocationEnter, RecurrenceRule.LocationExit -> false
    }
  }

  private fun hasNextRecurV2(
    reminder: ReminderV2,
    rule: RecurrenceRule.ICalendar,
  ): Boolean = recurEventManager.getNextAfterDateTime(reminder.schedule.eventDateTime, rule.rrule) != null

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
      is ReminderAction.Email -> "${action.target}\n${action.subject}"
      ReminderAction.Shopping, ReminderAction.None -> null
    }

  private fun createGroupBadge(reminder: Reminder): UiTextElement? =
    uiGroupListAdapter
      .convert(
        reminder.groupUuId,
        reminder.groupColor,
        reminder.groupTitle,
      )?.let {
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

  private fun createTertiaryText(
    type: UiReminderType,
    reminder: Reminder,
  ): UiTextElement? =
    if (type.isSubTasks()) {
      UiTextElement(
        text = formatSubTasks(reminder),
        textFormat =
          UiTextFormat(
            fontSize = unitsConverter.spToPx(14f),
            textStyle = UiTextStyle.NORMAL,
            textColor = colorProvider.getColorOnSurface(),
          ),
      )
    } else {
      getTargetFromType(type, reminder)?.let {
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

  private fun formatSubTasks(reminder: Reminder): String {
    val itemsToShow = reminder.shoppings.filter { !it.isChecked && !it.isDeleted }
    return if (itemsToShow.size > 5) {
      shopItemsFormatter.format(itemsToShow.take(5)) + "\n..."
    } else {
      shopItemsFormatter.format(itemsToShow)
    }
  }

  private fun createSecondaryText(
    dueData: UiReminderDueData?,
    type: UiReminderType,
    data: Reminder,
  ): UiTextElement? {
    return if (type.isGpsType()) {
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

  private fun createMainText(
    type: UiReminderType,
    reminder: Reminder,
  ): UiTextElement {
    val summary = reminder.summary
    return if (summary.isEmpty()) {
      val text = reminder.description ?: getTextFromType(type)
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

  private fun getTextFromType(type: UiReminderType): String =
    when {
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

  private fun getTargetFromType(
    type: UiReminderType,
    reminder: Reminder,
  ): String? =
    when (val target = uiReminderCommonAdapter.getTarget(reminder, type)) {
      is UiSmsTarget -> target.target
      is UiCallTarget -> target.target
      is UiAppTarget -> target.name ?: target.target
      is UiLinkTarget -> target.target
      is UiEmailTarget -> {
        target.target + "\n" + target.subject
      }

      else -> null
    }

  private fun hasNextRecur(reminder: Reminder): Boolean {
    val currentEventTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)
    return recurEventManager.getNextAfterDateTime(
      currentEventTime,
      reminder.recurDataObject,
    ) != null
  }
}
