package com.elementary.tasks.core.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;

import com.elementary.tasks.birthdays.BirthdayItem;
import com.elementary.tasks.core.cloud.FileConfig;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.navigation.settings.additional.TemplateItem;
import com.elementary.tasks.notes.NoteItem;
import com.elementary.tasks.places.PlaceItem;
import com.elementary.tasks.reminder.models.Reminder;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
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

public final class BackupTool {

    private static final String TAG = "BackupTool";
    private static BackupTool instance;

    private BackupTool() {
    }

    public static BackupTool getInstance() {
        if (instance == null) {
            synchronized (BackupTool.class) {
                if (instance == null) {
                    instance = new BackupTool();
                }
            }
        }
        return instance;
    }

    public void exportTemplates() {
        for (TemplateItem item : RealmDb.getInstance().getAllTemplates()) {
            exportTemplate(item);
        }
    }

    public void importTemplates() throws IOException, IllegalStateException {
        File dir = MemoryUtil.getTemplatesDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                RealmDb realmDb = RealmDb.getInstance();
                for (File file : files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_TEMPLATE)) {
                        TemplateItem item = getTemplate(file.toString(), null);
                        if (item == null || TextUtils.isEmpty(item.getTitle())
                                || TextUtils.isEmpty(item.getKey())) {
                            continue;
                        }
                        realmDb.saveObject(item);
                    }
                }
            }
        }
    }

    public void exportTemplate(@NonNull TemplateItem item) {
        WeakReference<String> jsonData = new WeakReference<>(new Gson().toJson(item));
        File dir = MemoryUtil.getTemplatesDir();
        if (dir != null) {
            String exportFileName = item.getKey() + FileConfig.FILE_NAME_TEMPLATE;
            try {
                writeFile(new File(dir, exportFileName), jsonData.get());
                jsonData.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.i(TAG, "Couldn't find external storage!");
        }
    }

    @Nullable
    public TemplateItem getTemplate(@NonNull ContentResolver cr, @NonNull Uri name) throws IOException, IllegalStateException {
        WeakReference<TemplateItem> item = new WeakReference<>(new Gson().fromJson(readFileToJson(cr, name), TemplateItem.class));
        return item.get();
    }

    @Nullable
    public TemplateItem getTemplate(@Nullable String filePath, @Nullable String json) throws IOException, IllegalStateException {
        if (filePath != null && MemoryUtil.isSdPresent()) {
            WeakReference<TemplateItem> item = new WeakReference<>(new Gson().fromJson(readFileToJson(filePath), TemplateItem.class));
            return item.get();
        } else if (json != null) {
            WeakReference<TemplateItem> item = new WeakReference<>(new Gson().fromJson(json, TemplateItem.class));
            return item.get();
        } else {
            return null;
        }
    }

    public void exportPlaces() {
        for (PlaceItem item : RealmDb.getInstance().getAllPlaces()) {
            exportPlace(item);
        }
    }

    public void importPlaces() throws IOException, IllegalStateException {
        File dir = MemoryUtil.getPlacesDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                RealmDb realmDb = RealmDb.getInstance();
                for (File file : files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_PLACE)) {
                        PlaceItem item = getPlace(file.toString(), null);
                        if (item == null || TextUtils.isEmpty(item.getTitle()) ||
                                TextUtils.isEmpty(item.getKey())) {
                            continue;
                        }
                        realmDb.saveObject(item);
                    }
                }
            }
        }
    }

    public void exportPlace(@NonNull PlaceItem item) {
        WeakReference<String> jsonData = new WeakReference<>(new Gson().toJson(item));
        File dir = MemoryUtil.getPlacesDir();
        if (dir != null) {
            String exportFileName = item.getKey() + FileConfig.FILE_NAME_PLACE;
            try {
                writeFile(new File(dir, exportFileName), jsonData.get());
                jsonData.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.i(TAG, "Couldn't find external storage!");
        }
    }

    @Nullable
    public PlaceItem getPlace(@NonNull ContentResolver cr, @NonNull Uri name) throws IOException, IllegalStateException {
        WeakReference<PlaceItem> item = new WeakReference<>(new Gson().fromJson(readFileToJson(cr, name), PlaceItem.class));
        return item.get();
    }

    @Nullable
    public PlaceItem getPlace(@Nullable String filePath, @Nullable String json) throws IOException, IllegalStateException {
        if (filePath != null && MemoryUtil.isSdPresent()) {
            WeakReference<PlaceItem> item = new WeakReference<>(new Gson().fromJson(readFileToJson(filePath), PlaceItem.class));
            return item.get();
        } else if (json != null) {
            WeakReference<PlaceItem> item = new WeakReference<>(new Gson().fromJson(json, PlaceItem.class));
            return item.get();
        } else {
            return null;
        }
    }

    public void exportBirthdays() {
        for (BirthdayItem item : RealmDb.getInstance().getAllBirthdays()) {
            exportBirthday(item);
        }
    }

    public void importBirthdays() throws IOException, IllegalStateException {
        File dir = MemoryUtil.getBirthdaysDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                RealmDb realmDb = RealmDb.getInstance();
                for (File file : files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_BIRTHDAY)) {
                        BirthdayItem item = getBirthday(file.toString(), null);
                        if (item == null || TextUtils.isEmpty(item.getName())
                                || TextUtils.isEmpty(item.getUuId())) {
                            continue;
                        }
                        realmDb.saveObject(item);
                    }
                }
            }
        }
    }

    public void exportBirthday(@NonNull BirthdayItem item) {
        WeakReference<String> jsonData = new WeakReference<>(new Gson().toJson(item));
        File dir = MemoryUtil.getBirthdaysDir();
        if (dir != null) {
            String exportFileName = item.getUuId() + FileConfig.FILE_NAME_BIRTHDAY;
            try {
                writeFile(new File(dir, exportFileName), jsonData.get());
                jsonData.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.i(TAG, "Couldn't find external storage!");
        }
    }

    @Nullable
    public BirthdayItem getBirthday(@NonNull ContentResolver cr, @NonNull Uri name) throws IOException, IllegalStateException {
        WeakReference<BirthdayItem> item = new WeakReference<>(new Gson().fromJson(readFileToJson(cr, name), BirthdayItem.class));
        return item.get();
    }

    @Nullable
    public BirthdayItem getBirthday(@Nullable String filePath, @Nullable String json) throws IOException, IllegalStateException {
        if (filePath != null && MemoryUtil.isSdPresent()) {
            WeakReference<BirthdayItem> item = new WeakReference<>(new Gson().fromJson(readFileToJson(filePath), BirthdayItem.class));
            return item.get();
        } else if (json != null) {
            WeakReference<BirthdayItem> item = new WeakReference<>(new Gson().fromJson(json, BirthdayItem.class));
            return item.get();
        } else {
            return null;
        }
    }

    public void exportGroups() {
        for (GroupItem item : RealmDb.getInstance().getAllGroups()) {
            exportGroup(item);
        }
    }

    public void exportGroup(@NonNull GroupItem item) {
        WeakReference<String> jsonData = new WeakReference<>(new Gson().toJson(item));
        File dir = MemoryUtil.getGroupsDir();
        if (dir != null) {
            String exportFileName = item.getUuId() + FileConfig.FILE_NAME_GROUP;
            File file = new File(dir, exportFileName);
            LogUtil.d(TAG, "exportGroup: " + file);
            try {
                writeFile(file, jsonData.get());
                jsonData.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.i(TAG, "Couldn't find external storage!");
        }
    }

    public void importGroups() throws IOException, IllegalStateException {
        File dir = MemoryUtil.getGroupsDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                RealmDb realmDb = RealmDb.getInstance();
                List<GroupItem> groupItems = realmDb.getAllGroups();
                for (File file : files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_GROUP)) {
                        GroupItem item = getGroup(file.toString(), null);
                        if (item == null || TextUtils.isEmpty(item.getUuId())) continue;
                        if (!TextUtils.isEmpty(item.getTitle()) && !hasGroup(groupItems, item.getTitle())) {
                            realmDb.saveObject(item);
                            groupItems.add(item);
                        }
                    }
                }
            }
        }
    }

    private boolean hasGroup(@NonNull List<GroupItem> list, @Nullable String comparable) {
        if (comparable == null) return true;
        for (GroupItem item : list) {
            if (comparable.equals(item.getTitle())) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public GroupItem getGroup(@NonNull ContentResolver cr, @NonNull Uri name) throws IOException, IllegalStateException {
        WeakReference<GroupItem> item = new WeakReference<>(new Gson().fromJson(readFileToJson(cr, name), GroupItem.class));
        return item.get();
    }

    @Nullable
    public GroupItem getGroup(@Nullable String filePath, @Nullable String json) throws IOException, IllegalStateException {
        if (filePath != null && MemoryUtil.isSdPresent()) {
            WeakReference<GroupItem> item = new WeakReference<>(new Gson().fromJson(readFileToJson(filePath), GroupItem.class));
            return item.get();
        } else if (json != null) {
            WeakReference<GroupItem> item = new WeakReference<>(new Gson().fromJson(json, GroupItem.class));
            return item.get();
        } else {
            return null;
        }
    }

    public void exportReminders() {
        for (Reminder reminder : RealmDb.getInstance().getEnabledReminders()) {
            exportReminder(reminder);
        }
    }

    public void importReminders(Context mContext) throws IOException, IllegalStateException {
        File dir = MemoryUtil.getRemindersDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                RealmDb realmDb = RealmDb.getInstance();
                GroupItem defaultGroup = realmDb.getDefaultGroup();
                for (File file : files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_REMINDER)) {
                        Reminder reminder = getReminder(file.toString(), null);
                        if (reminder == null) {
                            continue;
                        }
                        if (reminder.isRemoved() || !reminder.isActive()) {
                            continue;
                        }
                        if (TextUtils.isEmpty(reminder.getSummary()) ||
                                TextUtils.isEmpty(reminder.getEventTime()) ||
                                TextUtils.isEmpty(reminder.getUuId())) {
                            continue;
                        }
                        if (realmDb.getGroup(reminder.getGroupUuId()) == null && defaultGroup != null) {
                            reminder.setGroupUuId(defaultGroup.getUuId());
                        }
                        realmDb.saveReminder(reminder, () -> {
                            EventControl control = EventControlFactory.getController(mContext, reminder);
                            if (control.canSkip()) {
                                control.next();
                            } else {
                                control.start();
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * Export reminder object to file.
     * @param item reminder object
     * @return Path to file
     */
    @Nullable
    public String exportReminder(@NonNull Reminder item) {
        WeakReference<String> jsonData = new WeakReference<>(new Gson().toJson(item));
        File dir = MemoryUtil.getRemindersDir();
        if (dir != null) {
            String exportFileName = item.getUuId() + FileConfig.FILE_NAME_REMINDER;
            try {
                return writeFile(new File(dir, exportFileName), jsonData.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.i(TAG, "Couldn't find external storage!");
        }
        return null;
    }

    @Nullable
    public Reminder getReminder(@NonNull ContentResolver cr, @NonNull Uri name) throws IOException, IllegalStateException {
        Reminder reminder = null;
        try {
            reminder = new Gson().fromJson(readFileToJson(cr, name), Reminder.class);
        } catch (IllegalStateException ignored) {
        }
        return reminder;
    }

    @Nullable
    public Reminder getReminder(@Nullable String filePath, @Nullable String json) throws IOException, IllegalStateException {
        if (filePath != null && MemoryUtil.isSdPresent()) {
            WeakReference<Reminder> item = new WeakReference<>(new Gson().fromJson(readFileToJson(filePath), Reminder.class));
            return item.get();
        } else if (json != null) {
            WeakReference<Reminder> item = new WeakReference<>(new Gson().fromJson(json, Reminder.class));
            return item.get();
        } else {
            return null;
        }
    }

    @Nullable
    public NoteItem getNote(@NonNull ContentResolver cr, @NonNull Uri name) throws IOException, IllegalStateException {
        try {
            WeakReference<NoteItem> note = new WeakReference<>(new Gson().fromJson(readFileToJson(cr, name), NoteItem.class));
            return note.get();
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public NoteItem getNote(@Nullable String filePath, @Nullable String json) throws IOException, IllegalStateException {
        if (filePath != null && MemoryUtil.isSdPresent()) {
            WeakReference<NoteItem> item = new WeakReference<>(new Gson().fromJson(readFileToJson(filePath), NoteItem.class));
            return item.get();
        } else if (json != null) {
            WeakReference<NoteItem> item = new WeakReference<>(new Gson().fromJson(json, NoteItem.class));
            return item.get();
        } else {
            return null;
        }
    }

    public void exportNotes() {
        for (NoteItem item : RealmDb.getInstance().getAllNotes(null)) {
            exportNote(item);
        }
    }

    public void importNotes() throws IOException, IllegalStateException {
        File dir = MemoryUtil.getNotesDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                RealmDb realmDb = RealmDb.getInstance();
                for (File file : files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_NOTE)) {
                        NoteItem item = getNote(file.toString(), null);
                        if (item == null || TextUtils.isEmpty(item.getKey())) {
                            continue;
                        }
                        realmDb.saveObject(item);
                    }
                }
            }
        }
    }

    public void exportNote(@NonNull NoteItem item) {
        WeakReference<String> jsonData = new WeakReference<>(new Gson().toJson(item));
        File dir = MemoryUtil.getNotesDir();
        if (dir != null) {
            String exportFileName = item.getKey() + FileConfig.FILE_NAME_NOTE;
            try {
                writeFile(new File(dir, exportFileName), jsonData.get());
                jsonData.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.i(TAG, "Couldn't find external storage!");
        }
    }

    public void createNote(@Nullable NoteItem item, @Nullable CreateCallback callback) {
        if (item == null) return;
        WeakReference<String> jsonData = new WeakReference<>(new Gson().toJson(item));
        File file = null;
        File dir = MemoryUtil.getMailDir();
        if (dir != null) {
            String exportFileName = item.getKey() + FileConfig.FILE_NAME_NOTE;
            file = new File(dir, exportFileName);
            try {
                writeFile(file, jsonData.get());
                jsonData.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.i(TAG, "Couldn't find external storage!");
        }
        if (callback != null) {
            callback.onReady(file);
        }
    }

    @Nullable
    public String readFileToJson(@NonNull ContentResolver cr, @NonNull Uri name) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = cr.openInputStream(name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (inputStream == null) {
            return null;
        }
        Base64InputStream output64 = new Base64InputStream(inputStream, Base64.DEFAULT);
        BufferedReader r = new BufferedReader(new InputStreamReader(output64));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        output64.close();
        inputStream.close();
        String res = total.toString();
        if ((res.startsWith("{") && res.endsWith("}")) || (res.startsWith("[") && res.endsWith("]"))) return res;
        else {
            throw new IOException("Bad JSON");
        }
    }

    @NonNull
    public String readFileToJson(@NonNull String path) throws IOException {
        FileInputStream inputStream = new FileInputStream(path);
        Base64InputStream output64 = new Base64InputStream(inputStream, Base64.DEFAULT);
        BufferedReader r = new BufferedReader(new InputStreamReader(output64));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        output64.close();
        inputStream.close();
        String res = total.toString();
        if ((res.startsWith("{") && res.endsWith("}")) || (res.startsWith("[") && res.endsWith("]"))) return res;
        else {
            throw new IOException("Bad JSON");
        }
    }

    /**
     * Write data to file.
     * @param file target file.
     * @param data object data.
     * @return Path to file
     * @throws IOException
     */
    @Nullable
    private String writeFile(@NonNull File file, @Nullable String data) throws IOException {
        if (data == null) return null;
        InputStream inputStream = new ByteArrayInputStream(data.getBytes());
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Base64OutputStream output64 = new Base64OutputStream(output, Base64.DEFAULT);
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output64.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        output64.close();

        if (file.exists()) {
            file.delete();
        }
        FileWriter fw = new FileWriter(file);
        fw.write(output.toString());
        fw.close();
        output.close();
        return file.toString();
    }

    public interface CreateCallback {
        void onReady(File file);
    }
}
