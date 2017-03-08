package com.elementary.tasks.core.utils;

import android.util.Log;

import com.elementary.tasks.BuildConfig;

/**
 * Copyright 2017 Nazar Suhovich
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

public final class LogUtil {

    private static final String TAG = "Reminder->";
    private static final boolean D = true;

    private LogUtil() {}

    public static void i(String tag, String message) {
        if (D) {
            Log.i(TAG + tag, message);
        }
    }

    public static void e(String tag, String message, Exception e) {
        if (D) {
            Log.e(TAG + tag, message + e.getLocalizedMessage());
        }
    }

    public static void v(String tag, String message) {
        if (D) {
            Log.v(TAG + tag, message);
        }
    }

    public static void d(String tag, String message) {
        if (D) {
            Log.d(TAG + tag, message);
        }
    }
}
