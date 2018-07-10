package com.elementary.tasks.core.dialogs

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View

import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.view_models.reminders.ReminderViewModel
import com.elementary.tasks.databinding.ListItemReminderBinding
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity

import androidx.lifecycle.ViewModelProviders

/**
 * Copyright 2017 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class VoiceResultDialog : BaseDialog() {

    private val mCancelListener = { dialogInterface -> finish() }
    private val mOnDismissListener = { dialogInterface -> finish() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getIntExtra(Constants.INTENT_ID, 0)

        val viewModel = ViewModelProviders.of(this, ReminderViewModel.Factory(application, id)).get(ReminderViewModel::class.java)
        viewModel.reminder.observe(this, { reminder ->
            if (reminder != null) {
                showReminder(reminder)
            }
        })
    }

    private fun showReminder(reminder: Reminder?) {
        val alert = Dialogues.getDialog(this)
        alert.setTitle(getString(R.string.saved))

        val binding = ListItemReminderBinding.inflate(LayoutInflater.from(this), null, false)
        binding.item = reminder
        binding.itemCheck.visibility = View.GONE
        binding.reminderContainer.setBackgroundColor(themeUtil!!.cardStyle)
        alert.setView(binding.root)
        alert.setCancelable(true)
        alert.setNegativeButton(R.string.edit) { dialogInterface, i ->
            dialogInterface.dismiss()
            startActivity(Intent(this@VoiceResultDialog, CreateReminderActivity::class.java).putExtra(Constants.INTENT_ID, reminder!!.uniqueId))
            finish()
        }
        alert.setPositiveButton(R.string.ok) { dialog, id ->
            dialog.dismiss()
            finish()
        }
        val alertDialog = alert.create()
        alertDialog.setOnCancelListener(mCancelListener)
        alertDialog.setOnDismissListener(mOnDismissListener)
        alertDialog.show()
    }
}
