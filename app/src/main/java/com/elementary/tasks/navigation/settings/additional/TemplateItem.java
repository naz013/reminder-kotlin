package com.elementary.tasks.navigation.settings.additional;

import androidx.annotation.Nullable;

import com.elementary.tasks.core.utils.TimeUtil;

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

public class TemplateItem {
    @Nullable
    private String title;
    private String key;
    private String date;
    private boolean selected;

    public TemplateItem(RealmTemplate item) {
        this.title = item.getTitle();
        this.date = item.getDate();
        this.key = item.getKey();
        this.selected = item.isSelected();
    }

    public TemplateItem(@Nullable String title, String date) {
        this.title = title;
        this.date = date;
        this.key = UUID.randomUUID().toString();
    }

    public TemplateItem(@Nullable String title) {
        this.title = title;
        this.date = TimeUtil.getGmtDateTime();
        this.key = UUID.randomUUID().toString();
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
