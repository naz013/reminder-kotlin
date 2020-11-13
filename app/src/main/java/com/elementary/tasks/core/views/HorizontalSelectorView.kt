package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.views.HorizontalSelectorViewBinding
import com.elementary.tasks.core.utils.Dialogues

class HorizontalSelectorView : LinearLayout {

  private lateinit var binding: HorizontalSelectorViewBinding
  private var pointer: Int = 0
  private var mItemSelect: Int = 0

  var selectListener: (pointer: Int, title: String) -> Unit = { _, _ -> }
  var titleProvider: (pointer: Int) -> String = { "" }
  var pickerProvider: () -> List<String> = { listOf() }
  var dataSize: Int = 0

  constructor(context: Context) : super(context) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
    init(context)
  }

  private fun init(context: Context) {
    View.inflate(context, R.layout.view_horizontal_selector, this)
    orientation = HORIZONTAL
    binding = HorizontalSelectorViewBinding(this)
    binding.leftButton.setOnClickListener { moveLeft() }
    binding.rightButton.setOnClickListener { moveRight() }
    binding.text1.setOnClickListener { showPicker() }
    updateTitle()
  }

  override fun invalidate() {
    super.invalidate()
    updateTitle()
  }

  fun selectItem(i: Int) {
    if (i == pointer) {
      updateTitle()
      return
    }
    if (i >= dataSize) {
      return
    }
    pointer = i
    updateTitle()
  }

  private fun moveRight() {
    if (dataSize == 0) {
      pointer = 0
      return
    }
    if (pointer >= dataSize - 1) {
      pointer = 0
    } else {
      pointer++
    }
    notifyListener()
    updateTitle()
  }

  private fun moveLeft() {
    if (dataSize == 0) {
      pointer = 0
      return
    }
    if (pointer <= 0) {
      pointer = dataSize - 1
    } else {
      pointer--
    }
    notifyListener()
    updateTitle()
  }

  private fun updateTitle() {
    val data = pickerProvider.invoke()
    if (data.isEmpty() || pointer >= data.size) {
      notSelected()
      return
    }
    binding.text1.text = titleProvider.invoke(pointer)
  }

  private fun notSelected() {
    binding.text1.text = ""
  }

  private fun showPicker() {
    val data = pickerProvider.invoke()
    if (data.isEmpty()) {
      notSelected()
      return
    }
    if (pointer >= data.size) {
      pointer = 0
    }
    val builder = Dialogues.getMaterialDialog(context)
    builder.setCancelable(true)
    mItemSelect = pointer
    builder.setSingleChoiceItems(data.toTypedArray(), mItemSelect) { _, which -> mItemSelect = which }
    builder.setPositiveButton(context.getString(R.string.save)) { dialog, _ ->
      dialog.dismiss()
      pointer = mItemSelect
      notifyListener()
      updateTitle()
    }
    builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
      dialog.dismiss()
    }
    builder.create().show()
  }

  private fun notifyListener() {
    selectListener.invoke(pointer, titleProvider.invoke(pointer))
  }
}
