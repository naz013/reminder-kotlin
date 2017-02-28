package com.elementary.tasks.core.utils;

import android.content.Context;

import com.elementary.tasks.core.cloud.Dropbox;
import com.elementary.tasks.core.cloud.Google;

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
    private Google mDrive;
    private Dropbox mDropbox;

    public IoHelper(Context context) {
        this.mContext = context;
        isConnected = SuperUtil.isConnected(context);
        mDrive = Google.getInstance(context);
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
                if (mDrive != null) {
                    mDrive.getDrive().saveSettingsToDrive();
                }
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
            if (mDrive != null) {
                mDrive.getDrive().saveGroupsToDrive();
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
            mDropbox.downloadGroups(delete);
            try {
                if (mDrive != null) {
                    mDrive.getDrive().downloadGroups(delete);
                }
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
            if (mDrive != null) {
                mDrive.getDrive().saveRemindersToDrive();
            }
        }
    }

    /**
     * Restore all reminder from backup files.
     */
    public void restoreReminder(boolean delete) {
        try {
            BackupTool.getInstance().importReminders(mContext);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isConnected) {
            mDropbox.downloadReminders(delete);
            try {
                if (mDrive != null) {
                    mDrive.getDrive().downloadReminders(mContext, delete);
                }
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
            if (mDrive != null) {
                mDrive.getDrive().saveNotesToDrive();
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
            mDropbox.downloadNotes(delete);
            try {
                if (mDrive != null) {
                    mDrive.getDrive().downloadNotes(delete);
                }
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
            if (mDrive != null) {
                mDrive.getDrive().saveBirthdaysToDrive();
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
            mDropbox.downloadBirthdays(delete);
            try {
                if (mDrive != null) {
                    mDrive.getDrive().downloadBirthdays(delete);
                }
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
            if (mDrive != null) {
                mDrive.getDrive().savePlacesToDrive();
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
            mDropbox.downloadPlaces(delete);
            try {
                if (mDrive != null) {
                    mDrive.getDrive().downloadPlaces(delete);
                }
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
            if (mDrive != null) {
                mDrive.getDrive().saveTemplatesToDrive();
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
            mDropbox.downloadTemplates(delete);
            try {
                if (mDrive != null) {
                    mDrive.getDrive().downloadTemplates(delete);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
