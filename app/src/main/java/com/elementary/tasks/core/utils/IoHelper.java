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

    public IoHelper(Context context) {
        this.mContext = context;
        isConnected = SuperUtil.isConnected(context);
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
            new Dropbox(mContext).uploadSettings();
            try {
                new GoogleDrive(mContext).saveSettingsToDrive();
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
            new Dropbox(mContext).uploadGroups();
            try {
                new GoogleDrive(mContext).saveGroupsToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            new Dropbox(mContext).downloadGroups();
            try {
                new GoogleDrive(mContext).downloadGroups(delete);
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
            new Dropbox(mContext).uploadReminder(null);
            try {
                new GoogleDrive(mContext).saveRemindersToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            new Dropbox(mContext).downloadReminders();
            try {
                new GoogleDrive(mContext).downloadReminders(delete);
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
            new Dropbox(mContext).uploadNotes();
            try {
                new GoogleDrive(mContext).saveNotesToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            new Dropbox(mContext).downloadNotes();
            try {
                new GoogleDrive(mContext).downloadNotes(delete);
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
            new Dropbox(mContext).uploadBirthdays();
            try {
                new GoogleDrive(mContext).saveBirthdaysToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            new Dropbox(mContext).downloadBirthdays();
            try {
                new GoogleDrive(mContext).downloadBirthdays(delete);
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
            new Dropbox(mContext).uploadPlaces();
            try {
                new GoogleDrive(mContext).savePlacesToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            new Dropbox(mContext).downloadPlaces();
            try {
                new GoogleDrive(mContext).downloadPlaces(delete);
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
            new Dropbox(mContext).uploadTemplates();
            try {
                new GoogleDrive(mContext).saveTemplatesToDrive();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            new Dropbox(mContext).downloadTemplates();
            try {
                new GoogleDrive(mContext).downloadTemplates(delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
