package com.elementary.tasks.core.utils

import android.app.Activity
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ActivityBindingProperty<out T : ViewDataBinding>(@LayoutRes private val resId: Int) : ReadOnlyProperty<Activity, T> {

    private var binding: T? = null

    override operator fun getValue(thisRef: Activity, property: KProperty<*>): T = binding ?: createBinding(thisRef).also { binding = it }

    private fun createBinding(activity: Activity): T = DataBindingUtil.setContentView(activity, resId)
}