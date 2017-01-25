package com.elementary.tasks.core.utils;

import android.content.Context;

import com.elementary.tasks.core.cloud.Dropbox;
import com.elementary.tasks.core.cloud.GoogleDrive;

import java.io.IOException;

/**
 * Copyright 2017 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class IoHelper {

    private Context mContext;
    private boolean isConnected;
    private GoogleDrive mDrive;
    private Dropbox mDropbox;

    public IoHelper(Context context) {
        this.mContext = context;
        isConnected = SuperUtil.isConnected(context);
        mDrive = new GoogleDrive(context);
        mDropbox = new Dropbox(context);
    }

    /**
     * Create backup files for reminders, groups, birthdays and notes.
     */
    public void backup() {
        backupGroup();
        backupReminder();
        backupNote();
        backupBirthday();
        backupPlaces();
        backupTemplates();
        backupSettings();
    }

    public void backupSettings() {
        Prefs.getInstance(mContext).savePrefsBackup();
        if (isConnected) {
            mDropbox.uploadSettings();
            try {
                mDrive.saveSettingsToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create backup files for groups.
     */
    public void backupGroup() {
        BackupTool.getInstance().exportGroups();
        if (isConnected) {
            mDropbox.uploadGroups();
            mDrive.saveGroupsToDrive();
        }
    }

    /**
     * Restore all groups from backup files.
     */
    public void restoreGroup(boolean delete) {
        try {
            BackupTool.getInstance().importGroups();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isConnected) {
            mDropbox.downloadGroups(delete);
            try {
                mDrive.downloadGroups(delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create backup files for reminder.
     */
    public void backupReminder() {
        BackupTool.getInstance().exportReminders();
        if (isConnected) {
            mDropbox.uploadReminder(null);
            mDrive.saveRemindersToDrive();
        }
    }

    /**
     * Restore all reminder from backup files.
     */
    public void restoreReminder(boolean delete) {
        try {
            BackupTool.getInstance(mContext).importReminders();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isConnected) {
            mDropbox.downloadReminders(delete);
            try {
                mDrive.downloadReminders(delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create backup files for notes.
     */
    public void backupNote() {
        BackupTool.getInstance().exportNotes();
        if (isConnected) {
            mDropbox.uploadNotes();
            mDrive.saveNotesToDrive();
        }
    }

    /**
     * Restore all notes from backup files.
     */
    public void restoreNote(boolean delete) {
        try {
            BackupTool.getInstance().importNotes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isConnected) {
            mDropbox.downloadNotes(delete);
            try {
                mDrive.downloadNotes(delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create backup files for birthdays.
     */
    public void backupBirthday() {
        BackupTool.getInstance().exportBirthdays();
        if (isConnected) {
            mDropbox.uploadBirthdays();
            mDrive.saveBirthdaysToDrive();
        }
    }

    /**
     * Restore all birthdays from backup files.
     */
    public void restoreBirthday(boolean delete) {
        try {
            BackupTool.getInstance().importBirthdays();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isConnected) {
            mDropbox.downloadBirthdays(delete);
            try {
                mDrive.downloadBirthdays(delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create backup files for places.
     */
    public void backupPlaces() {
        BackupTool.getInstance().exportPlaces();
        if (isConnected) {
            mDropbox.uploadPlaces();
            mDrive.savePlacesToDrive();
        }
    }

    /**
     * Restore all birthdays from backup files.
     */
    public void restorePlaces(boolean delete) {
        try {
            BackupTool.getInstance().importPlaces();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isConnected) {
            mDropbox.downloadPlaces(delete);
            try {
                mDrive.downloadPlaces(delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create backup files for places.
     */
    public void backupTemplates() {
        BackupTool.getInstance().exportTemplates();
        if (isConnected) {
            mDropbox.uploadTemplates();
            mDrive.saveTemplatesToDrive();
        }
    }

    /**
     * Restore all birthdays from backup files.
     */
    public void restoreTemplates(boolean delete) {
        try {
            BackupTool.getInstance().importTemplates();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isConnected) {
            mDropbox.downloadTemplates(delete);
            try {
                mDrive.downloadTemplates(delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
