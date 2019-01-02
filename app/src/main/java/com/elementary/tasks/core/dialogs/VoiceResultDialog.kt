package com.elementary.tasks.core.dialogs

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.viewModels.reminders.ReminderViewModel
import com.elementary.tasks.reminder.createEdit.CreateReminderActivity
import com.elementary.tasks.reminder.lists.adapter.ReminderHolder

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""

        val viewModel = ViewModelProviders.of(this, ReminderViewModel.Factory(application, id)).get(ReminderViewModel::class.java)
        viewModel.reminder.observe(this, Observer{ reminder ->
            if (reminder != null) {
                showReminder(reminder)
            }
        })
    }

    private fun showReminder(reminder: Reminder) {
        val alert = dialogues.getDialog(this)
        alert.setTitle(getString(R.string.saved))

        val parent = LinearLayout(this)
        parent.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        parent.orientation = LinearLayout.VERTICAL

        val holder = ReminderHolder(parent, false, false)
        holder.setData(reminder)

        parent.addView(holder.itemView)

        alert.setView(parent)
        alert.setCancelable(true)
        alert.setNegativeButton(R.string.edit) { dialogInterface, _ ->
            dialogInterface.dismiss()
            startActivity(Intent(this@VoiceResultDialog, CreateReminderActivity::class.java)
                    .putExtra(Constants.INTENT_ID, reminder.uniqueId))
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
