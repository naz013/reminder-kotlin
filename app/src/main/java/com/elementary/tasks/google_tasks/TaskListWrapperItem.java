package com.elementary.tasks.google_tasks;

import java.util.ArrayList;
import java.util.List;

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
public class TaskListWrapperItem {

    private TaskListItem taskList;
    private List<TaskItem> mData;
    private int position;

    public TaskListWrapperItem(TaskListItem taskList, List<TaskItem> mData,
                               int position) {
        this.taskList = taskList;
        this.mData = new ArrayList<>(mData);
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public List<TaskItem> getmData() {
        return mData;
    }

    public TaskListItem getTaskList() {
        return taskList;
    }
}
