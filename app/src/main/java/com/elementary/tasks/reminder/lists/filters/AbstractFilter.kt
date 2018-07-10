package com.elementary.tasks.reminder.lists.filters

import io.reactivex.functions.Consumer

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
abstract class AbstractFilter<V, O>(private val filter: ObjectFilter<O>?) : ObjectFilter<O>, Consumer<V> {

    override fun filter(o: O): Boolean {
        return filter == null || filter.filter(o)
    }
}
