package com.example.Nazar.myapplication.backend;

import com.google.appengine.repackaged.com.google.gson.GsonBuilder;

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

public class NotificationBuilder {

    public NotificationBuilder() {
    }

    public byte[] getMessengerEntity(NotificationForm form) {
        Data data = new Data(form.getType(), form.getVersion(), form.getChanges());
        Body body = new Body(data);
        String json = new GsonBuilder().create().toJson(body);
        return json.getBytes();
    }

    private class Data {
        String type;
        String version;
        String changes;

        Data(String type, String versionName, String changes) {
            this.type = type;
            this.version = versionName;
            this.changes = changes;
        }
    }

    private class Body {
        Data data;
        final String to = "/topics/updates";

        Body(Data data) {
            this.data = data;
        }
    }
}
