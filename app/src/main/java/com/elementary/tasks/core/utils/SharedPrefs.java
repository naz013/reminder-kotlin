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

package com.elementary.tasks.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

abstract class SharedPrefs extends PrefsConstants {

    private SharedPreferences prefs;

    private SharedPrefs() {
    }

    SharedPrefs(Context context){
        prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE);
    }

    void putString(String stringToSave, String value){
        prefs.edit().putString(stringToSave, value).apply();
    }

    void putInt(String stringToSave, int value){
        prefs.edit().putInt(stringToSave, value).apply();
    }

    int getInt(String stringToLoad){
        int x;
        try {
            x = prefs.getInt(stringToLoad, 0);
        } catch (ClassCastException e) {
            try {
                x = Integer.parseInt(prefs.getString(stringToLoad, "0"));
            } catch (ClassCastException e1) {
                x = 0;
            }
        }
        return x;
    }

    void putLong(String stringToSave, long value){
        prefs.edit().putLong(stringToSave, value).apply();
    }

    long getLong(String stringToLoad){
        long x;
        try {
            x = prefs.getLong(stringToLoad, 1000);
        } catch (ClassCastException e) {
            x = Long.parseLong(prefs.getString(stringToLoad, "1000"));
        }
        return x;
    }

    void putObject(String key, Object obj) {
        Gson gson = new Gson();
        putString(key, gson.toJson(obj));
    }

    Object getObject(String key, Class<?> classOfT) {
        String json = getString(key);
        Object value = new Gson().fromJson(json, classOfT);
        if (value == null)
            throw new NullPointerException();
        return value;
    }

    String getString(String stringToLoad){
        String res;
        try {
            res = prefs.getString(stringToLoad, "");
        } catch (NullPointerException e) {
            e.printStackTrace();
            res = "";
        }
        return res;
    }

    boolean hasKey(String checkString){
        return prefs.contains(checkString);
    }

    void putBoolean(String stringToSave, boolean value){
        prefs.edit().putBoolean(stringToSave, value).apply();
    }

    boolean getBoolean(String stringToLoad){
        boolean res;
        try {
            res = prefs.getBoolean(stringToLoad, false);
        } catch (ClassCastException e){
            res = Boolean.parseBoolean(prefs.getString(stringToLoad, "false"));
        }
        return res;
    }

    void saveVersionBoolean(String stringToSave){
        prefs.edit().putBoolean(stringToSave, true).apply();
    }

    boolean getVersion(String stringToLoad){
        boolean res;
        try {
            res = prefs.getBoolean(stringToLoad, false);
        } catch (ClassCastException e){
            res = Boolean.parseBoolean(prefs.getString(stringToLoad, "false"));
        }
        return res;
    }

    void savePrefsBackup(String key){
        File dir = MemoryUtil.getPrefsDir();
        if (dir != null) {
            File prefsFile = new File(dir + "/prefs.xml");
            if (prefsFile.exists()) prefsFile.delete();
            ObjectOutputStream output = null;
            try {
                output = new ObjectOutputStream(new FileOutputStream(prefsFile));
                Map<String, ?> list = prefs.getAll();
                if (list.containsKey(key)) list.remove(key);
                output.writeObject(list);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if (output != null) {
                        output.flush();
                        output.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    void loadPrefsFromFile(){
        File dir = MemoryUtil.getPrefsDir();
        if (dir == null) return;

        File prefsFile = new File(dir + "/prefs.xml");
        if (prefsFile.exists()) {
            ObjectInputStream input = null;
            try {
                input = new ObjectInputStream(new FileInputStream(prefsFile));
                SharedPreferences.Editor prefEdit = prefs.edit();
                prefEdit.clear();
                Map<String, ?> entries = (Map<String, ?>) input.readObject();
                for (Map.Entry<String, ?> entry : entries.entrySet()) {
                    Object v = entry.getValue();
                    String key = entry.getKey();
                    if (v instanceof Boolean)
                        prefEdit.putBoolean(key, (Boolean) v);
                    else if (v instanceof Float)
                        prefEdit.putFloat(key, (Float) v);
                    else if (v instanceof Integer)
                        prefEdit.putInt(key, (Integer) v);
                    else if (v instanceof Long)
                        prefEdit.putLong(key, (Long) v);
                    else if (v instanceof String)
                        prefEdit.putString(key, ((String) v));
                }
                prefEdit.apply();
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}