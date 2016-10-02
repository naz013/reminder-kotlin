/*
 * Copyright 2015 Nazar Suhovich
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

import java.util.ArrayList;
import java.util.List;

public class Action {

    private String type;
    private String target;
    private String subject;
    private String attachmentFile;
    private List<String> attachmentFiles = new ArrayList<>();
    private boolean auto;

    public Action(String type, String target, String subject, String attachmentFile, List<String> attachmentFiles, boolean auto) {
        this.type = type;
        this.target = target;
        this.subject = subject;
        this.attachmentFile = attachmentFile;
        this.attachmentFiles = attachmentFiles;
        this.auto = auto;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
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

    public List<String> getAttachmentFiles() {
        return attachmentFiles;
    }

    public void setAttachmentFiles(List<String> attachmentFiles) {
        this.attachmentFiles = attachmentFiles;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    @Override
    public String toString(){
        return "Action->Type: " + type +
                "->Target: " + target +
                "->Subject: " + subject +
                "->Attachment: " + attachmentFile +
                "->Auto: " + auto;
    }
}
