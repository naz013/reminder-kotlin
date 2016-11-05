package com.elementary.tasks.core.cloud;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.SuperUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
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
    private static final String APPLICATION_NAME = "Reminder/5.0";

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
//    public UserItem getData() {
//        if (isLinked()) {
//            authorize();
//            try {
//                About about = driveService.about().get().setFields("user, storageQuota").execute();
//                About.StorageQuota quota = about.getStorageQuota();
//                return new UserItem(about.getUser().getDisplayName(), quota.getLimit(),
//                        quota.getUsage(), countFiles(), about.getUser().getPhotoLink());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }

    /**
     * Count all backup files stored on Google Drive.
     * @return number of files in local folder.
     */
    public int countFiles(){
        int count = 0;
//        File dir = MemoryUtil.getGRDir();
//        if (dir != null && dir.exists()) {
//            File[] files = dir.listFiles();
//            if (files != null) count += files.length;
//        }
//        dir = MemoryUtil.getGNDir();
//        if (dir != null && dir.exists()) {
//            File[] files = dir.listFiles();
//            if (files != null) count += files.length;
//        }
//        dir = MemoryUtil.getGBDir();
//        if (dir != null && dir.exists()) {
//            File[] files = dir.listFiles();
//            if (files != null) count += files.length;
//        }
//        dir = MemoryUtil.getGGroupsDir();
//        if (dir != null && dir.exists()) {
//            File[] files = dir.listFiles();
//            if (files != null) count += files.length;
//        }

        return count;
    }

    /**
     * Upload all reminder backup files stored on SD Card.
     * @throws IOException
     */
    public void saveReminderToDrive() throws IOException {
        Log.d(TAG, "saveReminderToDrive: ");
//        if (isLinked()) {
//            authorize();
//            String folderId = getFolderId();
//            if (folderId == null){
//                com.google.api.services.drive.model.File destFolder = createFolder();
//                folderId = destFolder.getId();
//            }
//
//            File sdPath = Environment.getExternalStorageDirectory();
//            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD);
//            File[] files = sdPathDr.listFiles();
//            if (files != null) {
//                for (File file : files) {
//                    com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
//                    fileMetadata.setName(file.getName());
//                    fileMetadata.setDescription("Reminder Backup");
//                    fileMetadata.setParents(Collections.singletonList(folderId));
//                    FileContent mediaContent = new FileContent("text/plain", file);
//                    driveService.files().create(fileMetadata, mediaContent)
//                            .setFields("id")
//                            .execute();
//                }
//            }
//        }
    }

    /**
     * Upload all note backup files stored on SD Card.
     * @throws IOException
     */
    public void saveNoteToDrive() throws IOException {
//        if (isLinked()) {
//            authorize();
//            String folderId = getFolderId();
//            if (folderId == null){
//                com.google.api.services.drive.model.File destFolder = createFolder();
//                folderId = destFolder.getId();
//            }
//
//            File sdPath = Environment.getExternalStorageDirectory();
//            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_NOTES_SD);
//            File[] files = sdPathDr.listFiles();
//            if (files != null) {
//                for (File file : files) {
//                    com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
//                    fileMetadata.setName(file.getName());
//                    fileMetadata.setDescription("Note Backup");
//                    fileMetadata.setParents(Collections.singletonList(folderId));
//                    FileContent mediaContent = new FileContent("text/plain", file);
//                    driveService.files().create(fileMetadata, mediaContent)
//                            .setFields("id")
//                            .execute();
//                }
//            }
//        }
    }

    /**
     * Upload all group backup files stored on SD Card.
     * @throws IOException
     */
    public void saveGroupToDrive() throws IOException {
//        if (isLinked()) {
//            authorize();
//            String folderId = getFolderId();
//            if (folderId == null){
//                com.google.api.services.drive.model.File destFolder = createFolder();
//                folderId = destFolder.getId();
//            }
//
//            File sdPath = Environment.getExternalStorageDirectory();
//            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_GROUP_SD);
//            File[] files = sdPathDr.listFiles();
//            if (files != null) {
//                for (File file : files) {
//                    com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
//                    fileMetadata.setName(file.getName());
//                    fileMetadata.setDescription("Group Backup");
//                    fileMetadata.setParents(Collections.singletonList(folderId));
//                    FileContent mediaContent = new FileContent("text/plain", file);
//                    driveService.files().create(fileMetadata, mediaContent)
//                            .setFields("id")
//                            .execute();
//                }
//            }
//        }
    }

    /**
     * Upload all birthday backup files stored on SD Card.
     * @throws IOException
     */
    public void saveBirthToDrive() throws IOException {
//        if (isLinked()) {
//            authorize();
//            String folderId = getFolderId();
//            if (folderId == null){
//                com.google.api.services.drive.model.File destFolder = createFolder();
//                folderId = destFolder.getId();
//            }
//
//            File sdPath = Environment.getExternalStorageDirectory();
//            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_BIRTHDAY_SD);
//            File[] files = sdPathDr.listFiles();
//            if (files != null) {
//                for (File file : files) {
//                    if (file.getName().contains("null")) continue;
//                    com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
//                    fileMetadata.setName(file.getName());
//                    fileMetadata.setDescription("Birthday Backup");
//                    fileMetadata.setParents(Collections.singletonList(folderId));
//                    FileContent mediaContent = new FileContent("text/plain", file);
//                    driveService.files()
//                            .create(fileMetadata, mediaContent)
//                            .setFields("id")
//                            .execute();
//                }
//            }
//        }
    }

    /**
     * Download on SD Card all reminder backup files stored on Google Drive.
     * @throws IOException
     */
    public void downloadReminder(boolean deleteBackup) throws IOException {
        Log.d(TAG, "downloadReminder: ");
//        if (isLinked()) {
//            authorize();
//            File sdPath = Environment.getExternalStorageDirectory();
//            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_SD_GDRIVE_TMP);
//            Drive.Files.List request;
//            try {
//                request = driveService.files().list().setQ("mimeType = 'text/plain'").setFields("nextPageToken, files"); // .setQ("mimeType=\"text/plain\"");
//            } catch (IOException e) {
//                e.printStackTrace();
//                return;
//            }
//            do {
//                FileList files;
//                try {
//                    files = request.execute();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return;
//                }
//                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
//                for (com.google.api.services.drive.model.File f : fileList) {
//                    String title = f.getName();
//                    if (!sdPathDr.exists() && !sdPathDr.mkdirs()) {
//                        throw new IOException("Unable to create parent directory");
//                    }
//
//                    if (title.endsWith(FileConfig.FILE_NAME_REMINDER)) {
//                        File file = new File(sdPathDr, title);
//                        if (!file.exists()) {
//                            try {
//                                file.createNewFile();
//                            } catch (IOException e1) {
//                                e1.printStackTrace();
//                            }
//                        }
//                        OutputStream out = new FileOutputStream(file);
//                        driveService.files().get(f.getId()).executeMediaAndDownloadTo(out);
//                        try {
//                            new SyncHelper(mContext).reminderFromJson(file.toString());
//                        } catch (JSONException e1) {
//                            e1.printStackTrace();
//                        }
//                        if (file.exists()) {
//                            file.delete();
//                        }
//                        if (deleteBackup) deleteReminderFile(f.getId());
//                    }
//                }
//                request.setPageToken(files.getNextPageToken());
//            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
//        }
    }

    /**
     * Download on SD Card all note backup files stored on Google Drive.
     * @throws IOException
     */
    public void downloadNote(boolean deleteBackup) throws IOException {
        if (isLinked()) {
            authorize();
//            File sdPath = Environment.getExternalStorageDirectory();
//            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + Constants.DIR_NOTES_SD_GDRIVE_TMP);
//            Drive.Files.List request;
//            try {
//                request = driveService.files().list().setQ("mimeType = 'text/plain'").setFields("nextPageToken, files"); // .setQ("mimeType=\"text/plain\"");
//            } catch (IOException e) {
//                e.printStackTrace();
//                return;
//            }
//            do {
//                FileList files;
//                try {
//                    files = request.execute();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return;
//                }
//                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
//                for (com.google.api.services.drive.model.File f : fileList) {
//                    String title = f.getName();
//                    if (!sdPathDr.exists() && !sdPathDr.mkdirs()) {
//                        throw new IOException("Unable to create parent directory");
//                    }
//                    if (title.endsWith(FileConfig.FILE_NAME_NOTE)) {
//                        File file = new File(sdPathDr, title);
//                        if (!file.exists()) {
//                            try {
//                                file.createNewFile(); //otherwise dropbox client will fail silently
//                            } catch (IOException e1) {
//                                e1.printStackTrace();
//                            }
//                        }
//                        OutputStream out = new FileOutputStream(file);
//                        driveService.files().get(f.getId()).executeMediaAndDownloadTo(out);
//                        try {
//                            new SyncHelper(mContext).noteFromJson(file.toString(), title);
//                        } catch (JSONException e1) {
//                            e1.printStackTrace();
//                        }
//                        if (file.exists()) {
//                            file.delete();
//                        }
//                        if (deleteBackup) deleteNoteFile(f.getId());
//                    }
//                }
//                request.setPageToken(files.getNextPageToken());
//            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        }
    }

    /**
     * Download on SD Card all group backup files stored on Google Drive.
     * @throws IOException
     */
    public void downloadGroup(boolean deleteBackup) throws IOException {
        if (isLinked()) {
            authorize();
            java.io.File sdPath = Environment.getExternalStorageDirectory();
            java.io.File sdPathDr = new java.io.File(sdPath.toString() + "/JustReminder/" + Constants.DIR_GROUP_SD_GDRIVE_TMP);
            Drive.Files.List request;
            try {
                request = driveService.files().list().setQ("mimeType = 'text/plain'").setFields("nextPageToken, files"); // .setQ("mimeType=\"text/plain\"");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            do {
                FileList files;
                try {
                    files = request.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                for (com.google.api.services.drive.model.File f : fileList) {
                    String title = f.getName();
                    if (!sdPathDr.exists() && !sdPathDr.mkdirs()) {
                        throw new IOException("Unable to create parent directory");
                    }
                    if (title.endsWith(FileConfig.FILE_NAME_GROUP)) {
                        java.io.File file = new java.io.File(sdPathDr, title);
                        if (!file.exists()) {
                            try {
                                file.createNewFile();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        OutputStream out = new FileOutputStream(file);
                        driveService.files().get(f.getId()).executeMediaAndDownloadTo(out);
//                        try {
//                            new SyncHelper(mContext).groupFromJson(file);
//                        } catch (JSONException e1) {
//                            e1.printStackTrace();
//                        }
                        if (file.exists()) {
                            file.delete();
                        }
                        if (deleteBackup) deleteGroupFile(f.getId());
                    }
                }
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        }
    }

    /**
     * Download on SD Card all birthday backup files stored on Google Drive.
     * @throws IOException
     */
    public void downloadBirthday(boolean deleteBackup) throws IOException {
        if (isLinked()) {
            authorize();
            java.io.File sdPath = Environment.getExternalStorageDirectory();
            java.io.File sdPathDr = new java.io.File(sdPath.toString() + "/JustReminder/" + Constants.DIR_BIRTHDAY_SD_GDRIVE_TMP);
            Drive.Files.List request;
            try {
                request = driveService.files().list().setQ("mimeType = 'text/plain'").setFields("nextPageToken, files"); // .setQ("mimeType=\"text/plain\"");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            do {
                FileList files;
                try {
                    files = request.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                for (com.google.api.services.drive.model.File f : fileList) {
                    String title = f.getName();
                    if (!sdPathDr.exists() && !sdPathDr.mkdirs()) {
                        throw new IOException("Unable to create parent directory");
                    }
                    if (title.endsWith(FileConfig.FILE_NAME_BIRTHDAY)) {
                        java.io.File file = new java.io.File(sdPathDr, title);
                        if (!file.exists()) {
                            try {
                                file.createNewFile();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        OutputStream out = new FileOutputStream(file);
                        driveService.files().get(f.getId()).executeMediaAndDownloadTo(out);
//                        try {
//                            new SyncHelper(mContext).birthdayFromJson(file);
//                        } catch (JSONException e1) {
//                            e1.printStackTrace();
//                        }
                        if (file.exists()) {
                            file.delete();
                        }
                        if (deleteBackup) deleteBirthdayFile(f.getId());
                    }
                }
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        }
    }

    public void deleteReminderFile(String id){
        Log.d(TAG, "deleteReminderFile: ");
        if (isLinked()) {
            authorize();
            try {
                driveService.files().delete(id).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Delete reminder backup file from Google Drive by file name.
     * @param title file name.
     */
    public void deleteReminderFileByName(String title){
        Log.d(TAG, "deleteReminderFileByName: ");
        if (title != null) {
            String[] strs = title.split(".");
            if (strs.length != 0) {
                title = strs[0];
            }
        }
        if (isLinked()) {
            authorize();
            Drive.Files.List request = null;
            try {
                request = driveService.files().list().setQ("mimeType = 'text/plain'");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (request != null) {
                do {
                    FileList files;
                    try {
                        files = request.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                    ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                    for (com.google.api.services.drive.model.File f : fileList) {
                        String fileTitle = f.getName();
                        if (fileTitle.endsWith(FileConfig.FILE_NAME_REMINDER) && fileTitle.contains(title)) {
                            try {
                                driveService.files().delete(f.getId()).execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    request.setPageToken(files.getNextPageToken());
                } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
            }
        }
    }

    public void deleteNoteFile(String id){
        if (isLinked()) {
            authorize();
            try {
                driveService.files().delete(id).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Delete note backup file from Google Drive by file name.
     * @param title file name.
     */
    public void deleteNoteFileByName(String title){
        if (title != null) {
            String[] strs = title.split(".");
            if (strs.length != 0) {
                title = strs[0];
            }
        }
        if (isLinked()) {
            authorize();
            Drive.Files.List request = null;
            try {
                request = driveService.files().list().setQ("mimeType = 'text/plain'");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (request != null) {
                do {
                    FileList files;
                    try {
                        files = request.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                    ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                    for (com.google.api.services.drive.model.File f : fileList) {
                        String fileTitle = f.getName();
                        if (fileTitle.endsWith(FileConfig.FILE_NAME_NOTE) && fileTitle.contains(title)) {
                            try {
                                driveService.files().delete(f.getId()).execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    request.setPageToken(files.getNextPageToken());
                } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
            }
        }
    }

    public void deleteGroupFile(String id){
        if (isLinked()) {
            authorize();
            try {
                driveService.files().delete(id).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Delete group backup file from Google Drive by file name.
     * @param title file name.
     */
    public void deleteGroupFileByName(String title){
        if (title != null) {
            String[] strs = title.split(".");
            if (strs.length != 0) {
                title = strs[0];
            }
        }
        if (isLinked()) {
            authorize();
            Drive.Files.List request = null;
            try {
                request = driveService.files().list().setQ("mimeType = 'text/plain'");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (request != null) {
                do {
                    FileList files;
                    try {
                        files = request.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                    ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                    for (com.google.api.services.drive.model.File f : fileList) {
                        String fileTitle = f.getName();
                        if (fileTitle.endsWith(FileConfig.FILE_NAME_GROUP) && fileTitle.contains(title)) {
                            try {
                                driveService.files().delete(f.getId()).execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    request.setPageToken(files.getNextPageToken());
                } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
            }
        }
    }

    public void deleteBirthdayFile(String id){
        if (isLinked()) {
            authorize();
            try {
                driveService.files().delete(id).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Delete birthday backup file from Google Drive by file name.
     * @param title file name.
     */
    public void deleteBirthdayFileByName(String title){
        if (title != null) {
            String[] strs = title.split(".");
            if (strs.length != 0) {
                title = strs[0];
            }
        }
        if (isLinked()) {
            authorize();
            Drive.Files.List request = null;
            try {
                request = driveService.files().list().setQ("mimeType = 'text/plain'");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (request != null) {
                do {
                    FileList files;
                    try {
                        files = request.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                    ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                    for (com.google.api.services.drive.model.File f : fileList) {
                        String fileTitle = f.getName();
                        if (fileTitle.endsWith(FileConfig.FILE_NAME_BIRTHDAY) && fileTitle.contains(title)) {
                            try {
                                driveService.files().delete(f.getId()).execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    request.setPageToken(files.getNextPageToken());
                } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
            }
        }
    }

    public void deleteFile(String fileName) {
        if (fileName.endsWith(FileConfig.FILE_NAME_REMINDER))
            deleteReminderFileByName(fileName);
        else if (fileName.endsWith(FileConfig.FILE_NAME_NOTE))
            deleteNoteFileByName(fileName);
        else if (fileName.endsWith(FileConfig.FILE_NAME_GROUP))
            deleteGroupFileByName(fileName);
        else if (fileName.endsWith(FileConfig.FILE_NAME_BIRTHDAY))
            deleteBirthdayFileByName(fileName);
    }

    /**
     * Delete application folder from Google Drive.
     */
    public void clean(){
        if (isLinked()) {
            authorize();
            Drive.Files.List requestF = null;
            try {
                requestF = driveService.files().list().setQ("mimeType = 'application/vnd.google-apps.folder'");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (requestF != null) {
                do {
                    FileList files;
                    try {
                        files = requestF.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                    ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                    for (com.google.api.services.drive.model.File f : fileList) {
                        String fileMIME = f.getMimeType();
                        if (fileMIME.contains("application/vnd.google-apps.folder") && f.getName().contains("Reminder")) {
                            try {
                                driveService.files().delete(f.getId()).execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                    requestF.setPageToken(files.getNextPageToken());
                } while (requestF.getPageToken() != null && requestF.getPageToken().length() >= 0);
            }
        }
    }

    /**
     * Get application folder identifier on Google Drive.
     * @return Drive folder identifier.
     */
    private String getFolderId(){
        String id = null;
        Drive.Files.List request = null;
        try {
            request = driveService.files().list().setQ("mimeType = 'application/vnd.google-apps.folder'");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (request != null) {
            do {
                FileList files = null;
                try {
                    files = request.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    id = null;
                }
                if (files != null) {
                    ArrayList<com.google.api.services.drive.model.File> fileList =
                            (ArrayList<File>) files.getFiles();
                    for (com.google.api.services.drive.model.File f : fileList) {
                        String fileMIME = f.getMimeType();
                        if (fileMIME.trim().contains("application/vnd.google-apps.folder") &&
                                f.getName().contains("Reminder")) {
                            id = f.getId();
                        }
                    }
                    request.setPageToken(files.getNextPageToken());
                } else id = null;
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        } else id = null;
        return id;
    }

    /**
     * Create application folder on Google Drive.
     * @return Drive folder
     * @throws IOException
     */
    private com.google.api.services.drive.model.File createFolder() throws IOException {
        com.google.api.services.drive.model.File folder = new com.google.api.services.drive.model.File();
        folder.setName("Reminder");
        folder.setMimeType("application/vnd.google-apps.folder");
        Drive.Files.Create folderInsert = null;
        try {
            folderInsert = driveService.files().create(folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return folderInsert != null ? folderInsert.execute() : null;
    }
}
