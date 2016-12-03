package com.elementary.tasks.notes;

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

public class NoteItem {
    private String summary;
    private String key;
    private String date;
    private int color;
    private int style;
    private byte[] image;
    private int uniqueId;

    public NoteItem(RealmNote item) {
        setColor(item.getColor());
        setDate(item.getDate());
        setImage(item.getImage());
        setKey(item.getKey());
        setStyle(item.getStyle());
        setSummary(item.getSummary());
        setUniqueId(item.getUniqueId());
    }

    public NoteItem(String summary, String key, String date, int color, int style, byte[] image) {
        this.summary = summary;
        this.key = key;
        this.date = date;
        this.color = color;
        this.style = style;
        this.image = image;
        this.uniqueId = new Random().nextInt(Integer.MAX_VALUE);
    }

    public NoteItem(String key) {
        this.uniqueId = new Random().nextInt(Integer.MAX_VALUE);
        setKey(key);
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return SuperUtil.getObjectPrint(this, NoteItem.class);
    }
}
