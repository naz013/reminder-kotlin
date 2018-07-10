package com.elementary.tasks.reminder.create_edit.fragments

import android.app.Activity
import android.content.Context

import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder

import androidx.fragment.app.Fragment

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
abstract class TypeFragment : Fragment() {

    private var mContext: Context? = null
    var `interface`: ReminderInterface? = null
        private set

    abstract fun prepare(): Reminder?

    override fun getContext(): Context? {
        return mContext
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (mContext == null) {
            mContext = context
        }
        if (`interface` == null) {
            `interface` = context as ReminderInterface?
            setDefault()
        }
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (mContext == null) {
            mContext = activity
        }
        if (`interface` == null) {
            `interface` = activity as ReminderInterface?
            setDefault()
        }
    }

    private fun setDefault() {
        `interface`!!.setExclusionAction(null)
        `interface`!!.setRepeatAction(null)
        `interface`!!.setEventHint(getString(R.string.remind_me))
        `interface`!!.setHasAutoExtra(false, null)
    }

    open fun onBackPressed(): Boolean {
        return true
    }
}
