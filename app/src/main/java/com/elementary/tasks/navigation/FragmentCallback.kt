package com.elementary.tasks.navigation

import android.view.View

import com.elementary.tasks.core.views.FilterView

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

/**
 * Copyright 2016 Nazar Suhovich
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
interface FragmentCallback {

    val isFiltersVisible: Boolean
    fun replaceFragment(fragment: Fragment, title: String)

    fun onTitleChange(title: String)

    fun onFragmentSelect(fragment: Fragment)

    fun onThemeChange(primary: Int, primaryDark: Int, accent: Int)

    fun refreshMenu()

    fun onMenuSelect(menu: Int)

    fun onScrollChanged(recyclerView: RecyclerView?)

    fun addFilters(filters: List<FilterView.Filter>, clear: Boolean)

    fun hideFilters()
}
