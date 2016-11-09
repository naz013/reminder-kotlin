package com.elementary.tasks.core.utils;

import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.groups.RealmGroup;
import com.elementary.tasks.navigation.settings.additional.RealmTemplate;
import com.elementary.tasks.navigation.settings.additional.TemplateItem;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

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

public class RealmDb {

    private static RealmDb instance;

    private RealmDb() {}

    public static RealmDb getInstance() {
        if (instance == null) {
            instance = new RealmDb();
        }
        return instance;
    }

    public void saveTemplate(TemplateItem item) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(new RealmTemplate(item));
        realm.commitTransaction();
    }

    public void deleteTemplates(TemplateItem item) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmTemplate template = realm.where(RealmTemplate.class).equalTo("key", item.getKey()).findFirst();
        template.deleteFromRealm();
        realm.commitTransaction();
    }

    public TemplateItem getTemplate(String id) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmTemplate template = realm.where(RealmTemplate.class).equalTo("key", id).findFirst();
        realm.commitTransaction();
        return new TemplateItem(template);
    }

    public List<TemplateItem> getAllTemplates() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        List<RealmTemplate> list = realm.where(RealmTemplate.class).findAll();
        List<TemplateItem> items = new ArrayList<>();
        for (RealmTemplate template : list) {
            items.add(new TemplateItem(template));
        }
        realm.commitTransaction();
        return items;
    }

    public void saveGroup(GroupItem item) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(new RealmGroup(item));
        realm.commitTransaction();
    }

    public void deleteGroup(GroupItem item) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmGroup object = realm.where(RealmGroup.class).equalTo("uuId", item.getUuId()).findFirst();
        object.deleteFromRealm();
        realm.commitTransaction();
    }

    public GroupItem getGroup(String id) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmGroup object = realm.where(RealmGroup.class).equalTo("uuId", id).findFirst();
        realm.commitTransaction();
        return new GroupItem(object);
    }

    public void changeGroupColor(String id, int color) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmGroup object = realm.where(RealmGroup.class).equalTo("uuId", id).findFirst();
        object.setColor(color);
        realm.commitTransaction();
    }

    public List<GroupItem> getAllGroups() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        List<RealmGroup> list = realm.where(RealmGroup.class).findAll();
        List<GroupItem> items = new ArrayList<>();
        for (RealmGroup object : list) {
            items.add(new GroupItem(object));
        }
        realm.commitTransaction();
        return items;
    }
}
