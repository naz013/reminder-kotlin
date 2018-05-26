package com.elementary.tasks.core.view_models.groups;

import android.app.Application;

import com.elementary.tasks.core.data.models.Group;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

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
public class GroupsViewModel extends BaseGroupsViewModel {



    public GroupsViewModel(Application application) {
        super(application);
    }

    public void changeGroupColor(@NonNull Group group, int color) {
        isInProgress.postValue(true);
        run(() -> {
            group.setColor(color);
            getAppDb().groupDao().insert(group);
            end(() -> isInProgress.postValue(false));
        });
    }
}
