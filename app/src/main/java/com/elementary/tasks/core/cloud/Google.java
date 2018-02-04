package com.elementary.tasks.core.cloud;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.elementary.tasks.backups.UserItem;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.utils.BackupTool;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.MemoryUtil;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.google_tasks.TaskItem;
import com.elementary.tasks.google_tasks.TaskListItem;
import com.elementary.tasks.reminder.models.Reminder;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Data;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

public class Google {

    public static final String TASKS_NEED_ACTION = "needsAction";
    public static final String TASKS_COMPLETE = "completed";
    private static final String TAG = "Google";
    private static final String APPLICATION_NAME = "Reminder/6.0";
    private static final String FOLDER_NAME = "Reminder";

    @Nullable
    private Drive driveService;
    @Nullable
    private Tasks tasksService;

    @Nullable
    private GTasks mTasks;
    @Nullable
    private Drives mDrives;

    @Nullable
    private static Google instance = null;

    private Google(@NonNull Context context) throws IllegalStateException {
        String user = Prefs.getInstance(context).getDriveUser();
        if (user.matches(".*@.*")) {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(DriveScopes.DRIVE, TasksScopes.TASKS));
            credential.setSelectedAccountName(user);
            JsonFactory mJsonFactory = GsonFactory.getDefaultInstance();
            HttpTransport mTransport = AndroidHttp.newCompatibleTransport();
            driveService = new Drive.Builder(mTransport, mJsonFactory, credential).setApplicationName(APPLICATION_NAME).build();
            tasksService = new com.google.api.services.tasks.Tasks.Builder(mTransport, mJsonFactory, credential).setApplicationName(APPLICATION_NAME).build();
            mDrives = new Drives();
            mTasks = new GTasks();
        } else {
            logOut();
            throw new IllegalArgumentException("Not logged to Google");
        }
    }

    @Nullable
    public static Google getInstance(@NonNull Context context) {
        try {
            instance = new Google(context.getApplicationContext());
        } catch (IllegalArgumentException | NullPointerException e) {
            LogUtil.d(TAG, "getInstance: " + e.getLocalizedMessage());
        }
        return instance;
    }

    void logOut() {
        Prefs.getInstance().setDriveUser(Prefs.DRIVE_USER_NONE);
        instance = null;
    }

    @Nullable
    public GTasks getTasks() {
        return mTasks;
    }

    @Nullable
    public Drives getDrive() {
        return mDrives;
    }

    public class GTasks {
        public boolean insertTask(@NonNull TaskItem item) throws IOException {
            if (TextUtils.isEmpty(item.getTitle()) || tasksService == null) {
                return false;
            }
            try {
                Task task = new Task();
                task.setTitle(item.getTitle());
                if (item.getNotes() != null) {
                    task.setNotes(item.getNotes());
                }
                if (item.getDueDate() != 0) {
                    task.setDue(new DateTime(item.getDueDate()));
                }
                Task result;
                String listId = item.getListId();
                if (!TextUtils.isEmpty(listId)) {
                    result = tasksService.tasks().insert(listId, task).execute();
                } else {
                    TaskListItem taskListItem = RealmDb.getInstance().getDefaultTaskList();
                    if (taskListItem != null) {
                        item.setListId(taskListItem.getListId());
                        result = tasksService.tasks().insert(taskListItem.getListId(), task).execute();
                    } else {
                        result = tasksService.tasks().insert("@default", task).execute();
                        TaskList list = tasksService.tasklists().get("@default").execute();
                        if (list != null) {
                            item.setListId(list.getId());
                        }
                    }
                }
                if (result != null) {
                    item.update(result);
                    RealmDb.getInstance().saveObject(item);
                    return true;
                }
            } catch (IllegalArgumentException e) {
                return false;
            }
            return false;
        }

        public void updateTaskStatus(@NonNull String status, String listId, String taskId) throws IOException {
            if (tasksService == null) return;
            Task task = tasksService.tasks().get(listId, taskId).execute();
            task.setStatus(status);
            if (status.matches(TASKS_NEED_ACTION)) {
                task.setCompleted(Data.NULL_DATE_TIME);
            }
            task.setUpdated(new DateTime(System.currentTimeMillis()));
            tasksService.tasks().update(listId, task.getId(), task).execute();
        }

        public void deleteTask(@NonNull TaskItem item) throws IOException {
            if (item.getListId() == null || tasksService == null) return;
            tasksService.tasks().delete(item.getListId(), item.getTaskId()).execute();
        }

        public void updateTask(@NonNull TaskItem item) throws IOException {
            if (tasksService == null) return;
            Task task = tasksService.tasks().get(item.getListId(), item.getTaskId()).execute();
            task.setStatus(TASKS_NEED_ACTION);
            task.setTitle(item.getTitle());
            task.setCompleted(Data.NULL_DATE_TIME);
            if (item.getDueDate() != 0) task.setDue(new DateTime(item.getDueDate()));
            if (item.getNotes() != null) task.setNotes(item.getNotes());
            task.setUpdated(new DateTime(System.currentTimeMillis()));
            tasksService.tasks().update(item.getListId(), task.getId(), task).execute();
        }

        @NonNull
        public List<Task> getTasks(String listId) {
            List<Task> taskLists = new ArrayList<>();
            if (tasksService == null) return taskLists;
            try {
                taskLists = tasksService.tasks().list(listId).execute().getItems();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (taskLists == null) return new ArrayList<>();
            return taskLists;
        }

        @Nullable
        public TaskLists getTaskLists() throws IOException {
            if (tasksService == null) return null;
            return tasksService.tasklists().list().execute();
        }

        public void insertTasksList(String listTitle, int color) {
            if (tasksService == null) return;
            TaskList taskList = new TaskList();
            taskList.setTitle(listTitle);
            try {
                TaskList result = tasksService.tasklists().insert(taskList).execute();
                TaskListItem item = new TaskListItem(result, color);
                RealmDb.getInstance().saveObject(item);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void updateTasksList(final String listTitle, @Nullable String listId) throws IOException {
            if (listId == null || tasksService == null) {
                return;
            }
            TaskList taskList = tasksService.tasklists().get(listId).execute();
            taskList.setTitle(listTitle);
            tasksService.tasklists().update(listId, taskList).execute();
            TaskListItem item = RealmDb.getInstance().getTaskList(listId);
            if (item != null) {
                item.update(taskList);
                RealmDb.getInstance().saveObject(item);
            }
        }

        public void deleteTaskList(@Nullable String listId) {
            if (listId == null || tasksService == null) {
                return;
            }
            try {
                tasksService.tasklists().delete(listId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void clearTaskList(@Nullable String listId) {
            if (listId == null || tasksService == null) {
                return;
            }
            try {
                tasksService.tasks().clear(listId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean moveTask(@NonNull TaskItem item, String oldList) {
            if (tasksService == null) {
                return false;
            }
            try {
                Task task = tasksService.tasks().get(oldList, item.getTaskId()).execute();
                if (task != null) {
                    TaskItem clone = new TaskItem(item);
                    clone.setListId(oldList);
                    deleteTask(clone);
                    return insertTask(item);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public class Drives {
        /**
         * Holder information about user.
         *
         * @return user info object
         */
        @Nullable
        public UserItem getData() {
            if (driveService == null) return null;
            try {
                About about = driveService.about().get().setFields("user, storageQuota").execute();
                About.StorageQuota quota = about.getStorageQuota();
                return new UserItem(about.getUser().getDisplayName(), quota.getLimit(),
                        quota.getUsage(), countFiles(), about.getUser().getPhotoLink());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Count all backup files stored on Google Drive.
         *
         * @return number of files in local folder.
         */
        int countFiles() throws IOException {
            int count = 0;
            if (driveService == null) return 0;
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

        public void saveSettingsToDrive() throws IOException {
            String folderId = null;
            try {
                folderId = getFolderId();
            } catch (IllegalArgumentException ignored) {
            }
            if (folderId == null || driveService == null) {
                return;
            }
            java.io.File folder = MemoryUtil.getPrefsDir();
            if (folder == null) return;
            java.io.File[] files = folder.listFiles();
            if (files == null) return;
            for (java.io.File file : files) {
                if (!file.toString().endsWith(FileConfig.FILE_NAME_SETTINGS)) continue;
                removeAllCopies(file.getName());
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

        private void removeAllCopies(@NonNull String fileName) throws IOException {
            if (driveService == null || TextUtils.isEmpty(fileName)) return;
            Drive.Files.List request = driveService.files().list()
                    .setQ("mimeType = 'text/plain' and name contains '" + fileName + "'")
                    .setFields("nextPageToken, files");
            do {
                FileList files = request.execute();
                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                for (com.google.api.services.drive.model.File f : fileList) {
                    driveService.files().delete(f.getId()).execute();
                }
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        }

        public void downloadSettings(@NonNull Context context, boolean deleteFile) throws IOException {
            if (driveService == null) return;
            java.io.File folder = MemoryUtil.getPrefsDir();
            if (folder == null || !folder.exists() && !folder.mkdirs()) {
                return;
            }
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
                        if (deleteFile) {
                            driveService.files().delete(f.getId()).execute();
                        }
                        Prefs.getInstance(context).loadPrefsFromFile();
                        break;
                    }
                }
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        }

        /**
         * Upload all template backup files stored on SD Card.
         */
        public void saveTemplatesToDrive() {
            try {
                saveToDrive(new Metadata(FileConfig.FILE_NAME_TEMPLATE, MemoryUtil.getTemplatesDir(), "Template Backup", null));
            } catch (IOException e) {
                LogUtil.d(TAG, "saveTemplatesToDrive: " + e.getLocalizedMessage());
            }
        }

        /**
         * Upload all reminder backup files stored on SD Card.
         */
        public void saveRemindersToDrive() {
            try {
                saveToDrive(new Metadata(FileConfig.FILE_NAME_REMINDER, MemoryUtil.getRemindersDir(), "Reminder Backup", null));
            } catch (IOException e) {
                LogUtil.d(TAG, "saveRemindersToDrive: " + e.getLocalizedMessage());
            }
        }

        /**
         * Upload reminder backup file stored on SD Card.
         */
        public void saveReminderToDrive(@NonNull String pathToFile) {
            try {
                Metadata metadata = new Metadata(FileConfig.FILE_NAME_REMINDER, MemoryUtil.getRemindersDir(), "Reminder Backup", null);
                saveFileToDrive(pathToFile, metadata);
            } catch (IOException e) {
                LogUtil.d(TAG, "saveRemindersToDrive: " + e.getLocalizedMessage());
            }
        }

        /**
         * Upload all note backup files stored on SD Card.
         */
        public void saveNotesToDrive() {
            try {
                saveToDrive(new Metadata(FileConfig.FILE_NAME_NOTE, MemoryUtil.getNotesDir(), "Note Backup", null));
            } catch (IOException e) {
                LogUtil.d(TAG, "saveNotesToDrive: " + e.getLocalizedMessage());
            }
        }

        /**
         * Upload all group backup files stored on SD Card.
         */
        public void saveGroupsToDrive() {
            try {
                saveToDrive(new Metadata(FileConfig.FILE_NAME_GROUP, MemoryUtil.getGroupsDir(), "Group Backup", null));
            } catch (IOException e) {
                LogUtil.d(TAG, "saveGroupsToDrive: " + e.getLocalizedMessage());
            }
        }

        /**
         * Upload all birthday backup files stored on SD Card.
         */
        public void saveBirthdaysToDrive() {
            try {
                saveToDrive(new Metadata(FileConfig.FILE_NAME_BIRTHDAY, MemoryUtil.getBirthdaysDir(), "Birthday Backup", null));
            } catch (IOException e) {
                LogUtil.d(TAG, "saveBirthdaysToDrive: " + e.getLocalizedMessage());
            }
        }

        /**
         * Upload all place backup files stored on SD Card.
         */
        public void savePlacesToDrive() {
            try {
                saveToDrive(new Metadata(FileConfig.FILE_NAME_PLACE, MemoryUtil.getPlacesDir(), "Place Backup", null));
            } catch (IOException e) {
                LogUtil.d(TAG, "savePlacesToDrive: " + e.getLocalizedMessage());
            }
        }

        /**
         * Upload files from folder to Google Drive.
         *
         * @param metadata metadata.
         * @throws IOException
         */
        private void saveToDrive(@NonNull Metadata metadata) throws IOException {
            if (metadata.getFolder() == null) return;
            if (driveService == null) return;
            java.io.File[] files = metadata.getFolder().listFiles();
            if (files == null) return;
            String folderId = null;
            try {
                folderId = getFolderId();
            } catch (IllegalArgumentException ignored) {
            }
            if (folderId == null) {
                return;
            }
            for (java.io.File file : files) {
                if (!file.getName().endsWith(metadata.getFileExt())) continue;
                removeAllCopies(file.getName());
                File fileMetadata = new File();
                fileMetadata.setName(file.getName());
                fileMetadata.setDescription(metadata.getMeta());
                fileMetadata.setParents(Collections.singletonList(folderId));
                FileContent mediaContent = new FileContent("text/plain", file);
                driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
            }
        }

        /**
         * Upload file from folder to Google Drive.
         *
         * @param metadata metadata.
         * @throws IOException
         */
        private void saveFileToDrive(@NonNull String pathToFile, @NonNull Metadata metadata) throws IOException {
            if (metadata.getFolder() == null) return;
            if (driveService == null) return;
            java.io.File[] files = metadata.getFolder().listFiles();
            if (files == null) return;
            String folderId = null;
            try {
                folderId = getFolderId();
            } catch (IllegalArgumentException ignored) {
            }
            if (folderId == null) {
                return;
            }
            java.io.File f = new java.io.File(pathToFile);
            if (!f.exists()) {
                return;
            }
            if (!f.getName().endsWith(metadata.getFileExt())) return;
            removeAllCopies(f.getName());
            File fileMetadata = new File();
            fileMetadata.setName(f.getName());
            fileMetadata.setDescription(metadata.getMeta());
            fileMetadata.setParents(Collections.singletonList(folderId));
            FileContent mediaContent = new FileContent("text/plain", f);
            driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
        }

        public void download(boolean deleteBackup, @NonNull Metadata metadata) throws IOException {
            if (driveService == null) return;
            java.io.File folder = metadata.getFolder();
            if (folder == null || !folder.exists() && !folder.mkdirs()) {
                return;
            }
            Drive.Files.List request = driveService.files().list()
                    .setQ("mimeType = 'text/plain' and name contains '" + metadata.getFileExt() + "'")
                    .setFields("nextPageToken, files");
            do {
                FileList files = request.execute();
                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                for (com.google.api.services.drive.model.File f : fileList) {
                    String title = f.getName();
                    if (title.endsWith(metadata.getFileExt())) {
                        java.io.File file = new java.io.File(folder, title);
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        OutputStream out = new FileOutputStream(file);
                        driveService.files().get(f.getId()).executeMediaAndDownloadTo(out);
                        if (metadata.action != null) {
                            metadata.action.onSave(file);
                        }
                        if (deleteBackup) {
                            if (file.exists()) {
                                file.delete();
                            }
                            driveService.files().delete(f.getId()).execute();
                        }
                    }
                }
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        }

        /**
         * Download on SD Card all reminder backup files stored on Google Drive.
         *
         * @throws IOException
         */
        public void downloadTemplates(boolean deleteBackup) throws IOException {
            RealmDb realmDb = RealmDb.getInstance();
            BackupTool backupTool = BackupTool.getInstance();
            download(deleteBackup, new Metadata(FileConfig.FILE_NAME_TEMPLATE, MemoryUtil.getGoogleRemindersDir(), null, file -> {
                try {
                    realmDb.saveObject(backupTool.getTemplate(file.toString(), null));
                } catch (IOException | IllegalStateException e) {
                    LogUtil.d(TAG, "downloadTemplates: " + e.getLocalizedMessage());
                }
            }));
        }

        /**
         * Download on SD Card all reminder backup files stored on Google Drive.
         *
         * @throws IOException
         */
        public void downloadReminders(Context context, boolean deleteBackup) throws IOException {
            RealmDb realmDb = RealmDb.getInstance();
            BackupTool backupTool = BackupTool.getInstance();
            download(deleteBackup, new Metadata(FileConfig.FILE_NAME_REMINDER, MemoryUtil.getGoogleRemindersDir(), null, file -> {
                try {
                    Reminder reminder = backupTool.getReminder(file.toString(), null);
                    if (reminder == null || reminder.isRemoved() || !reminder.isActive()) return;
                    realmDb.saveReminder(reminder, () -> {
                        EventControl control = EventControlFactory.getController(context, reminder);
                        if (control.canSkip()) {
                            control.next();
                        } else {
                            control.start();
                        }
                    });
                } catch (IOException | IllegalStateException e) {
                    LogUtil.d(TAG, "downloadReminders: " + e.getLocalizedMessage());
                }
            }));
        }

        /**
         * Download on SD Card all place backup files stored on Google Drive.
         *
         * @throws IOException
         */
        public void downloadPlaces(boolean deleteBackup) throws IOException {
            RealmDb realmDb = RealmDb.getInstance();
            BackupTool backupTool = BackupTool.getInstance();
            download(deleteBackup, new Metadata(FileConfig.FILE_NAME_PLACE, MemoryUtil.getGooglePlacesDir(), null, file -> {
                try {
                    realmDb.saveObject(backupTool.getPlace(file.toString(), null));
                } catch (IOException | IllegalStateException e) {
                    LogUtil.d(TAG, "downloadPlaces: " + e.getLocalizedMessage());
                }
            }));
        }

        /**
         * Download on SD Card all note backup files stored on Google Drive.
         *
         * @throws IOException
         */
        public void downloadNotes(boolean deleteBackup) throws IOException {
            RealmDb realmDb = RealmDb.getInstance();
            BackupTool backupTool = BackupTool.getInstance();
            download(deleteBackup, new Metadata(FileConfig.FILE_NAME_NOTE, MemoryUtil.getGoogleNotesDir(), null, file -> {
                try {
                    realmDb.saveObject(backupTool.getNote(file.toString(), null));
                } catch (IOException | IllegalStateException e) {
                    LogUtil.d(TAG, "downloadNotes: " + e.getLocalizedMessage());
                }
            }));
        }

        /**
         * Download on SD Card all group backup files stored on Google Drive.
         *
         * @throws IOException
         */
        public void downloadGroups(boolean deleteBackup) throws IOException {
            RealmDb realmDb = RealmDb.getInstance();
            BackupTool backupTool = BackupTool.getInstance();
            download(deleteBackup, new Metadata(FileConfig.FILE_NAME_GROUP, MemoryUtil.getGoogleGroupsDir(), null, file -> {
                try {
                    realmDb.saveObject(backupTool.getGroup(file.toString(), null));
                } catch (IOException | IllegalStateException e) {
                    LogUtil.d(TAG, "downloadGroups: " + e.getLocalizedMessage());
                }
            }));
        }

        /**
         * Download on SD Card all birthday backup files stored on Google Drive.
         *
         * @throws IOException
         */
        public void downloadBirthdays(boolean deleteBackup) throws IOException {
            RealmDb realmDb = RealmDb.getInstance();
            BackupTool backupTool = BackupTool.getInstance();
            download(deleteBackup, new Metadata(FileConfig.FILE_NAME_BIRTHDAY, MemoryUtil.getGoogleBirthdaysDir(), null, file -> {
                try {
                    realmDb.saveObject(backupTool.getBirthday(file.toString(), null));
                } catch (IOException | IllegalStateException e) {
                    LogUtil.d(TAG, "downloadBirthdays: " + e.getLocalizedMessage());
                }
            }));
        }

        /**
         * Delete reminder backup file from Google Drive by file name.
         *
         * @param title file name.
         */
        public void deleteReminderFileByName(@Nullable String title) throws IOException {
            LogUtil.d(TAG, "deleteReminderFileByName: " + title);
            if (title == null || driveService == null) {
                return;
            }
            String[] strs = title.split(".");
            if (strs.length != 0) {
                title = strs[0];
            }
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
         *
         * @param title file name.
         */
        public void deleteNoteFileByName(@Nullable String title) throws IOException {
            if (title == null || driveService == null) {
                return;
            }
            String[] strs = title.split(".");
            if (strs.length != 0) {
                title = strs[0];
            }
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
         *
         * @param title file name.
         */
        public void deleteGroupFileByName(@Nullable String title) throws IOException {
            if (title == null || driveService == null) {
                return;
            }
            String[] strs = title.split(".");
            if (strs.length != 0) {
                title = strs[0];
            }
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

        /**
         * Delete birthday backup file from Google Drive by file name.
         *
         * @param title file name.
         */
        public void deleteBirthdayFileByName(@Nullable String title) throws IOException {
            if (title == null || driveService == null) {
                return;
            }
            String[] strs = title.split(".");
            if (strs.length != 0) {
                title = strs[0];
            }
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
         *
         * @param title file name.
         */
        public void deletePlaceFileByName(@Nullable String title) throws IOException {
            if (title == null || driveService == null) {
                return;
            }
            String[] strs = title.split(".");
            if (strs.length != 0) {
                title = strs[0];
            }
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

        public void deleteTemplateFileByName(@Nullable String title) throws IOException {
            if (title == null || driveService == null) {
                return;
            }
            String[] strs = title.split(".");
            if (strs.length != 0) {
                title = strs[0];
            }
            Drive.Files.List request = driveService.files().list()
                    .setQ("mimeType = 'text/plain' and name contains '" + title + "'");
            if (request == null) return;
            do {
                FileList files = request.execute();
                ArrayList<com.google.api.services.drive.model.File> fileList = (ArrayList<com.google.api.services.drive.model.File>) files.getFiles();
                for (com.google.api.services.drive.model.File f : fileList) {
                    String fileTitle = f.getName();
                    if (fileTitle.endsWith(FileConfig.FILE_NAME_TEMPLATE)) {
                        driveService.files().delete(f.getId()).execute();
                    }
                }
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null && request.getPageToken().length() >= 0);
        }

        /**
         * Delete application folder from Google Drive.
         */
        public void clean() throws IOException {
            if (driveService == null) return;
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
         *
         * @throws IOException
         */
        public void cleanFolder() throws IOException {
            if (driveService == null) return;
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
         * Holder application folder identifier on Google Drive.
         *
         * @return Drive folder identifier.
         */
        @Nullable
        private String getFolderId() throws IOException, IllegalArgumentException {
            if (driveService == null) return null;
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
         *
         * @return Drive folder
         * @throws IOException
         */
        @Nullable
        private File createFolder() throws IOException {
            if (driveService == null) return null;
            File folder = new File();
            folder.setName(FOLDER_NAME);
            folder.setMimeType("application/vnd.google-apps.folder");
            Drive.Files.Create folderInsert = driveService.files().create(folder);
            return folderInsert != null ? folderInsert.execute() : null;
        }

        private class Metadata {

            @NonNull
            private final String fileExt;
            @Nullable
            private final java.io.File folder;
            @Nullable
            private final String meta;
            @Nullable
            private final Action action;

            Metadata(@NonNull String fileExt, @Nullable java.io.File folder, @Nullable String meta, @Nullable Action action) {
                this.fileExt = fileExt;
                this.folder = folder;
                this.meta = meta;
                this.action = action;
            }

            @Nullable
            public Action getAction() {
                return action;
            }

            @NonNull
            String getFileExt() {
                return fileExt;
            }

            @Nullable
            java.io.File getFolder() {
                return folder;
            }

            @Nullable
            String getMeta() {
                return meta;
            }
        }
    }

    private interface Action {
        void onSave(java.io.File file);
    }
}
