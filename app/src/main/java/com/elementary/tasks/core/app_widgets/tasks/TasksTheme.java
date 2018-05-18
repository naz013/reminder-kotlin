package com.elementary.tasks.core.app_widgets.tasks;

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

public class TasksTheme implements Parcelable {

    private int headerColor;
    private int backgroundColor;
    private int titleColor;
    private int plusIcon;
    private int settingsIcon;
    private int itemTextColor;
    private String title;
    private int windowColor;
    private int windowTextColor;

    private TasksTheme() {
    }

    public TasksTheme(@ColorRes int headerColor, @ColorRes int backgroundColor, @ColorInt int titleColor,
                      @DrawableRes int plusIcon, @DrawableRes int settingsIcon, @ColorInt int itemTextColor,
                      String title, @ColorRes int windowColor, @ColorInt int windowTextColor) {
        this.headerColor = headerColor;
        this.backgroundColor = backgroundColor;
        this.titleColor = titleColor;
        this.plusIcon = plusIcon;
        this.settingsIcon = settingsIcon;
        this.title = title;
        this.windowColor = windowColor;
        this.windowTextColor = windowTextColor;
        this.itemTextColor = itemTextColor;
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

    public static List<TasksTheme> getThemes(Context context) {
        List<TasksTheme> list = new ArrayList<>();
        list.clear();
        list.add(new TasksTheme(R.color.indigoPrimary, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), context.getString(R.string.indigo), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new TasksTheme(R.color.tealPrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), context.getString(R.string.teal), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new TasksTheme(R.color.limePrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), context.getString(R.string.lime), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new TasksTheme(R.color.bluePrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), context.getString(R.string.blue), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new TasksTheme(R.color.material_grey, R.color.material_divider,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, getResColor(context, R.color.blackPrimary), context.getString(R.string.grey), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new TasksTheme(R.color.greenPrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), context.getString(R.string.green), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new TasksTheme(R.color.blackPrimary, R.color.blackPrimary,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), context.getString(R.string.dark), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new TasksTheme(R.color.whitePrimary, R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary), R.drawable.ic_add_black_24dp,
                R.drawable.ic_settings, getResColor(context, R.color.blackPrimary), context.getString(R.string.white), R.color.material_grey,
                getResColor(context, R.color.whitePrimary)));

        list.add(new TasksTheme(R.color.orangePrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), context.getString(R.string.orange), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new TasksTheme(R.color.redPrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), context.getString(R.string.red), R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new TasksTheme(R.color.material_grey_dialog, R.color.orangeAccent,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, getResColor(context, R.color.blackPrimary), "Simple Orange", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new TasksTheme(R.color.simple_transparent_header_color, R.color.simple_transparent_header_color,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), "Transparent Light", R.color.material_grey,
                getResColor(context, R.color.whitePrimary)));

        list.add(new TasksTheme(R.color.simple_transparent_header_color, R.color.simple_transparent_header_color,
                getResColor(context, R.color.blackPrimary), R.drawable.ic_add_black_24dp,
                R.drawable.ic_settings, getResColor(context, R.color.blackPrimary), "Transparent Dark", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new TasksTheme(R.color.pinkAccent, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), "Simple Pink", R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));
        return list;
    }

    public TasksTheme(Parcel in) {
        super();
        readFromParcel(in);
    }

    public final Creator<TasksTheme> CREATOR = new Creator<TasksTheme>() {
        public TasksTheme createFromParcel(Parcel in) {
            return new TasksTheme(in);
        }

        public TasksTheme[] newArray(int size) {
            return new TasksTheme[size];
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
        itemTextColor = in.readInt();
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
        dest.writeInt(itemTextColor);
    }
}
