package com.elementary.tasks.navigation.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Prefs
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
abstract class BaseFragment : Fragment() {

    var callback: FragmentCallback? = null
        private set
    lateinit var prefs: Prefs
        private set

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (callback == null) {
            try {
                callback = context as FragmentCallback?
            } catch (e: ClassCastException) {
            }
        }
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (callback == null) {
            try {
                callback = activity as FragmentCallback?
            } catch (e: ClassCastException) {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs.getInstance(context!!)
    }

    protected fun replaceFragment(fragment: Fragment, title: String) {
        val ft = fragmentManager!!.beginTransaction()
        ft.replace(R.id.main_container, fragment, title)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.addToBackStack(title)
        ft.commit()
        if (callback != null) {
            callback!!.onTitleChange(title)
            callback!!.onFragmentSelect(fragment)
        }
    }
}
