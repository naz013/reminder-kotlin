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

    public static final int REQUEST_CODE_CONTACTS = 101;
    public static final int REQUEST_CODE_THEME = 105;
    public static final int REQUEST_CODE_FONT_STYLE = 106;
    public static final int REQUEST_CODE_SELECTED_RADIUS = 116;
    public static final int REQUEST_CODE_SELECTED_COLOR = 118;
    public static final int REQUEST_CODE_APPLICATION = 117;

    public static final int ACTION_REQUEST_GALLERY = 111;
    public static final int ACTION_REQUEST_CAMERA = 112;
    public static final String SELECTED_COLOR = "selected_color";

    public static final String SELECTED_CONTACT_NAME = "selected_name";
    public static final String SELECTED_FONT_STYLE = "selected_style";
    public static final String SELECTED_RADIUS = "selected_radius";
    public static final String SELECTED_LED_COLOR = "selected_led_color";
    public static final String SELECTED_APPLICATION = "selected_application";

    public static final String ORDER_DATE_A_Z = "date_az";
    public static final String ORDER_DATE_Z_A = "date_za";
    public static final String ORDER_COMPLETED_A_Z = "completed_az";
    public static final String ORDER_COMPLETED_Z_A = "completed_za";
    public static final String ORDER_DEFAULT = "default";
    public static final String ORDER_NAME_A_Z = "name_az";
    public static final String ORDER_NAME_Z_A = "name_za";
    public static final String INTENT_IMAGE = "intent_image";

    public static final String WEB_URL = "http://future-graph-651.appspot.com/";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TEXT = "task_text";
    public static final String COLUMN_TYPE = "task_type";
    public static final String COLUMN_NUMBER = "call_number";
    public static final String COLUMN_DAY = "day";
    public static final String COLUMN_MONTH = "month";
    public static final String COLUMN_YEAR = "year";
    public static final String COLUMN_HOUR = "hour";
    public static final String COLUMN_MINUTE = "minute";
    public static final String COLUMN_REMIND_TIME = "remind_time";
    public static final String COLUMN_REPEAT = "repeat";
    public static final String COLUMN_REMINDERS_COUNT = "reminders_count";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_FEATURE_TIME = "tech_int";
    public static final String COLUMN_DELAY = "tech_lint";
    public static final String COLUMN_TECH_VAR = "tech_var";
    public static final String COLUMN_WEEKDAYS = "tech_lvar";
    public static final String COLUMN_EXPORT_TO_CALENDAR = "export_calendar";
    public static final String COLUMN_CUSTOM_MELODY = "custom_melody";
    public static final String COLUMN_CUSTOM_RADIUS = "custom_radius";
    public static final String COLUMN_ARCHIVED = "archived";
    public static final String COLUMN_DATE_TIME = "var";
    public static final String COLUMN_CATEGORY = "var2";
    public static final String COLUMN_LED_COLOR = "int";
    public static final String COLUMN_SYNC_CODE = "int2";
    public static final String COLUMN_VIBRATION = "vibration";
    public static final String COLUMN_AUTO_ACTION = "action_";
    public static final String COLUMN_WAKE_SCREEN = "awake_screen";
    public static final String COLUMN_UNLOCK_DEVICE = "unlock_device";
    public static final String COLUMN_NOTIFICATION_REPEAT = "notification_repeat";
    public static final String COLUMN_VOICE = "voice_notification";
    public static final String COLUMN_REPEAT_LIMIT = "column_extra";
    public static final String COLUMN_EXTRA_1 = "column_extra_1";
    public static final String COLUMN_EXTRA_3 = "column_extra_3";

    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_COLOR = "color";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_UUID = "uuid";
    public static final String COLUMN_ENCRYPTED = "tech";
    public static final String COLUMN_FONT_STYLE = "font_style";
    public static final String COLUMN_FONT_COLOR = "font_color";
    public static final String COLUMN_FONT_SIZE = "font_size";
    public static final String COLUMN_FONT_UNDERLINED = "font_underlined";
    public static final String COLUMN_LINK_ID = "font_crossed";

    private Constants() {}
}
