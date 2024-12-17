package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.elementary.tasks.R
import com.github.naz013.logging.Logger

class UiCheckableImageView : AppCompatImageView {

  private var mIsChecked: Boolean = false
  private var checkedIcon: Int = 0
  private var uncheckedIcon: Int = 0

  constructor(context: Context) : super(context) {
    init(context, null)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context, attrs)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
    context,
    attrs,
    defStyle
  ) {
    init(context, attrs)
  }

  fun isChecked(): Boolean {
    return mIsChecked
  }

  fun setChecked(checked: Boolean) {
    this.mIsChecked = checked
    updateIconState()
  }

  private fun updateIconState() {
    if (mIsChecked) {
      setImageResource(checkedIcon)
    } else {
      setImageResource(uncheckedIcon)
    }
  }

  private fun init(context: Context, attrs: AttributeSet?) {
    if (attrs != null) {
      val a = context.theme.obtainStyledAttributes(attrs, R.styleable.UiCheckableImageView, 0, 0)
      try {
        mIsChecked = a.getBoolean(R.styleable.UiCheckableImageView_civ_checked, false)
        checkedIcon = a.getResourceId(R.styleable.UiCheckableImageView_civ_checkedIcon, 0)
        uncheckedIcon = a.getResourceId(R.styleable.UiCheckableImageView_civ_uncheckedIcon, 0)
      } catch (e: Exception) {
        Logger.d("init: ${e.message}")
      } finally {
        a.recycle()
      }
    }
    updateIconState()
  }
}
