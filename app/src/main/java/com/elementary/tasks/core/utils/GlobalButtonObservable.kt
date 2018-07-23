package com.elementary.tasks.core.utils

import android.view.View

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
class GlobalButtonObservable {

    private val mObservers: MutableMap<Action, MutableList<((View, Action) -> Unit)>> = mutableMapOf()

    fun addObserver(action: Action, listener: (View, Action) -> Unit) {
        val list: MutableList<((View, Action) -> Unit)> = if (mObservers.containsKey(action)) {
            mObservers[action] ?: mutableListOf()
        } else {
            mutableListOf()
        }
        list.add(listener)
        mObservers[action] = list
    }

    fun removeObserver(action: Action, listener: (View, Action) -> Unit) {
        val list: MutableList<((View, Action) -> Unit)> = if (mObservers.containsKey(action)) {
            mObservers[action] ?: mutableListOf()
        } else {
            mutableListOf()
        }
        list.remove(listener)
        mObservers[action] = list
    }

    fun fireAction(view: View, action: Action) {
        if (mObservers.containsKey(action)) {
            mObservers[action].let {
                it?.forEach { it.invoke(view, action) }
            }
        }
    }

    enum class Action {
        QUICK_NOTE,
        VOICE
    }
}