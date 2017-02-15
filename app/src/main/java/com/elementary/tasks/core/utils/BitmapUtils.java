package com.elementary.tasks.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
public class BitmapUtils {

    private static final double MAX_SIZE = 768500;
    private static final String TAG = "BitmapUtils";

    public static Bitmap compressBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            int length = bitmap.getByteCount();
            LogUtil.d(TAG, "compressBitmap: " + length);
            if (length > MAX_SIZE) {
                double scalar = (double) length / MAX_SIZE;
                int coefficient = (int) ((double) 100 / scalar);
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, coefficient, byteStream);
                LogUtil.d(TAG, "compressBitmap: " + byteStream.toByteArray().length);
                return BitmapFactory.decodeStream(new ByteArrayInputStream(byteStream.toByteArray()));
            } else {
                return bitmap;
            }
        }
        return null;
    }

    public static Bitmap decodeUriToBitmap(Context context, Uri selectedImage) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o);
        final int REQUIRED_SIZE = 350;
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o2);
    }
}
