package com.elementary.tasks.core.utils;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.elementary.tasks.birthdays.BirthdayItem;
import com.elementary.tasks.core.cloud.FileConfig;
import com.elementary.tasks.groups.GroupItem;
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
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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

public class BackupTool {

    private static final String TAG = "BackupTool";
    private static BackupTool instance;

    private BackupTool() {
    }

    public static BackupTool getInstance() {
        if (instance == null) {
            instance = new BackupTool();
        }
        return instance;
    }

    public void exportPlace(PlaceItem item) {
        WeakReference<String> jsonData = new WeakReference<>(new Gson().toJson(item));
        WeakReference<String> encrypted = new WeakReference<>(encrypt(jsonData.get()));
        File dir = MemoryUtil.getGroupsDir();
        if (dir != null) {
            String exportFileName = item.getKey() + FileConfig.FILE_NAME_PLACE;
            try {
                writeFile(new File(dir, exportFileName), encrypted.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else Log.i(TAG, "Couldn't find external storage!");
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
        } else return null;
    }

    public void exportBirthday(BirthdayItem item) {
        WeakReference<String> jsonData = new WeakReference<>(new Gson().toJson(item));
        WeakReference<String> encrypted = new WeakReference<>(encrypt(jsonData.get()));
        File dir = MemoryUtil.getGroupsDir();
        if (dir != null) {
            String exportFileName = item.getUuId() + FileConfig.FILE_NAME_BIRTHDAY;
            try {
                writeFile(new File(dir, exportFileName), encrypted.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else Log.i(TAG, "Couldn't find external storage!");
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
        } else return null;
    }

    public void exportGroup(GroupItem item) {
        WeakReference<String> jsonData = new WeakReference<>(new Gson().toJson(item));
        WeakReference<String> encrypted = new WeakReference<>(encrypt(jsonData.get()));
        File dir = MemoryUtil.getGroupsDir();
        if (dir != null) {
            String exportFileName = item.getUuId() + FileConfig.FILE_NAME_GROUP;
            try {
                writeFile(new File(dir, exportFileName), encrypted.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else Log.i(TAG, "Couldn't find external storage!");
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
        } else return null;
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
        } else Log.i(TAG, "Couldn't find external storage!");
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
        } else return null;
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
        } else return null;
    }

    public void exportNote(NoteItem item) {
        WeakReference<String> jsonData = new WeakReference<>(new Gson().toJson(item));
        WeakReference<String> encrypted = new WeakReference<>(encrypt(jsonData.get()));
        File dir = MemoryUtil.getRemindersDir();
        if (dir != null) {
            String exportFileName = item.getKey() + FileConfig.FILE_NAME_NOTE;
            try {
                writeFile(new File(dir, exportFileName), encrypted.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else Log.i(TAG, "Couldn't find external storage!");
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
        } else Log.i(TAG, "Couldn't find external storage!");
        return file;
    }

    public String readFileToJson(ContentResolver cr, Uri name) throws IOException {
        InputStream is = null;
        try {
            is = cr.openInputStream(name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader r = null;
        if (is != null) {
            r = new BufferedReader(new InputStreamReader(is));
        }
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r != null ? r.readLine() : null) != null) {
            total.append(line);
        }
        WeakReference<String> file = new WeakReference<>(total.toString());
        WeakReference<String> decrypted = new WeakReference<>(decrypt(file.get()));
        return decrypted.get();
    }

    public String readFileToJson(String path) throws IOException {
        FileInputStream stream = new FileInputStream(path);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        stream.close();
        WeakReference<String> decrypted = new WeakReference<>(decrypt(writer.toString()));
        return decrypted.get();
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
        byte[] byte_string = Base64.decode(string, Base64.DEFAULT);
        try {
            result = new String(byte_string, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        Log.d(TAG, "decrypt: " + result);
        return result;
    }

    /**
     * Encrypt string.
     *
     * @param string string to encrypt.
     * @return Encrypted string
     */
    public static String encrypt(String string) {
        Log.d(TAG, "encrypt: " + string);
        byte[] string_byted = null;
        try {
            string_byted = string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(string_byted, Base64.DEFAULT).trim();
    }
}
