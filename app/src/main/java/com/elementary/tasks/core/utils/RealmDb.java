package com.elementary.tasks.core.utils;

import android.app.AlarmManager;
import android.util.Log;

import com.elementary.tasks.birthdays.BirthdayItem;
import com.elementary.tasks.birthdays.RealmBirthdayItem;
import com.elementary.tasks.core.calendar.CalendarEvent;
import com.elementary.tasks.core.calendar.RealmCalendarEvent;
import com.elementary.tasks.core.cloud.GoogleTasks;
import com.elementary.tasks.google_tasks.RealmTask;
import com.elementary.tasks.google_tasks.RealmTaskList;
import com.elementary.tasks.google_tasks.TaskItem;
import com.elementary.tasks.google_tasks.TaskListItem;
import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.groups.Position;
import com.elementary.tasks.groups.RealmGroup;
import com.elementary.tasks.missed_calls.CallItem;
import com.elementary.tasks.missed_calls.RealmCallItem;
import com.elementary.tasks.navigation.settings.additional.RealmTemplate;
import com.elementary.tasks.navigation.settings.additional.TemplateItem;
import com.elementary.tasks.notes.NoteItem;
import com.elementary.tasks.notes.RealmNote;
import com.elementary.tasks.places.PlaceItem;
import com.elementary.tasks.places.RealmPlace;
import com.elementary.tasks.reminder.models.RealmReminder;
import com.elementary.tasks.reminder.models.Reminder;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
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

    private static final String TAG = "RealmDb";
    private static final SimpleDateFormat birthFormat = new SimpleDateFormat("dd|MM", Locale.getDefault());

    private static RealmDb instance;

    private RealmDb() {}

    public static RealmDb getInstance() {
        if (instance == null) {
            instance = new RealmDb();
        }
        return instance;
    }

    public void saveObject(Object o) {
        if (o == null) return;
        Log.d(TAG, "saveObject: " + o);
        if (o instanceof TemplateItem) {
            saveTemplate((TemplateItem) o);
        } else if (o instanceof NoteItem) {
            saveNote((NoteItem) o);
        } else if (o instanceof PlaceItem) {
            savePlace((PlaceItem) o);
        } else if (o instanceof TaskItem) {
            saveTask((TaskItem) o);
        } else if (o instanceof TaskListItem) {
            saveTaskList((TaskListItem) o);
        } else if (o instanceof GroupItem) {
            saveGroup((GroupItem) o);
        } else if (o instanceof Reminder) {
            saveReminder((Reminder) o);
        } else if (o instanceof CalendarEvent) {
            saveCalendarEvent((CalendarEvent) o);
        } else if (o instanceof CallItem) {
            saveMissedCall((CallItem) o);
        } else if (o instanceof BirthdayItem) {
            saveBirthday((BirthdayItem) o);
        }
    }

    private void saveBirthday(BirthdayItem item) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(new RealmBirthdayItem(item));
        realm.commitTransaction();
    }

    public void deleteBirthday(BirthdayItem item) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmBirthdayItem callItem = realm.where(RealmBirthdayItem.class).equalTo("key", item.getKey()).findFirst();
        callItem.deleteFromRealm();
        realm.commitTransaction();
    }

    public BirthdayItem getBirthday(String key) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmBirthdayItem item = realm.where(RealmBirthdayItem.class).equalTo("key", key).findFirst();
        realm.commitTransaction();
        if (item != null) {
            return new BirthdayItem(item);
        } else {
            return null;
        }
    }

    public List<BirthdayItem> getAllBirthdays() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        List<RealmBirthdayItem> list = realm.where(RealmBirthdayItem.class).findAll();
        List<BirthdayItem> items = new ArrayList<>();
        for (RealmBirthdayItem item : list) {
            WeakReference<BirthdayItem> reference = new WeakReference<>(new BirthdayItem(item));
            items.add(reference.get());
        }
        realm.commitTransaction();
        return items;
    }

    public List<BirthdayItem> getTodayBirthdays(int daysBefore) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        int mYear = cal.get(Calendar.YEAR);
        String mDate = birthFormat.format(cal.getTime());
        List<BirthdayItem> list = new ArrayList<>();
        for (BirthdayItem item : getAllBirthdays()) {
            int year = item.getShowedYear();
            String birthValue = getBirthdayValue(item.getMonth(), item.getDay(), daysBefore);
            if (birthValue.equals(mDate) && year != mYear) {
                list.add(item);
            }
        }
        return list;
    }

    private String getBirthdayValue(int month, int day, int daysBefore) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.setTimeInMillis(calendar.getTimeInMillis() - (AlarmManager.INTERVAL_DAY * daysBefore));
        return birthFormat.format(calendar.getTime());
    }

    public List<BirthdayItem> getBirthdays(int day, int month) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        List<RealmBirthdayItem> list = realm.where(RealmBirthdayItem.class).equalTo("dayMonth", day + "|" + month).findAll();
        List<BirthdayItem> items = new ArrayList<>();
        for (RealmBirthdayItem item : list) {
            WeakReference<BirthdayItem> reference = new WeakReference<>(new BirthdayItem(item));
            items.add(reference.get());
        }
        realm.commitTransaction();
        return items;
    }

    private void saveMissedCall(CallItem item) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(new RealmCallItem(item));
        realm.commitTransaction();
    }

    public void deleteMissedCall(CallItem item) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmCallItem callItem = realm.where(RealmCallItem.class).equalTo("number", item.getNumber()).findFirst();
        callItem.deleteFromRealm();
        realm.commitTransaction();
    }

    public CallItem getMissedCall(String number) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmCallItem template = realm.where(RealmCallItem.class).equalTo("number", number).findFirst();
        realm.commitTransaction();
        if (template != null) {
            return new CallItem(template);
        } else {
            return null;
        }
    }

    private void saveCalendarEvent(CalendarEvent item) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(new RealmCalendarEvent(item));
        realm.commitTransaction();
    }

    public void deleteCalendarEvent(CalendarEvent item) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmCalendarEvent template = realm.where(RealmCalendarEvent.class).equalTo("uuId", item.getUuId()).findFirst();
        template.deleteFromRealm();
        realm.commitTransaction();
    }

    public CalendarEvent getCalendarEvent(String id) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmCalendarEvent template = realm.where(RealmCalendarEvent.class).equalTo("uuId", id).findFirst();
        realm.commitTransaction();
        if (template != null) {
            return new CalendarEvent(template);
        } else {
            return null;
        }
    }

    public List<CalendarEvent> getCalendarEvents(String reminderId) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        List<RealmCalendarEvent> list = realm.where(RealmCalendarEvent.class).equalTo("reminderId", reminderId).findAll();
        List<CalendarEvent> items = new ArrayList<>();
        for (RealmCalendarEvent item : list) {
            WeakReference<CalendarEvent> reference = new WeakReference<>(new CalendarEvent(item));
            items.add(reference.get());
        }
        realm.commitTransaction();
        return items;
    }

    public List<CalendarEvent> getCalendarEvents() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        List<RealmCalendarEvent> list = realm.where(RealmCalendarEvent.class).findAll();
        List<CalendarEvent> items = new ArrayList<>();
        for (RealmCalendarEvent item : list) {
            WeakReference<CalendarEvent> reference = new WeakReference<>(new CalendarEvent(item));
            items.add(reference.get());
        }
        realm.commitTransaction();
        return items;
    }

    public List<Long> getCalendarEventsIds() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        List<RealmCalendarEvent> list = realm.where(RealmCalendarEvent.class).findAll();
        List<Long> items = new ArrayList<>();
        for (RealmCalendarEvent item : list) {
            WeakReference<CalendarEvent> reference = new WeakReference<>(new CalendarEvent(item));
            items.add(reference.get().getEventId());
        }
        realm.commitTransaction();
        return items;
    }

    private void saveTemplate(TemplateItem item) {
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
        if (template != null) {
            return new TemplateItem(template);
        } else {
            return null;
        }
    }

    public List<TemplateItem> getAllTemplates() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        List<RealmTemplate> list = realm.where(RealmTemplate.class).findAll();
        List<TemplateItem> items = new ArrayList<>();
        for (RealmTemplate template : list) {
            WeakReference<TemplateItem> reference = new WeakReference<>(new TemplateItem(template));
            items.add(reference.get());
        }
        realm.commitTransaction();
        return items;
    }

    private void saveGroup(GroupItem item) {
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
        if (object != null) {
            return new GroupItem(object);
        } else return null;
    }

    public void changeGroupColor(String id, int color) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmGroup object = realm.where(RealmGroup.class).equalTo("uuId", id).findFirst();
        object.setColor(color);
        realm.commitTransaction();
    }

    public GroupItem getDefaultGroup() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmGroup realmGroup = realm.where(RealmGroup.class).findFirst();
        realm.commitTransaction();
        if (realmGroup == null) return null;
        else return new GroupItem(realmGroup);
    }

    public List<GroupItem> getAllGroups() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        List<RealmGroup> list = realm.where(RealmGroup.class).findAll();
        List<GroupItem> items = new ArrayList<>();
        for (RealmGroup object : list) {
            WeakReference<GroupItem> reference = new WeakReference<>(new GroupItem(object));
            items.add(reference.get());
        }
        realm.commitTransaction();
        return items;
    }

    public List<String> getAllGroupsNames(List<GroupItem> items, String uuId, Position p) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        List<RealmGroup> list = realm.where(RealmGroup.class).findAll();
        for (RealmGroup object : list) {
            WeakReference<GroupItem> reference = new WeakReference<>(new GroupItem(object));
            items.add(reference.get());
        }
        if (uuId == null) uuId = "";
        List<String> names = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            GroupItem item = items.get(i);
            names.add(item.getTitle());
            if (item.getUuId().matches(uuId)) {
                p.i = i;
            }
        }
        realm.commitTransaction();
        return names;
    }

    private void savePlace(PlaceItem item) {
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
        if (object != null) {
            return new PlaceItem(object);
        } else {
            return null;
        }
    }

    public List<PlaceItem> getAllPlaces() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        List<RealmPlace> list = realm.where(RealmPlace.class).findAll();
        List<PlaceItem> items = new ArrayList<>();
        for (RealmPlace object : list) {
            WeakReference<PlaceItem> reference = new WeakReference<>(new PlaceItem(object));
            items.add(reference.get());
        }
        realm.commitTransaction();
        return items;
    }

    private void saveNote(NoteItem item) {
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
        if (object != null) {
            return new NoteItem(object);
        } else {
            return null;
        }
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
            WeakReference<NoteItem> reference = new WeakReference<>(new NoteItem(object));
            items.add(reference.get());
        }
        realm.commitTransaction();
        return items;
    }

    private void saveTask(TaskItem item) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(new RealmTask(item));
        realm.commitTransaction();
    }

    public void deleteTask(TaskItem item) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmTask object = realm.where(RealmTask.class).equalTo("taskId", item.getTaskId()).findFirst();
        object.deleteFromRealm();
        realm.commitTransaction();
    }

    public TaskItem getTaskByReminder(String id) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmTask object = realm.where(RealmTask.class).equalTo("uuId", id).findFirst();
        realm.commitTransaction();
        if (object == null) return null;
        else return new TaskItem(object);
    }

    public TaskItem getTask(String id) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmTask object = realm.where(RealmTask.class).equalTo("taskId", id).findFirst();
        realm.commitTransaction();
        if (object == null) return null;
        else return new TaskItem(object);
    }

    public List<TaskItem> getTasks(String orderPrefs) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        String field = "position";
        Sort order = Sort.ASCENDING;
        if (orderPrefs != null) {
            if (orderPrefs.matches(Constants.ORDER_DEFAULT)) {
                field = "position";
                order = Sort.ASCENDING;
            } else if (orderPrefs.matches(Constants.ORDER_DATE_A_Z)) {
                field = "dueDate";
                order = Sort.ASCENDING;
            } else if (orderPrefs.matches(Constants.ORDER_DATE_Z_A)) {
                field = "dueDate";
                order = Sort.DESCENDING;
            } else if (orderPrefs.matches(Constants.ORDER_COMPLETED_A_Z)) {
                field = "completeDate";
                order = Sort.ASCENDING;
            } else if (orderPrefs.matches(Constants.ORDER_COMPLETED_Z_A)) {
                field = "completeDate";
                order = Sort.DESCENDING;
            }
        }
        List<RealmTask> list = realm.where(RealmTask.class).findAllSorted(field, order);
        List<TaskItem> items = new ArrayList<>();
        for (RealmTask object : list) {
            WeakReference<TaskItem> reference = new WeakReference<>(new TaskItem(object));
            items.add(reference.get());
        }
        realm.commitTransaction();
        return items;
    }

    public List<TaskItem> getTasks(String listId, String orderPrefs) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        String field = "position";
        Sort order = Sort.ASCENDING;
        if (orderPrefs != null) {
            if (orderPrefs.matches(Constants.ORDER_DEFAULT)) {
                field = "position";
                order = Sort.ASCENDING;
            } else if (orderPrefs.matches(Constants.ORDER_DATE_A_Z)) {
                field = "dueDate";
                order = Sort.ASCENDING;
            } else if (orderPrefs.matches(Constants.ORDER_DATE_Z_A)) {
                field = "dueDate";
                order = Sort.DESCENDING;
            } else if (orderPrefs.matches(Constants.ORDER_COMPLETED_A_Z)) {
                field = "completeDate";
                order = Sort.ASCENDING;
            } else if (orderPrefs.matches(Constants.ORDER_COMPLETED_Z_A)) {
                field = "completeDate";
                order = Sort.DESCENDING;
            }
        }
        List<RealmTask> list = realm.where(RealmTask.class).equalTo("listId", listId).findAllSorted(field, order);
        List<TaskItem> items = new ArrayList<>();
        for (RealmTask object : list) {
            WeakReference<TaskItem> reference = new WeakReference<>(new TaskItem(object));
            items.add(reference.get());
        }
        realm.commitTransaction();
        return items;
    }

    public void deleteTasks(String listId) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            RealmResults<RealmTask> list = realm.where(RealmTask.class).equalTo("listId", listId).findAll();
            list.deleteAllFromRealm();
        });
    }

    public void deleteTasks() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            RealmResults<RealmTask> list = realm.where(RealmTask.class).findAll();
            list.deleteAllFromRealm();
        });
    }

    public void deleteCompletedTasks(String listId) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            RealmResults<RealmTask> list = realm.where(RealmTask.class).equalTo("listId", listId).equalTo("status", GoogleTasks.TASKS_COMPLETE).findAll();
            list.deleteAllFromRealm();
        });
    }

    public TaskListItem getTaskList(String listId) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmTaskList object = realm.where(RealmTaskList.class).equalTo("listId", listId).findFirst();
        realm.commitTransaction();
        if (object == null) return null;
        else return new TaskListItem(object);
    }

    public TaskListItem getDefaultTaskList() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmTaskList object = realm.where(RealmTaskList.class).equalTo("def", 1).findFirst();
        realm.commitTransaction();
        if (object == null) return null;
        else return new TaskListItem(object);
    }

    public List<TaskListItem> getTaskLists() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        List<RealmTaskList> list = realm.where(RealmTaskList.class).findAll();
        List<TaskListItem> items = new ArrayList<>();
        for (RealmTaskList object : list) {
            WeakReference<TaskListItem> reference = new WeakReference<>(new TaskListItem(object));
            items.add(reference.get());
        }
        realm.commitTransaction();
        return items;
    }

    public void deleteTaskLists() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            RealmResults<RealmTaskList> list = realm.where(RealmTaskList.class).findAll();
            list.deleteAllFromRealm();
        });
    }

    public boolean deleteTaskList(String id){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmTaskList object = realm.where(RealmTaskList.class).equalTo("listId", id).findFirst();
        object.deleteFromRealm();
        realm.commitTransaction();
        return true;
    }

    private void saveTaskList(TaskListItem item) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(new RealmTaskList(item));
        realm.commitTransaction();
    }

    public void setDefault(String id){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmTaskList object = realm.where(RealmTaskList.class).equalTo("listId", id).findFirst();
        object.setDef(1);
        realm.commitTransaction();
    }

    public void setSystemDefault(String id){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmTaskList object = realm.where(RealmTaskList.class).equalTo("listId", id).findFirst();
        object.setSystemDefault(1);
        realm.commitTransaction();
    }

    public void setSimple(String id){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmTaskList object = realm.where(RealmTaskList.class).equalTo("listId", id).findFirst();
        object.setDef(0);
        realm.commitTransaction();
    }

    public void setStatus(String id, boolean status){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmTask object = realm.where(RealmTask.class).equalTo("taskId", id).findFirst();
        if (status) {
            object.setStatus(GoogleTasks.TASKS_COMPLETE);
            object.setCompleteDate(System.currentTimeMillis());
        } else {
            object.setStatus(GoogleTasks.TASKS_NEED_ACTION);
            object.setCompleteDate(0);
        }
        realm.commitTransaction();
    }

    public Reminder getReminder(String id) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmReminder object = realm.where(RealmReminder.class).equalTo("uuId", id).findFirst();
        realm.commitTransaction();
        if (object == null) return null;
        else return new Reminder(object);
    }

    public Reminder getReminderByNote(String id) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmReminder object = realm.where(RealmReminder.class).equalTo("noteId", id).findFirst();
        realm.commitTransaction();
        if (object == null) return null;
        else return new Reminder(object);
    }

    public void changeReminderGroup(String id, String groupId) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmReminder object = realm.where(RealmReminder.class).equalTo("uuId", id).findFirst();
        object.setGroupUuId(groupId);
        realm.commitTransaction();
    }

    public boolean deleteReminder(String id){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmReminder object = realm.where(RealmReminder.class).equalTo("uuId", id).findFirst();
        if (object != null) {
            object.deleteFromRealm();
        }
        realm.commitTransaction();
        return true;
    }

    public boolean moveToTrash(String id){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmReminder object = realm.where(RealmReminder.class).equalTo("uuId", id).findFirst();
        boolean res = false;
        if (object != null) {
            object.setRemoved(true);
            object.setActive(false);
            res = true;
        }
        realm.commitTransaction();
        return res;
    }

    private void saveReminder(Reminder item) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(new RealmReminder(item));
        realm.commitTransaction();
    }

    public List<Reminder> getActiveReminders() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        String[] fields = new String[]{"isActive", "eventTime"};
        Sort[] orders = new Sort[]{Sort.DESCENDING, Sort.ASCENDING};
        List<RealmReminder> list = realm.where(RealmReminder.class).equalTo("isRemoved", false).findAllSorted(fields, orders);
        List<Reminder> items = new ArrayList<>();
        for (RealmReminder object : list) {
            WeakReference<Reminder> reference = new WeakReference<>(new Reminder(object));
            items.add(reference.get());
        }
        realm.commitTransaction();
        return items;
    }

    public List<Reminder> getEnabledReminders() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        List<RealmReminder> list = realm.where(RealmReminder.class).equalTo("isActive", true).findAll();
        List<Reminder> items = new ArrayList<>();
        for (RealmReminder object : list) {
            WeakReference<Reminder> reference = new WeakReference<>(new Reminder(object));
            items.add(reference.get());
        }
        realm.commitTransaction();
        return items;
    }

    public List<Reminder> getActiveReminders(String groupId) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        String[] fields = new String[]{"isActive", "eventTime"};
        Sort[] orders = new Sort[]{Sort.DESCENDING, Sort.ASCENDING};
        List<RealmReminder> list = realm.where(RealmReminder.class).equalTo("groupUuId", groupId).equalTo("isRemoved", false).findAllSorted(fields, orders);
        List<Reminder> items = new ArrayList<>();
        for (RealmReminder object : list) {
            WeakReference<Reminder> reference = new WeakReference<>(new Reminder(object));
            items.add(reference.get());
        }
        realm.commitTransaction();
        return items;
    }

    public List<Reminder> getArchivedReminder() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        String[] fields = new String[]{"eventTime"};
        Sort[] orders = new Sort[]{Sort.ASCENDING};
        List<RealmReminder> list = realm.where(RealmReminder.class).equalTo("isRemoved", true).findAllSorted(fields, orders);
        List<Reminder> items = new ArrayList<>();
        for (RealmReminder object : list) {
            WeakReference<Reminder> reference = new WeakReference<>(new Reminder(object));
            items.add(reference.get());
        }
        realm.commitTransaction();
        return items;
    }
}