package com.cray.software.justreminder;

import android.support.annotation.ColorRes;

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
public class ColorUtil {

    /**
     * Get accent color by code.
     * @return Color resource
     */
    @ColorRes
    public static int colorAccent(boolean isDark, int code){
        int color;
        if (isDark) {
            switch (code) {
                case 0:
                    color = R.color.indigoAccent;
                    break;
                case 1:
                    color = R.color.amberAccent;
                    break;
                case 2:
                    color = R.color.pinkAccent;
                    break;
                case 3:
                    color = R.color.purpleAccent;
                    break;
                case 4:
                    color = R.color.yellowAccent;
                    break;
                case 5:
                    color = R.color.redAccent;
                    break;
                case 6:
                    color = R.color.redAccent;
                    break;
                case 7:
                    color = R.color.greenAccent;
                    break;
                case 8:
                    color = R.color.purpleDeepAccent;
                    break;
                case 9:
                    color = R.color.blueLightAccent;
                    break;
                case 10:
                    color = R.color.pinkAccent;
                    break;
                case 11:
                    color = R.color.blueAccent;
                    break;
                case 12:
                    color = R.color.greenAccent;
                    break;
                case 13:
                    color = R.color.purpleAccent;
                    break;
                case 14:
                    color = R.color.redAccent;
                    break;
                case 15:
                    color = R.color.pinkAccent;
                    break;
                default:
                    color = R.color.redAccent;
                    break;
            }
        } else {
            switch (code) {
                case 0:
                    color = R.color.indigoAccent;
                    break;
                case 1:
                    color = R.color.amberAccent;
                    break;
                case 2:
                    color = R.color.purpleDeepAccent;
                    break;
                case 3:
                    color = R.color.cyanAccent;
                    break;
                case 4:
                    color = R.color.pinkAccent;
                    break;
                case 5:
                    color = R.color.yellowAccent;
                    break;
                case 6:
                    color = R.color.cyanAccent;
                    break;
                case 7:
                    color = R.color.pinkAccent;
                    break;
                case 8:
                    color = R.color.redAccent;
                    break;
                case 9:
                    color = R.color.cyanAccent;
                    break;
                case 10:
                    color = R.color.redAccent;
                    break;
                case 11:
                    color = R.color.indigoAccent;
                    break;
                case 12:
                    color = R.color.greenLightAccent;
                    break;
                case 13:
                    color = R.color.purpleDeepAccent;
                    break;
                case 14:
                    color = R.color.purpleAccent;
                    break;
                case 15:
                    color = R.color.pinkAccent;
                    break;
                default:
                    color = R.color.yellowAccent;
                    break;
            }
        }
        return color;
    }
}
