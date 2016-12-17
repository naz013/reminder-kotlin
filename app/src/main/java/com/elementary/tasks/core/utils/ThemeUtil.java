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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StyleRes;

import com.elementary.tasks.R;

import java.util.Calendar;

public class ThemeUtil {

    public static final int THEME_AUTO = 0;
    public static final int THEME_WHITE = 1;
    public static final int THEME_DARK = 2;
    public static final int NUM_OF_MARKERS = 16;

    private Context mContext;
    private static ThemeUtil instance;

    private ThemeUtil(Context context) {
        this.mContext = context;
    }

    public static ThemeUtil getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeUtil(context);
        }
        return instance;
    }

    @ColorInt
    public int getColor(@ColorRes int color) {
        return ViewUtils.getColor(mContext, color);
    }

    @ColorRes
    public int colorPrimary() {
        return colorPrimary(Prefs.getInstance(mContext).getAppThemeColor());
    }

    @ColorRes
    public int colorAccent() {
        return colorAccent(Prefs.getInstance(mContext).getAppThemeColor());
    }

    @ColorRes
    public int colorAccent(int code) {
        int color;
        if (isDark()) {
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
                default:
                    if (Module.isPro()) {
                        switch (code) {
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
                    } else color = R.color.redAccent;
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
                default:
                    if (Module.isPro()) {
                        switch (code) {
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
                    } else color = R.color.yellowAccent;
                    break;
            }
        }
        return color;
    }

    public boolean isDark() {
        Prefs prefs = Prefs.getInstance(mContext);
        int appTheme = prefs.getAppTheme();
        boolean isDark = appTheme == THEME_DARK;
        if (appTheme == THEME_AUTO) {
            Calendar calendar = Calendar.getInstance();
            long mTime = System.currentTimeMillis();
            calendar.setTimeInMillis(mTime);
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            calendar.set(Calendar.MINUTE, 0);
            long min = calendar.getTimeInMillis();
            calendar.set(Calendar.HOUR_OF_DAY, 19);
            long max = calendar.getTimeInMillis();
            return !(mTime >= min && mTime <= max);
        }
        return isDark;
    }

    @StyleRes
    public int getStyle() {
        int id;
        int loadedColor = Prefs.getInstance(mContext).getAppThemeColor();
        if (isDark()) {
            switch (loadedColor) {
                case 0:
                    id = R.style.HomeDark_Red;
                    break;
                case 1:
                    id = R.style.HomeDark_Purple;
                    break;
                case 2:
                    id = R.style.HomeDark_LightGreen;
                    break;
                case 3:
                    id = R.style.HomeDark_Green;
                    break;
                case 4:
                    id = R.style.HomeDark_LightBlue;
                    break;
                case 5:
                    id = R.style.HomeDark_Blue;
                    break;
                case 6:
                    id = R.style.HomeDark_Yellow;
                    break;
                case 7:
                    id = R.style.HomeDark_Orange;
                    break;
                case 8:
                    id = R.style.HomeDark_Cyan;
                    break;
                case 9:
                    id = R.style.HomeDark_Pink;
                    break;
                case 10:
                    id = R.style.HomeDark_Teal;
                    break;
                case 11:
                    id = R.style.HomeDark_Amber;
                    break;
                default:
                    if (Module.isPro()) {
                        switch (loadedColor) {
                            case 12:
                                id = R.style.HomeDark_DeepPurple;
                                break;
                            case 13:
                                id = R.style.HomeDark_DeepOrange;
                                break;
                            case 14:
                                id = R.style.HomeDark_Lime;
                                break;
                            case 15:
                                id = R.style.HomeDark_Indigo;
                                break;
                            default:
                                id = R.style.HomeDark_Blue;
                                break;
                        }
                    } else id = R.style.HomeDark_Blue;
                    break;
            }
        } else {
            switch (loadedColor) {
                case 0:
                    id = R.style.HomeWhite_Red;
                    break;
                case 1:
                    id = R.style.HomeWhite_Purple;
                    break;
                case 2:
                    id = R.style.HomeWhite_LightGreen;
                    break;
                case 3:
                    id = R.style.HomeWhite_Green;
                    break;
                case 4:
                    id = R.style.HomeWhite_LightBlue;
                    break;
                case 5:
                    id = R.style.HomeWhite_Blue;
                    break;
                case 6:
                    id = R.style.HomeWhite_Yellow;
                    break;
                case 7:
                    id = R.style.HomeWhite_Orange;
                    break;
                case 8:
                    id = R.style.HomeWhite_Cyan;
                    break;
                case 9:
                    id = R.style.HomeWhite_Pink;
                    break;
                case 10:
                    id = R.style.HomeWhite_Teal;
                    break;
                case 11:
                    id = R.style.HomeWhite_Amber;
                    break;
                default:
                    if (Module.isPro()) {
                        switch (loadedColor) {
                            case 12:
                                id = R.style.HomeWhite_DeepPurple;
                                break;
                            case 13:
                                id = R.style.HomeWhite_DeepOrange;
                                break;
                            case 14:
                                id = R.style.HomeWhite_Lime;
                                break;
                            case 15:
                                id = R.style.HomeWhite_Indigo;
                                break;
                            default:
                                id = R.style.HomeWhite_Blue;
                                break;
                        }
                    } else id = R.style.HomeWhite_Blue;
                    break;
            }
        }
        return id;
    }

    @ColorRes
    public int colorBirthdayCalendar() {
        return colorPrimary(Prefs.getInstance(mContext).getBirthdayColor());
    }

    @ColorRes
    public int colorPrimary(int code) {
        int color;
        switch (code) {
            case 0:
                color = R.color.redPrimary;
                break;
            case 1:
                color = R.color.purplePrimary;
                break;
            case 2:
                color = R.color.greenLightPrimary;
                break;
            case 3:
                color = R.color.greenPrimary;
                break;
            case 4:
                color = R.color.blueLightPrimary;
                break;
            case 5:
                color = R.color.bluePrimary;
                break;
            case 6:
                color = R.color.yellowPrimary;
                break;
            case 7:
                color = R.color.orangePrimary;
                break;
            case 8:
                color = R.color.cyanPrimary;
                break;
            case 9:
                color = R.color.pinkPrimary;
                break;
            case 10:
                color = R.color.tealPrimary;
                break;
            case 11:
                color = R.color.amberPrimary;
                break;
            default:
                if (Module.isPro()) {
                    switch (code) {
                        case 12:
                            color = R.color.purpleDeepPrimary;
                            break;
                        case 13:
                            color = R.color.orangeDeepPrimary;
                            break;
                        case 14:
                            color = R.color.limePrimary;
                            break;
                        case 15:
                            color = R.color.indigoPrimary;
                            break;
                        default:
                            color = R.color.cyanPrimary;
                            break;
                    }
                } else {
                    color = R.color.cyanPrimary;
                }
                break;
        }
        return color;
    }

    @ColorRes
    public int colorReminderCalendar() {
        return colorPrimary(Prefs.getInstance(mContext).getReminderColor());
    }

    @ColorRes
    public int colorCurrentCalendar() {
        return colorPrimary(Prefs.getInstance(mContext).getTodayColor());
    }

    @DrawableRes
    public int getIndicator(int color) {
        int drawable;
        switch (color) {
            case 0:
                drawable = R.drawable.drawable_red;
                break;
            case 1:
                drawable = R.drawable.drawable_purple;
                break;
            case 2:
                drawable = R.drawable.drawable_green_light;
                break;
            case 3:
                drawable = R.drawable.drawable_green;
                break;
            case 4:
                drawable = R.drawable.drawable_blue_light;
                break;
            case 5:
                drawable = R.drawable.drawable_blue;
                break;
            case 6:
                drawable = R.drawable.drawable_yellow;
                break;
            case 7:
                drawable = R.drawable.drawable_orange;
                break;
            case 8:
                drawable = R.drawable.drawable_cyan;
                break;
            case 9:
                drawable = R.drawable.drawable_pink;
                break;
            case 10:
                drawable = R.drawable.drawable_teal;
                break;
            case 11:
                drawable = R.drawable.drawable_amber;
                break;
            case 12:
                drawable = R.drawable.drawable_deep_purple;
                break;
            case 13:
                drawable = R.drawable.drawable_deep_orange;
                break;
            case 14:
                drawable = R.drawable.drawable_lime;
                break;
            case 15:
                drawable = R.drawable.drawable_indigo;
                break;
            default:
                drawable = R.drawable.drawable_cyan;
                break;
        }
        return drawable;
    }

    private Drawable getDrawable(@DrawableRes int i) {
        return ViewUtils.getDrawable(mContext, i);
    }

    public Drawable toggleDrawable() {
        int loadedColor = Prefs.getInstance(mContext).getAppThemeColor();
        int color;
        switch (loadedColor) {
            case 0:
                color = R.drawable.toggle_red;
                break;
            case 1:
                color = R.drawable.toggle_purple;
                break;
            case 2:
                color = R.drawable.toggle_green_light;
                break;
            case 3:
                color = R.drawable.toggle_green;
                break;
            case 4:
                color = R.drawable.toggle_blue_light;
                break;
            case 5:
                color = R.drawable.toggle_blue;
                break;
            case 6:
                color = R.drawable.toggle_yellow;
                break;
            case 7:
                color = R.drawable.toggle_orange;
                break;
            case 8:
                color = R.drawable.toggle_cyan;
                break;
            case 9:
                color = R.drawable.toggle_pink;
                break;
            case 10:
                color = R.drawable.toggle_teal;
                break;
            case 11:
                color = R.drawable.toggle_amber;
                break;
            default:
                if (Module.isPro()) {
                    switch (loadedColor) {
                        case 12:
                            color = R.drawable.toggle_deep_purple;
                            break;
                        case 13:
                            color = R.drawable.toggle_deep_orange;
                            break;
                        case 14:
                            color = R.drawable.toggle_lime;
                            break;
                        case 15:
                            color = R.drawable.toggle_indigo;
                            break;
                        default:
                            color = R.drawable.toggle_cyan;
                            break;
                    }
                } else color = R.drawable.toggle_cyan;
                break;
        }
        return getDrawable(color);
    }

    @ColorRes
    public int colorPrimaryDark(int code) {
        int color;
        switch (code) {
            case 0:
                color = R.color.redPrimaryDark;
                break;
            case 1:
                color = R.color.purplePrimaryDark;
                break;
            case 2:
                color = R.color.greenLightPrimaryDark;
                break;
            case 3:
                color = R.color.greenPrimaryDark;
                break;
            case 4:
                color = R.color.blueLightPrimaryDark;
                break;
            case 5:
                color = R.color.bluePrimaryDark;
                break;
            case 6:
                color = R.color.yellowPrimaryDark;
                break;
            case 7:
                color = R.color.orangePrimaryDark;
                break;
            case 8:
                color = R.color.cyanPrimaryDark;
                break;
            case 9:
                color = R.color.pinkPrimaryDark;
                break;
            case 10:
                color = R.color.tealPrimaryDark;
                break;
            case 11:
                color = R.color.amberPrimaryDark;
                break;
            default:
                if (Module.isPro()) {
                    switch (code) {
                        case 12:
                            color = R.color.purpleDeepPrimaryDark;
                            break;
                        case 13:
                            color = R.color.orangeDeepPrimaryDark;
                            break;
                        case 14:
                            color = R.color.limePrimaryDark;
                            break;
                        case 15:
                            color = R.color.indigoPrimaryDark;
                            break;
                        default:
                            color = R.color.cyanPrimaryDark;
                            break;
                    }
                } else color = R.color.cyanPrimaryDark;
                break;
        }
        return color;
    }

    @ColorRes
    public int colorPrimaryDark() {
        int loadedColor = Prefs.getInstance(mContext).getAppThemeColor();
        return colorPrimaryDark(loadedColor);
    }

    @ColorInt
    public int getSpinnerStyle() {
        int color;
        if (isDark()) color = R.color.material_grey;
        else color = R.color.whitePrimary;
        return getColor(color);
    }

    @StyleRes
    public int getDialogStyle() {
        int id;
        int loadedColor = Prefs.getInstance(mContext).getAppThemeColor();
        if (isDark()) {
            switch (loadedColor) {
                case 0:
                    id = R.style.HomeDarkDialog_Red;
                    break;
                case 1:
                    id = R.style.HomeDarkDialog_Purple;
                    break;
                case 2:
                    id = R.style.HomeDarkDialog_LightGreen;
                    break;
                case 3:
                    id = R.style.HomeDarkDialog_Green;
                    break;
                case 4:
                    id = R.style.HomeDarkDialog_LightBlue;
                    break;
                case 5:
                    id = R.style.HomeDarkDialog_Blue;
                    break;
                case 6:
                    id = R.style.HomeDarkDialog_Yellow;
                    break;
                case 7:
                    id = R.style.HomeDarkDialog_Orange;
                    break;
                case 8:
                    id = R.style.HomeDarkDialog_Cyan;
                    break;
                case 9:
                    id = R.style.HomeDarkDialog_Pink;
                    break;
                case 10:
                    id = R.style.HomeDarkDialog_Teal;
                    break;
                case 11:
                    id = R.style.HomeDarkDialog_Amber;
                    break;
                default:
                    if (Module.isPro()) {
                        switch (loadedColor) {
                            case 12:
                                id = R.style.HomeDarkDialog_DeepPurple;
                                break;
                            case 13:
                                id = R.style.HomeDarkDialog_DeepOrange;
                                break;
                            case 14:
                                id = R.style.HomeDarkDialog_Lime;
                                break;
                            case 15:
                                id = R.style.HomeDarkDialog_Indigo;
                                break;
                            default:
                                id = R.style.HomeDarkDialog_Blue;
                                break;
                        }
                    } else id = R.style.HomeDarkDialog_Blue;
                    break;
            }
        } else {
            switch (loadedColor) {
                case 0:
                    id = R.style.HomeWhiteDialog_Red;
                    break;
                case 1:
                    id = R.style.HomeWhiteDialog_Purple;
                    break;
                case 2:
                    id = R.style.HomeWhiteDialog_LightGreen;
                    break;
                case 3:
                    id = R.style.HomeWhiteDialog_Green;
                    break;
                case 4:
                    id = R.style.HomeWhiteDialog_LightBlue;
                    break;
                case 5:
                    id = R.style.HomeWhiteDialog_Blue;
                    break;
                case 6:
                    id = R.style.HomeWhiteDialog_Yellow;
                    break;
                case 7:
                    id = R.style.HomeWhiteDialog_Orange;
                    break;
                case 8:
                    id = R.style.HomeWhiteDialog_Cyan;
                    break;
                case 9:
                    id = R.style.HomeWhiteDialog_Pink;
                    break;
                case 10:
                    id = R.style.HomeWhiteDialog_Teal;
                    break;
                case 11:
                    id = R.style.HomeWhiteDialog_Amber;
                    break;
                default:
                    if (Module.isPro()) {
                        switch (loadedColor) {
                            case 12:
                                id = R.style.HomeWhiteDialog_DeepPurple;
                                break;
                            case 13:
                                id = R.style.HomeWhiteDialog_DeepOrange;
                                break;
                            case 14:
                                id = R.style.HomeWhiteDialog_Lime;
                                break;
                            case 15:
                                id = R.style.HomeWhiteDialog_Indigo;
                                break;
                            default:
                                id = R.style.HomeWhiteDialog_Blue;
                                break;
                        }
                    } else id = R.style.HomeWhiteDialog_Blue;
                    break;
            }
        }
        return id;
    }

    @StyleRes
    public int getFullscreenStyle() {
        int id;
        if (isDark()) id = R.style.HomeDarkFullscreen;
        else id = R.style.HomeWhiteFullscreen;
        return id;
    }

    @StyleRes
    public int getTransparentStyle() {
        int id;
        if (isDark()) id = R.style.HomeDarkTranslucent;
        else id = R.style.HomeWhiteTranslucent;
        return id;
    }

    @ColorInt
    public int getBackgroundStyle() {
        int id;
        if (isDark()) id = getColor(R.color.material_grey);
        else id = getColor(R.color.material_white);
        return id;
    }

    @ColorInt
    public int getStatusBarStyle() {
        if (isDark()) return getColor(R.color.material_grey);
        else return getColor(colorPrimaryDark());
    }

    @ColorInt
    public int getCardStyle() {
        int color;
        if (isDark()) color = getColor(R.color.grey_x);
        else color = getColor(R.color.whitePrimary);
        return color;
    }

    public int getRequestOrientation() {
        return Prefs.getInstance(mContext).getScreenOrientation();
    }

    @ColorRes
    public int[] getMarkerRadiusStyle() {
        return getMarkerRadiusStyle(Prefs.getInstance(mContext).getMarkerStyle());
    }

    @ColorRes
    public int[] getMarkerRadiusStyle(int color) {
        int fillColor;
        int strokeColor;
        switch (color) {
            case 0:
                fillColor = R.color.red50;
                strokeColor = R.color.redPrimaryDark;
                break;
            case 1:
                fillColor = R.color.purple50;
                strokeColor = R.color.purplePrimaryDark;
                break;
            case 2:
                fillColor = R.color.greenLight50;
                strokeColor = R.color.greenLightPrimaryDark;
                break;
            case 3:
                fillColor = R.color.green50;
                strokeColor = R.color.greenPrimaryDark;
                break;
            case 4:
                fillColor = R.color.blueLight50;
                strokeColor = R.color.blueLightPrimaryDark;
                break;
            case 5:
                fillColor = R.color.blue50;
                strokeColor = R.color.bluePrimaryDark;
                break;
            case 6:
                fillColor = R.color.yellow50;
                strokeColor = R.color.yellowPrimaryDark;
                break;
            case 7:
                fillColor = R.color.orange50;
                strokeColor = R.color.orangePrimaryDark;
                break;
            case 8:
                fillColor = R.color.cyan50;
                strokeColor = R.color.cyanPrimaryDark;
                break;
            case 9:
                fillColor = R.color.pink50;
                strokeColor = R.color.pinkPrimaryDark;
                break;
            case 10:
                fillColor = R.color.teal50;
                strokeColor = R.color.tealPrimaryDark;
                break;
            case 11:
                fillColor = R.color.amber50;
                strokeColor = R.color.amberPrimaryDark;
                break;
            case 12:
                fillColor = R.color.purpleDeep50;
                strokeColor = R.color.purpleDeepPrimaryDark;
                break;
            case 13:
                fillColor = R.color.orangeDeep50;
                strokeColor = R.color.orangeDeepPrimaryDark;
                break;
            case 14:
                fillColor = R.color.indigo50;
                strokeColor = R.color.indigoPrimaryDark;
                break;
            case 15:
                fillColor = R.color.lime50;
                strokeColor = R.color.limePrimaryDark;
                break;
            default:
                fillColor = R.color.blue50;
                strokeColor = R.color.bluePrimaryDark;
                break;
        }
        return new int[]{fillColor, strokeColor};
    }

    @DrawableRes
    public int getMarkerStyle() {
        if (Module.isPro()) {
            int loaded = Prefs.getInstance(mContext).getMarkerStyle();
            return getMarkerStyle(loaded);
        } else {
            return R.drawable.ic_location_pointer_blue;
        }
    }

    @DrawableRes
    public int getMarkerStyle(int code) {
        int color;
        switch (code) {
            case 0:
                color = R.drawable.ic_location_pointer_red;
                break;
            case 1:
                color = R.drawable.ic_location_pointer_purple;
                break;
            case 2:
                color = R.drawable.ic_location_pointer_green_light;
                break;
            case 3:
                color = R.drawable.ic_location_pointer_green;
                break;
            case 4:
                color = R.drawable.ic_location_pointer_blue_light;
                break;
            case 5:
                color = R.drawable.ic_location_pointer_blue;
                break;
            case 6:
                color = R.drawable.ic_location_pointer_yellow;
                break;
            case 7:
                color = R.drawable.ic_location_pointer_orange;
                break;
            case 8:
                color = R.drawable.ic_location_pointer_cyan;
                break;
            case 9:
                color = R.drawable.ic_location_pointer_pink;
                break;
            case 10:
                color = R.drawable.ic_location_pointer_teal;
                break;
            case 11:
                color = R.drawable.ic_location_pointer_amber;
                break;
            case 12:
                color = R.drawable.ic_location_pointer_purple_deep;
                break;
            case 13:
                color = R.drawable.ic_location_pointer_orange_deep;
                break;
            case 14:
                color = R.drawable.ic_location_pointer_indigo;
                break;
            case 15:
                color = R.drawable.ic_location_pointer_lime;
                break;
            default:
                color = R.drawable.ic_location_pointer_blue;
                break;
        }
        return color;
    }

    @DrawableRes
    public int getCategoryIndicator(int code) {
        int color;
        switch (code) {
            case 0:
                color = R.drawable.circle_red;
                break;
            case 1:
                color = R.drawable.circle_purple;
                break;
            case 2:
                color = R.drawable.circle_green_light;
                break;
            case 3:
                color = R.drawable.circle_green;
                break;
            case 4:
                color = R.drawable.circle_blue_light;
                break;
            case 5:
                color = R.drawable.circle_blue;
                break;
            case 6:
                color = R.drawable.circle_yellow;
                break;
            case 7:
                color = R.drawable.circle_orange;
                break;
            case 8:
                color = R.drawable.circle_cyan;
                break;
            case 9:
                color = R.drawable.circle_pink;
                break;
            case 10:
                color = R.drawable.circle_teal;
                break;
            case 11:
                color = R.drawable.circle_amber;
                break;
            default:
                if (Module.isPro()) {
                    switch (code) {
                        case 12:
                            color = R.drawable.circle_deep_purple;
                            break;
                        case 13:
                            color = R.drawable.circle_deep_orange;
                            break;
                        case 14:
                            color = R.drawable.circle_lime;
                            break;
                        case 15:
                            color = R.drawable.circle_indigo;
                            break;
                        default:
                            color = R.drawable.circle_blue;
                            break;
                    }
                } else color = R.drawable.circle_blue;
                break;
        }
        return color;
    }

    @ColorRes
    public int getCategoryColor(int code) {
        return colorPrimary(code);
    }

    @ColorInt
    public int getNoteColor(int code) {
        return getColor(colorPrimary(code));
    }

    @ColorInt
    public int getNoteDarkColor(int code) {
        return getColor(colorPrimaryDark(code));
    }

    @ColorInt
    public int getNoteLightColor(int code) {
        return getColor(colorAccent(code));
    }
}
