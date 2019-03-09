package com.elementary.tasks

/**
 * Copyright 2018 Nazar Suhovich
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
class Actions {

    object Reminder {
        const val ACTION_SB_HIDE = "com.elementary.tasks.pro.HIDE"
        const val ACTION_SB_SHOW = "com.elementary.tasks.pro.SHOW"
        const val ACTION_RUN = "com.elementary.tasks.pro.reminder.RUN"
        const val ACTION_SHOW_FULL = "com.elementary.tasks.pro.reminder.SHOW_SCREEN"
        const val ACTION_HIDE_SIMPLE = "com.elementary.tasks.pro.reminder.SIMPLE_HIDE"
        const val ACTION_EDIT_EVENT = "com.elementary.tasks.pro.reminder.EVENT_EDIT"
    }

    object Birthday {
        const val ACTION_SB_HIDE = "com.elementary.tasks.pro.birthday.HIDE"
        const val ACTION_SB_SHOW = "com.elementary.tasks.pro.birthday.SHOW"
        const val ACTION_CALL = "com.elementary.tasks.pro.birthday.CALL"
        const val ACTION_SMS = "com.elementary.tasks.pro.birthday.SMS"
        const val ACTION_SHOW_FULL = "com.elementary.tasks.pro.birthday.SHOW_SCREEN"
    }
}
