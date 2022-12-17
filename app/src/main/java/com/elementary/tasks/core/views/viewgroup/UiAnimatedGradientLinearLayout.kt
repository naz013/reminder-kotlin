package com.elementary.tasks.core.views.viewgroup

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.widget.LinearLayout
import com.elementary.tasks.R

class UiAnimatedGradientLinearLayout : LinearLayout {

  init {
    runCatching {
      val animDrawable = background as AnimationDrawable
      animDrawable.setEnterFadeDuration(10)
      animDrawable.setExitFadeDuration(resources.getInteger(R.integer.banner_anim_duration))
      animDrawable.start()
    }
  }

  constructor(context: Context) : super(context)

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
    context,
    attrs,
    defStyle
  )
}
