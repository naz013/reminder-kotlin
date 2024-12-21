package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.github.naz013.domain.ReminderGroup
import com.elementary.tasks.databinding.ViewGroupBinding

class GroupView : LinearLayout {

  private lateinit var binding: ViewGroupBinding
  var onGroupSelectListener: (() -> Unit)? = null
  var reminderGroup: ReminderGroup? = null
    set(value) {
      if (value != null && value.groupUuId != "") {
        field = value
        binding.text.text = value.groupTitle
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

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
    context,
    attrs,
    defStyle
  ) {
    init(context)
  }

  private fun noGroup() {
    binding.text.text = context.getString(R.string.not_selected)
  }

  private fun init(context: Context) {
    View.inflate(context, R.layout.view_group, this)
    orientation = VERTICAL
    binding = ViewGroupBinding.bind(this)

    binding.selectButton.setOnClickListener {
      onGroupSelectListener?.invoke()
    }
    reminderGroup = null
  }
}
