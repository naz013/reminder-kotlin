package com.elementary.tasks.missed_calls;

import com.elementary.tasks.core.utils.SuperUtil;

import java.util.Random;

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

public class CallItem {

    private String number;
    private long dateTime;
    private int uniqueId;

    public CallItem() {
        this.uniqueId = new Random().nextInt(Integer.MAX_VALUE);
    }

    public CallItem(RealmCallItem item) {
        this.number = item.getNumber();
        this.dateTime = item.getDateTime();
        this.uniqueId = item.getUniqueId();
    }

    public CallItem(String number, long dateTime) {
        this.number = number;
        this.dateTime = dateTime;
        this.uniqueId = new Random().nextInt(Integer.MAX_VALUE);
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
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

    @Override
    public String toString() {
        return SuperUtil.getObjectPrint(this, CallItem.class);
    }
}
