package com.elementary.tasks.core.data.adapter

import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.UiGroup
import com.elementary.tasks.core.data.ui.UiReminderList
import com.elementary.tasks.core.data.ui.UiReminderListActive
import com.elementary.tasks.core.data.ui.UiReminderListActiveGps
import com.elementary.tasks.core.data.ui.UiReminderListActiveShop
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.data.ui.UiReminderListRemoved
import com.elementary.tasks.core.data.ui.UiReminderListRemovedGps
import com.elementary.tasks.core.data.ui.UiReminderListRemovedShop
import com.elementary.tasks.core.data.ui.reminder.UiReminderIllustration
import com.elementary.tasks.core.data.ui.reminder.UiReminderType

class UiReminderListAdapter(
  private val uiReminderPlaceAdapter: UiReminderPlaceAdapter,
  private val uiReminderCommonAdapter: UiReminderCommonAdapter
) : UiAdapter<Reminder, UiReminderList> {

  override fun create(data: Reminder): UiReminderListData {
    val type = UiReminderType(data.type)
    val isRepeating = !type.isGpsType() && (data.repeatInterval > 0L || type.isByWeekday()
      || type.isMonthly() || type.isYearly())
    return if (data.isRemoved) {
      when {
        type.isShopping() -> {
          UiReminderListRemovedShop(
            id = data.uuId,
            type = type,
            summary = data.summary,
            illustration = UiReminderIllustration(
              title = uiReminderCommonAdapter.getTypeString(type),
              icon = uiReminderCommonAdapter.getReminderIllustration(type)
            ),
            priority = uiReminderCommonAdapter.getPriorityTitle(data.priority),
            group = UiGroup(data.groupUuId, data.groupColor, data.groupTitle),
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
            illustration = UiReminderIllustration(
              title = uiReminderCommonAdapter.getTypeString(type),
              icon = uiReminderCommonAdapter.getReminderIllustration(type)
            ),
            priority = uiReminderCommonAdapter.getPriorityTitle(data.priority),
            group = UiGroup(data.groupUuId, data.groupColor, data.groupTitle),
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
            illustration = UiReminderIllustration(
              title = uiReminderCommonAdapter.getTypeString(type),
              icon = uiReminderCommonAdapter.getReminderIllustration(type)
            ),
            priority = uiReminderCommonAdapter.getPriorityTitle(data.priority),
            group = UiGroup(data.groupUuId, data.groupColor, data.groupTitle),
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
            illustration = UiReminderIllustration(
              title = uiReminderCommonAdapter.getTypeString(type),
              icon = uiReminderCommonAdapter.getReminderIllustration(type)
            ),
            priority = uiReminderCommonAdapter.getPriorityTitle(data.priority),
            group = UiGroup(data.groupUuId, data.groupColor, data.groupTitle),
            due = uiReminderCommonAdapter.getDue(data, type),
            isRunning = data.isActive && !data.isRemoved,
            status = uiReminderCommonAdapter.getReminderStatus(data.isActive, data.isRemoved),
            shopList = data.shoppings
          )
        }
        type.isGpsType() -> {
          UiReminderListActiveGps(
            id = data.uuId,
            type = type,
            summary = data.summary,
            illustration = UiReminderIllustration(
              title = uiReminderCommonAdapter.getTypeString(type),
              icon = uiReminderCommonAdapter.getReminderIllustration(type)
            ),
            priority = uiReminderCommonAdapter.getPriorityTitle(data.priority),
            group = UiGroup(data.groupUuId, data.groupColor, data.groupTitle),
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
            illustration = UiReminderIllustration(
              title = uiReminderCommonAdapter.getTypeString(type),
              icon = uiReminderCommonAdapter.getReminderIllustration(type)
            ),
            priority = uiReminderCommonAdapter.getPriorityTitle(data.priority),
            group = UiGroup(data.groupUuId, data.groupColor, data.groupTitle),
            due = uiReminderCommonAdapter.getDue(data, type),
            isRunning = data.isActive && !data.isRemoved,
            status = uiReminderCommonAdapter.getReminderStatus(data.isActive, data.isRemoved),
            actionTarget = uiReminderCommonAdapter.getTarget(data, type),
            isRepeating = isRepeating
          )
        }
      }
    }
  }
}