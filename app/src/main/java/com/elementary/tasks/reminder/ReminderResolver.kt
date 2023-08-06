package com.elementary.tasks.reminder

import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.intentForClass
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.reminder.preview.ReminderPreviewActivity

class ReminderResolver(
  private val dialogAction: () -> Dialogues,
  private val deleteAction: (reminder: UiReminderListData) -> Unit,
  private val toggleAction: (reminder: UiReminderListData) -> Unit,
  private val skipAction: (reminder: UiReminderListData) -> Unit
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
        ListActions.OPEN -> previewReminder(view, reminder)
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
        0 -> previewReminder(view, reminder)
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
        0 -> previewReminder(view, reminder)
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
      PinLoginActivity.openLogged(
        this,
        intentForClass(CreateReminderActivity::class.java)
          .putExtra(Constants.INTENT_ID, reminder.id)
      )
    }
  }

  private fun previewReminder(view: View, reminder: UiReminderListData) {
    view.context.run {
      startActivity(
        intentForClass(ReminderPreviewActivity::class.java)
          .putExtra(Constants.INTENT_ID, reminder.id)
      )
    }
  }
}
