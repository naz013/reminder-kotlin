package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.elementary.tasks.R
import com.elementary.tasks.databinding.ViewLegacyBuilderRemovalBinding
import org.koin.core.component.KoinComponent

class ClosableLegacyBuilderRemovalWarningView : FrameLayout, KoinComponent {

  private lateinit var binding: ViewLegacyBuilderRemovalBinding
  var onTryClicked: (() -> Unit)? = null
  var onCloseClicked: (() -> Unit)? = null

  constructor(context: Context) : super(context) {
    init(context, null)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context, attrs)
  }

  constructor(
    context: Context,
    attrs: AttributeSet,
    defStyle: Int
  ) : super(context, attrs, defStyle) {
    init(context, attrs)
  }

  private fun init(context: Context, attrs: AttributeSet?) {
    View.inflate(context, R.layout.view_legacy_builder_removal, this)
    binding = ViewLegacyBuilderRemovalBinding.bind(this)

    binding.tryButton.setOnClickListener { onTryClicked?.invoke() }
    binding.closeButton.setOnClickListener { onCloseClicked?.invoke() }
  }
}
