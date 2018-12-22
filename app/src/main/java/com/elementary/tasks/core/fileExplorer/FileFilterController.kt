package com.elementary.tasks.core.fileExplorer

import com.elementary.tasks.reminder.lists.filters.AbstractFilter
import com.elementary.tasks.reminder.lists.filters.FilterCallback
import com.elementary.tasks.reminder.lists.filters.FilterValue
import com.elementary.tasks.reminder.lists.filters.ObjectFilter
import java.util.*

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
class FileFilterController(private val mCallback: FilterCallback<FileItem>?) {

    private val searchValue = FilterValue<String>()

    var original: List<FileItem> = ArrayList()
        set(original) {
            field = original
            onChanged()
        }
    private var mFilter: ObjectFilter<FileItem>? = null

    init {
        initFilters()
    }

    private fun initFilters() {
        val filter = object : AbstractFilter<String, FileItem>(null) {
            private var query: String? = null

            override fun filter(item: FileItem): Boolean {
                return query == null || query!!.isEmpty() || item.fileName.toLowerCase().contains(query!!.toLowerCase())
            }

            override fun accept(s: String) {
                this.query = s
            }
        }
        searchValue.subscribe(filter)
        this.mFilter = filter
    }

    fun setSearchValue(value: String?) {
        if (value == null) {
            searchValue.setValue("")
        } else {
            searchValue.setValue(value)
        }
        onChanged()
    }

    private fun onChanged() {
        val list = ArrayList<FileItem>()
        for (item in this.original) {
            val filter = mFilter
            if (filter != null) {
                if (filter.filter(item)) list.add(item)
            } else
                list.add(item)
        }
        mCallback?.onChanged(list)
    }
}
