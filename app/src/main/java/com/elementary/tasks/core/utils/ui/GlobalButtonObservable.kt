package com.elementary.tasks.core.utils.ui

import android.view.View

typealias GlobalAction = (View, GlobalButtonObservable.Action) -> Unit

class GlobalButtonObservable {

  private val observers: MutableMap<Action, MutableList<GlobalAction>> = mutableMapOf()

  fun addObserver(action: Action, listener: GlobalAction) {
    val list: MutableList<GlobalAction> = if (observers.containsKey(action)) {
      observers[action] ?: mutableListOf()
    } else {
      mutableListOf()
    }
    list.add(listener)
    observers[action] = list
  }

  fun removeObserver(action: Action, listener: GlobalAction) {
    val list: MutableList<GlobalAction> = if (observers.containsKey(action)) {
      observers[action] ?: mutableListOf()
    } else {
      mutableListOf()
    }
    list.remove(listener)
    observers[action] = list
  }

  fun fireAction(view: View, action: Action) {
    if (observers.containsKey(action)) {
      observers[action].let { list ->
        list?.forEach { it.invoke(view, action) }
      }
    }
  }

  enum class Action {
    QUICK_NOTE,
    VOICE
  }
}