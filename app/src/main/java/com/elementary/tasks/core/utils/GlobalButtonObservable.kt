package com.elementary.tasks.core.utils

import android.view.View

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
            mObservers[action].let { list ->
                list?.forEach { it.invoke(view, action) }
            }
        }
    }

    enum class Action {
        QUICK_NOTE,
        VOICE
    }
}