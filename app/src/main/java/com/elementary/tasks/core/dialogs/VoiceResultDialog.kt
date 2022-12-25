package com.elementary.tasks.core.dialogs

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.UiReminderList
import com.elementary.tasks.core.data.ui.UiReminderListActive
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.core.view_models.reminders.VoiceResultDialogViewModel
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.reminder.lists.adapter.ReminderViewHolder
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class VoiceResultDialog : BaseDialog() {

  private val viewModel by viewModel<VoiceResultDialogViewModel> { parametersOf(getId()) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.reminder.observe(this) { showReminder(it) }
    lifecycle.addObserver(viewModel)
  }

  private fun getId() = intent.getStringExtra(Constants.INTENT_ID) ?: ""

  private fun showReminder(reminder: UiReminderList) {
    val alert = dialogues.getMaterialDialog(this)
    alert.setTitle(getString(R.string.saved))

    val parent = LinearLayout(this)
    parent.layoutParams = LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT,
      LinearLayout.LayoutParams.WRAP_CONTENT
    )
    parent.orientation = LinearLayout.VERTICAL

    val holder = ReminderViewHolder(parent, editable = false)
    holder.setData(reminder as UiReminderListActive)

    parent.addView(holder.itemView)

    alert.setView(parent)
    alert.setCancelable(true)
    alert.setNegativeButton(R.string.edit) { dialogInterface, _ ->
      dialogInterface.dismiss()
      PinLoginActivity.openLogged(
        this,
        Intent(this@VoiceResultDialog, CreateReminderActivity::class.java)
          .putExtra(Constants.INTENT_ID, reminder.id)
      )
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
