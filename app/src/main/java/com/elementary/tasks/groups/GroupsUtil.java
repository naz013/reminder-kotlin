package com.elementary.tasks.groups;

import android.content.Context;

import com.elementary.tasks.R;
import com.elementary.tasks.core.data.AppDb;
import com.elementary.tasks.core.data.dao.GroupDao;
import com.elementary.tasks.core.data.models.Group;

import java.util.Random;

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
public class GroupsUtil {

    public static String initDefault(Context context) {
        Random random = new Random();
        Group def = new Group(context.getString(R.string.general), random.nextInt(16));
        GroupDao dao = AppDb.getAppDatabase(context).groupDao();
        dao.insert(def);
        dao.insert(new Group(context.getString(R.string.work), random.nextInt(16)));
        dao.insert(new Group(context.getString(R.string.personal), random.nextInt(16)));
        return def.getUuId();
    }
}
