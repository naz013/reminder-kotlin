package com.elementary.tasks;

/**
 * Copyright 2018 Nazar Suhovich
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
public class Actions {

    public class Reminder {
        public static final String ACTION_SB_HIDE = "com.elementary.tasks.pro.HIDE";
        public static final String ACTION_SB_SHOW = "com.elementary.tasks.pro.SHOW";
        public static final String ACTION_RUN = "com.elementary.tasks.pro.reminder.RUN";
        public static final String ACTION_SHOW_FULL = "com.elementary.tasks.pro.reminder.SHOW_SCREEN";
        public static final String ACTION_HIDE_SIMPLE = "com.elementary.tasks.pro.reminder.SIMPLE_HIDE";
    }

    public class Birthday {
        public static final String ACTION_SB_HIDE = "com.elementary.tasks.pro.birthday.HIDE";
        public static final String ACTION_SB_SHOW = "com.elementary.tasks.pro.birthday.SHOW";
        public static final String ACTION_CALL = "com.elementary.tasks.pro.birthday.CALL";
        public static final String ACTION_SMS = "com.elementary.tasks.pro.birthday.SMS";
        public static final String ACTION_SHOW_FULL = "com.elementary.tasks.pro.birthday.SHOW_SCREEN";
    }
}
