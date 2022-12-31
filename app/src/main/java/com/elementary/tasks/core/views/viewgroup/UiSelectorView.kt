package com.elementary.tasks.core.views.viewgroup

import android.content.Context
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.elementary.tasks.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import timber.log.Timber

class UiSelectorView : LinearLayout {

  var onItemSelectedListener: OnItemSelectedListener? = null

  private var index = 0
  private var emptyText = ""
  private val items = mutableListOf<String>()

  private lateinit var buttonLeft: AppCompatImageView
  private lateinit var buttonRight: AppCompatImageView
  private lateinit var labelView: TextView

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

  fun setEmptyText(text: String) {
    this.emptyText = text
    updateSelection()
  }

  fun setItems(items: List<String>) {
    this.items.clear()
    this.items.addAll(items)
    if (index < 0 || index >= items.size) {
      index = 0
    }
    updateSelection()
    updateButtons()
  }

  private fun init(context: Context, attrs: AttributeSet?) {
    View.inflate(context, R.layout.view_selector, this)

    orientation = HORIZONTAL
    descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS

    buttonLeft = findViewById(R.id.left_button)
    buttonRight = findViewById(R.id.right_button)
    labelView = findViewById(R.id.label_view)

    buttonLeft.setOnClickListener { moveLeft() }
    buttonRight.setOnClickListener { moveRight() }
    labelView.setOnClickListener { showSelectionDialog() }

    if (attrs != null) {
      val a = context.theme.obtainStyledAttributes(attrs, R.styleable.UiSelectorView, 0, 0)
      try {
        val showDialog = a.getBoolean(R.styleable.UiSelectorView_selector_canShowDialog, true)
        emptyText = a.getString(R.styleable.UiSelectorView_selector_emptyText) ?: ""
        val leftBackgroundColor =
          a.getColor(R.styleable.UiSelectorView_selector_leftButtonBackground, 0)
        val rightBackgroundColor =
          a.getColor(R.styleable.UiSelectorView_selector_rightButtonBackground, 0)
        val labelBackgroundColor = a.getColor(R.styleable.UiSelectorView_selector_textBackground, 0)

        val leftIconColor = a.getColor(R.styleable.UiSelectorView_selector_leftIconTintColor, 0)
        val rightIconColor = a.getColor(R.styleable.UiSelectorView_selector_rightIconTintColor, 0)

        a.getDrawable(R.styleable.UiSelectorView_selector_leftIcon)
          ?.let { DrawableCompat.wrap(it).mutate() }
          ?.also {
            if (leftIconColor != 0) {
              it.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                leftIconColor,
                BlendModeCompat.SRC_ATOP
              )
            }
          }
          ?.also { buttonLeft.setImageDrawable(it) }
        a.getDrawable(R.styleable.UiSelectorView_selector_rightIcon)
          ?.let { DrawableCompat.wrap(it).mutate() }
          ?.also {
            if (rightIconColor != 0) {
              it.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                rightIconColor,
                BlendModeCompat.SRC_ATOP
              )
            }
          }
          ?.also { buttonRight.setImageDrawable(it) }

        if (leftBackgroundColor != 0) {
          buttonLeft.setBackgroundColor(leftBackgroundColor)
        }
        if (rightBackgroundColor != 0) {
          buttonRight.setBackgroundColor(rightBackgroundColor)
        }
        if (labelBackgroundColor != 0) {
          labelView.setBackgroundColor(labelBackgroundColor)
        }

        labelView.isEnabled = showDialog
      } catch (e: Exception) {
        Timber.d("init: ${e.message}")
      } finally {
        a.recycle()
      }
    }
    updateSelection()
    updateButtons()
  }

  fun setSelectedItemPosition(position: Int) {
    if (isIndexValid(position)) {
      index = position
      updateSelection()
      updateButtons()
      onItemSelectedListener?.onItemSelected(this, position)
    }
  }

  fun selectedItemPosition(): Int {
    return index
  }

  private fun showSelectionDialog() {
    if (items.isEmpty()) return

    MaterialAlertDialogBuilder(context)
      .setItems(items.toTypedArray()) { dialog, which ->
        dialog.dismiss()
        setSelectedItemPosition(which)
      }
      .create()
      .show()
  }

  private fun moveLeft() {
    if (canMoveLeft()) {
      index--
      updateSelection()
      updateButtons()
      onItemSelectedListener?.onItemSelected(this, index)
    }
  }

  private fun moveRight() {
    if (canMoveRight()) {
      index++
      updateSelection()
      updateButtons()
      onItemSelectedListener?.onItemSelected(this, index)
    }
  }

  private fun canMoveLeft(): Boolean {
    return index > 0
  }

  private fun canMoveRight(): Boolean {
    return index < items.size - 1
  }

  private fun updateSelection() {
    if (isIndexValid(index)) {
      labelView.text = items[index]
    } else {
      labelView.text = emptyText
    }
  }

  private fun isIndexValid(index: Int): Boolean {
    return index >= 0 && index < items.size
  }

  private fun updateButtons() {
    buttonLeft.isEnabled = canMoveLeft()
    buttonRight.isEnabled = canMoveRight()
  }

  interface OnItemSelectedListener {
    fun onItemSelected(view: UiSelectorView, position: Int)
  }
}
