package com.elementary.tasks.home.scheduleview.data

import com.elementary.tasks.R
import com.elementary.tasks.core.data.adapter.UiAdapter
import com.elementary.tasks.core.data.adapter.UiReminderCommonAdapter
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.UiTextElement
import com.elementary.tasks.core.data.ui.reminder.UiAppTarget
import com.elementary.tasks.core.data.ui.reminder.UiCallTarget
import com.elementary.tasks.core.data.ui.reminder.UiEmailTarget
import com.elementary.tasks.core.data.ui.reminder.UiLinkTarget
import com.elementary.tasks.core.data.ui.reminder.UiReminderDueData
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.data.ui.reminder.UiSmsTarget
import com.elementary.tasks.core.os.ColorProvider
import com.elementary.tasks.core.os.UnitsConverter
import com.elementary.tasks.core.text.UiTextFormat
import com.elementary.tasks.core.text.UiTextStyle
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.adjustAlpha
import com.elementary.tasks.reminder.build.formatter.ShopItemsFormatter

class UiReminderScheduleListAdapter(
  private val uiReminderCommonAdapter: UiReminderCommonAdapter,
  private val unitsConverter: UnitsConverter,
  private val colorProvider: ColorProvider,
  private val textProvider: TextProvider,
  private val shopItemsFormatter: ShopItemsFormatter,
  private val uiGroupListAdapter: UiGroupListAdapter
) : UiAdapter<Reminder, UiReminderScheduleList> {

  override fun create(data: Reminder): UiReminderScheduleList {
    val type = UiReminderType(data.type)
    val due = uiReminderCommonAdapter.getDue(data, type)
    return UiReminderScheduleList(
      id = data.uuId,
      dueDateTime = due.localDateTime,
      noteId = data.noteId.takeIf { it.isNotEmpty() },
      mainText = createMainText(type, data),
      secondaryText = createSecondaryText(type, data),
      timeText = createTimeText(due),
      tags = listOfNotNull(
        createRemainingBadge(due),
        createGroupBadge(data)
      )
    )
  }

  private fun createGroupBadge(
    reminder: Reminder
  ): UiTextElement {
    return uiGroupListAdapter.convert(
      reminder.groupUuId,
      reminder.groupColor,
      reminder.groupTitle
    ).let {
      UiTextElement(
        text = it.title,
        textFormat = UiTextFormat(
          fontSize = unitsConverter.spToPx(12f),
          textStyle = UiTextStyle.BOLD,
          textColor = colorProvider.getColorOnSecondaryContainer()
        )
      )
    }
  }

  private fun createRemainingBadge(
    dueData: UiReminderDueData?
  ): UiTextElement? {
    return dueData?.remaining?.let {
      UiTextElement(
        text = it,
        textFormat = UiTextFormat(
          fontSize = unitsConverter.spToPx(12f),
          textStyle = UiTextStyle.BOLD,
          textColor = colorProvider.getColorOnSecondaryContainer()
        )
      )
    }
  }

  private fun createSecondaryText(
    type: UiReminderType,
    reminder: Reminder
  ): UiTextElement? {
    return if (type.isSubTasks()) {
      UiTextElement(
        text = formatSubTasks(reminder),
        textFormat = UiTextFormat(
          fontSize = unitsConverter.spToPx(14f),
          textStyle = UiTextStyle.NORMAL,
          textColor = colorProvider.getColorOnSurface()
        )
      )
    } else {
      getTargetFromType(type, reminder)?.let {
        UiTextElement(
          text = it,
          textFormat = UiTextFormat(
            fontSize = unitsConverter.spToPx(14f),
            textStyle = UiTextStyle.NORMAL,
            textColor = colorProvider.getColorOnSurface()
          )
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

  private fun createTimeText(dueData: UiReminderDueData?): UiTextElement {
    val time = dueData?.formattedTime ?: ""
    return UiTextElement(
      text = time,
      textFormat = UiTextFormat(
        fontSize = unitsConverter.spToPx(18f),
        textStyle = UiTextStyle.BOLD,
        textColor = colorProvider.getColorOnSurface()
      )
    )
  }

  private fun createMainText(
    type: UiReminderType,
    reminder: Reminder
  ): UiTextElement {
    val summary = reminder.summary
    return if (summary.isEmpty()) {
      val text = reminder.description ?: getTextFromType(type)
      UiTextElement(
        text = "($text)",
        textFormat = UiTextFormat(
          fontSize = unitsConverter.spToPx(16f),
          textStyle = UiTextStyle.ITALIC,
          textColor = colorProvider.getColorOnSurface().adjustAlpha(75)
        )
      )
    } else {
      UiTextElement(
        text = summary,
        textFormat = UiTextFormat(
          fontSize = unitsConverter.spToPx(16f),
          textStyle = UiTextStyle.NORMAL,
          textColor = colorProvider.getColorOnSurface()
        )
      )
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
}
