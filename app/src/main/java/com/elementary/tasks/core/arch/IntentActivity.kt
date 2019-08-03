package com.elementary.tasks.core.arch

import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.core.data.models.*
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.groups.create.CreateGroupActivity
import com.elementary.tasks.navigation.settings.additional.TemplateActivity
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.places.create.CreatePlaceActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import timber.log.Timber

class IntentActivity: ThemedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.data ?: return
        val scheme = data.scheme

        Timber.d("onCreate: $data, $scheme")

        if (ContentResolver.SCHEME_CONTENT == scheme) {
            val any = MemoryUtil.decryptToJson(this, data)
            Timber.d("getPlace: $any")
            if (any != null) {
                when (any) {
                    is Place -> {
                        if (any.isValid()) {
                            startActivity(Intent(this, CreatePlaceActivity::class.java)
                                    .putExtra(Constants.INTENT_ITEM, any))
                        } else {
                            toast(getString(R.string.unsupported_file_format))
                        }
                        finish()
                    }
                    is OldNote -> {
                        val noteWithImages = NoteWithImages()
                        any.images.forEach {
                            it.noteId = any.key
                        }
                        noteWithImages.note = Note(any)
                        noteWithImages.images = any.images
                        if (noteWithImages.isValid()) {
                            startActivity(Intent(this, CreateNoteActivity::class.java)
                                    .putExtra(Constants.INTENT_ITEM, noteWithImages))
                        } else {
                            toast(getString(R.string.unsupported_file_format))
                        }
                        finish()
                    }
                    is Birthday -> {
                        if (any.isValid()) {
                            startActivity(Intent(this, AddBirthdayActivity::class.java)
                                    .putExtra(Constants.INTENT_ITEM, any))
                        } else {
                            toast(getString(R.string.unsupported_file_format))
                        }
                        finish()
                    }
                    is Reminder -> {
                        if (any.isValid()) {
                            startActivity(Intent(this, CreateReminderActivity::class.java)
                                    .putExtra(Constants.INTENT_ITEM, any))
                        } else {
                            toast(getString(R.string.unsupported_file_format))
                        }
                        finish()
                    }
                    is ReminderGroup -> {
                        if (any.isValid()) {
                            startActivity(Intent(this, CreateGroupActivity::class.java)
                                    .putExtra(Constants.INTENT_ITEM, any))
                        } else {
                            toast(getString(R.string.unsupported_file_format))
                        }
                        finish()
                    }
                    is SmsTemplate -> {
                        if (any.isValid()) {
                            startActivity(Intent(this, TemplateActivity::class.java)
                                    .putExtra(Constants.INTENT_ITEM, any))
                        } else {
                            toast(getString(R.string.unsupported_file_format))
                        }
                        finish()
                    }
                    else -> {
                        toast(getString(R.string.unsupported_file_format))
                        finish()
                    }
                }
            } else {
                toast(getString(R.string.unsupported_file_format))
                finish()
            }
        } else {
            toast(getString(R.string.unsupported_file_format))
            finish()
        }
    }
}

private fun SmsTemplate.isValid(): Boolean {
    return title.isNotBlank()
}

private fun ReminderGroup.isValid(): Boolean {
    return groupTitle.isNotBlank() && groupUuId.isNotEmpty()
}

private fun Place.isValid(): Boolean {
    return latitude != 0.0 && longitude != 0.0 && name.isNotBlank()
}

private fun NoteWithImages.isValid(): Boolean {
    val nt = note
    return nt != null && nt.key.isNotEmpty()
}

private fun Reminder.isValid(): Boolean {
    return summary.isNotBlank() && type > 0 && groupUuId.isNotBlank()
}

private fun Birthday.isValid(): Boolean {
    return name.isNotBlank() && date.isNotBlank() && key.isNotBlank() && day > 0
}
