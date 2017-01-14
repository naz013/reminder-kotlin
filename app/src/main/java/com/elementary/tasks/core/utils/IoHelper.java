package com.elementary.tasks.core.utils;

import android.content.Context;

import com.elementary.tasks.core.cloud.Dropbox;
import com.elementary.tasks.core.cloud.FileConfig;
import com.elementary.tasks.core.cloud.GoogleDrive;

import java.io.File;
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
     * Delete all local and cloud file copies.
     *
     * @param name file name.
     */
    public void deleteReminder(String name) {
        String exportFileName = name + FileConfig.FILE_NAME_REMINDER;
        File dir = MemoryUtil.getRemindersDir();
        if (dir != null) {
            File file = new File(dir, exportFileName);
            if (file.exists()) file.delete();
        }
        dir = MemoryUtil.getDropboxRemindersDir();
        if (dir != null) {
            File file = new File(dir, exportFileName);
            if (file.exists()) file.delete();
        }
        dir = MemoryUtil.getGoogleRemindersDir();
        if (dir != null) {
            File file = new File(dir, exportFileName);
            if (file.exists()) file.delete();
        }
        if (isConnected) {
            new Dropbox(mContext).deleteReminder(name);
            try {
                new GoogleDrive(mContext).deleteReminderFileByName(name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create backup files for reminders, groups, birthdays and notes.
     */
    public void backup() {
        backupGroup(true);
        backupReminder(true);
        backupNote(true);
        backupBirthday(true);
        backupPlaces(true);
        backupTemplates(true);
    }

    /**
     * Create backup files for groups.
     *
     * @param isCloud create cloud backup.
     */
    public void backupGroup(boolean isCloud) {
        BackupTool.getInstance().exportGroups();
        if (isConnected && isCloud) {
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
     *
     * @param isCloud restore from cloud.
     */
    public void restoreGroup(boolean isCloud, boolean delete) {
        try {
            BackupTool.getInstance().importGroups();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
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
     *
     * @param isCloud create cloud backup.
     */
    public void backupReminder(boolean isCloud) {
        BackupTool.getInstance().exportReminders();
        if (isConnected && isCloud) {
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
     *
     * @param isCloud restore from cloud.
     */
    public void restoreReminder(boolean isCloud, boolean delete) {
        try {
            BackupTool.getInstance().importReminders();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
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
     *
     * @param isCloud create cloud backup.
     */
    public void backupNote(boolean isCloud) {
        BackupTool.getInstance().exportNotes();
        if (isConnected && isCloud) {
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
     *
     * @param isCloud restore from cloud.
     */
    public void restoreNote(boolean isCloud, boolean delete) {
        try {
            BackupTool.getInstance().importNotes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
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
     *
     * @param isCloud create cloud backup.
     */
    public void backupBirthday(boolean isCloud) {
        BackupTool.getInstance().exportBirthdays();
        if (isConnected && isCloud) {
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
     *
     * @param isCloud restore from cloud.
     */
    public void restoreBirthday(boolean isCloud, boolean delete) {
        try {
            BackupTool.getInstance().importBirthdays();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
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
     *
     * @param isCloud create cloud backup.
     */
    public void backupPlaces(boolean isCloud) {
        BackupTool.getInstance().exportPlaces();
        if (isConnected && isCloud) {
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
     *
     * @param isCloud restore from cloud.
     */
    public void restorePlaces(boolean isCloud, boolean delete) {
        try {
            BackupTool.getInstance().importPlaces();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
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
     *
     * @param isCloud create cloud backup.
     */
    public void backupTemplates(boolean isCloud) {
        BackupTool.getInstance().exportTemplates();
        if (isConnected && isCloud) {
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
     *
     * @param isCloud restore from cloud.
     */
    public void restoreTemplates(boolean isCloud, boolean delete) {
        try {
            BackupTool.getInstance().importTemplates();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isConnected && isCloud) {
            new Dropbox(mContext).downloadTemplates();
            try {
                new GoogleDrive(mContext).downloadTemplates(delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
