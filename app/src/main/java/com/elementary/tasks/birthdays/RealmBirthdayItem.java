package com.elementary.tasks.birthdays;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

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

public class RealmBirthdayItem extends RealmObject {

    private String name;
    private String date;
    private String number;
    @PrimaryKey
    private String key;
    private String uuId;
    private int showedYear;
    private int contactId;
    private int day;
    private int month;
    private int uniqueId;
    private String dayMonth;

    public RealmBirthdayItem() {

    }

    public RealmBirthdayItem(BirthdayItem item) {
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

    public int getUniqueId() {
        return uniqueId;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
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

    public int getContactId() {
        return contactId;
    }

    public String getDayMonth() {
        return dayMonth;
    }
}
