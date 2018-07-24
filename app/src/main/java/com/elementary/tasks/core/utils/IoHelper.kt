package com.elementary.tasks.core.utils

import android.content.Context
import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.Google
import javax.inject.Inject
import javax.inject.Singleton

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
@Singleton
class IoHelper @Inject constructor(private val context: Context, private val prefs: Prefs,
                                   private val backupTool: BackupTool) {

    private val isConnected: Boolean = SuperUtil.isConnected(context)
    private val mDrive: Google? = Google.getInstance()
    private val mDropbox: Dropbox = Dropbox()

    /**
     * Create backup files for reminders, groups, birthdays and notes.
     */
    fun backup() {
        backupGroup()
        backupReminder()
        backupNote()
        backupBirthday()
        backupPlaces()
        backupTemplates()
        backupSettings()
    }

    fun backupSettings() {
        prefs.savePrefsBackup()
        if (isConnected) {
            mDropbox.uploadSettings()
            try {
                if (mDrive?.drive != null) {
                    mDrive.drive?.saveSettingsToDrive()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Create backup files for groups.
     */
    fun backupGroup() {
        backupTool.exportGroups()
        if (isConnected) {
            mDropbox.uploadGroups()
            if (mDrive?.drive != null) {
                try {
                    mDrive.drive?.saveGroupsToDrive()
                } catch (e: Exception) {
                }
            }
        }
    }

    /**
     * Restore all groups from backup files.
     */
    fun restoreGroup(delete: Boolean) {
        try {
            backupTool.importGroups()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (isConnected) {
            mDropbox.downloadGroups(delete)
            try {
                if (mDrive?.drive != null) {
                    mDrive.drive?.downloadGroups(delete)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Create backup files for reminder.
     */
    fun backupReminder() {
        backupTool.exportReminders()
        if (isConnected) {
            mDropbox.uploadReminderByFileName(null)
            if (mDrive?.drive != null) {
                try {
                    mDrive.drive?.saveRemindersToDrive()
                } catch (e: Exception) {
                }
            }
        }
    }

    /**
     * Restore all reminder from backup files.
     */
    fun restoreReminder(delete: Boolean) {
        try {
            backupTool.importReminders(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (isConnected) {
            mDropbox.downloadReminders(delete)
            try {
                if (mDrive?.drive != null) {
                    mDrive.drive?.downloadReminders(context, delete)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Create backup files for notes.
     */
    fun backupNote() {
        backupTool.exportNotes()
        if (isConnected) {
            mDropbox.uploadNotes()
            if (mDrive?.drive != null) {
                try {
                    mDrive.drive?.saveNotesToDrive()
                } catch (e: Exception) {
                }
            }
        }
    }

    /**
     * Restore all notes from backup files.
     */
    fun restoreNote(delete: Boolean) {
        try {
            backupTool.importNotes()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (isConnected) {
            mDropbox.downloadNotes(delete)
            try {
                if (mDrive?.drive != null) {
                    mDrive.drive?.downloadNotes(delete)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Create backup files for birthdays.
     */
    fun backupBirthday() {
        backupTool.exportBirthdays()
        if (isConnected) {
            mDropbox.uploadBirthdays()
            if (mDrive?.drive != null) {
                try {
                    mDrive.drive?.saveBirthdaysToDrive()
                } catch (e: Exception) {
                }
            }
        }
    }

    /**
     * Restore all birthdays from backup files.
     */
    fun restoreBirthday(delete: Boolean) {
        try {
            backupTool.importBirthdays()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (isConnected) {
            mDropbox.downloadBirthdays(delete)
            try {
                if (mDrive?.drive != null) {
                    mDrive.drive?.downloadBirthdays(delete)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Create backup files for places.
     */
    fun backupPlaces() {
        backupTool.exportPlaces()
        if (isConnected) {
            mDropbox.uploadPlaces()
            if (mDrive?.drive != null) {
                try {
                    mDrive.drive?.savePlacesToDrive()
                } catch (e: Exception) {
                }
            }
        }
    }

    /**
     * Restore all birthdays from backup files.
     */
    fun restorePlaces(delete: Boolean) {
        try {
            backupTool.importPlaces()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (isConnected) {
            mDropbox.downloadPlaces(delete)
            try {
                if (mDrive?.drive != null) {
                    mDrive.drive?.downloadPlaces(delete)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Create backup files for places.
     */
    fun backupTemplates() {
        backupTool.exportTemplates()
        if (isConnected) {
            mDropbox.uploadTemplates()
            if (mDrive?.drive != null) {
                try {
                    mDrive.drive?.saveTemplatesToDrive()
                } catch (e: Exception) {
                }
            }
        }
    }

    /**
     * Restore all birthdays from backup files.
     */
    fun restoreTemplates(delete: Boolean) {
        try {
            backupTool.importTemplates()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (isConnected) {
            mDropbox.downloadTemplates(delete)
            try {
                if (mDrive?.drive != null) {
                    mDrive.drive?.downloadTemplates(delete)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
