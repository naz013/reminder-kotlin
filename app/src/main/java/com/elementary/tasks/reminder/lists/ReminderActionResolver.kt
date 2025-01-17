package com.elementary.tasks.reminder.lists

import android.content.Context
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.reminder.lists.data.UiReminderListActions
import com.github.naz013.common.Permissions
import com.github.naz013.ui.common.Dialogues

class ReminderActionResolver(
  private val context: Context,
  private val dialogues: Dialogues,
  private val permissionFlow: PermissionFlow,
  private val deleteAction: (id: String) -> Unit,
  private val toggleAction: (id: String) -> Unit,
  private val skipAction: (id: String) -> Unit,
  private val openAction: (id: String) -> Unit,
  private val editAction: (id: String) -> Unit
) {

  fun resolveItemClick(
    id: String,
    isRemoved: Boolean
  ) {
    if (isRemoved) {
      editReminder(id)
    } else {
      previewReminder(id)
    }
  }

  fun resolveItemMore(
    view: View,
    id: String,
    isRemoved: Boolean,
    actions: UiReminderListActions
  ) {
    if (isRemoved) {
      showDeletedActionDialog(view, id)
    } else {
      showActionDialog(view, actions, id)
    }
  }

  fun resolveItemToggle(
    id: String,
    isGps: Boolean
  ) {
    if (isGps) {
      permissionFlow.askPermissions(
        listOf(
          Permissions.FOREGROUND_SERVICE,
          Permissions.FOREGROUND_SERVICE_LOCATION
        )
      ) {
        toggleAction(id)
      }
    } else {
      toggleAction(id)
    }
  }

  private fun showDeletedActionDialog(view: View, id: String) {
    val items = arrayOf(
      context.getString(R.string.open),
      context.getString(R.string.edit),
      context.getString(R.string.delete)
    )
    Dialogues.showPopup(view, { item ->
      when (item) {
        0 -> previewReminder(id)
        1 -> editReminder(id)
        2 -> askConfirmation(items[item]) {
          if (it) {
            deleteAction(id)
          }
        }
      }
    }, *items)
  }

  private fun showActionDialog(
    view: View,
    actions: UiReminderListActions,
    id: String
  ) {
    val items = if (actions.canSkip) {
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
        0 -> previewReminder(id)
        1 -> editReminder(id)
        2 -> askConfirmation(items[item]) {
          if (it) {
            deleteAction(id)
          }
        }

        3 -> skipAction(id)
      }
    }, *items)
  }

  private fun askConfirmation(title: String, onAction: (Boolean) -> Unit) {
    dialogues.askConfirmation(context, title, onAction)
  }

  private fun editReminder(id: String) {
    editAction(id)
  }

  private fun previewReminder(id: String) {
    openAction(id)
  }
}
