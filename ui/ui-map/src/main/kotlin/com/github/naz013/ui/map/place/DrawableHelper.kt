package com.github.naz013.ui.map.place

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

internal class DrawableHelper(
  private val mContext: Context,
) {
  @ColorInt
  private var mColor: Int = 0
  private var mDrawable: Drawable? = null
  private var mWrappedDrawable: Drawable? = null

  fun withDrawable(
    @DrawableRes drawableRes: Int,
  ): DrawableHelper {
    mDrawable = ContextCompat.getDrawable(mContext, drawableRes)
    return this
  }

  fun withColor(
    @ColorInt colorRes: Int,
  ): DrawableHelper {
    mColor = colorRes
    return this
  }

  fun tint(): DrawableHelper {
    if (mDrawable == null) {
      throw NullPointerException("É preciso informar o recurso drawable pelo método withDrawable()")
    }

    if (mColor == 0) {
      throw IllegalStateException(
        "É necessário informar a cor a ser definida pelo método withColor()",
      )
    }

    mWrappedDrawable = mDrawable!!.mutate()
    mWrappedDrawable = DrawableCompat.wrap(mWrappedDrawable!!)
    DrawableCompat.setTint(mWrappedDrawable!!, mColor)
    DrawableCompat.setTintMode(mWrappedDrawable!!, PorterDuff.Mode.SRC_IN)

    return this
  }

  fun applyTo(imageView: ImageView) {
    if (mWrappedDrawable == null) {
      throw NullPointerException("É preciso chamar o método tint()")
    }

    imageView.setImageDrawable(mWrappedDrawable)
  }

  fun get(): Drawable = mWrappedDrawable ?: throw NullPointerException("É preciso chamar o método tint()")

  companion object {
    fun withContext(context: Context): DrawableHelper = DrawableHelper(context)
  }
}
