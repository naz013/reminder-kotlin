package com.elementary.tasks.groups;

import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.google.gson.annotations.SerializedName;

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

public class GroupItem {

    @SerializedName("title")
    private String title;
    @SerializedName("uuId")
    private String uuId;
    @SerializedName("color")
    private int color;
    @SerializedName("dateTime")
    private String dateTime;

    public GroupItem(RealmGroup item) {
        this.color = item.getColor();
        this.dateTime = item.getDateTime();
        this.uuId = item.getUuId();
        this.title = item.getTitle();
    }

    public GroupItem(String title, int color) {
        this.title = title;
        this.uuId = UUID.randomUUID().toString();
        this.color = color;
        this.dateTime = TimeUtil.getGmtDateTime();
    }

    public GroupItem(String title, String uuId, int color, String dateTime) {
        this.title = title;
        this.uuId = uuId;
        this.color = color;
        this.dateTime = dateTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUuId() {
        return uuId;
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return SuperUtil.getObjectPrint(this, GroupItem.class);
    }
}
