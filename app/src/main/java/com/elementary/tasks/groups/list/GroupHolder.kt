package com.elementary.tasks.groups.list

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.databinding.ListItemGroupBinding
import org.koin.core.inject

class GroupHolder(parent: ViewGroup, listener: ((View, Int, ListActions) -> Unit)?) :
        BaseHolder<ListItemGroupBinding>(parent, R.layout.list_item_group) {

    private val themeUtil: ThemeUtil by inject()

    init {
        binding.clickView.setOnClickListener { view ->
            listener?.invoke(view, adapterPosition, ListActions.EDIT)
        }
        binding.buttonMore.setOnClickListener { view ->
            listener?.invoke(view, adapterPosition, ListActions.MORE)
        }
    }

    fun setData(item: ReminderGroup) {
        binding.textView.text = item.groupTitle
        gradientBg(binding.gradientView, item)
    }

    private fun gradientBg(gradientView: ImageView, item: ReminderGroup) {
        val gd = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(Color.TRANSPARENT, themeUtil.getNoteLightColor(item.groupColor)))
        gd.cornerRadius = 0f
        gradientView.background = gd
    }
}