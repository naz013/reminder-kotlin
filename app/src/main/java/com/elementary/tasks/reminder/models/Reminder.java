package com.elementary.tasks.reminder.models;

import com.elementary.tasks.core.event_tree.EventInterface;
import com.elementary.tasks.core.interfaces.RecyclerInterface;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.creators.fragments.ReminderInterface;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
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

public class Reminder implements RecyclerInterface, EventInterface {

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
    private int delay;
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
    private List<Integer> hours = new ArrayList<>();
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
    private boolean isActive;
    private boolean isRemoved;
    private boolean isNotificationShown;
    private boolean isLocked;

    public static boolean isBase(int type, int base) {
        int res = type - base;
        return res >= 0 && res < 10;
    }

    public static boolean isKind(int type, int kind) {
        return type % BY_DATE == kind;
    }

    public static boolean isSame(int type, int base) {
        return type == base;
    }

    public static boolean isGpsType(int type) {
        return isBase(type, BY_LOCATION) || isBase(type, BY_OUT) || isBase(type, BY_PLACES);
    }

    public Reminder() {
        this.uuId = UUID.randomUUID().toString();
        this.uniqueId = new Random().nextInt(Integer.MAX_VALUE);
        this.isActive = true;
        this.isRemoved = false;
        this.useGlobal = true;
    }

    public Reminder(Reminder item, boolean fullCopy) {
        this.summary = item.getSummary();
        this.reminderType = item.getReminderType();
        this.groupUuId = item.getGroupUuId();
        this.eventCount = 0;
        this.color = item.getColor();
        this.delay = 0;
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
        this.hours = item.getHours();
        this.fileName = item.getFileName();
        this.melodyPath = item.getMelodyPath();
        this.volume = item.getVolume();
        this.dayOfMonth = item.getDayOfMonth();
        this.repeatInterval = item.getRepeatInterval();
        this.repeatLimit = item.getRepeatLimit();
        this.after = item.getAfter();
        this.weekdays = item.getWeekdays();
        this.type = item.getType();
        this.target = item.getTarget();
        this.subject = item.getSubject();
        this.attachmentFile = item.getAttachmentFile();
        this.attachmentFiles = item.getAttachmentFiles();
        this.auto = item.isAuto();
        this.isActive = item.isActive();
        this.isRemoved = item.isRemoved();
        this.isNotificationShown = item.isNotificationShown();
        this.isLocked = item.isLocked();
        this.places = item.getPlaces();
        this.shoppings = item.getShoppings();
        if (fullCopy) {
            this.uuId = item.getUuId();
            this.uniqueId = item.getUniqueId();
        }
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
        this.delay = item.getStatus();
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
        this.isActive = item.isActive();
        this.isRemoved = item.isRemoved();
        this.isNotificationShown = item.isNotificationShown();
        this.isLocked = item.isLocked();
        this.places = new ArrayList<>();
        for (RealmPlace2 place : item.getPlaces()) {
            places.add(new Place(place));
        }
        this.shoppings = new ArrayList<>();
        for (RealmShopItem shopItem : item.getShoppings()) {
            shoppings.add(new ShopItem(shopItem));
        }
    }

    private List<Integer> wrapIntegerArray(List<RealmInteger> list) {
        List<Integer> strings = new ArrayList<>();
        if (list != null) {
            for (RealmInteger integer : list) {
                strings.add(integer.getInteger());
            }
        }
        return strings;
    }

    private List<String> wrapStringArray(List<RealmString> list) {
        List<String> strings = new ArrayList<>();
        if (list != null) {
            for (RealmString string : list) {
                strings.add(string.getString());
            }
        }
        return strings;
    }

    public Reminder copy() {
        Reminder reminder = new Reminder(this, false);
        reminder.setUuId(UUID.randomUUID().toString());
        reminder.setUniqueId(new Random().nextInt(Integer.MAX_VALUE));
        reminder.setActive(true);
        reminder.setRemoved(false);
        return reminder;
    }

    public boolean isNotificationShown() {
        return isNotificationShown;
    }

    public Reminder setNotificationShown(boolean notificationShown) {
        isNotificationShown = notificationShown;
        return this;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public Reminder setLocked(boolean locked) {
        isLocked = locked;
        return this;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public Reminder setActive(boolean active) {
        isActive = active;
        return this;
    }

    public Reminder setRemoved(boolean removed) {
        isRemoved = removed;
        return this;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public Reminder setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
        return this;
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

    public int getDelay() {
        return delay;
    }

    public Reminder setDelay(int delay) {
        this.delay = delay;
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

    public void setClear(ReminderInterface mInterface) {
        setSummary(mInterface.getSummary());
        setGroupUuId(mInterface.getGroup());
        setRepeatLimit(mInterface.getRepeatLimit());
        setColor(mInterface.getLedColor());
        setMelodyPath(mInterface.getMelodyPath());
        setVolume(mInterface.getVolume());
        setAuto(mInterface.getAuto());
        setActive(true);
        setRemoved(false);
        setDelay(0);
        setEventCount(0);
        setVibrate(mInterface.getVibration());
        setNotifyByVoice(mInterface.getVoice());
        setRepeatNotification(mInterface.getNotificationRepeat());
        setUseGlobal(mInterface.getUseGlobal());
        setUnlock(mInterface.getUnlock());
        setAwake(mInterface.getWake());
    }

    public long getDateTime() {
        return TimeUtil.getDateTimeFromGmt(eventTime);
    }

    @Override
    public int getViewType() {
        if (isSame(type, BY_DATE_SHOP)) return SHOPPING;
        else return REMINDER;
    }

    @Override
    public String toString() {
        return SuperUtil.getObjectPrint(this, Reminder.class);
    }

    @Override
    public int hashCode() {
        return UUID.fromString(uuId).hashCode();
    }

    @Override
    public int[] getKeys() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(TimeUtil.getDateTimeFromGmt(eventTime));
        int y = calendar.get(Calendar.YEAR);
        int m = calendar.get(Calendar.MONTH);
        int d = calendar.get(Calendar.DAY_OF_MONTH);
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        return new int[]{y, m, d, h, min};
    }

    public static class Kind {
        public static final int SMS = 2;
        public static final int CALL = 1;
    }
}
