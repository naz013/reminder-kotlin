package com.elementary.tasks.core.cloud;

import android.content.Context;

import com.elementary.tasks.backups.UserItem;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlImpl;
import com.elementary.tasks.core.utils.BackupTool;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.MemoryUtil;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.reminder.models.Reminder;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

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

public class GoogleDrive {

    private static final String TAG = "GoogleDrive";

    private Context mContext;

    private final HttpTransport mTransport = AndroidHttp.newCompatibleTransport();
    private final JsonFactory mJsonFactory = GsonFactory.getDefaultInstance();
    private Drive driveService;
    private static final String APPLICATION_NAME = "Reminder/6.0";
    private static final String FOLDER_NAME = "Reminder";

    public GoogleDrive(Context context){
        this.mContext = context;
    }

    /**
     * Authorization method.
     */
    public void authorize(){
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(mContext, Collections.singleton(DriveScopes.DRIVE));
        credential.setSelectedAccountName(SuperUtil.decrypt(Prefs.getInstance(mContext).getDriveUser()));
        driveService = new Drive.Builder(
                mTransport, mJsonFactory, credential).setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Check if user has already login to Google Drive from this application.
     * @return return true if user was already logged.
     */
    public boolean isLinked(){
        return SuperUtil.decrypt(Prefs.getInstance(mContext).getDriveUser()).matches(".*@.*");
    }

    /**
     * Logout from Drive on this application.
     */
    public void unlink(){
        Prefs.getInstance(mContext).setDriveUser(Prefs.DRIVE_USER_NONE);
    }

    /**
     * Get information about user.
     * @return user info object
     */
    public UserItem getData() {
        if (isLinked()) {
            authorize();
            try {
                About about = driveService.about().get().setFields("user, storageQuota").execute();
                About.StorageQuota quota = about.getStorageQuota();
                return new UserItem(about.getUser().getDisplayName(), quota.getLimit(),
                        quota.getUsage(), countFiles(), about.getUser().getPhotoLink());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Count all backup files stored on Google Drive.
     * @return number of files in local folder.
     */
    public int countFiles() throws IOException {
        if (!isLinked()) return 0;
        int count = 0;
        authorize();
        Drive.Files.List request = driveService.files().list().setQ("mimeType = 'text/plain'").setFields("nextPageToken, files");
        do {
            FileList files = request.execute();
            ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
            for (com.google.api.services.drive.model.File f : fileList) {
                String title = f.getName();
                if (title.contains(FileConfig.FILE_NAME_SETTINGS)) {
                    count++;
                } else if (title.endsWith(FileConfig.FILE_NAME_TEMPLATE)) {
                    count++;
                } else if (title.endsWith(FileConfig.FILE_NAME_PLACE)) {
                    count++;
                } else if (title.endsWith(FileConfig.FILE_NAME_BIRTHDAY)) {
                    count++;
                } else if (title.endsWith(FileConfig.FILE_NAME_GROUP)) {
                    count++;
                } else if (title.endsWith(FileConfig.FILE_NAME_NOTE)) {
                    count++;
                } else if (title.endsWith(FileConfig.FILE_NAME_REMINDER)) {
                    count++;
                }
            }
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        return count;
    }

    public void downloadData(boolean deleteBackup, boolean isPrefs) throws IOException {
        if (!isLinked()) return;
        authorize();
        java.io.File folderR = MemoryUtil.getGoogleRemindersDir();
        boolean isReminders = true;
        if (!folderR.exists() && !folderR.mkdirs()) {
            isReminders = false;
        }
        java.io.File folderN = MemoryUtil.getGoogleNotesDir();
        boolean isNotes = true;
        if (!folderN.exists() && !folderN.mkdirs()) {
            isNotes = false;
        }
        java.io.File folderG = MemoryUtil.getGoogleGroupsDir();
        boolean isGroups = true;
        if (!folderG.exists() && !folderG.mkdirs()) {
            isGroups = false;
        }
        java.io.File folderB = MemoryUtil.getGoogleBirthdaysDir();
        boolean isBirthdays = true;
        if (!folderB.exists() && !folderB.mkdirs()) {
            isBirthdays = false;
        }
        java.io.File folderP = MemoryUtil.getGooglePlacesDir();
        boolean isPlaces = true;
        if (!folderP.exists() && !folderP.mkdirs()) {
            isPlaces = false;
        }
        java.io.File folderT = MemoryUtil.getGoogleTemplatesDir();
        boolean isTemplates = true;
        if (!folderT.exists() && !folderT.mkdirs()) {
            isTemplates = false;
        }
        java.io.File folderPrefs = MemoryUtil.getGooglePrefsDir();
        if (isPrefs) {
            if (!folderPrefs.exists() && !folderPrefs.mkdirs()) {
                isPrefs = false;
            }
        }
        Drive.Files.List request = driveService.files().list().setQ("mimeType = 'text/plain'").setFields("nextPageToken, files");
        RealmDb realmDb = RealmDb.getInstance();
        BackupTool backupTool = BackupTool.getInstance();
        do {
            FileList files = request.execute();
            ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
            for (com.google.api.services.drive.model.File f : fileList) {
                String title = f.getName();
                if (title.endsWith(FileConfig.FILE_NAME_REMINDER)) {
                    java.io.File file = new java.io.File(folderR, title);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    OutputStream out = new FileOutputStream(file);
                    driveService.files().get(f.getId()).executeMediaAndDownloadTo(out);
                    if (deleteBackup) deleteFileById(f.getId());
                    realmDb.saveObject(backupTool.getReminder(file.toString(), null));
                    if (file.exists()) {
                        file.delete();
                    }

                }
            }
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
    }

    public void saveSettingsToDrive() throws IOException {
        if (!isLinked()) return;
        authorize();
        String folderId = getFolderId();
        if (folderId == null){
            return;
        }
        java.io.File folder = MemoryUtil.getPrefsDir();
        if (folder == null) return;
        java.io.File[] files = folder.listFiles();
        if (files == null) return;
        for (java.io.File file : files) {
            if (!file.toString().endsWith(FileConfig.FILE_NAME_SETTINGS)) continue;
            File fileMetadata = new File();
            fileMetadata.setName(file.getName());
            fileMetadata.setDescription("Settings Backup");
            fileMetadata.setParents(Collections.singletonList(folderId));
            FileContent mediaContent = new FileContent("text/plain", file);
            driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
            break;
        }
    }

    public void downloadSettings() throws IOException {
        java.io.File folder = MemoryUtil.getPrefsDir();
        if (!folder.exists() && !folder.mkdirs() || !isLinked()) {
            return;
        }
        authorize();
        Drive.Files.List request = driveService.files().list()
                .setQ("mimeType = 'text/plain' and name contains '" + FileConfig.FILE_NAME_SETTINGS + "'")
                .setFields("nextPageToken, files");
        do {
            FileList files = request.execute();
            ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
            for (com.google.api.services.drive.model.File f : fileList) {
                String title = f.getName();
                if (title.contains(FileConfig.FILE_NAME_SETTINGS)) {
                    java.io.File file = new java.io.File(folder, title);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    OutputStream out = new FileOutputStream(file);
                    driveService.files().get(f.getId()).executeMediaAndDownloadTo(out);
                    Prefs.getInstance(mContext).loadPrefsFromFile();
                    break;
                }
            }
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
    }

    /**
     * Upload all template backup files stored on SD Card.
     * @throws IOException
     */
    public void saveTemplatesToDrive() throws IOException {
        if (!isLinked()) return;
        authorize();
        String folderId = getFolderId();
        if (folderId == null){
            return;
        }
        java.io.File folder = MemoryUtil.getTemplatesDir();
        if (folder == null) return;
        java.io.File[] files = folder.listFiles();
        if (files == null) return;
        for (java.io.File file : files) {
            if (!file.toString().endsWith(FileConfig.FILE_NAME_TEMPLATE)) continue;
            File fileMetadata = new File();
            fileMetadata.setName(file.getName());
            fileMetadata.setDescription("Template Backup");
            fileMetadata.setParents(Collections.singletonList(folderId));
            FileContent mediaContent = new FileContent("text/plain", file);
            driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
        }
    }

    /**
     * Upload all reminder backup files stored on SD Card.
     * @throws IOException
     */
    public void saveRemindersToDrive() throws IOException {
        if (!isLinked()) return;
        authorize();
        String folderId = getFolderId();
        if (folderId == null){
            return;
        }
        java.io.File folder = MemoryUtil.getRemindersDir();
        if (folder == null) return;
        java.io.File[] files = folder.listFiles();
        if (files == null) return;
        for (java.io.File file : files) {
            if (!file.toString().endsWith(FileConfig.FILE_NAME_REMINDER)) continue;
            File fileMetadata = new File();
            fileMetadata.setName(file.getName());
            fileMetadata.setDescription("Reminder Backup");
            fileMetadata.setParents(Collections.singletonList(folderId));
            FileContent mediaContent = new FileContent("text/plain", file);
            driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
        }
    }

    /**
     * Upload all note backup files stored on SD Card.
     * @throws IOException
     */
    public void saveNotesToDrive() throws IOException {
        if (!isLinked()) return;
        authorize();
        String folderId = getFolderId();
        if (folderId == null){
            return;
        }
        java.io.File folder = MemoryUtil.getNotesDir();
        if (folder == null) return;
        java.io.File[] files = folder.listFiles();
        if (files == null) return;
        for (java.io.File file : files) {
            if (!file.toString().endsWith(FileConfig.FILE_NAME_NOTE)) continue;
            File fileMetadata = new File();
            fileMetadata.setName(file.getName());
            fileMetadata.setDescription("Note Backup");
            fileMetadata.setParents(Collections.singletonList(folderId));
            FileContent mediaContent = new FileContent("text/plain", file);
            driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
        }
    }

    /**
     * Upload all group backup files stored on SD Card.
     * @throws IOException
     */
    public void saveGroupsToDrive() throws IOException {
        if (!isLinked()) return;
        java.io.File folder = MemoryUtil.getGroupsDir();
        if (folder == null) return;
        java.io.File[] files = folder.listFiles();
        if (files == null) return;
        authorize();
        String folderId = getFolderId();
        if (folderId == null){
            return;
        }
        for (java.io.File file : files) {
            if (!file.getName().endsWith(FileConfig.FILE_NAME_GROUP)) continue;
            File fileMetadata = new File();
            fileMetadata.setName(file.getName());
            fileMetadata.setDescription("Group Backup");
            fileMetadata.setParents(Collections.singletonList(folderId));
            FileContent mediaContent = new FileContent("text/plain", file);
            driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
        }
    }

    /**
     * Upload all birthday backup files stored on SD Card.
     * @throws IOException
     */
    public void saveBirthdaysToDrive() throws IOException {
        if (!isLinked()) return;
        authorize();
        String folderId = getFolderId();
        if (folderId == null){
            return;
        }
        java.io.File folder = MemoryUtil.getBirthdaysDir();
        if (folder == null) return;
        java.io.File[] files = folder.listFiles();
        if (files == null) return;
        for (java.io.File file : files) {
            if (!file.toString().endsWith(FileConfig.FILE_NAME_BIRTHDAY)) continue;
            File fileMetadata = new File();
            fileMetadata.setName(file.getName());
            fileMetadata.setDescription("Birthday Backup");
            fileMetadata.setParents(Collections.singletonList(folderId));
            FileContent mediaContent = new FileContent("text/plain", file);
            driveService.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
        }
    }

    /**
     * Upload all place backup files stored on SD Card.
     * @throws IOException
     */
    public void savePlacesToDrive() throws IOException {
        if (!isLinked()) return;
        authorize();
        String folderId = getFolderId();
        if (folderId == null){
            return;
        }
        java.io.File folder = MemoryUtil.getPlacesDir();
        if (folder == null) return;
        java.io.File[] files = folder.listFiles();
        if (files == null) return;
        for (java.io.File file : files) {
            if (!file.toString().endsWith(FileConfig.FILE_NAME_PLACE)) continue;
            File fileMetadata = new File();
            fileMetadata.setName(file.getName());
            fileMetadata.setDescription("Place Backup");
            fileMetadata.setParents(Collections.singletonList(folderId));
            FileContent mediaContent = new FileContent("text/plain", file);
            driveService.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
        }
    }

    /**
     * Download on SD Card all reminder backup files stored on Google Drive.
     * @throws IOException
     */
    public void downloadTemplates(boolean deleteBackup) throws IOException {
        java.io.File folder = MemoryUtil.getGoogleRemindersDir();
        if (!folder.exists() && !folder.mkdirs() || !isLinked()) {
            return;
        }
        authorize();
        Drive.Files.List request = driveService.files().list()
                .setQ("mimeType = 'text/plain' and name contains '" + FileConfig.FILE_NAME_TEMPLATE + "'")
                .setFields("nextPageToken, files");
        RealmDb realmDb = RealmDb.getInstance();
        BackupTool backupTool = BackupTool.getInstance();
        do {
            FileList files = request.execute();
            ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
            for (com.google.api.services.drive.model.File f : fileList) {
                String title = f.getName();
                if (title.endsWith(FileConfig.FILE_NAME_TEMPLATE)) {
                    java.io.File file = new java.io.File(folder, title);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    OutputStream out = new FileOutputStream(file);
                    driveService.files().get(f.getId()).executeMediaAndDownloadTo(out);
                    realmDb.saveObject(backupTool.getTemplate(file.toString(), null));
                    if (file.exists()) {
                        file.delete();
                    }
                    if (deleteBackup) deleteFileById(f.getId());
                }
            }
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
    }

    /**
     * Download on SD Card all reminder backup files stored on Google Drive.
     * @throws IOException
     */
    public void downloadReminders(boolean deleteBackup) throws IOException {
        java.io.File folder = MemoryUtil.getGoogleRemindersDir();
        if (!folder.exists() && !folder.mkdirs() || !isLinked()) {
            return;
        }
        authorize();
        Drive.Files.List request = driveService.files().list()
                .setQ("mimeType = 'text/plain' and name contains '" + FileConfig.FILE_NAME_REMINDER + "'")
                .setFields("nextPageToken, files");
        RealmDb realmDb = RealmDb.getInstance();
        BackupTool backupTool = BackupTool.getInstance();
        do {
            FileList files = request.execute();
            ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
            for (com.google.api.services.drive.model.File f : fileList) {
                String title = f.getName();
                if (title.endsWith(FileConfig.FILE_NAME_REMINDER)) {
                    java.io.File file = new java.io.File(folder, title);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    OutputStream out = new FileOutputStream(file);
                    driveService.files().get(f.getId()).executeMediaAndDownloadTo(out);
                    Reminder reminder = backupTool.getReminder(file.toString(), null);
                    if (reminder.isRemoved() || !reminder.isActive()) continue;
                    realmDb.saveObject(reminder);
                    EventControl control = EventControlImpl.getController(mContext, reminder);
                    control.next();
                    if (file.exists()) {
                        file.delete();
                    }
                    if (deleteBackup) deleteFileById(f.getId());
                }
            }
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
    }

    /**
     * Download on SD Card all place backup files stored on Google Drive.
     * @throws IOException
     */
    public void downloadPlaces(boolean deleteBackup) throws IOException {
        java.io.File folder = MemoryUtil.getGooglePlacesDir();
        if (!folder.exists() && !folder.mkdirs() || !isLinked()) {
            return;
        }
        authorize();
        Drive.Files.List request = driveService.files().list()
                .setQ("mimeType = 'text/plain' and name contains '" + FileConfig.FILE_NAME_PLACE + "'")
                .setFields("nextPageToken, files");
        RealmDb realmDb = RealmDb.getInstance();
        BackupTool backupTool = BackupTool.getInstance();
        do {
            FileList files = request.execute();
            ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
            for (com.google.api.services.drive.model.File f : fileList) {
                String title = f.getName();
                if (title.endsWith(FileConfig.FILE_NAME_PLACE)) {
                    java.io.File file = new java.io.File(folder, title);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    OutputStream out = new FileOutputStream(file);
                    driveService.files().get(f.getId()).executeMediaAndDownloadTo(out);
                    realmDb.saveObject(backupTool.getPlace(file.toString(), null));
                    if (file.exists()) {
                        file.delete();
                    }
                    if (deleteBackup) deleteFileById(f.getId());
                }
            }
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
    }

    /**
     * Download on SD Card all note backup files stored on Google Drive.
     * @throws IOException
     */
    public void downloadNotes(boolean deleteBackup) throws IOException {
        java.io.File folder = MemoryUtil.getGoogleNotesDir();
        if (!folder.exists() && !folder.mkdirs() || !isLinked()) {
            return;
        }
        authorize();
        Drive.Files.List request = driveService.files().list()
                .setQ("mimeType = 'text/plain' and name contains '" + FileConfig.FILE_NAME_NOTE + "'")
                .setFields("nextPageToken, files");
        RealmDb realmDb = RealmDb.getInstance();
        BackupTool backupTool = BackupTool.getInstance();
        do {
            FileList files = request.execute();
            ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
            for (com.google.api.services.drive.model.File f : fileList) {
                String title = f.getName();
                if (title.endsWith(FileConfig.FILE_NAME_NOTE)) {
                    java.io.File file = new java.io.File(folder, title);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    OutputStream out = new FileOutputStream(file);
                    driveService.files().get(f.getId()).executeMediaAndDownloadTo(out);
                    realmDb.saveObject(backupTool.getNote(file.toString(), null));
                    if (file.exists()) {
                        file.delete();
                    }
                    if (deleteBackup) deleteFileById(f.getId());
                }
            }
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
    }

    /**
     * Download on SD Card all group backup files stored on Google Drive.
     * @throws IOException
     */
    public void downloadGroups(boolean deleteBackup) throws IOException {
        java.io.File folder = MemoryUtil.getGoogleGroupsDir();
        if (!folder.exists() && !folder.mkdirs() || !isLinked()) {
            return;
        }
        authorize();
        Drive.Files.List request = driveService.files().list()
                .setQ("mimeType = 'text/plain' and name contains '" + FileConfig.FILE_NAME_GROUP + "'")
                .setFields("nextPageToken, files");
        RealmDb realmDb = RealmDb.getInstance();
        BackupTool backupTool = BackupTool.getInstance();
        do {
            FileList files = request.execute();
            ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
            for (com.google.api.services.drive.model.File f : fileList) {
                String title = f.getName();
                if (title.endsWith(FileConfig.FILE_NAME_GROUP)) {
                    java.io.File file = new java.io.File(folder, title);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    OutputStream out = new FileOutputStream(file);
                    driveService.files().get(f.getId()).executeMediaAndDownloadTo(out);
                    realmDb.saveObject(backupTool.getGroup(file.toString(), null));
                    if (file.exists()) {
                        file.delete();
                    }
                    if (deleteBackup) deleteFileById(f.getId());
                }
            }
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
    }

    /**
     * Download on SD Card all birthday backup files stored on Google Drive.
     * @throws IOException
     */
    public void downloadBirthdays(boolean deleteBackup) throws IOException {
        java.io.File folder = MemoryUtil.getGoogleBirthdaysDir();
        if (!folder.exists() && !folder.mkdirs() || !isLinked()) {
            return;
        }
        authorize();
        Drive.Files.List request = driveService.files().list()
                .setQ("mimeType = 'text/plain' and name contains '" + FileConfig.FILE_NAME_BIRTHDAY + "'")
                .setFields("nextPageToken, files");
        RealmDb realmDb = RealmDb.getInstance();
        BackupTool backupTool = BackupTool.getInstance();
        do {
            FileList files = request.execute();
            ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
            for (com.google.api.services.drive.model.File f : fileList) {
                String title = f.getName();
                if (title.endsWith(FileConfig.FILE_NAME_BIRTHDAY)) {
                    java.io.File file = new java.io.File(folder, title);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    OutputStream out = new FileOutputStream(file);
                    driveService.files().get(f.getId()).executeMediaAndDownloadTo(out);
                    realmDb.saveObject(backupTool.getBirthday(file.toString(), null));
                    if (file.exists()) {
                        file.delete();
                    }
                    if (deleteBackup) deleteFileById(f.getId());
                }
            }
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
    }

    /**
     * Delete reminder backup file from Google Drive by file name.
     * @param title file name.
     */
    public void deleteReminderFileByName(String title) throws IOException {
        LogUtil.d(TAG, "deleteReminderFileByName: " + title);
        if (title == null || !isLinked()) {
            return;
        }
        String[] strs = title.split(".");
        if (strs.length != 0) {
            title = strs[0];
        }
        authorize();
        Drive.Files.List request = driveService.files().list()
                .setQ("mimeType = 'text/plain' and name contains '" + title + "'");
        if (request == null) return;
        do {
            FileList files = request.execute();
            ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
            for (com.google.api.services.drive.model.File f : fileList) {
                String fileTitle = f.getName();
                if (fileTitle.endsWith(FileConfig.FILE_NAME_REMINDER)) {
                    driveService.files().delete(f.getId()).execute();
                }
            }
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
    }

    /**
     * Delete note backup file from Google Drive by file name.
     * @param title file name.
     */
    public void deleteNoteFileByName(String title) throws IOException {
        if (title == null || !isLinked()) {
            return;
        }
        String[] strs = title.split(".");
        if (strs.length != 0) {
            title = strs[0];
        }
        authorize();
        Drive.Files.List request = driveService.files().list()
                .setQ("mimeType = 'text/plain' and name contains '" + title + "'");
        if (request == null) return;
        do {
            FileList files = request.execute();
            ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
            for (com.google.api.services.drive.model.File f : fileList) {
                String fileTitle = f.getName();
                if (fileTitle.endsWith(FileConfig.FILE_NAME_NOTE)) {
                    driveService.files().delete(f.getId()).execute();
                }
            }
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
    }

    /**
     * Delete group backup file from Google Drive by file name.
     * @param title file name.
     */
    public void deleteGroupFileByName(String title) throws IOException {
        if (title == null || !isLinked()) {
            return;
        }
        String[] strs = title.split(".");
        if (strs.length != 0) {
            title = strs[0];
        }
        authorize();
        Drive.Files.List request = driveService.files().list()
                .setQ("mimeType = 'text/plain' and name contains '" + title + "'");
        if (request == null) return;
        do {
            FileList files = request.execute();
            ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
            for (com.google.api.services.drive.model.File f : fileList) {
                String fileTitle = f.getName();
                if (fileTitle.endsWith(FileConfig.FILE_NAME_GROUP)) {
                    driveService.files().delete(f.getId()).execute();
                }
            }
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
    }

    public void deleteFileById(String id) throws IOException {
        if (!isLinked()) {
            return;
        }
        authorize();
        driveService.files().delete(id).execute();
    }

    /**
     * Delete birthday backup file from Google Drive by file name.
     * @param title file name.
     */
    public void deleteBirthdayFileByName(String title) throws IOException {
        if (title == null || !isLinked()) {
            return;
        }
        String[] strs = title.split(".");
        if (strs.length != 0) {
            title = strs[0];
        }
        authorize();
        Drive.Files.List request = driveService.files().list()
                .setQ("mimeType = 'text/plain' and name contains '" + title + "'");
        if (request == null) return;
        do {
            FileList files = request.execute();
            ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
            for (com.google.api.services.drive.model.File f : fileList) {
                String fileTitle = f.getName();
                if (fileTitle.endsWith(FileConfig.FILE_NAME_BIRTHDAY)) {
                    driveService.files().delete(f.getId()).execute();
                }
            }
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
    }

    /**
     * Delete place backup file from Google Drive by file name.
     * @param title file name.
     */
    public void deletePlaceFileByName(String title) throws IOException {
        if (title == null || !isLinked()) {
            return;
        }
        String[] strs = title.split(".");
        if (strs.length != 0) {
            title = strs[0];
        }
        authorize();
        Drive.Files.List request = driveService.files().list()
                .setQ("mimeType = 'text/plain' and name contains '" + title + "'");
        if (request == null) return;
        do {
            FileList files = request.execute();
            ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
            for (com.google.api.services.drive.model.File f : fileList) {
                String fileTitle = f.getName();
                if (fileTitle.endsWith(FileConfig.FILE_NAME_PLACE)) {
                    driveService.files().delete(f.getId()).execute();
                }
            }
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
    }

    public void deleteFile(String fileName) throws IOException {
        if (fileName.endsWith(FileConfig.FILE_NAME_REMINDER)) {
            deleteReminderFileByName(fileName);
        } else if (fileName.endsWith(FileConfig.FILE_NAME_NOTE)) {
            deleteNoteFileByName(fileName);
        } else if (fileName.endsWith(FileConfig.FILE_NAME_GROUP)) {
            deleteGroupFileByName(fileName);
        } else if (fileName.endsWith(FileConfig.FILE_NAME_BIRTHDAY)) {
            deleteBirthdayFileByName(fileName);
        } else if (fileName.endsWith(FileConfig.FILE_NAME_PLACE)) {
            deletePlaceFileByName(fileName);
        }
    }

    /**
     * Delete application folder from Google Drive.
     */
    public void clean() throws IOException {
        if (!isLinked()) {
            return;
        }
        authorize();
        Drive.Files.List request = driveService.files().list()
                .setQ("mimeType = 'application/vnd.google-apps.folder' and name contains '" + FOLDER_NAME + "'");
        if (request == null) return;
        do {
            FileList files = request.execute();
            ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
            for (com.google.api.services.drive.model.File f : fileList) {
                String fileMIME = f.getMimeType();
                if (fileMIME.contains("application/vnd.google-apps.folder") && f.getName().contains(FOLDER_NAME)) {
                    driveService.files().delete(f.getId()).execute();
                    break;
                }
            }
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
    }

    /**
     * Remove all backup files from app folder.
     * @throws IOException
     */
    public void cleanFolder() throws IOException {
        if (!isLinked()) {
            return;
        }
        authorize();
        Drive.Files.List request = driveService.files().list()
                .setQ("mimeType = 'text/plain' and (name contains '" + FileConfig.FILE_NAME_SETTINGS + "' " +
                        "or name contains '" + FileConfig.FILE_NAME_TEMPLATE + "' " +
                        "or name contains '" + FileConfig.FILE_NAME_PLACE + "' " +
                        "or name contains '" + FileConfig.FILE_NAME_BIRTHDAY + "' " +
                        "or name contains '" + FileConfig.FILE_NAME_NOTE + "' " +
                        "or name contains '" + FileConfig.FILE_NAME_GROUP + "' " +
                        "or name contains '" + FileConfig.FILE_NAME_REMINDER + "' " +
                        ")");
        if (request == null) return;
        do {
            FileList files = request.execute();
            ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
            for (com.google.api.services.drive.model.File f : fileList) {
                driveService.files().delete(f.getId()).execute();
            }
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
    }

    /**
     * Get application folder identifier on Google Drive.
     * @return Drive folder identifier.
     */
    private String getFolderId() throws IOException {
        Drive.Files.List request = driveService.files().list()
                .setQ("mimeType = 'application/vnd.google-apps.folder' and name contains '" + FOLDER_NAME + "'");
        if (request == null) return null;
        do {
            FileList files = request.execute();
            if (files == null) return null;
            ArrayList<File> fileList = (ArrayList<File>) files.getFiles();
            for (File f : fileList) {
                String fileMIME = f.getMimeType();
                if (fileMIME.trim().contains("application/vnd.google-apps.folder") &&
                        f.getName().contains(FOLDER_NAME)) {
                    LogUtil.d(TAG, "getFolderId: " + f.getName() + ", " + f.getMimeType());
                    return f.getId();
                }
            }
            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        File file = createFolder();
        return file != null ? file.getId() : null;
    }

    /**
     * Create application folder on Google Drive.
     * @return Drive folder
     * @throws IOException
     */
    private File createFolder() throws IOException {
        File folder = new File();
        folder.setName(FOLDER_NAME);
        folder.setMimeType("application/vnd.google-apps.folder");
        Drive.Files.Create  folderInsert = driveService.files().create(folder);
        return folderInsert != null ? folderInsert.execute() : null;
    }
}
