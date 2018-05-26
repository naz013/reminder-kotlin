package com.elementary.tasks.core.view_models;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.elementary.tasks.ReminderApp;
import com.elementary.tasks.core.data.AppDb;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;

/**
 * Copyright 2018 Nazar Suhovich
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
public class BaseDbViewModel extends AndroidViewModel implements LifecycleObserver {

    public MutableLiveData<Commands> result = new MutableLiveData<>();
    public MutableLiveData<Boolean> isInProgress = new MutableLiveData<>();

    @Inject
    private AppDb appDb;
    private Handler handler = new Handler(Looper.getMainLooper());

    public BaseDbViewModel(Application application) {
        super(application);
        ReminderApp.getAppComponent().inject(this);
    }

    protected Handler getHandler() {
        return handler;
    }

    protected AppDb getAppDb() {
        return appDb;
    }

    protected void end(@NonNull Runnable runnable) {
        getHandler().post(runnable);
    }

    protected void run(@NonNull Runnable runnable) {
        new Thread(runnable).start();
    }
}
