package com.elementary.tasks.core.utils.ui

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.databinding.DialogBottomColorSliderBinding
import com.elementary.tasks.databinding.DialogBottomSeekAndTitleBinding
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.elementary.tasks.databinding.ViewColorSliderBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

data class SelectionList(
  val position: Int,
  val title: String,
  val okButtonTitle: String,
  val cancelButtonTitle: String,
  val items: List<String>
)

class Dialogues(
  private val currentStateHolder: CurrentStateHolder
) {

  private var selectedItemPosition: Int = 0

  fun showPropertyDialog(
    context: Context,
    selectionList: SelectionList,
    onOk: (position: Int) -> Unit,
    onCancel: (() -> Unit)? = null
  ) {
    propertyDialog(context, selectionList, onOk, onCancel).show()
  }

  fun propertyDialog(
    context: Context,
    selectionList: SelectionList,
    onOk: (position: Int) -> Unit,
    onCancel: (() -> Unit)? = null
  ) = getMaterialDialog(context).also {
    it.setTitle(selectionList.title)
    selectedItemPosition = selectionList.position
    it.setSingleChoiceItems(selectionList.items.toTypedArray(), selectedItemPosition) { _, which ->
      selectedItemPosition = which
    }
    it.setPositiveButton(selectionList.okButtonTitle) { dialog, _ ->
      onOk.invoke(selectedItemPosition)
      dialog.dismiss()
    }
    it.setNegativeButton(selectionList.cancelButtonTitle) { dialog, _ ->
      onCancel?.invoke()
      dialog.dismiss()
    }
  }.create()

  fun showColorBottomDialog(
    activity: Activity,
    current: Int,
    colors: IntArray = ThemeProvider.colorsForSlider(activity),
    onChange: (Int) -> Unit
  ) {
    val dialog = BottomSheetDialog(activity)
    val b = DialogBottomColorSliderBinding.inflate(LayoutInflater.from(activity))
    b.colorSlider.setColors(colors)
    b.colorSlider.setSelectorColorResource(
      currentStateHolder.theme.pickColorRes(R.color.pureBlack, R.color.pureWhite)
    )
    b.colorSlider.setSelection(current)
    b.colorSlider.setListener { i, _ ->
      onChange.invoke(i)
    }
    dialog.setContentView(b.root)
    dialog.show()
  }

  fun showRadiusBottomDialog(activity: Activity, current: Int, listener: (Int) -> String) {
    val dialog = BottomSheetDialog(activity)
    val b = DialogBottomSeekAndTitleBinding.inflate(LayoutInflater.from(activity))

    b.seekBar.addOnChangeListener { _, value, _ ->
      b.titleView.text = listener.invoke(value.toInt())
      val perc = value / b.seekBar.valueTo * 100f
      if (perc > 95f && b.seekBar.valueTo.toInt() < MAX_RADIUS) {
        b.seekBar.valueTo = b.seekBar.valueTo + (b.seekBar.valueTo * 0.2f)
      } else if (perc < 10f && b.seekBar.valueTo.toInt() > 5000) {
        b.seekBar.valueTo = b.seekBar.valueTo - (b.seekBar.valueTo * 0.2f)
      }
    }
    b.seekBar.stepSize = 1f
    b.seekBar.valueFrom = 0f
    b.seekBar.valueTo = MAX_DEF_RADIUS.toFloat()

    if (b.seekBar.valueTo < current && b.seekBar.valueTo < MAX_RADIUS) {
      b.seekBar.valueTo = current + (b.seekBar.valueTo * 0.2f)
    }
    if (current > MAX_RADIUS) {
      b.seekBar.valueTo = MAX_RADIUS.toFloat()
    }
    b.seekBar.valueTo = current * 2f
    if (current == 0) {
      b.seekBar.valueTo = MAX_DEF_RADIUS.toFloat()
    }
    b.seekBar.value = current.toFloat()

    b.titleView.text = listener.invoke(current)
    dialog.setContentView(b.root)
    dialog.show()
  }

  fun showColorDialog(
    activity: Activity, current: Int, title: String,
    colors: IntArray = ThemeProvider.colorsForSlider(activity),
    onDone: (Int) -> Unit
  ) {
    val builder = getMaterialDialog(activity)
    builder.setTitle(title)
    val bind = ViewColorSliderBinding.inflate(LayoutInflater.from(activity))
    bind.colorSlider.setColors(colors)
    bind.colorSlider.setSelectorColorResource(
      currentStateHolder.theme.pickColorRes(R.color.pureBlack, R.color.pureWhite)
    )
    bind.colorSlider.setSelection(current)
    builder.setView(bind.root)
    builder.setPositiveButton(R.string.save) { dialog, _ ->
      val selected = bind.colorSlider.selectedItem
      dialog.dismiss()
      onDone.invoke(selected)
    }
    builder.setNegativeButton(R.string.cancel) { dialog, _ ->
      dialog.dismiss()
    }
    val dialog = builder.create()
    dialog.show()
    setFullWidthDialog(dialog, activity)
  }

  fun showRadiusDialog(activity: Activity, current: Int, listener: OnValueSelectedListener<Int>) {
    val builder = getMaterialDialog(activity)
    builder.setTitle(R.string.radius)
    val b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(activity))

    b.seekBar.addOnChangeListener { _, value, _ ->
      b.titleView.text = listener.getTitle(value.toInt())
      val perc = value / b.seekBar.valueTo * 100f
      if (perc > 95f && b.seekBar.valueTo.toInt() < MAX_RADIUS) {
        b.seekBar.valueTo = b.seekBar.valueTo + (b.seekBar.valueTo * 0.2f)
      } else if (perc < 10f && b.seekBar.valueTo.toInt() > 5000) {
        b.seekBar.valueTo = b.seekBar.valueTo - (b.seekBar.valueTo * 0.2f)
      }
    }
    b.seekBar.stepSize = 1f
    b.seekBar.valueFrom = 0f

    b.seekBar.valueTo = MAX_DEF_RADIUS.toFloat()
    if (b.seekBar.valueTo < current && b.seekBar.valueTo < MAX_RADIUS) {
      b.seekBar.valueTo = current + (b.seekBar.valueTo * 0.2f)
    }
    if (current > MAX_RADIUS) {
      b.seekBar.valueTo = MAX_RADIUS.toFloat()
    }
    if (current == 0) {
      b.seekBar.valueTo = MAX_DEF_RADIUS.toFloat()
    }
    b.seekBar.value = current.toFloat()

    b.titleView.text = listener.getTitle(current)
    builder.setView(b.root)
    builder.setPositiveButton(R.string.ok) { _, _ -> listener.onSelected(b.seekBar.value.toInt()) }
    builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
    val dialog = builder.create()
    dialog.show()
    setFullWidthDialog(dialog, activity)
  }

  fun getMaterialDialog(context: Context): MaterialAlertDialogBuilder {
    return MaterialAlertDialogBuilder(context)
  }

  fun getNullableDialog(context: Context?): MaterialAlertDialogBuilder? {
    return if (context != null) {
      getMaterialDialog(context)
    } else null
  }

  fun askConfirmation(context: Context, title: String, onAction: (Boolean) -> Unit) {
    getMaterialDialog(context)
      .setTitle(title)
      .setMessage(context.getString(R.string.are_you_sure))
      .setPositiveButton(context.getString(R.string.yes)) { dialog, _ ->
        dialog.dismiss()
        onAction.invoke(true)
      }
      .setNegativeButton(context.getString(R.string.no)) { dialog, _ ->
        dialog.dismiss()
        onAction.invoke(false)
      }
      .create()
      .show()
  }

  interface OnValueSelectedListener<T> {
    fun onSelected(t: T)
    fun getTitle(t: T): String
  }

  companion object {
    private const val MAX_RADIUS = 100000
    private const val MAX_DEF_RADIUS = 5000

    fun getMaterialDialog(context: Context): MaterialAlertDialogBuilder {
      return MaterialAlertDialogBuilder(context)
    }

    fun showPopup(
      anchor: View,
      listener: ((Int) -> Unit)?, vararg actions: String
    ) {
      val popupMenu = PopupMenu(anchor.context, anchor)
      popupMenu.setOnMenuItemClickListener { item ->
        listener?.invoke(item.order)
        true
      }
      for (i in actions.indices) {
        popupMenu.menu.add(1, i + 1000, i, actions[i])
      }
      popupMenu.show()
    }

    fun setFullWidthDialog(dialog: AlertDialog, activity: Activity?) {
      if (activity == null) return
      val window = dialog.window
      window?.setGravity(Gravity.CENTER)
      window?.setLayout((activity.dp2px(380)), ViewGroup.LayoutParams.WRAP_CONTENT)
    }
  }
}
