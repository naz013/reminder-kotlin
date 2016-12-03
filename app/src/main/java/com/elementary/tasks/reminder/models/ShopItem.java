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

package com.elementary.tasks.reminder.models;

import com.elementary.tasks.core.utils.SuperUtil;

import java.util.UUID;

public class ShopItem {

    private String summary;
    private boolean visibility;
    private boolean checked;
    private String uuId;
    private String createTime;

    public ShopItem() {
        this.uuId = UUID.randomUUID().toString();
    }

    public ShopItem(RealmShopItem item) {
        this.summary = item.getSummary();
        this.visibility = item.isVisibility();
        this.checked = item.isChecked();
        this.uuId = item.getUuId();
        this.createTime = item.getCreateTime();
    }

    public ShopItem(String summary, boolean visibility, boolean checked, String uuId, String createTime) {
        this.summary = summary;
        this.visibility = visibility;
        this.checked = checked;
        this.uuId = uuId;
        this.createTime = createTime;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getUuId() {
        return uuId;
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString(){
        return SuperUtil.getObjectPrint(this, ShopItem.class);
    }
}