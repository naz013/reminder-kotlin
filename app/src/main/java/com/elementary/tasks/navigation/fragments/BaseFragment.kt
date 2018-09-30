package com.elementary.tasks.navigation.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.navigation.FragmentCallback
import javax.inject.Inject

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
abstract class BaseFragment : Fragment() {

    var callback: FragmentCallback? = null
        private set
    @Inject
    lateinit var prefs: Prefs
    @Inject
    lateinit var dialogues: Dialogues
    @Inject
    lateinit var themeUtil: ThemeUtil
    @Inject
    lateinit var buttonObservable: GlobalButtonObservable
    @Inject
    lateinit var language: Language
    @Inject
    lateinit var notifier: Notifier

    var isDark = false
        private set

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onAttach(context: Context?) {
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
        val res = layoutRes()
        return if (res != 0) {
            inflater.inflate(layoutRes(), container, false)
        } else {
            super.onCreateView(inflater, container, savedInstanceState)
        }
    }

    protected fun moveBack() {
        activity?.onBackPressed()
    }

    open fun canGoBack(): Boolean = true

    open fun onBackStackResume() {
        callback?.onFragmentSelect(this)
        callback?.onTitleChange(getTitle())
        callback?.onScrollUpdate(0)
    }

    override fun onResume() {
        super.onResume()
        onBackStackResume()
    }

    abstract fun getTitle(): String

    @LayoutRes
    open fun layoutRes(): Int = 0
}
