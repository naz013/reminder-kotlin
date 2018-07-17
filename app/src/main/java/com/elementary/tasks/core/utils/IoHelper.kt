package com.elementary.tasks.core.utils

import android.content.Context

import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.Google

import java.io.IOException

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

class IoHelper(private val mContext: Context) {

    private val isConnected: Boolean = SuperUtil.isConnected(mContext)
    private val mDrive: Google? = Google.getInstance()
    private val mDropbox: Dropbox = Dropbox(mContext)

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
        Prefs.getInstance(mContext).savePrefsBackup()
        if (isConnected) {
            mDropbox.uploadSettings()
            try {
                if (mDrive?.drive != null) {
                    mDrive.drive!!.saveSettingsToDrive()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * Create backup files for groups.
     */
    fun backupGroup() {
        BackupTool.getInstance().exportGroups()
        if (isConnected) {
            mDropbox.uploadGroups()
            if (mDrive?.drive != null) {
                mDrive.drive!!.saveGroupsToDrive()
            }
        }
    }

    /**
     * Restore all groups from backup files.
     */
    fun restoreGroup(delete: Boolean) {
        try {
            BackupTool.getInstance().importGroups()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        if (isConnected) {
            mDropbox.downloadGroups(delete)
            try {
                if (mDrive?.drive != null) {
                    mDrive.drive!!.downloadGroups(delete)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * Create backup files for reminder.
     */
    fun backupReminder() {
        BackupTool.getInstance().exportReminders()
        if (isConnected) {
            mDropbox.uploadReminderByFileName(null)
            if (mDrive?.drive != null) {
                mDrive.drive!!.saveRemindersToDrive()
            }
        }
    }

    /**
     * Restore all reminder from backup files.
     */
    fun restoreReminder(delete: Boolean) {
        try {
            BackupTool.getInstance().importReminders(mContext)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        if (isConnected) {
            mDropbox.downloadReminders(delete)
            try {
                if (mDrive?.drive != null) {
                    mDrive.drive!!.downloadReminders(mContext, delete)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * Create backup files for notes.
     */
    fun backupNote() {
        BackupTool.getInstance().exportNotes()
        if (isConnected) {
            mDropbox.uploadNotes()
            if (mDrive?.drive != null) {
                mDrive.drive!!.saveNotesToDrive()
            }
        }
    }

    /**
     * Restore all notes from backup files.
     */
    fun restoreNote(delete: Boolean) {
        try {
            BackupTool.getInstance().importNotes()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        if (isConnected) {
            mDropbox.downloadNotes(delete)
            try {
                if (mDrive?.drive != null) {
                    mDrive.drive!!.downloadNotes(delete)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * Create backup files for birthdays.
     */
    fun backupBirthday() {
        BackupTool.getInstance().exportBirthdays()
        if (isConnected) {
            mDropbox.uploadBirthdays()
            if (mDrive?.drive != null) {
                mDrive.drive!!.saveBirthdaysToDrive()
            }
        }
    }

    /**
     * Restore all birthdays from backup files.
     */
    fun restoreBirthday(delete: Boolean) {
        try {
            BackupTool.getInstance().importBirthdays()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        if (isConnected) {
            mDropbox.downloadBirthdays(delete)
            try {
                if (mDrive?.drive != null) {
                    mDrive.drive!!.downloadBirthdays(delete)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * Create backup files for places.
     */
    fun backupPlaces() {
        BackupTool.getInstance().exportPlaces()
        if (isConnected) {
            mDropbox.uploadPlaces()
            if (mDrive?.drive != null) {
                mDrive.drive!!.savePlacesToDrive()
            }
        }
    }

    /**
     * Restore all birthdays from backup files.
     */
    fun restorePlaces(delete: Boolean) {
        try {
            BackupTool.getInstance().importPlaces()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        if (isConnected) {
            mDropbox.downloadPlaces(delete)
            try {
                if (mDrive?.drive != null) {
                    mDrive.drive!!.downloadPlaces(delete)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * Create backup files for places.
     */
    fun backupTemplates() {
        BackupTool.getInstance().exportTemplates()
        if (isConnected) {
            mDropbox.uploadTemplates()
            if (mDrive?.drive != null) {
                mDrive.drive!!.saveTemplatesToDrive()
            }
        }
    }

    /**
     * Restore all birthdays from backup files.
     */
    fun restoreTemplates(delete: Boolean) {
        try {
            BackupTool.getInstance().importTemplates()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        if (isConnected) {
            mDropbox.downloadTemplates(delete)
            try {
                if (mDrive?.drive != null) {
                    mDrive.drive!!.downloadTemplates(delete)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
}
