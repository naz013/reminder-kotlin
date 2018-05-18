package com.elementary.tasks.core.app_widgets.notes;

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

public class NotesTheme implements Parcelable {

    private int headerColor;
    private int backgroundColor;
    private int titleColor;
    private int plusIcon;
    private int settingsIcon;
    private String title;
    private int windowColor;
    private int windowTextColor;

    private NotesTheme() {
    }

    public NotesTheme(@ColorRes int headerColor, @ColorRes int backgroundColor, @ColorInt int titleColor,
                      @DrawableRes int plusIcon, @DrawableRes int settingsIcon,
                      String title, @ColorRes int windowColor, @ColorInt int windowTextColor) {
        this.headerColor = headerColor;
        this.backgroundColor = backgroundColor;
        this.titleColor = titleColor;
        this.plusIcon = plusIcon;
        this.settingsIcon = settingsIcon;
        this.title = title;
        this.windowColor = windowColor;
        this.windowTextColor = windowTextColor;
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

    public static List<NotesTheme> getThemes(Context context) {
        List<NotesTheme> list = new ArrayList<>();
        list.clear();
        list.add(new NotesTheme(R.color.indigoPrimary, R.color.whitePrimary,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, context.getString(R.string.indigo), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new NotesTheme(R.color.tealPrimaryDark, R.color.whitePrimary,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, context.getString(R.string.teal), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new NotesTheme(R.color.limePrimaryDark, R.color.whitePrimary,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, context.getString(R.string.lime), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new NotesTheme(R.color.bluePrimaryDark, R.color.whitePrimary,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, context.getString(R.string.blue), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new NotesTheme(R.color.material_grey, R.color.material_divider,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, context.getString(R.string.grey), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new NotesTheme(R.color.greenPrimaryDark, R.color.whitePrimary,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, context.getString(R.string.green), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new NotesTheme(R.color.blackPrimary, R.color.blackPrimary,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, context.getString(R.string.dark), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new NotesTheme(R.color.whitePrimary, R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary), R.drawable.ic_add_black_24dp,
                R.drawable.ic_settings, context.getString(R.string.white), R.color.material_grey,
                getResColor(context, R.color.whitePrimary)));

        list.add(new NotesTheme(R.color.orangePrimaryDark, R.color.whitePrimary,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, context.getString(R.string.orange), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new NotesTheme(R.color.redPrimaryDark, R.color.whitePrimary,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, context.getString(R.string.red), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new NotesTheme(R.color.material_grey_dialog, R.color.orangeAccent,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, "Simple Orange", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new NotesTheme(R.color.simple_transparent_header_color, R.color.simple_transparent_header_color,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, "Transparent Light", R.color.material_grey,
                getResColor(context, R.color.whitePrimary)));

        list.add(new NotesTheme(R.color.simple_transparent_header_color, R.color.simple_transparent_header_color,
                getResColor(context, R.color.blackPrimary), R.drawable.ic_add_black_24dp,
                R.drawable.ic_settings, "Transparent Dark", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new NotesTheme(R.color.pinkAccent, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, "Simple Pink", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));
        return list;
    }

    public NotesTheme(Parcel in) {
        super();
        readFromParcel(in);
    }

    public final Creator<NotesTheme> CREATOR = new Creator<NotesTheme>() {
        public NotesTheme createFromParcel(Parcel in) {
            return new NotesTheme(in);
        }

        public NotesTheme[] newArray(int size) {
            return new NotesTheme[size];
        }
    };

    public void readFromParcel(Parcel in) {
        title = in.readString();
        backgroundColor = in.readInt();
        headerColor = in.readInt();
        titleColor = in.readInt();
        plusIcon = in.readInt();
        settingsIcon = in.readInt();
        windowColor = in.readInt();
        windowTextColor = in.readInt();
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
        dest.writeInt(windowColor);
        dest.writeInt(windowTextColor);
    }
}
