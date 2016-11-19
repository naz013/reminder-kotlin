package com.elementary.tasks.google_tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.elementary.tasks.core.cloud.GoogleTasks;

import java.io.IOException;

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

public class InsertTaskAsync extends AsyncTask<Void, Void, Boolean> {

    private Context mContext;
    private String title;
    private String note;
    private String listId;
    private long due;
    private TasksCallback mCallback;

    public InsertTaskAsync(Context context, String title, String note, String listId, long due, TasksCallback mCallback) {
        this.mContext = context;
        this.title = title;
        this.note = note;
        this.listId = listId;
        this.due = due;
        this.mCallback = mCallback;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            return new GoogleTasks(mContext).insertTask(title, listId, due, note);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean) {
            if (mCallback != null) mCallback.onComplete();
        } else {
            if (mCallback != null) mCallback.onFailed();
        }
    }
}
