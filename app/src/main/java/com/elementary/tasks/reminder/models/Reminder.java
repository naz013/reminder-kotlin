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

import android.util.Log;

import com.elementary.tasks.core.interfaces.RecyclerInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Reminder implements RecyclerInterface {

    private static final String TAG = "Reminder";

    public static final int REMINDER = 0;
    public static final int SHOPPING = 1;

    public static final int BY_DATE = 10;
    public static final int BY_DATE_CALL = 11;
    public static final int BY_DATE_SMS = 12;
    public static final int BY_DATE_APP = 13;
    public static final int BY_DATE_LINK = 14;
    public static final int BY_DATE_SHOP = 15;
    public static final int BY_DATE_EMAIL = 16;
    public static final int BY_TIME = 20;
    public static final int BY_WEEK = 30;
    public static final int BY_WEEK_CALL = 31;
    public static final int BY_WEEK_SMS = 32;
    public static final int BY_LOCATION = 40;
    public static final int BY_LOCATION_CALL = 41;
    public static final int BY_LOCATION_SMS = 42;
    public static final int BY_SKYPE = 50;
    public static final int BY_SKYPE_CALL = 51;
    public static final int BY_SKYPE_VIDEO = 52;
    public static final int BY_MONTH = 60;
    public static final int BY_MONTH_CALL = 61;
    public static final int BY_MONTH_SMS = 62;
    public static final int BY_OUT = 70;
    public static final int BY_OUT_CALL = 71;
    public static final int BY_OUT_SMS = 72;
    public static final int BY_PLACES = 80;
    public static final int BY_PLACES_CALL = 81;
    public static final int BY_PLACES_SMS = 82;

    private String summary;
    private String noteId;
    private int reminderType;
    private String groupUuId;
    private String uuId;
    private String eventTime;
    private String startTime;
    private long eventCount;
    private int color;
    private int status;
    private boolean vibrate;
    private boolean repeatNotification;
    private boolean notifyByVoice;
    private boolean awake;
    private boolean unlock;
    private boolean exportToTasks;
    private boolean exportToCalendar;
    private boolean useGlobal;
    private String from;
    private String to;
    private List<Integer> hours;
    private String fileName;
    private String melodyPath;
    private int volume;
    private int dayOfMonth;
    private long repeatInterval;
    private int repeatLimit;
    private long after;
    private List<Integer> weekdays = new ArrayList<>();
    private int type;
    private String target;
    private String subject;
    private String attachmentFile;
    private List<String> attachmentFiles = new ArrayList<>();
    private boolean auto;
    private List<Place> places = new ArrayList<>();
    private List<ShopItem> shoppings = new ArrayList<>();
    private int uniqueId;

    public static boolean isType(int type, int base) {
        int res = type - base;
        return res >= 0 && res < 10;
    }

    public static boolean isBase(int type, int base) {
        return type == base;
    }

    public Reminder() {
        this.uuId = UUID.randomUUID().toString();
        this.uniqueId = new Random().nextInt(Integer.MAX_VALUE);
        Log.d(TAG, "Reminder: " + uniqueId);
    }

    public Reminder(RealmReminder item) {
        this.summary = item.getSummary();
        this.noteId = item.getNoteId();
        this.reminderType = item.getReminderType();
        this.groupUuId = item.getGroupUuId();
        this.uuId = item.getUuId();
        this.eventTime = item.getEventTime();
        this.startTime = item.getStartTime();
        this.eventCount = item.getEventCount();
        this.color = item.getColor();
        this.status = item.getStatus();
        this.vibrate = item.isVibrate();
        this.repeatNotification = item.isRepeatNotification();
        this.notifyByVoice = item.isNotifyByVoice();
        this.awake = item.isAwake();
        this.unlock = item.isUnlock();
        this.exportToTasks = item.isExportToTasks();
        this.exportToCalendar = item.isExportToCalendar();
        this.useGlobal = item.isUseGlobal();
        this.from = item.getFrom();
        this.to = item.getTo();
        this.hours = wrapIntegerArray(item.getHours());
        this.fileName = item.getFileName();
        this.melodyPath = item.getMelodyPath();
        this.volume = item.getVolume();
        this.dayOfMonth = item.getDayOfMonth();
        this.repeatInterval = item.getRepeatInterval();
        this.repeatLimit = item.getRepeatLimit();
        this.after = item.getAfter();
        this.weekdays = wrapIntegerArray(item.getWeekdays());
        this.type = item.getType();
        this.target = item.getTarget();
        this.subject = item.getSubject();
        this.attachmentFile = item.getAttachmentFile();
        this.attachmentFiles = wrapStringArray(item.getAttachmentFiles());
        this.auto = item.isAuto();
        this.uniqueId = item.getUniqueId();
        for (RealmPlace2 place : item.getPlaces()) {
            places.add(new Place(place));
        }
        for (RealmShopItem shopItem : item.getShoppings()) {
            shoppings.add(new ShopItem(shopItem));
        }
    }

    private List<Integer> wrapIntegerArray(List<RealmInteger> list) {
        List<Integer> strings = new ArrayList<>();
        for (RealmInteger integer : list) {
            strings.add(integer.getInteger());
        }
        return strings;
    }

    private List<String> wrapStringArray(List<RealmString> list) {
        List<String> strings = new ArrayList<>();
        for (RealmString string : list) {
            strings.add(string.getString());
        }
        return strings;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getNoteId() {
        return noteId;
    }

    public Reminder setNoteId(String noteId) {
        this.noteId = noteId;
        return this;
    }

    public boolean isUseGlobal() {
        return useGlobal;
    }

    public Reminder setUseGlobal(boolean useGlobal) {
        this.useGlobal = useGlobal;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public Reminder setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public int getReminderType() {
        return reminderType;
    }

    public Reminder setReminderType(int reminderType) {
        this.reminderType = reminderType;
        return this;
    }

    public String getGroupUuId() {
        return groupUuId;
    }

    public Reminder setGroupUuId(String groupUuId) {
        this.groupUuId = groupUuId;
        return this;
    }

    public String getUuId() {
        return uuId;
    }

    public Reminder setUuId(String uuId) {
        this.uuId = uuId;
        return this;
    }

    public String getEventTime() {
        return eventTime;
    }

    public Reminder setEventTime(String eventTime) {
        this.eventTime = eventTime;
        return this;
    }

    public String getStartTime() {
        return startTime;
    }

    public Reminder setStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public long getEventCount() {
        return eventCount;
    }

    public Reminder setEventCount(long eventCount) {
        this.eventCount = eventCount;
        return this;
    }

    public int getColor() {
        return color;
    }

    public Reminder setColor(int color) {
        this.color = color;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public Reminder setStatus(int status) {
        this.status = status;
        return this;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public Reminder setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
        return this;
    }

    public boolean isRepeatNotification() {
        return repeatNotification;
    }

    public Reminder setRepeatNotification(boolean repeatNotification) {
        this.repeatNotification = repeatNotification;
        return this;
    }

    public boolean isNotifyByVoice() {
        return notifyByVoice;
    }

    public Reminder setNotifyByVoice(boolean notifyByVoice) {
        this.notifyByVoice = notifyByVoice;
        return this;
    }

    public boolean isAwake() {
        return awake;
    }

    public Reminder setAwake(boolean awake) {
        this.awake = awake;
        return this;
    }

    public boolean isUnlock() {
        return unlock;
    }

    public Reminder setUnlock(boolean unlock) {
        this.unlock = unlock;
        return this;
    }

    public boolean isExportToTasks() {
        return exportToTasks;
    }

    public Reminder setExportToTasks(boolean exportToTasks) {
        this.exportToTasks = exportToTasks;
        return this;
    }

    public boolean isExportToCalendar() {
        return exportToCalendar;
    }

    public Reminder setExportToCalendar(boolean exportToCalendar) {
        this.exportToCalendar = exportToCalendar;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public Reminder setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTo() {
        return to;
    }

    public Reminder setTo(String to) {
        this.to = to;
        return this;
    }

    public List<Integer> getHours() {
        return hours;
    }

    public Reminder setHours(List<Integer> hours) {
        this.hours = hours;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public Reminder setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getMelodyPath() {
        return melodyPath;
    }

    public Reminder setMelodyPath(String melodyPath) {
        this.melodyPath = melodyPath;
        return this;
    }

    public int getVolume() {
        return volume;
    }

    public Reminder setVolume(int volume) {
        this.volume = volume;
        return this;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public Reminder setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
        return this;
    }

    public long getRepeatInterval() {
        return repeatInterval;
    }

    public Reminder setRepeatInterval(long repeatInterval) {
        this.repeatInterval = repeatInterval;
        return this;
    }

    public int getRepeatLimit() {
        return repeatLimit;
    }

    public Reminder setRepeatLimit(int repeatLimit) {
        this.repeatLimit = repeatLimit;
        return this;
    }

    public long getAfter() {
        return after;
    }

    public Reminder setAfter(long after) {
        this.after = after;
        return this;
    }

    public List<Integer> getWeekdays() {
        return weekdays;
    }

    public Reminder setWeekdays(List<Integer> weekdays) {
        this.weekdays = weekdays;
        return this;
    }

    public int getType() {
        return type;
    }

    public Reminder setType(int type) {
        this.type = type;
        return this;
    }

    public String getTarget() {
        return target;
    }

    public Reminder setTarget(String target) {
        this.target = target;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public Reminder setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getAttachmentFile() {
        return attachmentFile;
    }

    public Reminder setAttachmentFile(String attachmentFile) {
        this.attachmentFile = attachmentFile;
        return this;
    }

    public List<String> getAttachmentFiles() {
        return attachmentFiles;
    }

    public Reminder setAttachmentFiles(List<String> attachmentFiles) {
        this.attachmentFiles = attachmentFiles;
        return this;
    }

    public boolean isAuto() {
        return auto;
    }

    public Reminder setAuto(boolean auto) {
        this.auto = auto;
        return this;
    }

    public List<Place> getPlaces() {
        return places;
    }

    public Reminder setPlaces(List<Place> places) {
        this.places = places;
        return this;
    }

    public List<ShopItem> getShoppings() {
        return shoppings;
    }

    public Reminder setShoppings(List<ShopItem> shoppings) {
        this.shoppings = shoppings;
        return this;
    }

    @Override
    public int getViewType() {
        if (shoppings.size() > 0) return SHOPPING;
        else return REMINDER;
    }
}
