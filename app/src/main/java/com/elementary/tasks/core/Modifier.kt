package com.elementary.tasks.core

import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import kotlinx.coroutines.Job

abstract class Modifier<V>(private var modifier: Modifier<V>? = null,
                           private var callback: ((List<V>) -> Unit)? = null) {

    private var external: Modifier<V>? = null
    private var mJob: Job? = null
    var original: List<V> = ArrayList()
        set(original) {
            field = original
            onChanged()
        }

    init {
        modifier?.setExternal(this)
    }

    protected open fun apply(data: List<V>): List<V> {
        val mod = modifier
        var list = data
        if (mod != null) {
            list = mod.apply(list)
        }
        return list
    }

    protected fun setExternal(external: Modifier<V>?) {
        this.external = external
    }

    protected fun onChanged() {
        val mod = external
        if (mod != null) {
            mod.onChanged()
        } else {
            mJob?.cancel()
            mJob = launchDefault {
                val list = apply(original)
                withUIContext {
                    callback?.invoke(list)
                }
            }
        }
    }
}