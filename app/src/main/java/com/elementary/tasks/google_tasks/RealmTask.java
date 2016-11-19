package com.elementary.tasks.google_tasks;

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

public class RealmTask extends RealmObject {

    private String title;
    @PrimaryKey
    private String taskId;
    private long completeDate;
    private int del;
    private long dueDate;
    private String eTag;
    private String kind;
    private String notes;
    private String parent;
    private String position;
    private String selfLink;
    private long updateDate;
    private String listId;
    private String status;
    private String uuId;
    private int hidden;

    public RealmTask() {
    }

    public RealmTask(TaskItem item) {
        this.listId = item.getListId();
        this.selfLink = item.getSelfLink();
        this.kind = item.getKind();
        this.eTag = item.geteTag();
        this.title = item.getTitle();
        this.taskId = item.getTaskId();
        this.completeDate = item.getCompleteDate();
        this.del = item.getDel();
        this.hidden = item.getHidden();
        this.dueDate = item.getDueDate();
        this.notes = item.getNotes();
        this.parent = item.getParent();
        this.position = item.getPosition();
        this.updateDate = item.getUpdateDate();
        this.status = item.getStatus();
        this.uuId = item.getUuId();
    }

    public String getUuId() {
        return uuId;
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public long getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(long completeDate) {
        this.completeDate = completeDate;
    }

    public int getDel() {
        return del;
    }

    public void setDel(int del) {
        this.del = del;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getHidden() {
        return hidden;
    }

    public void setHidden(int hidden) {
        this.hidden = hidden;
    }
}
