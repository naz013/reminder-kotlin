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
    public static final int REQUEST_CODE_APPLICATION = 117;

    public static final int ACTION_REQUEST_GALLERY = 111;
    public static final int ACTION_REQUEST_CAMERA = 112;

    public static final String SELECTED_CONTACT_NAME = "selected_name";
    public static final String SELECTED_APPLICATION = "selected_application";

    public static final String ORDER_DATE_A_Z = "date_az";
    public static final String ORDER_DATE_Z_A = "date_za";
    public static final String ORDER_COMPLETED_A_Z = "completed_az";
    public static final String ORDER_COMPLETED_Z_A = "completed_za";
    public static final String ORDER_DEFAULT = "default";
    public static final String ORDER_NAME_A_Z = "name_az";
    public static final String ORDER_NAME_Z_A = "name_za";

    public static final String WEB_URL = "http://future-graph-651.appspot.com/";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TEXT = "task_text";
    public static final String COLUMN_FEATURE_TIME = "tech_int";
    public static final String COLUMN_DELAY = "tech_lint";
    public static final String COLUMN_TECH_VAR = "tech_var";
    public static final String COLUMN_DATE_TIME = "var";
    public static final String COLUMN_CATEGORY = "var2";

    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_COLOR = "color";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_FONT_STYLE = "font_style";
    public static final String COLUMN_FONT_COLOR = "font_color";
    public static final String COLUMN_FONT_SIZE = "font_size";
    public static final String COLUMN_FONT_UNDERLINED = "font_underlined";
    public static final String COLUMN_LINK_ID = "font_crossed";

    public class Contacts {
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_CONTACT_ID = "contact_id";
        public static final String COLUMN_NAME = "display_name";
        public static final String COLUMN_NUMBER = "phone_number";
        public static final String COLUMN_CONTACT_MAIL = "e_mail";
        public static final String COLUMN_BIRTHDATE = "birthday";
        public static final String COLUMN_UUID = "photo_id";
        public static final String COLUMN_DAY = "day";
        public static final String COLUMN_VAR = "var";
        public static final String COLUMN_MONTH = "month";
    }

    private Constants() {
    }
}
