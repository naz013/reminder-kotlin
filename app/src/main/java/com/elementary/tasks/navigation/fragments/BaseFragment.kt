package com.elementary.tasks.navigation.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.BindingFragment
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.navigation.FragmentCallback

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
abstract class BaseFragment<B : ViewDataBinding> : BindingFragment<B>() {

    var callback: FragmentCallback? = null
        private set
    var prefs: Prefs = ReminderApp.appComponent.prefs()
    var dialogues: Dialogues = ReminderApp.appComponent.dialogues()
    var themeUtil: ThemeUtil = ReminderApp.appComponent.themeUtil()
    var buttonObservable: GlobalButtonObservable = ReminderApp.appComponent.buttonObservable()
    var notifier: Notifier = ReminderApp.appComponent.notifier()
    var isDark = false
        private set
    private var mLastScroll: Int = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (callback == null) {
            try {
                callback = context as FragmentCallback?
            } catch (e: ClassCastException) {
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        isDark = themeUtil.isDark
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    protected fun setScroll(scroll: Int) {
        if (isRemoving) return
        this.mLastScroll = scroll
        callback?.onScrollUpdate(scroll)
    }

    protected fun moveBack() {
        activity?.onBackPressed()
    }

    open fun canGoBack(): Boolean = true

    open fun onBackStackResume() {
        callback?.onFragmentSelect(this)
        callback?.onTitleChange(getTitle())
        setScroll(mLastScroll)
    }

    override fun onResume() {
        super.onResume()
        onBackStackResume()
    }

    abstract fun getTitle(): String
}
