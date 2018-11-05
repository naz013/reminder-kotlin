package com.elementary.tasks.groups.list

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.MeasureUtils
import kotlinx.android.synthetic.main.list_item_group.view.*

/**
 * Copyright 2017 Nazar Suhovich
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
class GroupHolder(parent: ViewGroup, listener: ((View, Int, ListActions) -> Unit)?) :
        BaseHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_group, parent, false)) {

    init {
        itemView.clickView.setOnClickListener { view ->
            listener?.invoke(view, adapterPosition, ListActions.EDIT)
        }
        itemView.button_more.setOnClickListener { view ->
            listener?.invoke(view, adapterPosition, ListActions.MORE)
        }
    }

    fun setData(item: ReminderGroup) {
        itemView.textView.text = item.groupTitle
        gradientBg(itemView.gradientView, item)
    }

    private fun gradientBg(gradientView: ImageView, item: ReminderGroup) {
        val gd = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(Color.TRANSPARENT, themeUtil.getNoteLightColor(item.groupColor)))
        gd.cornerRadius = MeasureUtils.dp2px(gradientView.context, 5).toFloat()
        gradientView.background = gd
    }
}