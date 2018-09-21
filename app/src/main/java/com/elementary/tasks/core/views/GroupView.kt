package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
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
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    private fun noGroup() {
        text.text = context.getString(R.string.not_selected)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        View.inflate(context, R.layout.view_group, this)
        orientation = LinearLayout.VERTICAL

        text.setOnClickListener {
            onGroupSelectListener?.invoke()
        }
        reminderGroup = null
    }
}
