package com.elementary.tasks.core.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.elementary.tasks.core.cloud.FileConfig;
import com.elementary.tasks.notes.NoteItem;
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

    private Context mContext;
    private static BackupTool instance;

    private BackupTool() {}

    private BackupTool(Context context) {
        this.mContext = context;
    }

    public static BackupTool getInstance(Context context) {
        if (instance == null) {
            instance = new BackupTool(context);
        }
        return instance;
    }

    public NoteItem getNote(String filePath, String json) throws IOException {
        if (filePath != null) {
            if (MemoryUtil.isSdPresent()){
                WeakReference<String> jsonText = new WeakReference<>(readFileToJson(filePath));
                WeakReference<NoteItem> note = new WeakReference<>(new Gson().fromJson(jsonText.get(), NoteItem.class));
                return note.get();
            } else return null;
        } else {
            WeakReference<NoteItem> note = new WeakReference<>(new Gson().fromJson(json, NoteItem.class));
            return note.get();
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
     * @param string string to decrypt.
     * @return Decrypted string
     */
    public static String decrypt(String string){
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
     * @param string string to encrypt.
     * @return Encrypted string
     */
    public static String encrypt(String string){
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
