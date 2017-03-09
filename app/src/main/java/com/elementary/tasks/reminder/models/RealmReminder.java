package com.elementary.tasks.reminder.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

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

public class RealmReminder extends RealmObject {

    @SerializedName("summary")
    private String summary;
    @SerializedName("noteId")
    private String noteId;
    @SerializedName("reminderType")
    private int reminderType;
    @SerializedName("groupUuId")
    private String groupUuId;
    @SerializedName("uuId")
    @PrimaryKey
    private String uuId;
    @SerializedName("eventTime")
    private String eventTime;
    @SerializedName("startTime")
    private String startTime;
    @SerializedName("eventCount")
    private long eventCount;
    @SerializedName("color")
    private int color;
    @SerializedName("status")
    private int status;
    @SerializedName("vibrate")
    private boolean vibrate;
    @SerializedName("repeatNotification")
    private boolean repeatNotification;
    @SerializedName("notifyByVoice")
    private boolean notifyByVoice;
    @SerializedName("awake")
    private boolean awake;
    @SerializedName("unlock")
    private boolean unlock;
    @SerializedName("exportToTasks")
    private boolean exportToTasks;
    @SerializedName("exportToCalendar")
    private boolean exportToCalendar;
    @SerializedName("useGlobal")
    private boolean useGlobal;
    @SerializedName("from")
    private String from;
    @SerializedName("to")
    private String to;
    @SerializedName("hours")
    private RealmList<RealmInteger> hours = new RealmList<>();
    @SerializedName("fileName")
    private String fileName;
    @SerializedName("melodyPath")
    private String melodyPath;
    @SerializedName("volume")
    private int volume;
    @SerializedName("dayOfMonth")
    private int dayOfMonth;
    @SerializedName("repeatInterval")
    private long repeatInterval;
    @SerializedName("repeatLimit")
    private int repeatLimit;
    @SerializedName("after")
    private long after;
    @SerializedName("weekdays")
    private RealmList<RealmInteger> weekdays = new RealmList<>();
    @SerializedName("type")
    private int type;
    @SerializedName("target")
    private String target;
    @SerializedName("subject")
    private String subject;
    @SerializedName("attachmentFile")
    private String attachmentFile;
    @SerializedName("attachmentFiles")
    private RealmList<RealmString> attachmentFiles = new RealmList<>();
    @SerializedName("auto")
    private boolean auto;
    @SerializedName("places")
    private RealmList<RealmPlace2> places = new RealmList<>();
    @SerializedName("shoppings")
    private RealmList<RealmShopItem> shoppings = new RealmList<>();
    @SerializedName("uniqueId")
    private int uniqueId;
    @SerializedName("isActive")
    private boolean isActive;
    @SerializedName("isRemoved")
    private boolean isRemoved;
    @SerializedName("isNotificationShown")
    private boolean isNotificationShown;
    @SerializedName("isLocked")
    private boolean isLocked;

    public RealmReminder() {
    }

    public RealmReminder(Reminder item) {
        this.summary = item.getSummary();
        this.noteId = item.getNoteId();
        this.reminderType = item.getReminderType();
        this.groupUuId = item.getGroupUuId();
        this.uuId = item.getUuId();
        this.eventTime = item.getEventTime();
        this.startTime = item.getStartTime();
        this.eventCount = item.getEventCount();
        this.color = item.getColor();
        this.status = item.getDelay();
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
        this.places = new RealmList<>();
        for (Place place : item.getPlaces()) {
            places.add(new RealmPlace2(place));
        }
        this.shoppings = new RealmList<>();
        for (ShopItem shopItem : item.getShoppings()) {
            shoppings.add(new RealmShopItem(shopItem));
        }
    }

    private RealmList<RealmInteger> wrapIntegerArray(List<Integer> list) {
        RealmList<RealmInteger> strings = new RealmList<>();
        if (list != null) {
            for (Integer integer : list) {
                strings.add(new RealmInteger(integer));
            }
        }
        return strings;
    }

    private RealmList<RealmString> wrapStringArray(List<String> list) {
        RealmList<RealmString> strings = new RealmList<>();
        if (list != null) {
            for (String string : list) {
                strings.add(new RealmString(string));
            }
        }
        return strings;
    }

    public boolean isNotificationShown() {
        return isNotificationShown;
    }

    public void setNotificationShown(boolean notificationShown) {
        isNotificationShown = notificationShown;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setRemoved(boolean removed) {
        isRemoved = removed;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public void setHours(RealmList<RealmInteger> hours) {
        this.hours = hours;
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setAttachmentFiles(RealmList<RealmString> attachmentFiles) {
        this.attachmentFiles = attachmentFiles;
    }

    public void setWeekdays(RealmList<RealmInteger> weekdays) {
        this.weekdays = weekdays;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public boolean isUseGlobal() {
        return useGlobal;
    }

    public void setUseGlobal(boolean useGlobal) {
        this.useGlobal = useGlobal;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public int getReminderType() {
        return reminderType;
    }

    public void setReminderType(int reminderType) {
        this.reminderType = reminderType;
    }

    public String getGroupUuId() {
        return groupUuId;
    }

    public void setGroupUuId(String groupUuId) {
        this.groupUuId = groupUuId;
    }

    public String getUuId() {
        return uuId;
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public long getEventCount() {
        return eventCount;
    }

    public void setEventCount(long eventCount) {
        this.eventCount = eventCount;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public boolean isRepeatNotification() {
        return repeatNotification;
    }

    public void setRepeatNotification(boolean repeatNotification) {
        this.repeatNotification = repeatNotification;
    }

    public boolean isNotifyByVoice() {
        return notifyByVoice;
    }

    public void setNotifyByVoice(boolean notifyByVoice) {
        this.notifyByVoice = notifyByVoice;
    }

    public boolean isAwake() {
        return awake;
    }

    public void setAwake(boolean awake) {
        this.awake = awake;
    }

    public boolean isUnlock() {
        return unlock;
    }

    public void setUnlock(boolean unlock) {
        this.unlock = unlock;
    }

    public boolean isExportToTasks() {
        return exportToTasks;
    }

    public void setExportToTasks(boolean exportToTasks) {
        this.exportToTasks = exportToTasks;
    }

    public boolean isExportToCalendar() {
        return exportToCalendar;
    }

    public void setExportToCalendar(boolean exportToCalendar) {
        this.exportToCalendar = exportToCalendar;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<RealmInteger> getHours() {
        return hours;
    }

    public void setHours(List<Integer> hours) {
        this.hours = wrapIntegerArray(hours);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMelodyPath() {
        return melodyPath;
    }

    public void setMelodyPath(String melodyPath) {
        this.melodyPath = melodyPath;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public int getRepeatLimit() {
        return repeatLimit;
    }

    public void setRepeatLimit(int repeatLimit) {
        this.repeatLimit = repeatLimit;
    }

    public long getAfter() {
        return after;
    }

    public void setAfter(long after) {
        this.after = after;
    }

    public List<RealmInteger> getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(List<Integer> weekdays) {
        this.weekdays = wrapIntegerArray(weekdays);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAttachmentFile() {
        return attachmentFile;
    }

    public void setAttachmentFile(String attachmentFile) {
        this.attachmentFile = attachmentFile;
    }

    public List<RealmString> getAttachmentFiles() {
        return attachmentFiles;
    }

    public void setAttachmentFiles(List<String> attachmentFiles) {
        this.attachmentFiles = wrapStringArray(attachmentFiles);
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public List<RealmPlace2> getPlaces() {
        return places;
    }

    public void setPlaces(RealmList<RealmPlace2> places) {
        this.places = places;
    }

    public List<RealmShopItem> getShoppings() {
        return shoppings;
    }

    public void setShoppings(RealmList<RealmShopItem> shoppings) {
        this.shoppings = shoppings;
    }
}
