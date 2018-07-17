package com.elementary.tasks.core.viewModels.groups

import android.app.Application
import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.utils.withUIContext
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

/**
 * Copyright 2018 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class GroupsViewModel(application: Application) : BaseGroupsViewModel(application) {

    fun changeGroupColor(group: Group, color: Int) {
        isInProgress.postValue(true)
        launch(CommonPool) {
            group.color = color
            appDb.groupDao().insert(group)
            withUIContext { isInProgress.postValue(false) }
        }
    }
}
