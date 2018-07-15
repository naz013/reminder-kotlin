package com.elementary.tasks.core.utils

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class Constants private constructor() {

    companion object {

        val MAP_NORMAL = 1
        val MAP_SATELLITE = 2
        val MAP_HYBRID = 4
        val MAP_TERRAIN = 3

        val NONE = "none"
        val DEFAULT = "defaut"

        val INTENT_ID = "item_id"
        val INTENT_POSITION = "item_position"
        val INTENT_DELETE = "item_delete"
        val INTENT_NOTIFICATION = "item_resumed"
        val INTENT_DATE = "item_date"
        val SELECTED_CONTACT_NUMBER = "contact_number"
        val SELECTED_TIME = "call_time"
        val FILE_PICKED = "selected_file"
        val FILE_TYPE = "file_type"

        val REQUEST_CODE_SELECTED_MELODY = 500

        val REQUEST_CODE_CONTACTS = 101
        val REQUEST_CODE_APPLICATION = 117

        val ACTION_REQUEST_GALLERY = 111
        val ACTION_REQUEST_CAMERA = 112

        val SELECTED_CONTACT_NAME = "selected_name"
        val SELECTED_APPLICATION = "selected_application"

        val ORDER_DATE_A_Z = "date_az"
        val ORDER_DATE_Z_A = "date_za"
        val ORDER_COMPLETED_A_Z = "completed_az"
        val ORDER_COMPLETED_Z_A = "completed_za"
        val ORDER_DEFAULT = "default"
        val ORDER_NAME_A_Z = "name_az"
        val ORDER_NAME_Z_A = "name_za"

        val WEB_URL = "http://future-graph-651.appspot.com/"
    }
}
