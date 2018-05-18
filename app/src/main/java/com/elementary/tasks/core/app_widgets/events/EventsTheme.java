package com.elementary.tasks.core.app_widgets.events;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

import com.elementary.tasks.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2015 Nazar Suhovich
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

public class EventsTheme implements Parcelable {

    private int headerColor;
    private int backgroundColor;
    private int titleColor;
    private int plusIcon;
    private int settingsIcon;
    private int voiceIcon;
    private int itemTextColor;
    private int itemBackgroud;
    private int checkboxColor;
    private String title;
    private int windowColor;
    private int windowTextColor;

    private EventsTheme() {
    }

    public EventsTheme(@ColorRes int headerColor, @ColorRes int backgroundColor, @ColorInt int titleColor,
                       @DrawableRes int plusIcon, @DrawableRes int settingsIcon, @DrawableRes int voiceIcon,
                       @ColorInt int itemTextColor, @ColorRes int itemBackground, int checkboxColor,
                       String title, @ColorRes int windowColor, @ColorInt int windowTextColor) {
        this.headerColor = headerColor;
        this.backgroundColor = backgroundColor;
        this.titleColor = titleColor;
        this.plusIcon = plusIcon;
        this.settingsIcon = settingsIcon;
        this.title = title;
        this.windowColor = windowColor;
        this.windowTextColor = windowTextColor;
        this.voiceIcon = voiceIcon;
        this.itemTextColor = itemTextColor;
        this.itemBackgroud = itemBackground;
        this.checkboxColor = checkboxColor;
    }

    @DrawableRes
    public int getVoiceIcon() {
        return voiceIcon;
    }

    public int getCheckboxColor() {
        return checkboxColor;
    }

    @ColorRes
    public int getItemBackground() {
        return itemBackgroud;
    }

    @ColorInt
    public int getItemTextColor() {
        return itemTextColor;
    }

    @ColorInt
    public int getWindowTextColor() {
        return windowTextColor;
    }

    @ColorRes
    public int getWindowColor() {
        return windowColor;
    }

    @ColorInt
    public int getTitleColor() {
        return titleColor;
    }

    @DrawableRes
    public int getSettingsIcon() {
        return settingsIcon;
    }

    @ColorRes
    public int getBackgroundColor() {
        return backgroundColor;
    }

    @ColorRes
    public int getHeaderColor() {
        return headerColor;
    }

    @DrawableRes
    public int getPlusIcon() {
        return plusIcon;
    }

    public String getTitle() {
        return title;
    }

    private static int getResColor(Context ctx, int res) {
        return ctx.getResources().getColor(res);
    }

    public static List<EventsTheme> getThemes(Context context) {
        List<EventsTheme> list = new ArrayList<>();
        list.clear();
        list.add(new EventsTheme(R.color.indigoPrimary, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, context.getString(R.string.indigo), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.tealPrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, context.getString(R.string.teal), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.limePrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, context.getString(R.string.lime), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.bluePrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, context.getString(R.string.blue), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.material_grey, R.color.material_divider,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, context.getString(R.string.grey), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.greenPrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, context.getString(R.string.green), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.blackPrimary, R.color.blackPrimary,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                getResColor(context, R.color.whitePrimary), R.color.blackPrimary, 1, context.getString(R.string.dark), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.whitePrimary, R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary), R.drawable.ic_add_black_24dp,
                R.drawable.ic_settings, R.drawable.ic_microphone_black,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, context.getString(R.string.white), R.color.material_grey,
                getResColor(context, R.color.whitePrimary)));

        list.add(new EventsTheme(R.color.orangePrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, context.getString(R.string.orange), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.redPrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, context.getString(R.string.red), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.material_grey_dialog, R.color.orangeAccent,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                getResColor(context, R.color.blackPrimary), R.color.material_divider, 0, "Simple Orange", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.simple_transparent_header_color, R.color.simple_transparent_header_color,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                getResColor(context, R.color.whitePrimary), R.color.simple_transparent_header_color, 1, "Transparent Light", R.color.material_grey,
                getResColor(context, R.color.whitePrimary)));

        list.add(new EventsTheme(R.color.simple_transparent_header_color, R.color.simple_transparent_header_color,
                getResColor(context, R.color.blackPrimary), R.drawable.ic_add_black_24dp,
                R.drawable.ic_settings, R.drawable.ic_microphone_black,
                getResColor(context, R.color.blackPrimary), R.color.simple_transparent_header_color, 0, "Transparent Dark", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new EventsTheme(R.color.pinkAccent, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 1, "Simple Pink", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));
        return list;
    }

    public EventsTheme(Parcel in) {
        super();
        readFromParcel(in);
    }

    public final Creator<EventsTheme> CREATOR = new Creator<EventsTheme>() {
        public EventsTheme createFromParcel(Parcel in) {
            return new EventsTheme(in);
        }

        public EventsTheme[] newArray(int size) {
            return new EventsTheme[size];
        }
    };

    public void readFromParcel(Parcel in) {
        title = in.readString();
        backgroundColor = in.readInt();
        headerColor = in.readInt();
        titleColor = in.readInt();
        plusIcon = in.readInt();
        settingsIcon = in.readInt();
        voiceIcon = in.readInt();
        windowColor = in.readInt();
        windowTextColor = in.readInt();
        itemBackgroud = in.readInt();
        itemTextColor = in.readInt();
        checkboxColor = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeInt(headerColor);
        dest.writeInt(titleColor);
        dest.writeInt(plusIcon);
        dest.writeInt(backgroundColor);
        dest.writeInt(settingsIcon);
        dest.writeInt(voiceIcon);
        dest.writeInt(windowColor);
        dest.writeInt(windowTextColor);
        dest.writeInt(itemBackgroud);
        dest.writeInt(itemTextColor);
        dest.writeInt(checkboxColor);
    }
}
