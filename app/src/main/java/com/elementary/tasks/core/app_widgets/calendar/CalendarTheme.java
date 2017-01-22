package com.elementary.tasks.core.app_widgets.calendar;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.elementary.tasks.R;

import java.util.ArrayList;

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

public class CalendarTheme implements Parcelable {

    private int itemTextColor, widgetBgColor, headerColor, borderColor, titleColor, rowColor;
    private int leftArrow, rightArrow, iconPlus, iconVoice, iconSettings;
    private int currentMark, birthdayMark, reminderMark;
    private String title;
    private int windowColor;
    private int windowTextColor;

    private CalendarTheme() {}

    public CalendarTheme(int itemTextColor, int widgetBgColor, int headerColor, int borderColor,
                         int titleColor, int rowColor, int leftArrow, int rightArrow, int iconPlus, int iconVoice,
                         int iconSettings, String title, int currentMark, int birthdayMark, int reminderMark,
                         int windowColor, int windowTextColor){
        this.itemTextColor = itemTextColor;
        this.widgetBgColor = widgetBgColor;
        this.headerColor = headerColor;
        this.borderColor = borderColor;
        this.titleColor = titleColor;
        this.leftArrow = leftArrow;
        this.rightArrow = rightArrow;
        this.iconPlus = iconPlus;
        this.iconVoice = iconVoice;
        this.iconSettings = iconSettings;
        this.rowColor = rowColor;
        this.title = title;
        this.currentMark = currentMark;
        this.birthdayMark = birthdayMark;
        this.reminderMark = reminderMark;
        this.windowColor = windowColor;
        this.windowTextColor = windowTextColor;
    }

    public int getCurrentMark(){
        return currentMark;
    }

    public int getBirthdayMark(){
        return birthdayMark;
    }

    public int getReminderMark(){
        return reminderMark;
    }

    public void setItemTextColor(int itemTextColor){
        this.itemTextColor = itemTextColor;
    }

    public int getItemTextColor(){
        return itemTextColor;
    }

    public int getRowColor(){
        return rowColor;
    }

    public int getWidgetBgColor(){
        return widgetBgColor;
    }

    public int getHeaderColor(){
        return headerColor;
    }

    public int getBorderColor(){
        return borderColor;
    }

    public int getTitleColor(){
        return titleColor;
    }

    public int getLeftArrow(){
        return leftArrow;
    }

    public int getRightArrow(){
        return rightArrow;
    }

    public int getIconPlus(){
        return iconPlus;
    }

    public int getIconVoice(){
        return iconVoice;
    }

    public int getIconSettings(){
        return iconSettings;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public int getWindowColor() {
        return windowColor;
    }

    public int getWindowTextColor() {
        return windowTextColor;
    }

    public String getTitle(){
        return title;
    }

    private static int getResColor(Context ctx, int res){
        return ctx.getResources().getColor(res);
    }

    public static ArrayList<CalendarTheme> getThemes(Context context) {
        ArrayList<CalendarTheme> list = new ArrayList<>();
        list.clear();
        list.add(new CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.whitePrimary,
                R.color.tealPrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.color.whitePrimary,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Teal", 0, 0, 0, R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.whitePrimary,
                R.color.indigoPrimary, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.color.whitePrimary,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Indigo", 0, 0, 0, R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.whitePrimary,
                R.color.limePrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.color.whitePrimary,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Lime", 0, 0, 0, R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.whitePrimary,
                R.color.bluePrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.color.whitePrimary,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Blue", 0, 0, 0, R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new CalendarTheme(getResColor(context, R.color.whitePrimary), R.color.material_divider,
                R.color.material_grey, R.color.material_divider,
                getResColor(context, R.color.whitePrimary), R.color.material_divider,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Gray", 0, 0, 0, R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.whitePrimary,
                R.color.greenPrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.color.whitePrimary,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Green", 0, 0, 0, R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new CalendarTheme(getResColor(context, R.color.whitePrimary), R.color.blackPrimary,
                R.color.blackPrimary, R.color.blackPrimary,
                getResColor(context, R.color.whitePrimary), R.color.blackPrimary,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Dark", 0, 0, 0, R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.whitePrimary,
                R.color.whitePrimary, R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary), R.color.whitePrimary,
                R.drawable.simple_left_arrow_black, R.drawable.simple_right_arrow_black,
                R.drawable.simple_plus_button_black, R.drawable.simple_voice_button_black,
                R.drawable.simple_settings_button_black, "White", 0, 0, 0, R.color.material_grey,
                getResColor(context, R.color.whitePrimary)));

        list.add(new CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.whitePrimary,
                R.color.orangePrimaryDark, R.color.whitePrimary,
                getResColor(context, R.color.whitePrimary), R.color.whitePrimary,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Orange", 0, 0, 0, R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.whitePrimary,
                R.color.redPrimaryDark, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.color.whitePrimary,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Red", 0, 0, 0, R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.orangeAccent,
                R.color.material_grey_dialog, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.color.whitePrimary,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Simple Black", 0, 0, 0, R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new CalendarTheme(getResColor(context, R.color.whitePrimary), R.color.simple_transparent_widget_color,
                R.color.simple_transparent_header_color, R.color.simple_transparent_border_color,
                getResColor(context, R.color.whitePrimary), R.color.simple_transparent_row_color,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Transparent Light", 0, 0, 0, R.color.material_grey,
                getResColor(context, R.color.whitePrimary)));

        list.add(new CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.simple_transparent_widget_color,
                R.color.simple_transparent_header_color, R.color.simple_transparent_border_color,
                getResColor(context, R.color.blackPrimary), R.color.simple_transparent_row_color,
                R.drawable.simple_left_arrow_black, R.drawable.simple_right_arrow_black,
                R.drawable.simple_plus_button_black, R.drawable.simple_voice_button_black,
                R.drawable.simple_settings_button_black, "Transparent Dark", 0, 0, 0, R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));

        list.add(new CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.orangeAccent,
                R.color.cyanPrimary, R.color.material_grey,
                getResColor(context, R.color.whitePrimary), R.color.orangeAccent,
                R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                R.drawable.simple_settings_button, "Simple Brown", 0, 0, 0, R.color.whitePrimary,
                getResColor(context, R.color.blackPrimary)));
        return list;
    }

    public CalendarTheme(Parcel in) {
        super();
        readFromParcel(in);
    }

    public final Creator<CalendarTheme> CREATOR = new Creator<CalendarTheme>() {
        public CalendarTheme createFromParcel(Parcel in) {
            return new CalendarTheme(in);
        }

        public CalendarTheme[] newArray(int size) {

            return new CalendarTheme[size];
        }

    };

    public void readFromParcel(Parcel in) {
        title = in.readString();
        itemTextColor = in.readInt();
        widgetBgColor = in.readInt();
        rowColor = in.readInt();
        borderColor = in.readInt();
        headerColor = in.readInt();
        titleColor = in.readInt();
        leftArrow = in.readInt();
        rightArrow = in.readInt();
        iconPlus = in.readInt();
        iconSettings = in.readInt();
        iconVoice = in.readInt();
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
        dest.writeInt(itemTextColor);
        dest.writeInt(widgetBgColor);
        dest.writeInt(rowColor);
        dest.writeInt(borderColor);
        dest.writeInt(headerColor);
        dest.writeInt(titleColor);
        dest.writeInt(leftArrow);
        dest.writeInt(rightArrow);
        dest.writeInt(iconPlus);
        dest.writeInt(iconVoice);
        dest.writeInt(iconSettings);
        dest.writeInt(windowColor);
        dest.writeInt(windowTextColor);
    }
}
