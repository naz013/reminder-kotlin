package com.elementary.tasks.core.views

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.TextView
import com.elementary.tasks.core.utils.Module
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale

open class FixedTextInputEditText : TextInputEditText {

  constructor(context: Context) : super(context)

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

  constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
    context,
    attrs,
    defStyle
  )

  override fun getHint(): CharSequence? {
    return if (isMeizu()) {
      getSuperHintHack()
    } else {
      super.getHint()
    }
  }

  private fun isMeizu(): Boolean {
    val manufacturer = Build.MANUFACTURER.lowercase(Locale.US)
    if (manufacturer.contains("meizu")) {
      return true
    }
    return false
  }

  @SuppressLint("SoonBlockedPrivateApi")
  private fun getSuperHintHack(): CharSequence? {
    if (Module.is15) {
      return hint
    }
    val f = try {
      TextView::class.java.getDeclaredField("mHint")
    } catch (e: Exception) {
      null
    } ?: return null
    f.isAccessible = true
    return f.get(this) as? CharSequence
  }

  override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
    return if (isMeizu()) {
      null
    } else {
      super.onCreateInputConnection(outAttrs)
    }
  }
}
