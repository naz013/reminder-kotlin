package com.elementary.tasks.core.data.models;

import android.text.TextUtils;

import com.elementary.tasks.core.interfaces.RecyclerInterface;
import com.elementary.tasks.core.utils.SuperUtil;
import com.google.gson.annotations.SerializedName;

import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

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
@Entity
public class Birthday implements RecyclerInterface {

    @SerializedName("name")
    private String name;
    @SerializedName("date")
    private String date;
    @SerializedName("number")
    private String number;
    @SerializedName("key")
    private String key;
    @SerializedName("showedYear")
    private int showedYear;
    @SerializedName("contactId")
    private int contactId;
    @SerializedName("day")
    private int day;
    @SerializedName("month")
    private int month;
    @SerializedName("uniqueId")
    @PrimaryKey(autoGenerate = true)
    private int uniqueId;
    @SerializedName("dayMonth")
    private String dayMonth;
    @SerializedName("uuId")
    private String uuId;

    public Birthday() {
    }

    @Ignore
    public Birthday(String name, String date, String number, int showedYear, int contactId, int day, int month) {
        this.name = name;
        this.date = date;
        this.number = number;
        String secKey = TextUtils.isEmpty(number) ? "0" : number.substring(1);
        this.key = name + "|" + secKey;
        this.showedYear = showedYear;
        this.contactId = contactId;
        this.day = day;
        this.month = month;
        this.dayMonth = day + "|" + month;
        this.uuId = UUID.randomUUID().toString();
    }

    public final int getUniqueId() {
        return uniqueId;
    }

    public final int getDay() {
        return day;
    }

    public final int getMonth() {
        return month;
    }

    public final void setDay(int day) {
        this.day = day;
    }

    public final void setMonth(int month) {
        this.month = month;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final String getDate() {
        return date;
    }

    public final void setDate(String date) {
        this.date = date;
    }

    public final String getNumber() {
        return number;
    }

    public final void setNumber(String number) {
        this.number = number;
    }

    public final String getKey() {
        return key;
    }

    public final void setKey(String key) {
        this.key = key;
    }

    public final String getUuId() {
        return uuId;
    }

    public final void setUuId(String uuId) {
        this.uuId = uuId;
    }

    public final int getShowedYear() {
        return showedYear;
    }

    public final void setShowedYear(int showedYear) {
        this.showedYear = showedYear;
    }

    public final int getContactId() {
        return contactId;
    }

    public final void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public final String getDayMonth() {
        return dayMonth;
    }

    public final void setDayMonth(String dayMonth) {
        this.dayMonth = dayMonth;
    }

    public final long getDateTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return calendar.getTimeInMillis();
    }

    @Override
    public boolean equals(Object obj) {
        return this.key.equals(((Birthday) obj).getKey());
    }

    @Override
    public String toString() {
        return SuperUtil.getObjectPrint(this, Birthday.class);
    }

    @Override
    public int hashCode() {
        return UUID.fromString(uuId).hashCode();
    }

    @Override
    public int getViewType() {
        return 2;
    }
}
