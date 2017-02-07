package com.elementary.backend;

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

public class NotificationForm {

    private String type;
    private String version;
    private String changes;

    private NotificationForm() {
    }

    public NotificationForm(String type, String version, String changes) {
        this.type = type;
        this.version = version;
        this.changes = changes;
    }

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public String getChanges() {
        return changes;
    }
}
