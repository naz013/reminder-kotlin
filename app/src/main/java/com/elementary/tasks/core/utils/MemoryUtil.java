package com.elementary.tasks.core.utils;

import android.os.Environment;

import java.io.File;
import java.util.Locale;

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

public class MemoryUtil {

    public final static String DIR_SD = "backup";
    public final static String DIR_IMAGE_CACHE = "img";
    public final static String DIR_PREFS = "preferences";
    public final static String DIR_NOTES_SD = "notes";
    public final static String DIR_GROUP_SD = "groups";
    public final static String DIR_BIRTHDAY_SD = "birthdays";
    public final static String DIR_PLACES_SD = "places";
    public final static String DIR_TEMPLATES_SD = "templates";
    public final static String DIR_MAIL_SD = "mail_attachments";
    public final static String DIR_SD_DBX_TMP = "tmp_dropbox";
    public final static String DIR_NOTES_SD_DBX_TMP = "tmp_dropbox_notes";
    public final static String DIR_GROUP_SD_DBX_TMP = "tmp_dropbox_groups";
    public final static String DIR_BIRTHDAY_SD_DBX_TMP = "tmp_dropbox_birthdays";
    public final static String DIR_PLACES_SD_DBX_TMP = "tmp_dropbox_places";
    public final static String DIR_TEMPLATES_SD_DBX_TMP = "tmp_dropbox_templates";
    public final static String DIR_PREFERENCES_SD_DBX_TMP = "tmp_dropbox_preferences";
    public final static String DIR_SD_GDRIVE_TMP = "tmp_gdrive";
    public final static String DIR_NOTES_SD_GDRIVE_TMP = "tmp_gdrive_notes";
    public final static String DIR_GROUP_SD_GDRIVE_TMP = "tmp_gdrive_group";
    public final static String DIR_BIRTHDAY_SD_GDRIVE_TMP = "tmp_gdrive_birthdays";
    public final static String DIR_PLACES_SD_GDRIVE_TMP = "tmp_gdrive_places";
    public final static String DIR_TEMPLATES_SD_GDRIVE_TMP = "tmp_gdrive_templates";
    public final static String DIR_PREFERENCES_SD_GDRIVE_TMP = "tmp_gdrive_preferences";

    public static boolean isSdPresent() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static File getRemindersDir() {
        return getDir(DIR_SD);
    }

    public static File getGroupsDir() {
        return getDir(DIR_GROUP_SD);
    }

    public static File getBirthdaysDir() {
        return getDir(DIR_BIRTHDAY_SD);
    }

    public static File getNotesDir() {
        return getDir(DIR_NOTES_SD);
    }

    public static File getPlacesDir() {
        return getDir(DIR_PLACES_SD);
    }

    public static File getTemplatesDir() {
        return getDir(DIR_TEMPLATES_SD);
    }

    public static File getGooglePlacesDir() {
        return getDir(DIR_PLACES_SD_GDRIVE_TMP);
    }

    public static File getGoogleTemplatesDir() {
        return getDir(DIR_TEMPLATES_SD_GDRIVE_TMP);
    }

    public static File getDropboxPlacesDir() {
        return getDir(DIR_PLACES_SD_DBX_TMP);
    }

    public static File getDropboxTemplatesDir() {
        return getDir(DIR_TEMPLATES_SD_DBX_TMP);
    }

    public static File getDropboxRemindersDir() {
        return getDir(DIR_SD_DBX_TMP);
    }

    public static File getDropboxGroupsDir() {
        return getDir(DIR_GROUP_SD_DBX_TMP);
    }

    public static File getDropboxBirthdaysDir() {
        return getDir(DIR_BIRTHDAY_SD_DBX_TMP);
    }

    public static File getDropboxNotesDir() {
        return getDir(DIR_NOTES_SD_DBX_TMP);
    }

    public static File getGoogleRemindersDir() {
        return getDir(DIR_SD_GDRIVE_TMP);
    }

    public static File getGoogleGroupsDir() {
        return getDir(DIR_GROUP_SD_GDRIVE_TMP);
    }

    public static File getGoogleBirthdaysDir() {
        return getDir(DIR_BIRTHDAY_SD_GDRIVE_TMP);
    }

    public static File getGoogleNotesDir() {
        return getDir(DIR_NOTES_SD_GDRIVE_TMP);
    }

    public static File getMailDir() {
        return getDir(DIR_MAIL_SD);
    }

    public static File getPrefsDir() {
        return getDir(DIR_PREFS);
    }

    public static File getGooglePrefsDir() {
        return getDir(DIR_PREFERENCES_SD_GDRIVE_TMP);
    }

    public static File getDropboxPrefsDir() {
        return getDir(DIR_PREFERENCES_SD_DBX_TMP);
    }

    public static File getImageCacheDir() {
        return getDir(DIR_IMAGE_CACHE);
    }

    public static File getParent() {
        return getDir("");
    }

    private static File getDir(String directory) {
        if (isSdPresent()) {
            File sdPath = Environment.getExternalStorageDirectory();
            File dir = new File(sdPath.toString() + "/JustReminder/" + directory);
            if (!dir.exists() && dir.mkdirs()) {
                return dir;
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
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "");
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
