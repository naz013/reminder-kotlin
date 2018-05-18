package com.elementary.tasks.core.cloud;

import android.content.Context;
import android.os.Environment;
import androidx.annotation.Nullable;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.users.FullAccount;
import com.dropbox.core.v2.users.SpaceUsage;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.utils.BackupTool;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.MemoryUtil;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.reminder.models.Reminder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.OkHttpClient;

/**
 * Copyright 2016 Nazar Suhovich
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

public class Dropbox {

    private static final String TAG = "Dropbox";
    private static final String APP_KEY = "4zi1d414h0v8sxe";

    private Context mContext;

    private String dbxFolder = "/Reminders/";
    private String dbxNoteFolder = "/Notes/";
    private String dbxGroupFolder = "/Groups/";
    private String dbxBirthFolder = "/Birthdays/";
    private String dbxPlacesFolder = "/Places/";
    private String dbxTemplatesFolder = "/Templates/";
    private String dbxSettingsFolder = "/Settings/";

    private DbxClientV2 mDBApi;

    public Dropbox(Context context) {
        this.mContext = context;
    }

    /**
     * Start connection to Dropbox.
     */
    public void startSession() {
        String token = Prefs.getInstance(mContext).getDropboxToken();
        if (token == null) {
            token = Auth.getOAuth2Token();
            Prefs.getInstance(mContext).setDropboxToken(token);
        }
        LogUtil.d(TAG, "startSession: " + token);
        if (token == null) {
            Prefs.getInstance(mContext).setDropboxToken(null);
            return;
        }
        DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("Just Reminder")
                .withHttpRequestor(new OkHttp3Requestor(new OkHttpClient()))
                .build();

        mDBApi = new DbxClientV2(requestConfig, token);
    }

    /**
     * Check if user has already connected to Dropbox from this application.
     *
     * @return Boolean
     */
    public boolean isLinked() {
        return mDBApi != null && Prefs.getInstance(mContext).getDropboxToken() != null;
    }

    /**
     * Holder Dropbox user name.
     *
     * @return String user name
     */
    public String userName() {
        FullAccount account = null;
        try {
            account = mDBApi.users().getCurrentAccount();
        } catch (DbxException e) {
            e.printStackTrace();
        }
        return account != null ? account.getName().getDisplayName() : null;
    }

    /**
     * Holder user all apace on Dropbox.
     *
     * @return Long - user quota
     */
    public long userQuota() {
        SpaceUsage account = null;
        try {
            account = mDBApi.users().getSpaceUsage();
        } catch (DbxException e) {
            LogUtil.e(TAG, "userQuota: ", e);
        }
        return account != null ? account.getAllocation().getIndividualValue().getAllocated() : 0;
    }

    public long userQuotaNormal() {
        SpaceUsage account = null;
        try {
            account = mDBApi.users().getSpaceUsage();
        } catch (DbxException e) {
            LogUtil.e(TAG, "userQuotaNormal: ", e);
        }
        return account != null ? account.getUsed() : 0;
    }

    public void startLink() {
        Auth.startOAuth2Authentication(mContext, APP_KEY);
    }

    public boolean unlink() {
        boolean is = false;
        if (logOut()) {
            is = true;
        }
        return is;
    }

    private boolean logOut() {
        clearKeys();
        return true;
    }

    private void clearKeys() {
        Prefs.getInstance(mContext).setDropboxToken(null);
        Prefs.getInstance(mContext).setDropboxUid(null);
    }

    /**
     * Upload to Dropbox folder backup files from selected folder on SD Card.
     *
     * @param path name of folder to upload.
     */
    private void upload(String path) {
        startSession();
        if (!isLinked()) {
            return;
        }
        File sdPath = Environment.getExternalStorageDirectory();
        File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + path);
        File[] files = sdPathDr.listFiles();
        String fileLoc = sdPathDr.toString();
        if (files == null) {
            return;
        }
        for (File file : files) {
            String fileLoopName = file.getName();
            File tmpFile = new File(fileLoc, fileLoopName);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(tmpFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String folder;
            if (path.matches(MemoryUtil.DIR_NOTES_SD)) {
                folder = dbxNoteFolder;
            } else if (path.matches(MemoryUtil.DIR_GROUP_SD)) {
                folder = dbxGroupFolder;
            } else if (path.matches(MemoryUtil.DIR_BIRTHDAY_SD)) {
                folder = dbxBirthFolder;
            } else if (path.matches(MemoryUtil.DIR_PLACES_SD)) {
                folder = dbxPlacesFolder;
            } else if (path.matches(MemoryUtil.DIR_TEMPLATES_SD)) {
                folder = dbxTemplatesFolder;
            } else {
                folder = dbxFolder;
            }
            if (fis == null) return;
            try {
                String filePath = folder + fileLoopName;
                mDBApi.files().uploadBuilder(filePath)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(fis);
            } catch (DbxException | IOException e) {
                LogUtil.e(TAG, "Something went wrong while uploading.", e);
            }
        }
    }

    /**
     * Upload reminder backup files or selected file to Dropbox folder.
     *
     * @param fileName file name.
     */
    public void uploadReminderByFileName(@Nullable final String fileName) {
        File dir = MemoryUtil.getRemindersDir();
        if (dir == null) {
            return;
        }
        startSession();
        if (!isLinked()) {
            return;
        }
        if (fileName != null) {
            File tmpFile = new File(dir.toString(), fileName);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(tmpFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (fis == null) return;
            try {
                mDBApi.files().uploadBuilder(dbxFolder + fileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(fis);
            } catch (DbxException | IOException | NullPointerException e) {
                LogUtil.e(TAG, "Something went wrong while uploading.", e);
            }
        } else {
            upload(MemoryUtil.DIR_SD);
        }
    }

    /**
     * Upload all note backup files to Dropbox folder.
     */
    public void uploadNotes() {
        upload(MemoryUtil.DIR_NOTES_SD);
    }

    /**
     * Upload all group backup files to Dropbox folder.
     */
    public void uploadGroups() {
        upload(MemoryUtil.DIR_GROUP_SD);
    }

    /**
     * Upload all birthday backup files to Dropbox folder.
     */
    public void uploadBirthdays() {
        upload(MemoryUtil.DIR_BIRTHDAY_SD);
    }

    /**
     * Upload all places backup files to Dropbox folder.
     */
    public void uploadPlaces() {
        upload(MemoryUtil.DIR_PLACES_SD);
    }

    /**
     * Upload all templates backup files to Dropbox folder.
     */
    public void uploadTemplates() {
        upload(MemoryUtil.DIR_TEMPLATES_SD);
    }

    public void deleteFile(String fileName) {
        if (fileName.endsWith(FileConfig.FILE_NAME_REMINDER)) {
            deleteReminder(fileName);
        } else if (fileName.endsWith(FileConfig.FILE_NAME_NOTE)) {
            deleteNote(fileName);
        } else if (fileName.endsWith(FileConfig.FILE_NAME_GROUP)) {
            deleteGroup(fileName);
        } else if (fileName.endsWith(FileConfig.FILE_NAME_BIRTHDAY)) {
            deleteBirthday(fileName);
        } else if (fileName.endsWith(FileConfig.FILE_NAME_PLACE)) {
            deletePlace(fileName);
        } else if (fileName.endsWith(FileConfig.FILE_NAME_TEMPLATE)) {
            deleteTemplate(fileName);
        } else if (fileName.endsWith(FileConfig.FILE_NAME_SETTINGS)) {
            deleteSettings(fileName);
        }
    }

    /**
     * Delete reminder backup file from Dropbox folder.
     *
     * @param name file name.
     */
    public void deleteReminder(String name) {
        LogUtil.d(TAG, "deleteReminder: " + name);
        startSession();
        if (!isLinked()) {
            return;
        }
        try {
            mDBApi.files().delete(dbxFolder + name);
        } catch (DbxException e) {
            LogUtil.e(TAG, "deleteReminder: ", e);
        }
    }

    /**
     * Delete note backup file from Dropbox folder.
     *
     * @param name file name.
     */
    public void deleteNote(String name) {
        startSession();
        if (!isLinked()) {
            return;
        }
        try {
            mDBApi.files().delete(dbxNoteFolder + name);
        } catch (DbxException e) {
            LogUtil.e(TAG, "deleteNote: ", e);
        }
    }

    /**
     * Delete group backup file from Dropbox folder.
     *
     * @param name file name.
     */
    public void deleteGroup(String name) {
        startSession();
        if (!isLinked()) {
            return;
        }
        try {
            mDBApi.files().delete(dbxGroupFolder + name);
        } catch (DbxException e) {
            LogUtil.e(TAG, "deleteGroup: " + name, e);
        }
    }

    /**
     * Delete birthday backup file from Dropbox folder.
     *
     * @param name file name
     */
    public void deleteBirthday(String name) {
        startSession();
        if (!isLinked()) {
            return;
        }
        try {
            mDBApi.files().delete(dbxBirthFolder + name);
        } catch (DbxException e) {
            LogUtil.e(TAG, "deleteBirthday: ", e);
        }
    }

    /**
     * Delete place backup file from Dropbox folder.
     *
     * @param name file name
     */
    public void deletePlace(String name) {
        startSession();
        if (!isLinked()) {
            return;
        }
        try {
            mDBApi.files().delete(dbxPlacesFolder + name);
        } catch (DbxException e) {
            LogUtil.e(TAG, "deletePlace: ", e);
        }
    }

    /**
     * Delete place backup file from Dropbox folder.
     *
     * @param name file name
     */
    public void deleteTemplate(String name) {
        startSession();
        if (!isLinked()) {
            return;
        }
        try {
            mDBApi.files().delete(dbxTemplatesFolder + name);
        } catch (DbxException e) {
            LogUtil.e(TAG, "deleteTemplate: ", e);
        }
    }

    /**
     * Delete settings backup file from Dropbox folder.
     *
     * @param name file name
     */
    public void deleteSettings(String name) {
        startSession();
        if (!isLinked()) {
            return;
        }
        try {
            mDBApi.files().delete(dbxSettingsFolder + name);
        } catch (DbxException e) {
            LogUtil.e(TAG, "deleteSettings: ", e);
        }
    }

    /**
     * Delete all folders inside application folder on Dropbox.
     */
    public void cleanFolder() {
        startSession();
        if (!isLinked()) {
            return;
        }
        deleteFolder(dbxNoteFolder);
        deleteFolder(dbxGroupFolder);
        deleteFolder(dbxBirthFolder);
        deleteFolder(dbxPlacesFolder);
        deleteFolder(dbxTemplatesFolder);
        deleteFolder(dbxSettingsFolder);
        deleteFolder(dbxFolder);
    }

    private void deleteFolder(String folder) {
        try {
            mDBApi.files().delete(folder);
        } catch (DbxException e) {
            LogUtil.e(TAG, "deleteFolder: ", e);
        }
    }

    /**
     * Download on SD Card all template backup files found on Dropbox.
     */
    public void downloadTemplates(boolean deleteFile) {
        File dir = MemoryUtil.getDropboxTemplatesDir();
        if (dir == null) {
            return;
        }
        startSession();
        if (!isLinked()) {
            return;
        }
        try {
            ListFolderResult result = mDBApi.files().listFolder(dbxTemplatesFolder);
            if (result == null) {
                return;
            }
            RealmDb realmDb = RealmDb.getInstance();
            BackupTool backupTool = BackupTool.getInstance();
            for (Metadata e : result.getEntries()) {
                String fileName = e.getName();
                File localFile = new File(dir + "/" + fileName);
                String cloudFile = dbxTemplatesFolder + fileName;
                downloadFile(localFile, cloudFile);
                Reminder reminder = backupTool.getReminder(localFile.toString(), null);
                if (reminder != null) realmDb.saveReminder(reminder, null);
                if (deleteFile) {
                    if (localFile.exists()) {
                        localFile.delete();
                    }
                    mDBApi.files().delete(e.getPathLower());
                }
            }
        } catch (DbxException | IOException | IllegalStateException e) {
            LogUtil.e(TAG, "downloadTemplates: ", e);
        }
    }

    /**
     * Download on SD Card all reminder backup files found on Dropbox.
     */
    public void downloadReminders(boolean deleteFile) {
        File dir = MemoryUtil.getDropboxRemindersDir();
        if (dir == null) {
            return;
        }
        startSession();
        if (!isLinked()) {
            return;
        }
        try {
            ListFolderResult result = mDBApi.files().listFolder(dbxFolder);
            if (result == null) {
                return;
            }
            RealmDb realmDb = RealmDb.getInstance();
            BackupTool backupTool = BackupTool.getInstance();
            for (Metadata e : result.getEntries()) {
                String fileName = e.getName();
                File localFile = new File(dir + "/" + fileName);
                String cloudFile = dbxFolder + fileName;
                downloadFile(localFile, cloudFile);
                Reminder reminder = backupTool.getReminder(localFile.toString(), null);
                if (reminder == null || reminder.isRemoved() || !reminder.isActive()) {
                    continue;
                }
                realmDb.saveReminder(reminder, () -> {
                    EventControl control = EventControlFactory.getController(mContext, reminder);
                    if (control.canSkip()) {
                        control.next();
                    } else {
                        control.start();
                    }
                });
                if (deleteFile) {
                    if (localFile.exists()) {
                        localFile.delete();
                    }
                    mDBApi.files().delete(e.getPathLower());
                }
            }
        } catch (DbxException | IOException | IllegalStateException e) {
            LogUtil.e(TAG, "downloadReminders: ", e);
        }
    }

    private void downloadFile(File localFile, String cloudFile) {
        try {
            if (!localFile.exists()) {
                localFile.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(localFile);
            mDBApi.files().download(cloudFile).download(outputStream);
        } catch (DbxException | IOException e1) {
            LogUtil.e(TAG, "downloadFile: ", e1);
        }
    }

    /**
     * Download on SD Card all note backup files found on Dropbox.
     */
    public void downloadNotes(boolean deleteFile) {
        File dir = MemoryUtil.getDropboxNotesDir();
        if (dir == null) {
            return;
        }
        startSession();
        if (!isLinked()) {
            return;
        }
        try {
            ListFolderResult result = mDBApi.files().listFolder(dbxNoteFolder);
            if (result == null) {
                return;
            }
            RealmDb realmDb = RealmDb.getInstance();
            BackupTool backupTool = BackupTool.getInstance();
            for (Metadata e : result.getEntries()) {
                String fileName = e.getName();
                File localFile = new File(dir + "/" + fileName);
                String cloudFile = dbxNoteFolder + fileName;
                downloadFile(localFile, cloudFile);
                realmDb.saveObject(backupTool.getNote(localFile.toString(), null));
                if (deleteFile) {
                    if (localFile.exists()) {
                        localFile.delete();
                    }
                    mDBApi.files().delete(e.getPathLower());
                }
            }
        } catch (DbxException | IOException | IllegalStateException e) {
            LogUtil.e(TAG, "downloadNotes: ", e);
        }

    }

    /**
     * Download on SD Card all group backup files found on Dropbox.
     */
    public void downloadGroups(boolean deleteFile) {
        File dir = MemoryUtil.getDropboxGroupsDir();
        if (dir == null) {
            return;
        }
        startSession();
        if (!isLinked()) {
            return;
        }
        try {
            ListFolderResult result = mDBApi.files().listFolder(dbxGroupFolder);
            if (result == null) {
                return;
            }
            RealmDb realmDb = RealmDb.getInstance();
            BackupTool backupTool = BackupTool.getInstance();
            for (Metadata e : result.getEntries()) {
                String fileName = e.getName();
                File localFile = new File(dir + "/" + fileName);
                String cloudFile = dbxGroupFolder + fileName;
                downloadFile(localFile, cloudFile);
                realmDb.saveObject(backupTool.getGroup(localFile.toString(), null));
                if (deleteFile) {
                    if (localFile.exists()) {
                        localFile.delete();
                    }
                    mDBApi.files().delete(e.getPathLower());
                }
            }
        } catch (DbxException | IOException | IllegalStateException e) {
            LogUtil.e(TAG, "downloadGroups: ", e);
        }
    }

    /**
     * Download on SD Card all birthday backup files found on Dropbox.
     */
    public void downloadBirthdays(boolean deleteFile) {
        File dir = MemoryUtil.getDropboxBirthdaysDir();
        if (dir == null) {
            return;
        }
        startSession();
        if (!isLinked()) {
            return;
        }
        try {
            ListFolderResult result = mDBApi.files().listFolder(dbxBirthFolder);
            if (result == null) {
                return;
            }
            RealmDb realmDb = RealmDb.getInstance();
            BackupTool backupTool = BackupTool.getInstance();
            for (Metadata e : result.getEntries()) {
                String fileName = e.getName();
                File localFile = new File(dir + "/" + fileName);
                String cloudFile = dbxBirthFolder + fileName;
                downloadFile(localFile, cloudFile);
                realmDb.saveObject(backupTool.getBirthday(localFile.toString(), null));
                if (deleteFile) {
                    if (localFile.exists()) {
                        localFile.delete();
                    }
                    mDBApi.files().delete(e.getPathLower());
                }
            }
        } catch (DbxException | IOException | IllegalStateException e) {
            LogUtil.e(TAG, "downloadBirthdays: ", e);
        }
    }

    /**
     * Download on SD Card all places backup files found on Dropbox.
     */
    public void downloadPlaces(boolean deleteFile) {
        File dir = MemoryUtil.getDropboxPlacesDir();
        if (dir == null) {
            return;
        }
        startSession();
        if (!isLinked()) {
            return;
        }
        try {
            ListFolderResult result = mDBApi.files().listFolder(dbxPlacesFolder);
            if (result == null) {
                return;
            }
            RealmDb realmDb = RealmDb.getInstance();
            BackupTool backupTool = BackupTool.getInstance();
            for (Metadata e : result.getEntries()) {
                String fileName = e.getName();
                File localFile = new File(dir + "/" + fileName);
                String cloudFile = dbxPlacesFolder + fileName;
                downloadFile(localFile, cloudFile);
                realmDb.saveObject(backupTool.getPlace(localFile.toString(), null));
                if (deleteFile) {
                    if (localFile.exists()) {
                        localFile.delete();
                    }
                    mDBApi.files().delete(e.getPathLower());
                }
            }
        } catch (DbxException | IOException | IllegalStateException e) {
            LogUtil.e(TAG, "downloadPlaces: ", e);
        }
    }

    public void uploadSettings() {
        File dir = MemoryUtil.getPrefsDir();
        if (dir == null) {
            return;
        }
        startSession();
        if (!isLinked()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (!file.toString().endsWith(FileConfig.FILE_NAME_SETTINGS)) {
                continue;
            }
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (fis == null) return;
            try {
                mDBApi.files().uploadBuilder(dbxSettingsFolder + file.getName())
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(fis);
            } catch (DbxException | IOException e) {
                LogUtil.e(TAG, "Something went wrong while uploading.", e);
            }
            break;
        }
    }

    public void downloadSettings() {
        File dir = MemoryUtil.getPrefsDir();
        if (dir == null) {
            return;
        }
        startSession();
        if (!isLinked()) {
            return;
        }
        try {
            ListFolderResult result = mDBApi.files().listFolder(dbxSettingsFolder);
            if (result == null) {
                return;
            }
            for (Metadata e : result.getEntries()) {
                String fileName = e.getName();
                if (fileName.contains(FileConfig.FILE_NAME_SETTINGS)) {
                    File localFile = new File(dir + "/" + fileName);
                    String cloudFile = dbxPlacesFolder + fileName;
                    downloadFile(localFile, cloudFile);
                    Prefs.getInstance(mContext).loadPrefsFromFile();
                    break;
                }
            }
        } catch (DbxException e) {
            LogUtil.e(TAG, "downloadSettings: ", e);
        }
    }

    /**
     * Count all reminder backup files in Dropbox folder.
     *
     * @return number of found backup files.
     */
    public int countFiles() {
        int count = 0;
        startSession();
        if (!isLinked()) {
            return 0;
        }
        try {
            ListFolderResult result = mDBApi.files().listFolder("/");
            if (result == null) {
                return 0;
            }
        } catch (DbxException e) {
            LogUtil.e(TAG, "countFiles: ", e);
        }
        return count;
    }
}
