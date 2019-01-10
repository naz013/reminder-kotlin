package com.elementary.tasks.voice

import java.util.*

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
class Container<V>(list: List<V>?) {

    var list: MutableList<V> = ArrayList()
    var type: V? = null
        private set

    val isEmpty: Boolean
        get() = list.isEmpty()

    init {
        if (list != null) {
            this.list.clear()
            this.list.addAll(list)
            if (!list.isEmpty()) {
                this.type = list[0]
            }
        }
    }

    override fun toString(): String {
        return "Container of -> $type, size -> ${list.size}"
    }
}
