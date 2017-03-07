package com.elementary.tasks.core.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Base64;

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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

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

    private BackupTool() {}

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

    public void importTemplates() throws IOException {
        File dir = MemoryUtil.getTemplatesDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                RealmDb realmDb = RealmDb.getInstance();
                for (File file : files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_TEMPLATE)) {
                        realmDb.saveObject(getTemplate(file.toString(), null));
                    }
                }
            }
        }
    }

    public void exportTemplate(TemplateItem item) {
        WeakReference<String> jsonData = new WeakReference<>(new Gson().toJson(item));
        WeakReference<String> encrypted = new WeakReference<>(encrypt(jsonData.get()));
        File dir = MemoryUtil.getTemplatesDir();
        if (dir != null) {
            String exportFileName = item.getKey() + FileConfig.FILE_NAME_TEMPLATE;
            try {
                writeFile(new File(dir, exportFileName), encrypted.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.i(TAG, "Couldn't find external storage!");
        }
    }

    public TemplateItem getTemplate(ContentResolver cr, Uri name) throws IOException {
        WeakReference<TemplateItem> item = new WeakReference<>(new Gson().fromJson(readFileToJson(cr, name), TemplateItem.class));
        return item.get();
    }

    public TemplateItem getTemplate(String filePath, String json) throws IOException {
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

    public void importPlaces() throws IOException {
        File dir = MemoryUtil.getPlacesDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                RealmDb realmDb = RealmDb.getInstance();
                for (File file : files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_PLACE)) {
                        realmDb.saveObject(getPlace(file.toString(), null));
                    }
                }
            }
        }
    }

    public void exportPlace(PlaceItem item) {
        WeakReference<String> jsonData = new WeakReference<>(new Gson().toJson(item));
        WeakReference<String> encrypted = new WeakReference<>(encrypt(jsonData.get()));
        File dir = MemoryUtil.getPlacesDir();
        if (dir != null) {
            String exportFileName = item.getKey() + FileConfig.FILE_NAME_PLACE;
            try {
                writeFile(new File(dir, exportFileName), encrypted.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.i(TAG, "Couldn't find external storage!");
        }
    }

    public PlaceItem getPlace(ContentResolver cr, Uri name) throws IOException {
        WeakReference<PlaceItem> item = new WeakReference<>(new Gson().fromJson(readFileToJson(cr, name), PlaceItem.class));
        return item.get();
    }

    public PlaceItem getPlace(String filePath, String json) throws IOException {
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

    public void importBirthdays() throws IOException {
        File dir = MemoryUtil.getBirthdaysDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                RealmDb realmDb = RealmDb.getInstance();
                for (File file : files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_BIRTHDAY)) {
                        realmDb.saveObject(getBirthday(file.toString(), null));
                    }
                }
            }
        }
    }

    public void exportBirthday(BirthdayItem item) {
        WeakReference<String> jsonData = new WeakReference<>(new Gson().toJson(item));
        WeakReference<String> encrypted = new WeakReference<>(encrypt(jsonData.get()));
        File dir = MemoryUtil.getBirthdaysDir();
        if (dir != null) {
            String exportFileName = item.getUuId() + FileConfig.FILE_NAME_BIRTHDAY;
            try {
                writeFile(new File(dir, exportFileName), encrypted.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.i(TAG, "Couldn't find external storage!");
        }
    }

    public BirthdayItem getBirthday(ContentResolver cr, Uri name) throws IOException {
        WeakReference<BirthdayItem> item = new WeakReference<>(new Gson().fromJson(readFileToJson(cr, name), BirthdayItem.class));
        return item.get();
    }

    public BirthdayItem getBirthday(String filePath, String json) throws IOException {
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

    public void exportGroup(GroupItem item) {
        WeakReference<String> jsonData = new WeakReference<>(new Gson().toJson(item));
        WeakReference<String> encrypted = new WeakReference<>(encrypt(jsonData.get()));
        File dir = MemoryUtil.getGroupsDir();
        if (dir != null) {
            String exportFileName = item.getUuId() + FileConfig.FILE_NAME_GROUP;
            File file = new File(dir, exportFileName);
            LogUtil.d(TAG, "exportGroup: " + file);
            try {
                writeFile(file, encrypted.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.i(TAG, "Couldn't find external storage!");
        }
    }

    public void importGroups() throws IOException {
        File dir = MemoryUtil.getGroupsDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                RealmDb realmDb = RealmDb.getInstance();
                for (File file : files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_GROUP)) {
                        realmDb.saveObject(getGroup(file.toString(), null));
                    }
                }
            }
        }
    }

    public GroupItem getGroup(ContentResolver cr, Uri name) throws IOException {
        WeakReference<GroupItem> item = new WeakReference<>(new Gson().fromJson(readFileToJson(cr, name), GroupItem.class));
        return item.get();
    }

    public GroupItem getGroup(String filePath, String json) throws IOException {
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
        for (Reminder item : RealmDb.getInstance().getEnabledReminders()) {
            exportReminder(item);
        }
    }

    public void importReminders(Context mContext) throws IOException {
        File dir = MemoryUtil.getRemindersDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                RealmDb realmDb = RealmDb.getInstance();
                for (File file : files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_REMINDER)) {
                        Reminder reminder = getReminder(file.toString(), null);
                        if (reminder.isRemoved() || !reminder.isActive()) {
                            continue;
                        }
                        realmDb.saveObject(reminder);
                        EventControl control = EventControlFactory.getController(mContext, reminder);
                        control.next();
                    }
                }
            }
        }
    }

    public void exportReminder(Reminder item) {
        WeakReference<String> jsonData = new WeakReference<>(new Gson().toJson(item));
        WeakReference<String> encrypted = new WeakReference<>(encrypt(jsonData.get()));
        File dir = MemoryUtil.getRemindersDir();
        if (dir != null) {
            String exportFileName = item.getUuId() + FileConfig.FILE_NAME_REMINDER;
            try {
                writeFile(new File(dir, exportFileName), encrypted.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.i(TAG, "Couldn't find external storage!");
        }
    }

    public Reminder getReminder(ContentResolver cr, Uri name) throws IOException {
        WeakReference<Reminder> item = new WeakReference<>(new Gson().fromJson(readFileToJson(cr, name), Reminder.class));
        return item.get();
    }

    public Reminder getReminder(String filePath, String json) throws IOException {
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

    public NoteItem getNote(ContentResolver cr, Uri name) throws IOException {
        WeakReference<NoteItem> note = new WeakReference<>(new Gson().fromJson(readFileToJson(cr, name), NoteItem.class));
        return note.get();
    }

    public NoteItem getNote(String filePath, String json) throws IOException {
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

    public void importNotes() throws IOException {
        File dir = MemoryUtil.getNotesDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                RealmDb realmDb = RealmDb.getInstance();
                for (File file : files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_NOTE)) {
                        realmDb.saveObject(getNote(file.toString(), null));
                    }
                }
            }
        }
    }

    public void exportNote(NoteItem item) {
        WeakReference<String> jsonData = new WeakReference<>(new Gson().toJson(item));
        WeakReference<String> encrypted = new WeakReference<>(encrypt(jsonData.get()));
        File dir = MemoryUtil.getNotesDir();
        if (dir != null) {
            String exportFileName = item.getKey() + FileConfig.FILE_NAME_NOTE;
            try {
                writeFile(new File(dir, exportFileName), encrypted.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.i(TAG, "Couldn't find external storage!");
        }
    }

    public File createNote(NoteItem item) {
        WeakReference<String> jsonData = new WeakReference<>(new Gson().toJson(item));
        WeakReference<String> encrypted = new WeakReference<>(encrypt(jsonData.get()));
        File file = null;
        File dir = MemoryUtil.getMailDir();
        if (dir != null) {
            String exportFileName = item.getKey() + FileConfig.FILE_NAME_NOTE;
            file = new File(dir, exportFileName);
            try {
                writeFile(file, encrypted.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.i(TAG, "Couldn't find external storage!");
        }
        return file;
    }

    public String readFileToJson(ContentResolver cr, Uri name) throws IOException {
        InputStream is = null;
        try {
            is = cr.openInputStream(name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (is == null) {
            return null;
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        WeakReference<String> file = new WeakReference<>(total.toString());
        WeakReference<String> decrypted = new WeakReference<>(decrypt(file.get()));
        return decrypted.get();
    }

    public String readFileToJson(String path) throws IOException {
        FileInputStream stream = new FileInputStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            total.append(line);
        }
        stream.close();
        String decr = decrypt(total.toString());
        LogUtil.d(TAG, "readFileToJson: " + decr);
        return decr;
    }

    private void writeFile(File file, String data) throws IOException {
        if (file.exists()) {
            file.delete();
        }
        FileWriter fw = new FileWriter(file);
        fw.write(data);
        fw.close();
    }

    /**
     * Decrypt string to human readable format.
     *
     * @param string string to decrypt.
     * @return Decrypted string
     */
    public static String decrypt(String string) {
        String result = "";
        try {
            byte[] byteString = Base64.decode(string, Base64.DEFAULT);
            result = new String(byteString, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        return result;
    }

    /**
     * Encrypt string.
     *
     * @param string string to encrypt.
     * @return Encrypted string
     */
    public static String encrypt(String string) {
        byte[] stringByted = null;
        try {
            stringByted = string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(stringByted, Base64.DEFAULT).trim();
    }
}
