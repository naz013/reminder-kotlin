package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.ReminderGroup
import kotlinx.android.synthetic.main.view_group.view.*

/**
 * Copyright 2018 Nazar Suhovich
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
class GroupView : LinearLayout {

    var onGroupUpdateListener: ((group: ReminderGroup) -> Unit)? = null
    var onGroupSelectListener: (() -> Unit)? = null
    var reminderGroup: ReminderGroup? = null
        set(value) {
            if (value != null && value.groupUuId != "") {
                field = value
                text.text = value.groupTitle
                onGroupUpdateListener?.invoke(value)
            } else {
                noGroup()
            }
        }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    private fun noGroup() {
        text.text = context.getString(R.string.not_selected)
    }

    private fun init(context: Context) {
        View.inflate(context, R.layout.view_group, this)
        orientation = LinearLayout.VERTICAL

        text.setOnClickListener {
            onGroupSelectListener?.invoke()
        }
        hintIcon.setOnLongClickListener {
            Toast.makeText(context, context.getString(R.string.change_group), Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }
        TooltipCompat.setTooltipText(hintIcon, context.getString(R.string.change_group))
        reminderGroup = null
    }
}
