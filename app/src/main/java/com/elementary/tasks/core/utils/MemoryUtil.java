/*
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

import android.os.Environment;

import java.io.File;
import java.util.Locale;

public class MemoryUtil {

    private final static String DIR_SD = "backup";
    private final static String DIR_IMAGE_CACHE = "img";
    private final static String DIR_PREFS = "preferences";
    private final static String DIR_NOTES_SD = "notes";
    private final static String DIR_GROUP_SD = "groups";
    private final static String DIR_BIRTHDAY_SD = "birthdays";
    private final static String DIR_MAIL_SD = "mail_attachments";
    private final static String DIR_SD_DBX_TMP = "tmp_dropbox";
    private final static String DIR_NOTES_SD_DBX_TMP = "tmp_dropbox_notes";
    private final static String DIR_GROUP_SD_DBX_TMP = "tmp_dropbox_groups";
    private final static String DIR_BIRTHDAY_SD_DBX_TMP = "tmp_dropbox_birthdays";
    private final static String DIR_SD_GDRIVE_TMP = "tmp_gdrive";
    private final static String DIR_NOTES_SD_GDRIVE_TMP = "tmp_gdrive_notes";
    private final static String DIR_GROUP_SD_GDRIVE_TMP = "tmp_gdrive_group";
    private final static String DIR_BIRTHDAY_SD_GDRIVE_TMP = "tmp_gdrive_birthdays";

    public static boolean isSdPresent() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static File getRDir() {
        return getDir(DIR_SD);
    }

    public static File getGroupsDir() {
        return getDir(DIR_GROUP_SD);
    }

    public static File getBDir() {
        return getDir(DIR_BIRTHDAY_SD);
    }

    public static File getNDir() {
        return getDir(DIR_NOTES_SD);
    }

    public static File getDRDir() {
        return getDir(DIR_SD_DBX_TMP);
    }

    public static File getDGroupsDir() {
        return getDir(DIR_GROUP_SD_DBX_TMP);
    }

    public static File getDBDir() {
        return getDir(DIR_BIRTHDAY_SD_DBX_TMP);
    }

    public static File getDNDir() {
        return getDir(DIR_NOTES_SD_DBX_TMP);
    }

    public static File getGRDir() {
        return getDir(DIR_SD_GDRIVE_TMP);
    }

    public static File getGGroupsDir() {
        return getDir(DIR_GROUP_SD_GDRIVE_TMP);
    }

    public static File getGBDir() {
        return getDir(DIR_BIRTHDAY_SD_GDRIVE_TMP);
    }

    public static File getGNDir() {
        return getDir(DIR_NOTES_SD_GDRIVE_TMP);
    }

    public static File getMailDir() {
        return getDir(DIR_MAIL_SD);
    }

    public static File getPrefsDir() {
        return getDir(DIR_PREFS);
    }

    public static File getImageCacheDir() {
        return getDir(DIR_IMAGE_CACHE);
    }

    public static File getParent() {
        return getDir("");
    }

    public static File getDir(String directory) {
        if (isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            File dir = new File(sdPath.toString() + "/JustReminder/" + directory);
            if (!dir.exists()) {
                if (dir.mkdirs()) return dir;
            }
            return dir;
        } else return null;
    }

    public static File getImagesDir() {
        return getDir("image_cache");
    }

    public static String humanReadableByte(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "");
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
