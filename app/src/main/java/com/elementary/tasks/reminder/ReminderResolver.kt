package com.elementary.tasks.reminder

import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.reminder.build.BuildReminderActivity
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.login.LoginApi

class ReminderResolver(
  private val dialogAction: () -> Dialogues,
  private val deleteAction: (reminder: UiReminderListData) -> Unit,
  private val toggleAction: (reminder: UiReminderListData) -> Unit,
  private val skipAction: (reminder: UiReminderListData) -> Unit,
  private val openAction: (reminder: UiReminderListData) -> Unit
) {

  fun resolveAction(view: View, reminder: UiReminderListData, listActions: ListActions) {
    if (reminder.status.removed) {
      when (listActions) {
        ListActions.MORE -> showDeletedActionDialog(view, reminder)
        ListActions.OPEN -> editReminder(view, reminder)
        else -> {
        }
      }
    } else {
      when (listActions) {
        ListActions.MORE -> showActionDialog(view, reminder)
        ListActions.OPEN -> previewReminder(reminder)
        ListActions.SWITCH -> toggleAction.invoke(reminder)
        else -> {
        }
      }
    }
  }

  private fun showDeletedActionDialog(view: View, reminder: UiReminderListData) {
    val context = view.context
    val items = arrayOf(
      context.getString(R.string.open),
      context.getString(R.string.edit),
      context.getString(R.string.delete)
    )
    Dialogues.showPopup(view, { item ->
      when (item) {
        0 -> previewReminder(reminder)
        1 -> editReminder(view, reminder)
        2 -> askConfirmation(view, items[item]) {
          if (it) deleteAction.invoke(reminder)
        }
      }
    }, *items)
  }

  private fun showActionDialog(view: View, reminder: UiReminderListData) {
    val context = view.context
    val items = if (reminder.status.active && !reminder.status.removed && reminder.canSkip) {
      arrayOf(
        context.getString(R.string.open),
        context.getString(R.string.edit),
        context.getString(R.string.move_to_trash),
        context.getString(R.string.skip_event)
      )
    } else {
      arrayOf(
        context.getString(R.string.open),
        context.getString(R.string.edit),
        context.getString(R.string.move_to_trash)
      )
    }
    Dialogues.showPopup(view, { item ->
      when (item) {
        0 -> previewReminder(reminder)
        1 -> editReminder(view, reminder)
        2 -> askConfirmation(view, items[item]) {
          if (it) deleteAction.invoke(reminder)
        }

        3 -> skipAction.invoke(reminder)
      }
    }, *items)
  }

  private fun askConfirmation(view: View, title: String, onAction: (Boolean) -> Unit) {
    dialogAction.invoke().askConfirmation(view.context, title, onAction)
  }

  private fun editReminder(view: View, reminder: UiReminderListData) {
    view.context.run {
      LoginApi.openLogged(this, BuildReminderActivity::class.java) {
        putExtra(IntentKeys.INTENT_ID, reminder.id)
      }
    }
  }

  private fun previewReminder(reminder: UiReminderListData) {
    openAction(reminder)
  }
}
