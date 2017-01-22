package com.elementary.tasks.core.app_widgets.events;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Copyright 2017 Nazar Suhovich
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

public class CalendarItem implements Parcelable {

    public enum Type {
        BIRTHDAY, REMINDER
    }

    private Type type;
    private String name;
    private String number;
    private String time;
    private String dayDate;
    private String id;
    private long date;
    private int viewType;

    public CalendarItem(Type type, String name, String number, String id, String time,
                        String dayDate, long date, int viewType){
        this.type = type;
        this.time = time;
        this.viewType = viewType;
        this.dayDate = dayDate;
        this.name = name;
        this.id = id;
        this.number = number;
        this.date = date;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public String getDayDate(){
        return dayDate;
    }

    public void setDayDate(String dayDate){
        this.dayDate = dayDate;
    }

    public String getTime(){
        return time;
    }

    public void setTime(String time){
        this.time = time;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public long getDate(){
        return date;
    }

    public void setDate(long date){
        this.date = date;
    }

    public Type getType(){
        return type;
    }

    public void setType(Type type){
        this.type = type;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getNumber(){
        return number;
    }

    public void setNumber(String number){
        this.number = number;
    }

    public CalendarItem(Parcel in) {
        super();
        readFromParcel(in);
    }

    public static final Creator<CalendarItem> CREATOR = new Creator<CalendarItem>() {
        public CalendarItem createFromParcel(Parcel in) {
            return new CalendarItem(in);
        }

        public CalendarItem[] newArray(int size) {

            return new CalendarItem[size];
        }

    };

    public void readFromParcel(Parcel in) {
        name = in.readString();
        number = in.readString();
        id = in.readString();
        date = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(number);
        dest.writeString(id);
        dest.writeLong(date);
    }
}