package com.elementary.tasks.core.dialogs

import android.os.Bundle
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.UiReminderList
import com.elementary.tasks.core.data.ui.UiReminderListActive
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.reminder.ReminderBuilderLauncher
import com.elementary.tasks.reminder.lists.adapter.ReminderViewHolder
import com.elementary.tasks.voice.VoiceResultDialogViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class VoiceResultDialog : BaseDialog() {

  private val viewModel by viewModel<VoiceResultDialogViewModel> { parametersOf(getId()) }
  private val reminderBuilderLauncher by inject<ReminderBuilderLauncher>()

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
      reminderBuilderLauncher.openLogged(this) {
        putExtra(Constants.INTENT_ID, reminder.id)
      }
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
