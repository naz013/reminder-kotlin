package com.elementary.tasks.birthdays;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.elementary.tasks.core.event_tree.EventInterface;
import com.elementary.tasks.core.interfaces.RecyclerInterface;
import com.elementary.tasks.core.utils.SuperUtil;

import java.util.Random;
import java.util.UUID;

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

public class BirthdayItem implements RecyclerInterface, EventInterface {

    private String name;
    private String date;
    private String number;
    private String key;
    private String uuId;
    private int showedYear;
    private int contactId;
    private int day;
    private int month;
    private int uniqueId;
    private String dayMonth;

    public BirthdayItem(RealmBirthdayItem item) {
        this.name = item.getName();
        this.date = item.getDate();
        this.number = item.getNumber();
        this.key = item.getKey();
        this.showedYear = item.getShowedYear();
        this.contactId = item.getContactId();
        this.dayMonth = item.getDayMonth();
        this.uuId = item.getUuId();
        this.day = item.getDay();
        this.month = item.getMonth();
        this.uniqueId = item.getUniqueId();
    }

    public BirthdayItem(String name, String date, String number, int showedYear, int contactId, int day, int month) {
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
        this.uniqueId = new Random().nextInt(Integer.MAX_VALUE);
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUuId() {
        return uuId;
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }

    public int getShowedYear() {
        return showedYear;
    }

    public void setShowedYear(int showedYear) {
        this.showedYear = showedYear;
    }

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public String getDayMonth() {
        return dayMonth;
    }

    public void setDayMonth(String dayMonth) {
        this.dayMonth = dayMonth;
    }

    @Override
    public boolean equals(Object obj) {
        return this.key.equals(((BirthdayItem) obj).getKey());
    }

    @Override
    public String toString() {
        return SuperUtil.getObjectPrint(this, BirthdayItem.class);
    }

    @Override
    public int hashCode() {
        return UUID.fromString(uuId).hashCode();
    }

    @Override
    public int getViewType() {
        return 2;
    }

    @Override
    public int getYear() {
        return 0;
    }

    @Override
    public int getMinute() {
        return 0;
    }

    @Override
    public int getHour() {
        return 0;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        EventInterface eventInterface = (EventInterface) o;
        return eventInterface.hashCode() - hashCode();
    }
}
