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

package com.elementary.tasks.core.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.M)
public class Permissions {

    public static final String READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    public static final String GET_ACCOUNTS = Manifest.permission.GET_ACCOUNTS;

    public static final String READ_CALENDAR = Manifest.permission.READ_CALENDAR;
    public static final String WRITE_CALENDAR = Manifest.permission.WRITE_CALENDAR;

    public static final String WRITE_EXTERNAL = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String READ_EXTERNAL = Manifest.permission.READ_EXTERNAL_STORAGE;

    public static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    public static final String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    public static final String CALL_PHONE = Manifest.permission.CALL_PHONE;

    public static final String SEND_SMS = Manifest.permission.SEND_SMS;
    
    public static final String MANAGE_DOCUMENTS = Manifest.permission.MANAGE_DOCUMENTS;
    public static final String READ_CALLS = Manifest.permission.READ_CALL_LOG;
    public static final String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;

    public static boolean checkPermission(Activity a, String... permissions) {
        if (!Module.isMarshmallow()) return true;

        boolean res = true;
        for (String string : permissions) {
            if (a.checkSelfPermission(string) != PackageManager.PERMISSION_GRANTED) {
                res = false;
            }
        }

        return res;
    }

    public static boolean checkPermission(Activity a, String permission) {
        return !Module.isMarshmallow() || a.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(Activity a, int requestCode, String... permission){
        int size = permission.length;
        if (size == 1) {
            a.requestPermissions(permission, requestCode);
        } else {
            String[] array = new String[size];
            System.arraycopy(permission, 0, array, 0, size);
            a.requestPermissions(array, requestCode);
        }
    }

    public static void showInfo(Activity a, String permission){
        a.shouldShowRequestPermissionRationale(permission);
    }
}
