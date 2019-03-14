package com.elementary.tasks.reminder

import android.content.Intent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.reminder.preview.ReminderPreviewActivity

class ReminderResolver(
        private val dialogAction: () -> Dialogues,
        private val saveAction: (reminder: Reminder) -> Unit,
        private val deleteAction: (reminder: Reminder) -> Unit,
        private val toggleAction: (reminder: Reminder) -> Unit,
        private val allGroups: () -> List<ReminderGroup>
) {

    fun resolveAction(view: View, reminder: Reminder, listActions: ListActions) {
        if (reminder.isRemoved) {
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
                ListActions.SWITCH -> switchReminder(reminder)
                else -> {
                }
            }
        }
    }

    private fun showDeletedActionDialog(view: View, reminder: Reminder) {
        val context = view.context
        val items = arrayOf(context.getString(R.string.open), context.getString(R.string.edit),
                context.getString(R.string.change_group), context.getString(R.string.delete))
        Dialogues.showPopup(view, { item ->
            when (item) {
                0 -> previewReminder(view, reminder)
                1 -> editReminder(view, reminder)
                2 -> changeGroup(view, reminder)
                3 -> askConfirmation(view, items[item]) {
                    if (it) deleteAction.invoke(reminder)
                }
            }
        }, *items)
    }

    private fun showActionDialog(view: View, reminder: Reminder) {
        val context = view.context
        val items = arrayOf(context.getString(R.string.open), context.getString(R.string.edit),
                context.getString(R.string.change_group), context.getString(R.string.move_to_trash))
        Dialogues.showPopup(view, { item ->
            when (item) {
                0 -> previewReminder(view, reminder)
                1 -> editReminder(view, reminder)
                2 -> changeGroup(view, reminder)
                3 -> askConfirmation(view, items[item]) {
                    if (it) deleteAction.invoke(reminder)
                }
            }
        }, *items)
    }

    private fun askConfirmation(view: View, title: String, onAction: (Boolean) -> Unit) {
        dialogAction.invoke().askConfirmation(view.context, title, onAction)
    }

    private fun editReminder(view: View, reminder: Reminder) {
        CreateReminderActivity.openLogged(view.context,
                Intent(view.context, CreateReminderActivity::class.java)
                        .putExtra(Constants.INTENT_ID, reminder.uuId))
    }

    private fun switchReminder(reminder: Reminder) {
        toggleAction.invoke(reminder)
    }

    private fun changeGroup(view: View, reminder: Reminder) {
        val context = view.context
        val arrayAdapter = ArrayAdapter<String>(context, android.R.layout.select_dialog_item)
        val groups = allGroups.invoke()
        for (item in groups) {
            arrayAdapter.add(item.groupTitle)
        }
        val builder = dialogAction.invoke().getMaterialDialog(context)
        builder.setTitle(context.getString(R.string.choose_group))
        builder.setAdapter(arrayAdapter) { dialog, which ->
            dialog.dismiss()
            val catId = groups[which].groupUuId
            if (reminder.groupUuId.matches(catId.toRegex())) {
                Toast.makeText(context, context.getString(R.string.same_group), Toast.LENGTH_SHORT).show()
                return@setAdapter
            }
            reminder.groupUuId = catId
            saveAction.invoke(reminder)
        }
        builder.create().show()
    }

    private fun previewReminder(view: View, reminder: Reminder) {
        view.context.startActivity(Intent(view.context, ReminderPreviewActivity::class.java)
                .putExtra(Constants.INTENT_ID, reminder.uuId))
    }
}