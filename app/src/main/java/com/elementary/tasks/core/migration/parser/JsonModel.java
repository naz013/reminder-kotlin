package com.elementary.tasks.core.migration.parser;

import com.elementary.tasks.reminder.models.Place;
import com.elementary.tasks.reminder.models.Reminder;
import com.elementary.tasks.reminder.models.ShopItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copyright 2017 Nazar Suhovich
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

public class JsonModel {

    private List<JPlace> places = new ArrayList<>();
    private List<JShopping> shoppings = new ArrayList<>();
    private JExclusion exclusion;
    private JLed led;
    private JMelody melody;
    private JRecurrence recurrence;
    private JAction action;
    private JExport export;
    private JPlace place;
    private String summary;
    private String type;
    private String group;
    private String uuId;
    private long eventTime;
    private long startTime;
    private long count;
    private int vibrate;
    private int notificationRepeat;
    private int voice;
    private int awake;
    private int unlock;

    public void setExtra(Reminder item) {
        item.setVibrate(vibrate == 1);
        item.setRepeatNotification(notificationRepeat == 1);
        item.setNotifyByVoice(voice == 1);
        item.setAwake(awake == 1);
        item.setUnlock(unlock == 1);
        item.setEventCount(count);
        if (places != null && places.size() > 0) {
            List<Place> list = new ArrayList<>();
            for (JPlace place : places) {
                list.add(place.getPlace());
            }
            item.setPlaces(list);
        } else if (place != null) {
            item.setPlaces(Collections.singletonList(place.getPlace()));
        }
        if (export != null) {
            export.setExportData(item);
        }
        if (action != null) {
            action.setActionData(item);
        }
        if (recurrence != null) {
            recurrence.setData(item);
        }
        if (melody != null) {
            melody.setData(item);
        }
        if (led != null) {
            led.setData(item);
        }
        if (exclusion != null) {
            exclusion.setData(item);
        }
        if (shoppings != null && shoppings.size() > 0) {
            List<ShopItem> list = new ArrayList<>();
            for (JShopping shopping : shoppings) {
                list.add(shopping.getItem());
            }
            item.setShoppings(list);
        }
    }

    JsonModel() {
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public JPlace getPlace() {
        return place;
    }

    public void setPlace(JPlace place) {
        this.place = place;
    }

    public JExport getExport() {
        return export;
    }

    public String getType() {
        return type;
    }

    public String getSummary() {
        return summary;
    }

    public JAction getAction() {
        return action;
    }

    public JExclusion getExclusion() {
        return exclusion;
    }

    public JLed getLed() {
        return led;
    }

    public JMelody getMelody() {
        return melody;
    }

    public List<JPlace> getPlaces() {
        return places;
    }

    public List<JShopping> getShoppings() {
        return shoppings;
    }

    public int getAwake() {
        return awake;
    }

    public int getVibrate() {
        return vibrate;
    }

    public int getUnlock() {
        return unlock;
    }

    public int getVoice() {
        return voice;
    }

    public long getCount() {
        return count;
    }

    public long getEventTime() {
        return eventTime;
    }

    public String getGroup() {
        return group;
    }

    public String getUuId() {
        return uuId;
    }

    public void setExclusion(JExclusion exclusion) {
        this.exclusion = exclusion;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setAction(JAction action) {
        this.action = action;
    }

    public void setLed(JLed led) {
        this.led = led;
    }

    public void setMelody(JMelody melody) {
        this.melody = melody;
    }

    public void setPlaces(List<JPlace> places) {
        this.places = places;
    }

    public void setRecurrence(JRecurrence recurrence) {
        this.recurrence = recurrence;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setShoppings(List<JShopping> shoppings) {
        this.shoppings = shoppings;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public void setNotificationRepeat(int notificationRepeat) {
        this.notificationRepeat = notificationRepeat;
    }

    public void setAwake(int awake) {
        this.awake = awake;
    }

    public void setUnlock(int unlock) {
        this.unlock = unlock;
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }

    public void setVibrate(int vibrate) {
        this.vibrate = vibrate;
    }

    public void setVoice(int voice) {
        this.voice = voice;
    }

    public void setExport(JExport export) {
        this.export = export;
    }

    public void setShopping(JShopping shopping) {
        if (shoppings != null) {
            shoppings.add(shopping);
        } else {
            shoppings = new ArrayList<>();
            shoppings.add(shopping);
        }
    }
}
