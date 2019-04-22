package com.elementary.tasks.voice

import java.util.*

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
