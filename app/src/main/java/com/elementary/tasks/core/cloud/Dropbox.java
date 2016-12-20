package com.elementary.tasks.core.cloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.MemoryUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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

    private Context mContext;

    private String dbxFolder = "JustReminder/";
    private String dbxNoteFolder = "Notes/";
    private String dbxGroupFolder = "Groups/";
    private String dbxBirthFolder = "Birthdays/";

    private DropboxAPI<AndroidAuthSession> mDBApi;
    private DropboxAPI.Entry newEntry;

    public static final String APP_KEY = "4zi1d414h0v8sxe";
    public static final String APP_SECRET = "aopehxo80oq8g5o";
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

    public Dropbox(Context context){
        this.mContext = context;
    }

    /**
     * Start connection to Dropbox.
     */
    public void startSession(){
        AndroidAuthSession session = buildSession();
        mDBApi = new DropboxAPI<>(session);
        checkAppKeySetup();
    }

    /**
     * Check if user has already connected to Dropbox from this application.
     * @return Boolean
     */
    public boolean isLinked() {
        return mDBApi != null && mDBApi.getSession().isLinked();
    }

    /**
     * Get Dropbox user name.
     * @return String user name
     */
    public String userName(){
        DropboxAPI.Account account = null;
        try {
            account = mDBApi.accountInfo();
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return account != null ? account.displayName : null;
    }

    /**
     * Get user all apace on Dropbox.
     * @return Long - user quota
     */
    public long userQuota(){
        DropboxAPI.Account account = null;
        try {
            account = mDBApi.accountInfo();
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return account != null ? account.quota : 0;
    }

    public long userQuotaNormal(){
        DropboxAPI.Account account = null;
        try {
            account = mDBApi.accountInfo();
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return account != null ? (account.quotaNormal + account.quotaShared) : 0;
    }

    public boolean checkLink(){
        boolean isLogged = false;
        startSession();
        AndroidAuthSession session = mDBApi.getSession();
        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();
                storeAuth(session);
                isLogged = true;
            } catch (IllegalStateException e) {
                Toast.makeText(mContext, "Couldn't authenticate with Dropbox:" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        return isLogged;
    }

    public boolean startLink(){
        mDBApi.getSession().startOAuth2Authentication(mContext);
        return mDBApi.getSession().isLinked();
    }

    public boolean unlink(){
        boolean is = false;
        if (logOut()) {
            is = true;
        }
        return is;
    }

    public void checkAppKeySetup() {
        if (APP_KEY.startsWith("CHANGE") ||
                APP_SECRET.startsWith("CHANGE")) {
            Toast.makeText(mContext, "You must apply for an app key and secret from developers.dropbox.com, " +
                    "and add them to the DBRoulette ap before trying it.", Toast.LENGTH_SHORT).show();
            ((Activity) mContext).finish();
            return;
        }
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + APP_KEY;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = mContext.getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
            Toast.makeText(mContext, "URL scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme, Toast.LENGTH_SHORT).show();
            ((Activity) mContext).finish();
        }
    }

    public void loadAuth(AndroidAuthSession session) {
        SharedPreferences prefs = mContext.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

        if (key.equals("oauth2:")) {
            session.setOAuth2AccessToken(secret);
        } else {
            session.setAccessTokenPair(new AccessTokenPair(key, secret));
        }
    }

    public void storeAuth(AndroidAuthSession session) {
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = mContext.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.commit();
            return;
        }
        AccessTokenPair oauth1AccessToken = session.getAccessTokenPair();
        if (oauth1AccessToken != null) {
            SharedPreferences prefs = mContext.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, oauth1AccessToken.key);
            edit.putString(ACCESS_SECRET_NAME, oauth1AccessToken.secret);
            edit.commit();
        }
    }

    public AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }

    private boolean logOut() {
        mDBApi.getSession().unlink();
        clearKeys();
        return true;
    }

    private void clearKeys() {
        SharedPreferences prefs = mContext.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    /**
     * Upload to Dropbox folder backup files from selected folder on SD Card.
     * @param path name of folder to upload.
     */
    private void upload(String path){
        startSession();
        if (isLinked()) {
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + path);
            File[] files = sdPathDr.listFiles();
            String fileLoc = sdPathDr.toString();
            if (files != null) {
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
                    if (path.matches(Constants.DIR_NOTES_SD)) folder = dbxNoteFolder;
                    else if (path.matches(Constants.DIR_GROUP_SD)) folder = dbxGroupFolder;
                    else if (path.matches(Constants.DIR_BIRTHDAY_SD)) folder = dbxBirthFolder;
                    else folder = dbxFolder;
                    try {
                        newEntry = mDBApi.putFileOverwrite(folder + fileLoopName,
                                fis, tmpFile.length(), null);
                    } catch (DropboxUnlinkedException e) {
                        Log.e("DbLog", "User has unlinked.");
                    } catch (DropboxException e) {
                        Log.e("DbLog", "Something went wrong while uploading.");
                    }
                    if (file.exists()) file.delete();
                }
            }
        }
    }

    /**
     * Upload reminder backup files or selected file to Dropbox folder.
     * @param fileName file name.
     */
    public void uploadReminder(final String fileName) {
        startSession();
        if (isLinked()) {
            if (fileName != null) {
                File dir = MemoryUtil.getRDir();
                if (dir == null) return;
                File tmpFile = new File(dir.toString(), fileName);
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(tmpFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    newEntry = mDBApi.putFileOverwrite(dbxFolder + fileName, fis, tmpFile.length(), null);
                } catch (DropboxUnlinkedException e) {
                    Log.e("DbLog", "User has unlinked.");
                } catch (DropboxException e) {
                    Log.e("DbLog", "Something went wrong while uploading.");
                }
                if (tmpFile.exists()) tmpFile.delete();
            } else {
                upload(Constants.DIR_SD);
            }
        }
    }

    /**
     * Upload all note backup files to Dropbox folder.
     */
    public void uploadNote() {
        upload(Constants.DIR_NOTES_SD);
    }

    /**
     * Upload all group backup files to Dropbox folder.
     */
    public void uploadGroup() {
        upload(Constants.DIR_GROUP_SD);
    }

    /**
     * Upload all birthday backup files to Dropbox folder.
     */
    public void uploadBirthday() {
        upload(Constants.DIR_BIRTHDAY_SD);
    }

    public void deleteFile(String fileName) {
        if (fileName.endsWith(FileConfig.FILE_NAME_REMINDER))
            deleteReminder(fileName);
        else if (fileName.endsWith(FileConfig.FILE_NAME_NOTE))
            deleteNote(fileName);
        else if (fileName.endsWith(FileConfig.FILE_NAME_GROUP))
            deleteGroup(fileName);
        else if (fileName.endsWith(FileConfig.FILE_NAME_BIRTHDAY))
            deleteBirthday(fileName);
    }

    /**
     * Delete reminder backup file from Dropbox folder.
     * @param name file name.
     */
    public void deleteReminder(String name){
        startSession();
        if (isLinked()) {
            try {
                mDBApi.delete(dbxFolder + name);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Delete note backup file from Dropbox folder.
     * @param name file name.
     */
    public void deleteNote(String name){
        startSession();
        if (isLinked()) {
            try {
                mDBApi.delete(dbxNoteFolder + name + FileConfig.FILE_NAME_NOTE);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Delete group backup file from Dropbox folder.
     * @param name file name.
     */
    public void deleteGroup(String name){
        startSession();
        if (isLinked()) {
            try {
                mDBApi.delete(dbxGroupFolder + name + FileConfig.FILE_NAME_GROUP);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Delete birthday backup file from Dropbox folder.
     * @param name file name
     */
    public void deleteBirthday(String name){
        startSession();
        if (isLinked()) {
            try {
                mDBApi.delete(dbxBirthFolder + name);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Delete all folders inside application folder on Dropbox.
     */
    public void cleanFolder(){
        startSession();
        if (isLinked()) {
            try {
                mDBApi.delete(dbxNoteFolder);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            try {
                mDBApi.delete(dbxGroupFolder);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            try {
                mDBApi.delete(dbxBirthFolder);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            try {
                mDBApi.delete(dbxFolder);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Download on SD Card all reminder backup files found on Dropbox.
     */
    public void downloadReminder() {
        startSession();
        if (isLinked()) {
            try {
                newEntry = mDBApi.metadata("/" + dbxFolder, 1000, null, true, null);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            if (newEntry != null) {
                for (DropboxAPI.Entry e : newEntry.contents) {
                    if (!e.isDeleted) {
                        String fileName = e.fileName();
                        File dir = MemoryUtil.getDRDir();
                        if (dir == null) return;
                        File localFile = new File(dir + "/" + fileName);
                        String cloudFile = "/" + dbxFolder + fileName;
                        downloadFile(localFile, cloudFile);
//                        try {
//                            new SyncHelper(mContext).reminderFromJson(localFile.toString());
//                        } catch (JSONException e1) {
//                            e1.printStackTrace();
//                        }
                        try {
                            mDBApi.delete(e.path);
                        } catch (DropboxException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void downloadFile(File localFile, String cloudFile) {
        if (!localFile.exists()) {
            try {
                localFile.createNewFile(); //otherwise dropbox client will fail silently
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(localFile);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        try {
            mDBApi.getFile(cloudFile, null, outputStream, null);
        } catch (DropboxException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Download on SD Card all note backup files found on Dropbox.
     */
    public void downloadNote() {
        startSession();
        if (isLinked()) {
            try {
                newEntry = mDBApi.metadata("/" + dbxNoteFolder, 1000, null, true, null);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            if (newEntry != null) {
                for (DropboxAPI.Entry e : newEntry.contents) {
                    if (!e.isDeleted) {
                        String fileName = e.fileName();
                        File dir = MemoryUtil.getDNDir();
                        if (dir == null) return;
                        File localFile = new File(dir + "/" + fileName);
                        String cloudFile = "/" + dbxNoteFolder + fileName;
                        downloadFile(localFile, cloudFile);
//                        try {
//                            new SyncHelper(mContext).noteFromJson(localFile.toString(), fileName);
//                        } catch (JSONException e1) {
//                            e1.printStackTrace();
//                        }
                        try {
                            mDBApi.delete(e.path);
                        } catch (DropboxException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * Download on SD Card all group backup files found on Dropbox.
     */
    public void downloadGroup() {
        startSession();
        if (isLinked()) {
            try {
                newEntry = mDBApi.metadata("/" + dbxGroupFolder, 1000, null, true, null);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            if (newEntry != null) {
                for (DropboxAPI.Entry e : newEntry.contents) {
                    if (!e.isDeleted) {
                        String fileName = e.fileName();
                        File dir = MemoryUtil.getDGroupsDir();
                        if (dir == null) return;
                        File localFile = new File(dir + "/" + fileName);
                        String cloudFile = "/" + dbxGroupFolder + fileName;
                        downloadFile(localFile, cloudFile);
//                        try {
//                            new SyncHelper(mContext).groupFromJson(localFile);
//                        } catch (JSONException e1) {
//                            e1.printStackTrace();
//                        }
                        try {
                            mDBApi.delete(e.path);
                        } catch (DropboxException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * Download on SD Card all birthday backup files found on Dropbox.
     */
    public void downloadBirthday() {
        startSession();
        if (isLinked()) {
            try {
                newEntry = mDBApi.metadata("/" + dbxBirthFolder, 1000, null, true, null);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            if (newEntry != null) {
                for (DropboxAPI.Entry e : newEntry.contents) {
                    if (!e.isDeleted) {
                        String fileName = e.fileName();
                        File dir = MemoryUtil.getDBDir();
                        if (dir == null) return;
                        File localFile = new File(dir + "/" + fileName);
                        String cloudFile = "/" + dbxBirthFolder + fileName;
                        downloadFile(localFile, cloudFile);
//                        try {
//                            new SyncHelper(mContext).birthdayFromJson(localFile);
//                        } catch (JSONException e1) {
//                            e1.printStackTrace();
//                        }
                        try {
                            mDBApi.delete(e.path);
                        } catch (DropboxException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * Count all reminder backup files in Dropbox folder.
     * @return number of found backup files.
     */
    public int countFiles() {
        int count = 0;
        File dir = MemoryUtil.getDRDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) count += files.length;
        }
        dir = MemoryUtil.getDNDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) count += files.length;
        }
        dir = MemoryUtil.getDBDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) count += files.length;
        }
        dir = MemoryUtil.getDGroupsDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) count += files.length;
        }

        return count;
    }
}
