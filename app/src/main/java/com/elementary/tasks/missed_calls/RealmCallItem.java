package com.elementary.tasks.missed_calls;

import com.google.gson.annotations.SerializedName;

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

public class RealmCallItem extends RealmObject {

    @SerializedName("number")
    @PrimaryKey
    private String number;
    @SerializedName("dateTime")
    private long dateTime;
    @SerializedName("uniqueId")
    private int uniqueId;

    public RealmCallItem() {
    }

    public RealmCallItem(CallItem item) {
        this.number = item.getNumber();
        this.dateTime = item.getDateTime();
        this.uniqueId = item.getUniqueId();
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }
}
