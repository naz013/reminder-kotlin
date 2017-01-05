package com.elementary.tasks.core.utils;

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
public class Constants {
    public static final int MAP_NORMAL = 1;
    public static final int MAP_SATELLITE = 2;
    public static final int MAP_HYBRID = 4;
    public static final int MAP_TERRAIN = 3;

    public static final String NONE = "none";
    public static final String DEFAULT = "defaut";

    public static final String INTENT_ID = "item_id";
    public static final String INTENT_POSITION = "item_position";
    public static final String INTENT_DELETE = "item_delete";
    public static final String INTENT_NOTIFICATION = "item_resumed";
    public static final String INTENT_DATE = "item_date";
    public static final String SELECTED_CONTACT_NUMBER = "contact_number";
    public static final String SELECTED_TIME = "call_time";
    public static final String FILE_PICKED = "selected_file";
    public static final String FILE_TYPE = "file_type";

    public static final int REQUEST_CODE_SELECTED_MELODY = 500;

    public final static int REQUEST_CODE_CONTACTS = 101;
    public final static int REQUEST_CODE_THEME = 105;
    public final static int REQUEST_CODE_FONT_STYLE = 106;
    public final static int REQUEST_CODE_SELECTED_RADIUS = 116;
    public final static int REQUEST_CODE_SELECTED_COLOR = 118;
    public final static int REQUEST_CODE_APPLICATION = 117;

    public static final int ACTION_REQUEST_GALLERY = 111;
    public static final int ACTION_REQUEST_CAMERA = 112;
    public static final String SELECTED_COLOR = "selected_color";

    public final static String SELECTED_CONTACT_NAME = "selected_name";
    public final static String SELECTED_FONT_STYLE = "selected_style";
    public final static String SELECTED_RADIUS = "selected_radius";
    public final static String SELECTED_LED_COLOR = "selected_led_color";
    public final static String SELECTED_APPLICATION = "selected_application";

    public final static String DIR_SD = "backup";
    public final static String DIR_IMAGE_CACHE = "img";
    public final static String DIR_PREFS = "preferences";
    public final static String DIR_NOTES_SD = "notes";
    public final static String DIR_GROUP_SD = "groups";
    public final static String DIR_BIRTHDAY_SD = "birthdays";
    public final static String DIR_MAIL_SD = "mail_attachments";
    public final static String DIR_SD_DBX_TMP = "tmp_dropbox";
    public final static String DIR_NOTES_SD_DBX_TMP = "tmp_dropbox_notes";
    public final static String DIR_GROUP_SD_DBX_TMP = "tmp_dropbox_groups";
    public final static String DIR_BIRTHDAY_SD_DBX_TMP = "tmp_dropbox_birthdays";
    public final static String DIR_SD_GDRIVE_TMP = "tmp_gdrive";
    public final static String DIR_NOTES_SD_GDRIVE_TMP = "tmp_gdrive_notes";
    public final static String DIR_GROUP_SD_GDRIVE_TMP = "tmp_gdrive_group";
    public final static String DIR_BIRTHDAY_SD_GDRIVE_TMP = "tmp_gdrive_birthdays";

    public static final String ORDER_DATE_A_Z = "date_az";
    public static final String ORDER_DATE_Z_A = "date_za";
    public static final String ORDER_COMPLETED_A_Z = "completed_az";
    public static final String ORDER_COMPLETED_Z_A = "completed_za";
    public static final String ORDER_DEFAULT = "default";
    public static final String ORDER_NAME_A_Z = "name_az";
    public static final String ORDER_NAME_Z_A = "name_za";
}
