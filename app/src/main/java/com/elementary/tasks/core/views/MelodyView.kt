package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.core.utils.ui.visible
import com.elementary.tasks.databinding.ViewMelodyBinding
import java.io.File

class MelodyView : LinearLayout {

  private lateinit var binding: ViewMelodyBinding
  var onFileUpdateListener: ((path: String) -> Unit)? = null
  var onFileSelectListener: (() -> Unit)? = null
  var file: String = ""
    set(value) {
      field = value
      if (value != "") {
        val file = File(value)
        if (file.exists()) {
          binding.text.text = file.name
          binding.removeButton.visible()
          binding.selectButton.gone()
          onFileUpdateListener?.invoke(value)
        } else {
          noFile()
        }
      } else {
        noFile()
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

  private fun noFile() {
    binding.removeButton.gone()
    binding.selectButton.visible()
    binding.text.text = context.getString(R.string.not_selected)
  }

  private fun init(context: Context) {
    View.inflate(context, R.layout.view_melody, this)
    orientation = VERTICAL
    binding = ViewMelodyBinding.bind(this)

    binding.removeButton.setOnClickListener {
      file = ""
    }
    binding.selectButton.setOnClickListener {
      if (file == "") {
        onFileSelectListener?.invoke()
      }
    }
    file = ""
  }
}
