package com.elementary.tasks.core.migration.parser;

import com.elementary.tasks.core.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
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

public class JParser {

    /**
     * JSON keys.
     */
    private static final String VOICE_NOTIFICATION = "voice_notification";
    private static final String AWAKE_SCREEN = "awake_screen";
    private static final String UNLOCK_SCREEN = "unlock_screen";
    private static final String EXCLUSION = "exclusion";
    private static final String RECURRENCE = "recurrence";
    private static final String EVENT_TIME = "event_time";
    private static final String START_DATE = "event_start";
    private static final String EXPORT = "export";
    private static final String COUNT = "count";
    private static final String ACTION = "action";
    private static final String SUMMARY = "summary";
    private static final String MELODY = "melody";
    private static final String VIBRATION = "vibration";
    private static final String CATEGORY = "category";
    private static final String NOTIFICATION_REPEAT = "notification_repeat";
    private static final String LED = "led";
    private static final String PLACES = "places";
    private static final String PLACE = "place";
    private static final String UUID = "uuid";
    private static final String TYPE = "reminder_type";
    private static final String SHOPPING = "shopping";

    public static final String VERSION = "1.0";

    private JSONObject jsonObject;

    public JParser(String jsonObject) {
        try {
            this.jsonObject = new JSONObject(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JsonModel parse() {
        if (!jsonObject.has(Constants.COLUMN_TECH_VAR)) {
            JsonModel model = new JsonModel();
            model.setAction(getAction());
            model.setExport(getExport());
            model.setSummary(getSummary());
            model.setNotificationRepeat(getNotificationRepeat());
            model.setVoice(getVoice());
            model.setUnlock(getUnlock());
            model.setPlaces(getPlaces());
            model.setMelody(getMelody());
            model.setUuId(getUuid());
            model.setAwake(getAwake());
            model.setVibrate(getVibrate());
            model.setType(getType());
            model.setGroup(getCategory());
            model.setCount(getCount());
            model.setEventTime(getEventTime());
            model.setStartTime(getStartTime());
            model.setExclusion(getExclusion());
            model.setRecurrence(getRecurrence());
            model.setShoppings(getShoppings());
            model.setLed(getLed());
            model.setPlace(getPlace());
            return model;
        }
        return null;
    }

    public void setStartTime(long startDate) {
        try {
            jsonObject.put(START_DATE, startDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setEventTime(long startDate) {
        try {
            jsonObject.put(EVENT_TIME, startDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setType(String type) {
        try {
            jsonObject.put(TYPE, type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setSummary(String summary) {
        try {
            jsonObject.put(SUMMARY, summary);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setUuid(String uuid) {
        try {
            jsonObject.put(UUID, uuid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setExport(JExport export) {
        try {
            if (export != null) jsonObject.put(EXPORT, export.getJsonObject());
            else jsonObject.put(EXPORT, new JExport().getJsonObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setMelody(JMelody melody) {
        try {
            if (melody != null) jsonObject.put(MELODY, melody.getJsonObject());
            else jsonObject.put(MELODY, new JMelody().getJsonObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setExclusion(JExclusion exclusion) {
        try {
            if (exclusion != null) jsonObject.put(EXCLUSION, exclusion.getJsonObject());
            else jsonObject.put(EXCLUSION, new JExclusion().getJsonObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setLed(JLed led) {
        try {
            if (led != null) jsonObject.put(LED, led.getJsonObject());
            else jsonObject.put(LED, new JLed().getJsonObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setAction(JAction action) {
        try {
            if (action != null) jsonObject.put(ACTION, action.getJsonObject());
            else jsonObject.put(ACTION, new JAction().getJsonObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setPlace(JPlace jPlace) {
        try {
            if (jPlace != null) jsonObject.put(PLACE, jPlace.getJsonObject());
            else jsonObject.put(PLACE, new JPlace().getJsonObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setPlaces(List<JPlace> list) {
        if (list != null) {
            JSONArray array = new JSONArray();
            for (JPlace place : list) {
                array.put(place.getJsonObject());
            }
            try {
                jsonObject.put(PLACES, array);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setShopping(List<JShopping> list) {
        if (list != null) {
            JSONObject array = new JSONObject();
            try {
                for (JShopping shopping : list) {
                    array.put(shopping.getUuId(), shopping.getJsonObject());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                jsonObject.put(SHOPPING, array);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public List<JShopping> getShoppings() {
        if (jsonObject.has(SHOPPING)) {
            try {
                List<JShopping> places = new ArrayList<>();
                JSONObject object = jsonObject.getJSONObject(SHOPPING);
                Iterator<String> keys = object.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    places.add(new JShopping(object.getJSONObject(key)));
                }
                return places;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private JRecurrence getRecurrence() {
        if (jsonObject.has(RECURRENCE)) {
            try {
                JSONObject object = jsonObject.getJSONObject(RECURRENCE);
                return new JRecurrence(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JRecurrence();
    }

    public JExport getExport() {
        if (jsonObject.has(EXPORT)) {
            try {
                JSONObject object = jsonObject.getJSONObject(EXPORT);
                return new JExport(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JExport();
    }

    public JPlace getPlace() {
        if (jsonObject.has(PLACE)) {
            try {
                JSONObject object = jsonObject.getJSONObject(PLACE);
                return new JPlace(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JPlace();
    }

    public JExclusion getExclusion() {
        if (jsonObject.has(EXCLUSION)) {
            try {
                JSONObject object = jsonObject.getJSONObject(EXCLUSION);
                return new JExclusion(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JExclusion();
    }

    public JLed getLed() {
        if (jsonObject.has(LED)) {
            try {
                JSONObject object = jsonObject.getJSONObject(LED);
                return new JLed(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JLed();
    }

    public long getEventTime() {
        if (jsonObject.has(EVENT_TIME)) {
            try {
                return jsonObject.getLong(EVENT_TIME);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public long getStartTime() {
        if (jsonObject.has(START_DATE)) {
            try {
                return jsonObject.getLong(START_DATE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public void setCount(long count) {
        if (jsonObject != null) {
            try {
                jsonObject.put(COUNT, count);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public long getCount() {
        if (jsonObject.has(COUNT)) {
            try {
                return jsonObject.getLong(COUNT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private String getCategory() {
        if (jsonObject.has(CATEGORY)) {
            try {
                return jsonObject.getString(CATEGORY);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getType() {
        if (jsonObject.has(TYPE)) {
            try {
                return jsonObject.getString(TYPE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public int getVibrate() {
        if (jsonObject.has(VIBRATION)) {
            try {
                return jsonObject.getInt(VIBRATION);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public int getAwake() {
        if (jsonObject.has(AWAKE_SCREEN)) {
            try {
                return jsonObject.getInt(AWAKE_SCREEN);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public List<JPlace> getPlaces() {
        if (jsonObject.has(PLACES)) {
            try {
                List<JPlace> places = new ArrayList<>();
                JSONArray array = jsonObject.getJSONArray(PLACES);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    places.add(new JPlace(object));
                }
                return places;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public int getVoice() {
        if (jsonObject.has(VOICE_NOTIFICATION)) {
            try {
                return jsonObject.getInt(VOICE_NOTIFICATION);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public int getUnlock() {
        if (jsonObject.has(UNLOCK_SCREEN)) {
            try {
                return jsonObject.getInt(UNLOCK_SCREEN);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public String getUuid() {
        if (jsonObject.has(UUID)) {
            try {
                return jsonObject.getString(UUID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public JMelody getMelody() {
        if (jsonObject.has(MELODY)) {
            try {
                JSONObject object = jsonObject.getJSONObject(MELODY);
                return new JMelody(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JMelody();
    }

    private int getNotificationRepeat() {
        if (jsonObject.has(NOTIFICATION_REPEAT)) {
            try {
                return jsonObject.getInt(NOTIFICATION_REPEAT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public JAction getAction() {
        if (jsonObject.has(ACTION)) {
            try {
                JSONObject object = jsonObject.getJSONObject(ACTION);
                return new JAction(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JAction();
    }

    public String getSummary() {
        if (jsonObject.has(SUMMARY)) {
            try {
                return jsonObject.getString(SUMMARY);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
