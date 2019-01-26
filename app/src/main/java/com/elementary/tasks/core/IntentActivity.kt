package com.elementary.tasks.core

import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.core.data.models.*
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.groups.CreateGroupActivity
import com.elementary.tasks.navigation.settings.additional.TemplateActivity
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.places.create.CreatePlaceActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import timber.log.Timber
import javax.inject.Inject

class IntentActivity: ThemedActivity() {

    @Inject
    lateinit var backupTool: BackupTool

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.data ?: return
        val scheme = data.scheme

        Timber.d("onCreate: $data, $scheme")

        if (ContentResolver.SCHEME_CONTENT == scheme) {
            val birthday = backupTool.getBirthday(contentResolver, data)
            if (birthday != null && birthday.isValid()) {
                startActivity(Intent(this, AddBirthdayActivity::class.java)
                        .putExtra(Constants.INTENT_ITEM, birthday))
                finish()
                return
            }

            val reminder = backupTool.getReminder(contentResolver, data)
            if (reminder != null && reminder.isValid()) {
                startActivity(Intent(this, CreateReminderActivity::class.java)
                        .putExtra(Constants.INTENT_ITEM, reminder))
                finish()
                return
            }

            val noteWithImages = backupTool.getNote(contentResolver, data)
            if (noteWithImages != null && noteWithImages.isValid()) {
                startActivity(Intent(this, CreateNoteActivity::class.java)
                        .putExtra(Constants.INTENT_ITEM, noteWithImages))
                finish()
                return
            }

            val place = backupTool.getPlace(contentResolver, data)
            if (place != null && place.isValid()) {
                startActivity(Intent(this, CreatePlaceActivity::class.java)
                        .putExtra(Constants.INTENT_ITEM, place))
                finish()
                return
            }

            val group = backupTool.getGroup(contentResolver, data)
            if (group != null && group.isValid()) {
                startActivity(Intent(this, CreateGroupActivity::class.java)
                        .putExtra(Constants.INTENT_ITEM, group))
                finish()
                return
            }

            val template = backupTool.getTemplate(contentResolver, data)
            if (template != null && template.isValid()) {
                startActivity(Intent(this, TemplateActivity::class.java)
                        .putExtra(Constants.INTENT_ITEM, template))
                finish()
                return
            }
        }
        finish()
    }
}

private fun SmsTemplate.isValid(): Boolean {
    return title.isNotBlank()
}

private fun ReminderGroup.isValid(): Boolean {
    return groupTitle.isNotBlank()
}

private fun Place.isValid(): Boolean {
    return latitude != 0.0 && longitude != 0.0 && name.isNotBlank()
}

private fun NoteWithImages.isValid(): Boolean {
    return note != null
}

private fun Reminder.isValid(): Boolean {
    return summary.isNotBlank() && type > 0 && groupUuId.isNotBlank()
}

private fun Birthday.isValid(): Boolean {
    return name.isNotBlank() && date.isNotBlank() && key.isNotBlank() && day > 0
}
