package com.elementary.tasks.core.utils;

import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.groups.RealmGroup;
import com.elementary.tasks.navigation.settings.additional.RealmTemplate;
import com.elementary.tasks.navigation.settings.additional.TemplateItem;
import com.elementary.tasks.notes.NoteItem;
import com.elementary.tasks.notes.RealmNote;
import com.elementary.tasks.places.PlaceItem;
import com.elementary.tasks.places.RealmPlace;
import com.google.android.gms.drive.query.SortOrder;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.Sort;

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

    public void savePlace(PlaceItem item) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(new RealmPlace(item));
        realm.commitTransaction();
    }

    public void deletePlace(PlaceItem item) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmPlace object = realm.where(RealmPlace.class).equalTo("key", item.getKey()).findFirst();
        object.deleteFromRealm();
        realm.commitTransaction();
    }

    public PlaceItem getPlace(String id) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmPlace object = realm.where(RealmPlace.class).equalTo("key", id).findFirst();
        realm.commitTransaction();
        return new PlaceItem(object);
    }

    public List<PlaceItem> getAllPlaces() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        List<RealmPlace> list = realm.where(RealmPlace.class).findAll();
        List<PlaceItem> items = new ArrayList<>();
        for (RealmPlace object : list) {
            items.add(new PlaceItem(object));
        }
        realm.commitTransaction();
        return items;
    }

    public void saveNote(NoteItem item) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(new RealmNote(item));
        realm.commitTransaction();
    }

    public void deleteNote(NoteItem item) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmNote object = realm.where(RealmNote.class).equalTo("key", item.getKey()).findFirst();
        object.deleteFromRealm();
        realm.commitTransaction();
    }

    public NoteItem getNote(String id) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmNote object = realm.where(RealmNote.class).equalTo("key", id).findFirst();
        realm.commitTransaction();
        return new NoteItem(object);
    }

    public void changeNoteColor(String id, int color) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmNote object = realm.where(RealmNote.class).equalTo("key", id).findFirst();
        object.setColor(color);
        realm.commitTransaction();
    }

    public List<NoteItem> getAllNotes(String orderPrefs) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        String field = "date";
        Sort order = Sort.DESCENDING;
        if (orderPrefs != null) {
            if (orderPrefs.matches(Constants.ORDER_DATE_A_Z)) {
                field = "date";
                order = Sort.ASCENDING;
            } else if (orderPrefs.matches(Constants.ORDER_DATE_Z_A)) {
                field = "date";
                order = Sort.DESCENDING;
            } else if (orderPrefs.matches(Constants.ORDER_NAME_A_Z)) {
                field = "summary";
                order = Sort.ASCENDING;
            } else if (orderPrefs.matches(Constants.ORDER_NAME_Z_A)) {
                field = "summary";
                order = Sort.DESCENDING;
            }
        }
        List<RealmNote> list = realm.where(RealmNote.class).findAllSorted(field, order);
        List<NoteItem> items = new ArrayList<>();
        for (RealmNote object : list) {
            items.add(new NoteItem(object));
        }
        realm.commitTransaction();
        return items;
    }
}
