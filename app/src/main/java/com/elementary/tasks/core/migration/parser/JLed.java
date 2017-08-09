package com.elementary.tasks.core.migration.parser;

import com.elementary.tasks.reminder.models.Reminder;

import org.json.JSONException;
import org.json.JSONObject;

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

class JLed {

    /**
     * JSON keys.
     */
    private static final String COLOR = "color_led";
    private static final String STATUS = "status_led";

    private int color, status;

    /**
     * JSON object.
     */
    private JSONObject jsonObject;

    public void setData(Reminder item) {
        item.setColor(color);
    }

    /**
     * Get current JSON object.
     *
     * @return JSON object string
     */
    @Override
    public String toString() {
        return "JLed->Color: " + color +
                "->Status: " + status;
    }

    JLed(JSONObject jsonObject) {
        if (jsonObject != null) {
            this.jsonObject = jsonObject;
            parse(jsonObject);
        }
    }

    JLed() {
        jsonObject = new JSONObject();
        setColor(0);
        setStatus(-1);
    }

    private void parse(JSONObject jsonObject) {
        if (jsonObject.has(COLOR)) {
            try {
                color = jsonObject.getInt(COLOR);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(STATUS)) {
            try {
                status = jsonObject.getInt(STATUS);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get current JSON object.
     *
     * @return JSON object
     */
    JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setColor(int color) {
        this.color = color;
        try {
            jsonObject.put(COLOR, color);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setStatus(int status) {
        this.status = status;
        try {
            jsonObject.put(STATUS, status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getColor() {
        return color;
    }

    public int getStatus() {
        return status;
    }
}
