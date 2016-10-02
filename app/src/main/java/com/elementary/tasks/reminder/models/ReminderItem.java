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

import com.elementary.tasks.core.RecyclerInterface;

import java.util.ArrayList;
import java.util.List;

import io.realm.annotations.PrimaryKey;

public class ReminderItem implements RecyclerInterface {

    public static final int REMINDER = 0;
    public static final int SHOPPING = 1;

    private String summary;
    private String type;
    private String groupUuId;
    private String uuId;
    private String eventTime;
    private String startTime;
    private long eventCount;
    @PrimaryKey
    private long id;
    private boolean vibrate;
    private boolean repeatNotification;
    private boolean notifyByVoice;
    private boolean awake;
    private boolean unlock;
    private Exclusion exclusion;
    private Led led;
    private Melody melody;
    private Recurrence recurrence;
    private Action action;
    private Export export;
    private Place place;
    private List<Place> places = new ArrayList<>();
    private List<ShopItem> shoppings = new ArrayList<>();
    private List<String> tags = new ArrayList<>();

    public ReminderItem(String summary, String type, String groupUuId, String uuId, String eventTime,
                        String startTime, long eventCount, long id, boolean vibrate, boolean repeatNotification,
                        boolean notifyByVoice, boolean awake, boolean unlock, Exclusion exclusion, Led led,
                        Melody melody, Recurrence recurrence, Action action, Export export, Place place,
                        List<Place> places, List<ShopItem> shoppings, List<String> tags) {
        this.summary = summary;
        this.type = type;
        this.groupUuId = groupUuId;
        this.uuId = uuId;
        this.eventTime = eventTime;
        this.startTime = startTime;
        this.eventCount = eventCount;
        this.id = id;
        this.vibrate = vibrate;
        this.repeatNotification = repeatNotification;
        this.notifyByVoice = notifyByVoice;
        this.awake = awake;
        this.unlock = unlock;
        this.exclusion = exclusion;
        this.led = led;
        this.melody = melody;
        this.recurrence = recurrence;
        this.action = action;
        this.export = export;
        this.place = place;
        this.places = places;
        this.shoppings = shoppings;
        this.tags = tags;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public Exclusion getExclusion() {
        return exclusion;
    }

    public void setExclusion(Exclusion exclusion) {
        this.exclusion = exclusion;
    }

    public Led getLed() {
        return led;
    }

    public void setLed(Led led) {
        this.led = led;
    }

    public Melody getMelody() {
        return melody;
    }

    public void setMelody(Melody melody) {
        this.melody = melody;
    }

    public Recurrence getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(Recurrence recurrence) {
        this.recurrence = recurrence;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Export getExport() {
        return export;
    }

    public void setExport(Export export) {
        this.export = export;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public List<Place> getPlaces() {
        return places;
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
    }

    public List<ShopItem> getShoppings() {
        return shoppings;
    }

    public void setShoppings(List<ShopItem> shoppings) {
        this.shoppings = shoppings;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public int getViewType() {
        if (type == null) return 0;

        return 0;
    }
}
