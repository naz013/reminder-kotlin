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
import com.elementary.tasks.core.os.ColorProvider
import com.elementary.tasks.core.os.UnitsConverter
import com.elementary.tasks.core.text.UiTextFormat
import com.elementary.tasks.core.text.UiTextStyle
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.RecurEventManager
import com.elementary.tasks.reminder.build.formatter.factory.PlaceFormatterFactory
import com.elementary.tasks.reminder.build.formatter.`object`.PlaceFormatter
import com.elementary.tasks.reminder.build.formatter.`object`.ShopItemsFormatter
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.android.TextProvider
import com.github.naz013.feature.common.android.adjustAlpha

class UiReminderListAdapter(
  private val uiReminderCommonAdapter: UiReminderCommonAdapter,
  private val unitsConverter: UnitsConverter,
  private val colorProvider: ColorProvider,
  private val textProvider: TextProvider,
  private val shopItemsFormatter: ShopItemsFormatter,
  private val uiGroupListAdapter: UiGroupListAdapter,
  private val dateTimeManager: DateTimeManager,
  private val recurEventManager: RecurEventManager,
  private val placeFormatterFactory: PlaceFormatterFactory
) : UiAdapter<Reminder, UiReminderList> {

  private val placeFormatter: PlaceFormatter by lazy { placeFormatterFactory.create() }

  override fun create(data: Reminder): UiReminderList {
    val type = UiReminderType(data.type)
    val due = uiReminderCommonAdapter.getDue(data, type)
    val canSkip = !type.isGpsType() && (
      data.repeatInterval > 0L || type.isByWeekday() ||
        type.isMonthly() || type.isYearly() || (type.isRecur() && hasNextRecur(data))
      )

    return UiReminderList(
      id = data.uuId,
      noteId = data.noteId.takeIf { it.isNotEmpty() },
      dueDateTime = due.localDateTime,
      mainText = createMainText(type, data),
      secondaryText = createSecondaryText(due, type, data),
      tertiaryText = createTertiaryText(type, data),
      tags = listOfNotNull(
        createRepeatBadge(due),
        createRemainingBadge(due),
        createGroupBadge(data)
      ),
      actions = UiReminderListActions(
        canSkip = data.isActive && !data.isRemoved && canSkip,
        canDelete = !data.isRemoved,
        canToggle = true,
        canEdit = true,
        canOpen = !data.isRemoved
      ),
      state = UiReminderListState(
        isActive = data.isActive,
        isRemoved = data.isRemoved,
        isGps = type.isGpsType()
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

  private fun createRepeatBadge(
    dueData: UiReminderDueData?
  ): UiTextElement? {
    if (dueData?.localDateTime == null) {
      return null
    }
    return UiTextElement(
      text = dueData.repeat,
      textFormat = UiTextFormat(
        fontSize = unitsConverter.spToPx(12f),
        textStyle = UiTextStyle.BOLD,
        textColor = colorProvider.getColorOnSecondaryContainer()
      )
    )
  }

  private fun createRemainingBadge(
    dueData: UiReminderDueData?
  ): UiTextElement? {
    if (dueData?.localDateTime == null) {
      return null
    }
    return dueData.remaining?.let {
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

  private fun createTertiaryText(
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

  private fun createSecondaryText(
    dueData: UiReminderDueData?,
    type: UiReminderType,
    data: Reminder
  ): UiTextElement? {
    return if (type.isGpsType()) {
      val place = data.places.firstOrNull() ?: return null
      UiTextElement(
        text = placeFormatter.format(place),
        textFormat = UiTextFormat(
          fontSize = unitsConverter.spToPx(14f),
          textStyle = UiTextStyle.NORMAL,
          textColor = colorProvider.getColorOnSurface()
        )
      )
    } else {
      if (dueData?.localDateTime == null) {
        return null
      }
      UiTextElement(
        text = dueData.formattedDateTime ?: "",
        textFormat = UiTextFormat(
          fontSize = unitsConverter.spToPx(14f),
          textStyle = UiTextStyle.NORMAL,
          textColor = colorProvider.getColorOnSurface()
        )
      )
    }
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

  private fun hasNextRecur(reminder: Reminder): Boolean {
    val currentEventTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)
    return recurEventManager.getNextAfterDateTime(
      currentEventTime,
      reminder.recurDataObject
    ) != null
  }
}
