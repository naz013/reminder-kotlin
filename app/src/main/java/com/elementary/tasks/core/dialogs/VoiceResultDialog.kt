package com.elementary.tasks.core.dialogs

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.view_models.reminders.ReminderViewModel
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.reminder.lists.adapter.ReminderHolder

class VoiceResultDialog : BaseDialog() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""

        val viewModel = ViewModelProvider(this, ReminderViewModel.Factory(id)).get(ReminderViewModel::class.java)
        viewModel.reminder.observe(this, Observer { reminder ->
            if (reminder != null) {
                showReminder(reminder)
            }
        })
    }

    private fun showReminder(reminder: Reminder) {
        val alert = dialogues.getMaterialDialog(this)
        alert.setTitle(getString(R.string.saved))

        val parent = LinearLayout(this)
        parent.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        parent.orientation = LinearLayout.VERTICAL

        val holder = ReminderHolder(parent, false, editable = false)
        holder.setData(reminder)

        parent.addView(holder.itemView)

        alert.setView(parent)
        alert.setCancelable(true)
        alert.setNegativeButton(R.string.edit) { dialogInterface, _ ->
            dialogInterface.dismiss()
            CreateReminderActivity.openLogged(this,
                    Intent(this@VoiceResultDialog, CreateReminderActivity::class.java)
                            .putExtra(Constants.INTENT_ID, reminder.uuId))
            finish()
        }
        alert.setPositiveButton(R.string.ok) { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        val alertDialog = alert.create()
        alertDialog.setOnCancelListener { finish() }
        alertDialog.setOnDismissListener { finish() }
        alertDialog.show()

        Dialogues.setFullWidthDialog(alertDialog, this)
    }
}
