package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.views.GroupViewBinding
import com.elementary.tasks.core.data.models.ReminderGroup

class GroupView : LinearLayout {

  private lateinit var binding: GroupViewBinding
  var onGroupUpdateListener: ((group: ReminderGroup) -> Unit)? = null
  var onGroupSelectListener: (() -> Unit)? = null
  var reminderGroup: ReminderGroup? = null
    set(value) {
      if (value != null && value.groupUuId != "") {
        field = value
        binding.text.text = value.groupTitle
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
    binding.text.text = context.getString(R.string.not_selected)
  }

  private fun init(context: Context) {
    View.inflate(context, R.layout.view_group, this)
    orientation = VERTICAL
    binding = GroupViewBinding(this)

    binding.text.setOnClickListener {
      onGroupSelectListener?.invoke()
    }
    binding.hintIcon.setOnLongClickListener {
      Toast.makeText(context, context.getString(R.string.change_group), Toast.LENGTH_SHORT).show()
      return@setOnLongClickListener true
    }
    TooltipCompat.setTooltipText(binding.hintIcon, context.getString(R.string.change_group))
    reminderGroup = null
  }
}
