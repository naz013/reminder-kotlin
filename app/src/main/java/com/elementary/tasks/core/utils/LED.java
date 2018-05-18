package com.elementary.tasks.core.utils;

import android.content.Context;
import androidx.annotation.NonNull;

import com.elementary.tasks.R;

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

public final class LED {
    public static final int NUM_OF_LEDS = 17;

    public static final int WHITE = 0xffffffff;
    public static final int RED = 0xfff44336;
    public static final int GREEN = 0xff4caf50;
    public static final int BLUE = 0xff2196f3;
    public static final int ORANGE = 0xffff9800;
    public static final int YELLOW = 0xffffeb3b;
    public static final int AMBER = 0xffffc107;
    public static final int PINK = 0xffe91e63;
    public static final int GREEN_LIGHT = 0xff8bc34a;
    public static final int BLUE_LIGHT = 0xff03a9f4;
    public static final int CYAN = 0xff00bcd4;
    public static final int PURPLE = 0xff9c27b0;
    public static final int LIME = 0xffcddc39;
    public static final int INDIGO = 0xff3f51b5;
    public static final int DEEP_PURPLE = 0xff673ab7;
    public static final int DEEP_ORANGE = 0xffff5722;
    public static final int TEAL = 0xff009688;

    private LED() {
    }

    public static int getLED(int code) {
        if (!Module.isPro()) {
            return CYAN;
        }
        int color;
        switch (code) {
            case 0:
                color = WHITE;
                break;
            case 1:
                color = RED;
                break;
            case 2:
                color = GREEN;
                break;
            case 3:
                color = BLUE;
                break;
            case 4:
                color = ORANGE;
                break;
            case 5:
                color = YELLOW;
                break;
            case 6:
                color = PINK;
                break;
            case 7:
                color = GREEN_LIGHT;
                break;
            case 8:
                color = BLUE_LIGHT;
                break;
            case 9:
                color = PURPLE;
                break;
            case 10:
                color = AMBER;
                break;
            case 11:
                color = CYAN;
                break;
            case 12:
                color = LIME;
                break;
            case 13:
                color = INDIGO;
                break;
            case 14:
                color = DEEP_ORANGE;
                break;
            case 15:
                color = DEEP_PURPLE;
                break;
            case 16:
                color = TEAL;
                break;
            default:
                color = BLUE;
                break;
        }
        return color;
    }

    @NonNull
    public static String[] getAllNames(Context context) {
        String[] colors = new String[LED.NUM_OF_LEDS];
        for (int i = 0; i < LED.NUM_OF_LEDS; i++) {
            colors[i] = LED.getTitle(context, i);
        }
        return colors;
    }

    @NonNull
    public static String getTitle(Context context, int code) {
        String color;
        switch (code) {
            case 0:
                color = context.getString(R.string.white);
                break;
            case 1:
                color = context.getString(R.string.red);
                break;
            case 2:
                color = context.getString(R.string.green);
                break;
            case 3:
                color = context.getString(R.string.blue);
                break;
            case 4:
                color = context.getString(R.string.orange);
                break;
            case 5:
                color = context.getString(R.string.yellow);
                break;
            case 6:
                color = context.getString(R.string.pink);
                break;
            case 7:
                color = context.getString(R.string.green_light);
                break;
            case 8:
                color = context.getString(R.string.blue_light);
                break;
            case 9:
                color = context.getString(R.string.purple);
                break;
            case 10:
                color = context.getString(R.string.amber);
                break;
            case 11:
                color = context.getString(R.string.cyan);
                break;
            case 12:
                color = context.getString(R.string.lime);
                break;
            case 13:
                color = context.getString(R.string.indigo);
                break;
            case 14:
                color = context.getString(R.string.dark_orange);
                break;
            case 15:
                color = context.getString(R.string.dark_purple);
                break;
            case 16:
                color = context.getString(R.string.teal);
                break;
            default:
                color = context.getString(R.string.blue);
                break;
        }
        return color;
    }
}
