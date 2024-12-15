package com.elementary.tasks.core.utils.ui

import android.content.Context
import android.view.View
import android.widget.EditText
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import com.elementary.tasks.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

fun View.applyTopInsets(
  topExtra: Int = 0
) {
  ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
    val innerPadding = insets.getInsets(
      WindowInsetsCompat.Type.systemBars() or
        WindowInsetsCompat.Type.displayCutout()
    )
    v.setPadding(
      /* left = */ v.paddingLeft,
      /* top = */ innerPadding.top + topExtra,
      /* right = */ v.paddingRight,
      /* bottom = */ v.paddingBottom
    )
    insets
  }
}

fun View.applyBottomInsetsMargin(
  @DimenRes bottomExtraRes: Int = -1
) {
  val bottomMargin = if (bottomExtraRes != -1) {
    context.resources.getDimensionPixelSize(bottomExtraRes)
  } else {
    0
  }
  ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
    val innerPadding = insets.getInsets(
      WindowInsetsCompat.Type.systemBars() or
        WindowInsetsCompat.Type.displayCutout()
    )
    v.applyMarginsPx(bottom = v.marginBottom + bottomMargin + innerPadding.bottom)
    insets
  }
}

fun View.applyBottomInsets(
  @DimenRes bottomExtraRes: Int = -1
) {
  val bottomMargin = if (bottomExtraRes != -1) {
    context.resources.getDimensionPixelSize(bottomExtraRes)
  } else {
    0
  }
  ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
    val innerPadding = insets.getInsets(
      WindowInsetsCompat.Type.systemBars() or
        WindowInsetsCompat.Type.displayCutout()
    )
    v.setPadding(
      /* left = */ v.paddingLeft,
      /* top = */ v.paddingTop,
      /* right = */ v.paddingRight,
      /* bottom = */ innerPadding.bottom + bottomMargin
    )
    insets
  }
}

fun View.applyInsets(
  leftExtra: Int = 0,
  topExtra: Int = 0,
  rightExtra: Int = 0,
  bottomExtra: Int = 0
) {
  ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
    val innerPadding = insets.getInsets(
      WindowInsetsCompat.Type.systemBars() or
        WindowInsetsCompat.Type.displayCutout()
    )
    v.setPadding(
      /* left = */ innerPadding.left + leftExtra,
      /* top = */ innerPadding.top + topExtra,
      /* right = */ innerPadding.right + rightExtra,
      /* bottom = */ innerPadding.bottom + bottomExtra
    )
    insets
  }
}

fun EditText.applyEditTextInsets() {
  ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
    val innerPadding = insets.getInsets(
      WindowInsetsCompat.Type.systemBars() or
        WindowInsetsCompat.Type.displayCutout() or
        WindowInsetsCompat.Type.ime()
    )
    v.setPadding(
      /* left = */ innerPadding.left,
      /* top = */ innerPadding.top,
      /* right = */ innerPadding.right,
      /* bottom = */ innerPadding.bottom
    )
    insets
  }
}

fun FloatingActionButton.applyMargins(
  @DimenRes endRes: Int = R.dimen.fab_margin,
  @DimenRes bottomRes: Int = R.dimen.fab_margin,
  addInsets: Boolean = false
) {
  val endMarginExtra = if (endRes != -1) {
    context.resources.getDimensionPixelSize(endRes)
  } else {
    0
  }
  val bottomMarginExtra = if (bottomRes != -1) {
    context.resources.getDimensionPixelSize(bottomRes)
  } else {
    0
  }

  if (addInsets) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
      val innerPadding = insets.getInsets(
        WindowInsetsCompat.Type.systemBars() or
          WindowInsetsCompat.Type.displayCutout()
      )
      v.applyMarginsPx(
        end = endMarginExtra + innerPadding.right,
        bottom = bottomMarginExtra + innerPadding.bottom
      )
      insets
    }
  } else {
    applyMarginsPx(
      end = endMarginExtra,
      bottom = bottomMarginExtra
    )
  }
}

@Px
private fun Int.getPx(context: Context): Int {
  return if (this == -1) {
    0
  } else {
    context.resources.getDimensionPixelSize(this)
  }
}
