package com.elementary.tasks.core.views

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UpdateAppearance
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView

class GradientTextView : AppCompatTextView {

  private val sections = mutableListOf<Section>()

  constructor(context: Context) : super(context)

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  )

  fun clearSections() {
    sections.clear()
    text = text.toString()
  }

  fun addStrikeThroughSection(startIndex: Int, endIndex: Int) {
    val textString = text.toString()
    val length = textString.length
    if (startIndex < 0 || startIndex >= length) {
      return
    }
    if (endIndex <= startIndex || endIndex > length) {
      return
    }
    val value = textString.substring(startIndex, endIndex)
    sections.add(StrikeThroughSection(value, startIndex, endIndex))
    prepareSections()
  }

  fun addStrikeThroughSection(value: String) {
    val textString = text.toString()
    val startIndex = textString.indexOf(value)
    if (startIndex != -1) {
      addStrikeThroughSection(startIndex, startIndex + value.length)
    }
  }

  fun addGradientSection(
    startIndex: Int,
    endIndex: Int,
    @ColorInt startColor: Int,
    @ColorInt endColor: Int
  ) {
    val textString = text.toString()
    val length = textString.length
    if (startIndex < 0 || startIndex >= length) {
      return
    }
    if (endIndex <= startIndex || endIndex > length) {
      return
    }
    val value = textString.substring(startIndex, endIndex)
    sections.add(GradientSection(value, startColor, endColor, startIndex, endIndex))
    prepareSections()
  }

  fun addGradientSection(
    startIndex: Int,
    endIndex: Int,
    startColorHex: String,
    endColorHex: String
  ) {
    runCatching {
      val startColor = Color.parseColor(startColorHex)
      val endColor = Color.parseColor(endColorHex)
      addGradientSection(startIndex, endIndex, startColor, endColor)
    }
  }

  fun addGradientSection(value: String, @ColorInt startColor: Int, @ColorInt endColor: Int) {
    val textString = text.toString()
    val startIndex = textString.indexOf(value)
    if (startIndex != -1) {
      addGradientSection(startIndex, startIndex + value.length, startColor, endColor)
    }
  }

  fun addGradientSection(value: String, startColorHex: String, endColorHex: String) {
    runCatching {
      val startColor = Color.parseColor(startColorHex)
      val endColor = Color.parseColor(endColorHex)
      addGradientSection(value, startColor, endColor)
    }
  }

  fun addBoldSection(startIndex: Int, endIndex: Int) {
    val textString = text.toString()
    val length = textString.length
    if (startIndex < 0 || startIndex >= length) {
      return
    }
    if (endIndex <= startIndex || endIndex > length) {
      return
    }
    val value = textString.substring(startIndex, endIndex)
    sections.add(BoldSection(value, startIndex, endIndex))
    prepareSections()
  }

  fun addBoldSection(value: String) {
    val textString = text.toString()
    val startIndex = textString.indexOf(value)
    if (startIndex != -1) {
      addBoldSection(startIndex, startIndex + value.length)
    }
  }

  private fun prepareSections() {
    if (sections.isEmpty()) {
      clearSections()
      return
    }
    val textString = text.toString()
    val spannableString = SpannableString(textString)
    sections.forEach { section ->
      when (section) {
        is StrikeThroughSection -> {
          Log.d("GTV", "Add Strike through $section")
          spannableString.setSpan(
            StrikethroughSpan(),
            section.startIndex,
            section.endIndex,
            Spannable.SPAN_COMPOSING
          )
        }
        is BoldSection -> {
          Log.d("GTV", "Add Bold $section")
          spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            section.startIndex,
            section.endIndex,
            Spannable.SPAN_COMPOSING
          )
        }

        is GradientSection -> {
          Log.d("GTV", "Add Gradient $section")
          spannableString.setSpan(
            GradientSpan(
              textString,
              section.startIndex,
              section.endIndex,
              section.startColor,
              section.endColor
            ),
            section.startIndex,
            section.endIndex,
            Spannable.SPAN_COMPOSING
          )
        }
      }
    }
    text = spannableString
  }

  sealed class Section {
    abstract val value: String
    abstract val startIndex: Int
    abstract val endIndex: Int
  }

  data class StrikeThroughSection(
    override val value: String,
    override val startIndex: Int,
    override val endIndex: Int
  ) : Section()

  data class BoldSection(
    override val value: String,
    override val startIndex: Int,
    override val endIndex: Int
  ) : Section()

  data class GradientSection(
    override val value: String,
    val startColor: Int,
    val endColor: Int,
    override val startIndex: Int,
    override val endIndex: Int
  ) : Section()

  private class GradientSpan(
    private val text: String,
    private val startIndex: Int,
    private val endIndex: Int,
    private val colorStart: Int,
    private val colorEnd: Int
  ) : CharacterStyle(), UpdateAppearance {

    override fun updateDrawState(tp: TextPaint?) {
      tp ?: return
      val startX = tp.measureText(text, 0, startIndex)
      val endX = startX + tp.measureText(text, startIndex, endIndex)

      tp.shader = LinearGradient(
        startX,
        0f,
        endX,
        0f,
        colorStart,
        colorEnd,
        Shader.TileMode.MIRROR
      )
    }
  }
}
