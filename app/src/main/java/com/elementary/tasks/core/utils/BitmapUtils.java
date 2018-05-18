package com.elementary.tasks.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import androidx.annotation.NonNull;

import java.io.FileNotFoundException;

/**
 * Copyright 2016 Nazar Suhovich
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public final class BitmapUtils {

    private static final int REQUIRED_SIZE = 350;

    private BitmapUtils() {
    }

    @NonNull
    public static Bitmap decodeUriToBitmap(@NonNull Context context, @NonNull Uri selectedImage) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o);
        int widthTmp = o.outWidth, heightTmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (widthTmp / 2 < REQUIRED_SIZE || heightTmp / 2 < REQUIRED_SIZE) {
                break;
            }
            widthTmp /= 2;
            heightTmp /= 2;
            scale *= 2;
        }
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o2);
    }
}
