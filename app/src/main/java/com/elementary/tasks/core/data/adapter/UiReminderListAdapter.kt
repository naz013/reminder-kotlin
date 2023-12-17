package com.elementary.tasks.core.data.adapter

import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.UiReminderListActive
import com.elementary.tasks.core.data.ui.UiReminderListActiveGps
import com.elementary.tasks.core.data.ui.UiReminderListActiveShop
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.data.ui.UiReminderListRemoved
import com.elementary.tasks.core.data.ui.UiReminderListRemovedGps
import com.elementary.tasks.core.data.ui.UiReminderListRemovedShop
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.RecurEventManager

class UiReminderListAdapter(
  private val uiReminderPlaceAdapter: UiReminderPlaceAdapter,
  private val uiReminderCommonAdapter: UiReminderCommonAdapter,
  private val uiGroupListAdapter: UiGroupListAdapter,
  private val dateTimeManager: DateTimeManager,
  private val recurEventManager: RecurEventManager
) : UiAdapter<Reminder, UiReminderListData> {

  override fun create(data: Reminder): UiReminderListData {
    val type = UiReminderType(data.type)
    val isRepeating = !type.isGpsType() && (
      data.repeatInterval > 0L || type.isByWeekday() || type.isMonthly() ||
        type.isYearly() || type.isRecur()
      )
    val canSkip = !type.isGpsType() && (
      data.repeatInterval > 0L || type.isByWeekday() ||
        type.isMonthly() || type.isYearly() || (type.isRecur() && hasNextRecur(data))
      )
    return if (data.isRemoved) {
      when {
        type.isShopping() -> {
          UiReminderListRemovedShop(
            id = data.uuId,
            type = type,
            summary = data.summary,
            title = uiReminderCommonAdapter.getTypeString(type),
            priority = uiReminderCommonAdapter.getPriorityTitle(data.priority),
            group = uiGroupListAdapter.convert(data.groupUuId, data.groupColor, data.groupTitle),
            due = uiReminderCommonAdapter.getDue(data, type),
            status = uiReminderCommonAdapter.getReminderStatus(data.isActive, data.isRemoved),
            shopList = data.shoppings
          )
        }
        type.isGpsType() -> {
          UiReminderListRemovedGps(
            id = data.uuId,
            type = type,
            summary = data.summary,
            title = uiReminderCommonAdapter.getTypeString(type),
            priority = uiReminderCommonAdapter.getPriorityTitle(data.priority),
            group = uiGroupListAdapter.convert(data.groupUuId, data.groupColor, data.groupTitle),
            status = uiReminderCommonAdapter.getReminderStatus(data.isActive, data.isRemoved),
            actionTarget = uiReminderCommonAdapter.getTarget(data, type),
            places = data.places.map { uiReminderPlaceAdapter.create(it) }
          )
        }
        else -> {
          UiReminderListRemoved(
            id = data.uuId,
            type = type,
            summary = data.summary,
            title = uiReminderCommonAdapter.getTypeString(type),
            priority = uiReminderCommonAdapter.getPriorityTitle(data.priority),
            group = uiGroupListAdapter.convert(data.groupUuId, data.groupColor, data.groupTitle),
            due = uiReminderCommonAdapter.getDue(data, type),
            status = uiReminderCommonAdapter.getReminderStatus(data.isActive, data.isRemoved),
            actionTarget = uiReminderCommonAdapter.getTarget(data, type),
            isRepeating = isRepeating
          )
        }
      }
    } else {
      when {
        type.isShopping() -> {
          UiReminderListActiveShop(
            id = data.uuId,
            type = type,
            summary = data.summary,
            title = uiReminderCommonAdapter.getTypeString(type),
            priority = uiReminderCommonAdapter.getPriorityTitle(data.priority),
            group = uiGroupListAdapter.convert(data.groupUuId, data.groupColor, data.groupTitle),
            due = uiReminderCommonAdapter.getDue(data, type),
            isRunning = data.isActive && !data.isRemoved,
            status = uiReminderCommonAdapter.getReminderStatus(data.isActive, data.isRemoved),
            shopList = data.shoppings,
            noteId = data.noteId
          )
        }
        type.isGpsType() -> {
          UiReminderListActiveGps(
            id = data.uuId,
            type = type,
            summary = data.summary,
            title = uiReminderCommonAdapter.getTypeString(type),
            priority = uiReminderCommonAdapter.getPriorityTitle(data.priority),
            group = uiGroupListAdapter.convert(data.groupUuId, data.groupColor, data.groupTitle),
            isRunning = data.isActive && !data.isRemoved,
            status = uiReminderCommonAdapter.getReminderStatus(data.isActive, data.isRemoved),
            actionTarget = uiReminderCommonAdapter.getTarget(data, type),
            places = data.places.map { uiReminderPlaceAdapter.create(it) }
          )
        }
        else -> {
          UiReminderListActive(
            id = data.uuId,
            type = type,
            summary = data.summary,
            title = uiReminderCommonAdapter.getTypeString(type),
            priority = uiReminderCommonAdapter.getPriorityTitle(data.priority),
            group = uiGroupListAdapter.convert(data.groupUuId, data.groupColor, data.groupTitle),
            due = uiReminderCommonAdapter.getDue(data, type),
            isRunning = data.isActive && !data.isRemoved,
            status = uiReminderCommonAdapter.getReminderStatus(data.isActive, data.isRemoved),
            actionTarget = uiReminderCommonAdapter.getTarget(data, type),
            isRepeating = isRepeating,
            canSkip = canSkip,
            noteId = data.noteId
          )
        }
      }
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
