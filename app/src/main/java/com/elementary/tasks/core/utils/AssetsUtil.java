package com.elementary.tasks.core.utils;

import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.NonNull;

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

public final class AssetsUtil {

    private AssetsUtil() {
    }

    @NonNull
    public static Typeface getTypeface(@NonNull Context context, int code) {
        Typeface typeface;
        if (code == 0) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Black.ttf");
        } else if (code == 1) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-BlackItalic.ttf");
        } else if (code == 2) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");
        } else if (code == 3) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-BoldItalic.ttf");
        } else if (code == 4) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Italic.ttf");
        } else if (code == 5) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
        } else if (code == 6) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-LightItalic.ttf");
        } else if (code == 7) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");
        } else if (code == 8) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-MediumItalic.ttf");
        } else if (code == 9) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
        } else if (code == 10) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");
        } else if (code == 11) {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-ThinItalic.ttf");
        } else {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
        }
        return typeface;
    }

    @NonNull
    public static Typeface getDefaultTypeface(Context context) {
        return getTypeface(context, 9);
    }
}
