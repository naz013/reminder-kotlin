package com.elementary.tasks.core.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;

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
public final class BitmapUtils {

    private static final double MAX_SIZE = 768500;
    private static final int REQUIRED_SIZE = 350;
    private static final String TAG = "BitmapUtils";

    private BitmapUtils() {}

    public static Bitmap getBitmap(Context context, @DrawableRes int drawableResId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawableCompat) {
            return getBitmap((VectorDrawableCompat) drawable);
        } else if (drawable instanceof VectorDrawable) {
            return getBitmap((VectorDrawable) drawable);
        } else {
            throw new IllegalArgumentException("Unsupported drawable type");
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    private static Bitmap getBitmap(VectorDrawableCompat vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

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
