package com.elementary.tasks.core.dialogs

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Contacts
import com.elementary.tasks.core.utils.IntervalUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.viewModels.reminders.ReminderViewModel
import com.elementary.tasks.reminder.createEdit.CreateReminderActivity
import kotlinx.android.synthetic.main.list_item_reminder.view.*
import java.util.*

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

        val binding = LayoutInflater.from(this).inflate(R.layout.list_item_reminder, null, false)
        binding.taskText.text = reminder.summary
        bind(binding, reminder)
        binding.itemCheck.visibility = View.GONE
        alert.setView(binding)
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
    }

    private fun bind(binding: View, reminder: Reminder) {
//        if (reminder.reminderGroup != null) {
//            binding.itemCard.setCardBackgroundColor(themeUtil.getGroupColor(themeUtil.getCategoryColor(reminder.reminderGroup!!.groupColor)))
//        } else {
//            binding.itemCard.setCardBackgroundColor(themeUtil.getGroupColor(themeUtil.getCategoryColor(0)))
//        }
        val is24 = prefs.is24HourFormatEnabled
        if (Reminder.isGpsType(reminder.type)) {
            val place = reminder.places[0]
            binding.taskDate.text = String.format(Locale.getDefault(), "%.5f %.5f (%d)", place.latitude, place.longitude, reminder.places.size)
        } else {
            binding.taskDate.text = TimeUtil.getRealDateTime(reminder.eventTime, reminder.delay, is24)
        }
        if (reminder.isRemoved) {
            binding.itemCheck.visibility = View.GONE
        } else {
            binding.itemCheck.isChecked = reminder.isActive
        }
        loadContact(reminder, binding)
        if (reminder.isActive && !reminder.isRemoved) {
            binding.remainingTime.text = timeCount.getRemaining(reminder.eventTime, reminder.delay)
        } else {
            binding.remainingTime.text = ""
        }
        when {
            Reminder.isBase(reminder.type, Reminder.BY_MONTH) -> binding.repeatInterval.text = String.format(binding.repeatInterval.context.getString(R.string.xM), 1.toString())
            Reminder.isBase(reminder.type, Reminder.BY_WEEK) -> binding.repeatInterval.text = reminderUtils.getRepeatString(reminder.weekdays)
            Reminder.isBase(reminder.type, Reminder.BY_DAY_OF_YEAR) -> binding.repeatInterval.text = binding.repeatInterval.context.getString(R.string.yearly)
            else -> binding.repeatInterval.text = IntervalUtil.getInterval(binding.repeatInterval.context, reminder.repeatInterval)
        }
        if (Reminder.isBase(reminder.type, Reminder.BY_LOCATION)
                || Reminder.isBase(reminder.type, Reminder.BY_OUT)
                || Reminder.isBase(reminder.type, Reminder.BY_PLACES)) {
            binding.endContainer.visibility = View.GONE
        } else {
            binding.endContainer.visibility = View.VISIBLE
        }
        binding.chipType.text = reminderUtils.getTypeString(reminder.type)
    }

    private fun loadContact(model: Reminder, itemView: View) {
        val type = model.type
        val number = model.target
        if (Reminder.isBase(type, Reminder.BY_SKYPE)) {
            itemView.reminder_phone.visibility = View.VISIBLE
            itemView.reminder_phone.text = number
        } else if (Reminder.isKind(type, Reminder.Kind.CALL) || Reminder.isKind(type, Reminder.Kind.SMS)) {
            itemView.reminder_phone.visibility = View.VISIBLE
            val name = Contacts.getNameFromNumber(number, itemView.reminder_phone.context)
            if (name == null) {
                itemView.reminder_phone.text = number
            } else {
                itemView.reminder_phone.text = "$name($number)"
            }
        } else if (Reminder.isSame(type, Reminder.BY_DATE_APP)) {
            val packageManager = itemView.reminder_phone.context.packageManager
            var applicationInfo: ApplicationInfo? = null
            try {
                applicationInfo = packageManager.getApplicationInfo(number, 0)
            } catch (ignored: PackageManager.NameNotFoundException) {
            }

            val name = (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "???") as String
            itemView.reminder_phone.visibility = View.VISIBLE
            itemView.reminder_phone.text = "$name/$number"
        } else if (Reminder.isSame(type, Reminder.BY_DATE_EMAIL)) {
            val name = Contacts.getNameFromMail(number, itemView.reminder_phone.context)
            itemView.reminder_phone.visibility = View.VISIBLE
            if (name == null) {
                itemView.reminder_phone.text = number
            } else {
                itemView.reminder_phone.text = "$name($number)"
            }
        } else if (Reminder.isSame(type, Reminder.BY_DATE_LINK)) {
            itemView.reminder_phone.visibility = View.VISIBLE
            itemView.reminder_phone.text = number
        } else {
            itemView.reminder_phone.visibility = View.GONE
        }
    }
}
