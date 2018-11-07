package com.elementary.tasks.core.dialogs

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.utils.ThemeUtil
import javax.inject.Inject

/**
 * Copyright 2017 Nazar Suhovich
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

abstract class BaseDialog : FragmentActivity() {

    @Inject lateinit var themeUtil: ThemeUtil
    @Inject lateinit var dialogues: Dialogues
    @Inject lateinit var prefs: Prefs
    @Inject lateinit var reminderUtils: ReminderUtils

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(themeUtil.dialogStyle)
    }
}
