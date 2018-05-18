package com.elementary.tasks.core.utils;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.elementary.tasks.BuildConfig;

import java.io.File;

import timber.log.Timber;

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

public final class UriUtil {

    @NonNull
    public static Uri getUri(Context context, @NonNull String filePath) {
        Timber.d("getUri: %s", BuildConfig.APPLICATION_ID);
        if (Module.isNougat()) {
            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File(filePath));
        } else {
            return Uri.fromFile(new File(filePath));
        }
    }

    @NonNull
    public static Uri getUri(Context context, @NonNull File file) {
        Timber.d("getUri: %s", BuildConfig.APPLICATION_ID);
        if (Module.isNougat()) {
            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }
}
