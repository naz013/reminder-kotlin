package com.elementary.tasks.core.arch

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil

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
abstract class BaseHolder<B : ViewDataBinding>(parent: ViewGroup, @LayoutRes res: Int)
    : HolderBinding<B>(parent, res) {

    var prefs: Prefs = ReminderApp.appComponent.prefs()
    var themeUtil: ThemeUtil = ReminderApp.appComponent.themeUtil()

}